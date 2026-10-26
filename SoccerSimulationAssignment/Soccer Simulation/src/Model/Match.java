
package Model;

import java.awt.Graphics;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.DoubleSummaryStatistics;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.function.Consumer;

public class Match {

    private static final int CHASE_SPEED = 4;
    private static final int RETURN_SPEED = 3;
    private static final int DRIBBLE_SPEED = 3;
    private static final int FORWARD_PUSH = 60;
    private static final int CHALLENGE_RANGE = 32;
    private static final int SECOND_DEFENDER_RANGE = 45;
    private static final int SHOOTING_RANGE = 220;
    private static final double SHOT_ERROR_FACTOR = 0.4;
    private static final int PRESSURE_RANGE = 55;

    // Lower pass chance so players dribble more often
    private static final double PASS_CHANCE = 0.02;

    private static final int FORWARD_BIAS = 30;
    private static final int GOALKEEPER_FORWARD_BIAS = 10;
    private static final int GOALKEEPER_BOX_RADIUS = 100;
    private static final int DEFENSIVE_THIRD_RANGE = 260;
    private static final int PITCH_WIDTH = 700;

    private static final double SIM_MINUTES_PER_TICK = 90.0 / 2400.0;
    private static final int FULL_TIME_MINUTE = 90;
    private static final int HALF_TIME_MINUTE = 45;

    // Strategy for chasing the ball
    private final MovementStrategy chaseMovement =
        new ChaseMovementStrategy(CHASE_SPEED);

    // Lambda for moving players back into formation
    private final MovementStrategy formationMovement =
        (player, targetX, targetY) ->
            player.idleDriftToward(targetX, targetY);

    private ArrayList<Player> players;
    private Ball ball;
    private int maxPlayers = 22;

    private double matchMinute = 0;
    private boolean halfTimeAnnounced = false;
    private boolean fullTimeReached = false;

    private Set<Player> beatenDefenders = new HashSet<>();
    private Player lastKnownHolder = null;

    private String lastGoalScorerTeam = null;

    // Records the team holding the ball at the start of each match tick
    private final ArrayList<Team> possessionSamples = new ArrayList<>();
    private int nextStatsMinute = 15;

    public Match() {
        players = new ArrayList<>();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        if (players.size() > maxPlayers) {
            throw new IllegalArgumentException(
                "Cannot field more than " + maxPlayers + " players"
            );
        }
        this.players = players;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }

    public double getMatchMinute() {
        return matchMinute;
    }

    public boolean isFullTime() {
        return fullTimeReached;
    }

    public void drawAll(Graphics g) {
        for (Player player : players) {
            player.draw(g);
        }
        if (ball != null) {
            ball.draw(g);
        }
    }

    public String tick(Consumer<String> refereeLog) {
        if (ball == null || fullTimeReached) {
            return null;
        }

        advanceClock(refereeLog);

        if (fullTimeReached) {
            return null;
        }

        // Save a possession sample while a player has the ball
        if (!ball.isLoose() && ball.getPossessor() != null) {
            possessionSamples.add(ball.getPossessor().getTeam());
        }

        if (ball.isLoose()) {
            beatenDefenders.clear();
            lastKnownHolder = null;
            chaseLooseBall();

        } else if (ball.getPossessor() != null) {
            Player holder = ball.getPossessor();
            boolean justReceived = (lastKnownHolder != holder);

            if (justReceived) {
                beatenDefenders.clear();
                lastKnownHolder = holder;
            }

            if (holder.isGoalkeeper()) {
                idleDriftTeammates(holder);
                forceGoalkeeperPass(holder, refereeLog);

            } else {
                runChallengeAgainstPossessor(refereeLog);

                if (ball.getPossessor() == holder) {
                    idleDriftTeammates(holder);
                    applyFormationShift(holder.getTeam());

                    boolean shotTaken = attemptShotIfInRange(holder);
                    boolean passed = false;

                    if (!shotTaken) {
                        passed = attemptPass(holder, refereeLog);
                    }

                    if (!shotTaken && !passed) {
                        dribbleForward(holder);
                    }

                } else {
                    beatenDefenders.clear();
                    lastKnownHolder = ball.getPossessor();
                }
            }
        }

        String goalMessage = ball.updatePhysics();

        if (goalMessage != null) {
            // Handles goal messages for both teams
            lastGoalScorerTeam =
                goalMessage.toUpperCase().contains("RED") ? "RED" : "BLUE";

            resetToKickoff();
        }

        // Show updated statistics every 15 simulated minutes
        if (matchMinute >= nextStatsMinute) {
            logMatchStatistics(refereeLog);
            nextStatsMinute += 15;
        }

        return goalMessage;
    }

    public String consumeLastGoalTeam() {
        String team = lastGoalScorerTeam;
        lastGoalScorerTeam = null;
        return team;
    }

    private void resetToKickoff() {
        beatenDefenders.clear();
        lastKnownHolder = null;

        for (Player p : players) {
            p.resetToBase();
        }
    }

    private void advanceClock(Consumer<String> refereeLog) {
        matchMinute += SIM_MINUTES_PER_TICK;

        if (!halfTimeAnnounced && matchMinute >= HALF_TIME_MINUTE) {
            halfTimeAnnounced = true;
            refereeLog.accept("HALF-TIME. Score stands at the break.");
        }

        if (matchMinute >= FULL_TIME_MINUTE) {
            fullTimeReached = true;
            refereeLog.accept(
                "FULL-TIME. Referee blows the final whistle."
            );
            logMatchStatistics(refereeLog);
        }
    }

    // Uses collectors to create live match statistics
    private void logMatchStatistics(Consumer<String> refereeLog) {

        // Take a snapshot before parallel work to avoid shared mutable state.
        // The match tick remains sequential; only statistics run in parallel.
        List<Team> possessionSnapshot =
            new ArrayList<>(possessionSamples);

        // Parallel collector counts each team's possession samples.
        Map<Team, Long> possessionCounts =
            possessionSnapshot.parallelStream()
                .collect(Collectors.groupingBy(
                    team -> team,
                    Collectors.counting()
                ));

        long blueTicks = possessionCounts.getOrDefault(Team.BLUE, 0L);
        long redTicks = possessionCounts.getOrDefault(Team.RED, 0L);
        long totalTicks = blueTicks + redTicks;

        // Loose-ball ticks are not counted as possession
        double bluePercent = totalTicks == 0 ? 0.0
            : 100.0 * blueTicks / totalTicks;

        double redPercent = totalTicks == 0 ? 0.0
            : 100.0 * redTicks / totalTicks;

        // Capture the team and forward position of each eligible player.
        // Parallel workers read these fixed values, not moving Player objects.
        List<Map.Entry<Team, Double>> positionSnapshot =
            players.stream()
                .filter(p -> !p.isGoalkeeper())
                .filter(p -> !(p instanceof Defender) ||
                             !((Defender) p).isSentOff())
                .map(p -> new AbstractMap.SimpleImmutableEntry<>(
                    p.getTeam(), forwardnessOf(p)
                ))
                .collect(Collectors.toList());

        // Summarise attacking positions in parallel for each team.
        Map<Team, DoubleSummaryStatistics> attackingPositions =
            positionSnapshot.parallelStream()
                .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.summarizingDouble(
                        Map.Entry::getValue
                    )
                ));

        DoubleSummaryStatistics bluePositions =
            attackingPositions.get(Team.BLUE);

        DoubleSummaryStatistics redPositions =
            attackingPositions.get(Team.RED);

        double blueAverage = bluePositions == null ? 0.0
            : bluePositions.getAverage();

        double redAverage = redPositions == null ? 0.0
            : redPositions.getAverage();

        refereeLog.accept(String.format(Locale.ROOT,
            "MATCH STATS %d': Possession BLUE %.0f%% / RED %.0f%% " +
            "| Avg attacking position BLUE %.0f / RED %.0f",
            (int) matchMinute,
            bluePercent, redPercent, blueAverage, redAverage
        ));
    }

    // Uses different strategies depending on what the player needs to do
    private void chaseLooseBall() {
        Player blueKeeper = findGoalkeeper(Team.BLUE);
        Player redKeeper = findGoalkeeper(Team.RED);

        boolean blueKeeperClaims = isBallInOwnBox(blueKeeper);
        boolean redKeeperClaims = isBallInOwnBox(redKeeper);

        Player nearestBlue = blueKeeperClaims
            ? blueKeeper
            : nearestOutfielder(Team.BLUE);

        Player nearestRed = redKeeperClaims
            ? redKeeper
            : nearestOutfielder(Team.RED);

        for (Player p : players) {

            if (p == nearestRed || p == nearestBlue) {

                // Nearest players use the chase strategy
                chaseMovement.move(p, ball.getX(), ball.getY());

                if (p.distanceTo(ball.getX(), ball.getY()) < 12) {
                    ball.attachTo(p);
                }

            } else {

                // Other players use the lambda movement strategy
                formationMovement.move(p, p.baseX, p.baseY);
            }
        }
    }

    private boolean isBallInOwnBox(Player keeper) {
        if (keeper == null) {
            return false;
        }

        return keeper.distanceTo(
            ball.getX(), ball.getY()
        ) <= GOALKEEPER_BOX_RADIUS;
    }

    private Player findGoalkeeper(Team team) {
        for (Player p : players) {
            if (p.getTeam() == team && p.isGoalkeeper()) {
                return p;
            }
        }

        return null;
    }

    private Player nearestOutfielder(Team team) {
        return nearestOf(
            team, null, ball.getX(), ball.getY()
        );
    }

    private void runChallengeAgainstPossessor(
        Consumer<String> refereeLog
    ) {
        Player holder = ball.getPossessor();

        Team opponentTeam = (holder.getTeam() == Team.RED)
            ? Team.BLUE
            : Team.RED;

        Player challenger = nearestChallenger(
            opponentTeam, beatenDefenders
        );

        boolean nearOpponentGoal = isNearGoal(
            holder, opponentTeam
        );

        Player secondDefender = null;

        if (nearOpponentGoal && challenger != null) {
            Set<Player> excludeBoth =
                new HashSet<>(beatenDefenders);

            excludeBoth.add(challenger);

            secondDefender = nearestChallenger(
                opponentTeam, excludeBoth
            );
        }

        for (Player p : players) {
            if (p == holder) {
                continue;
            }

            if (p == challenger || p == secondDefender) {
                p.stepToward(
                    holder.getX(), holder.getY(), CHASE_SPEED
                );

            } else if (p.isGoalkeeper()) {
                p.idleDriftToward(p.baseX, p.baseY);

            } else if (p.getTeam() != holder.getTeam()) {
                p.idleDriftToward(p.baseX, p.baseY);
            }
        }

        Player winner = resolveChallenge(
            challenger, holder, refereeLog
        );

        if (winner == null && secondDefender != null) {
            winner = resolveChallenge(
                secondDefender, holder, refereeLog
            );
        }
    }

    private boolean isNearGoal(
        Player holder, Team defendingTeam
    ) {
        int goalX = (defendingTeam == Team.RED) ? 700 : 0;

        return holder.distanceTo(
            goalX, 200
        ) <= DEFENSIVE_THIRD_RANGE;
    }

    private Player resolveChallenge(
        Player challenger,
        Player holder,
        Consumer<String> refereeLog
    ) {
        if (challenger == null) {
            return null;
        }

        if (challenger.distanceTo(
            holder.getX(), holder.getY()
        ) > CHALLENGE_RANGE) {
            return null;
        }

        if (ball.getPossessor() != holder) {
            return null;
        }

        boolean challengerWins = Math.random() < 0.5;

        if (!challengerWins) {
            beatenDefenders.add(challenger);

            refereeLog.accept(
                "Player #" + holder.getJerseyNumber() +
                " (" + holder.getTeam() + ") gets past #" +
                challenger.getJerseyNumber() +
                " (" + challenger.getTeam() + ")!"
            );

            return null;
        }

        ball.releaseFrom();

        if (challenger instanceof Defender) {
            Defender d = (Defender) challenger;

            try {
                d.attemptTackle(
                    (int) (Math.random() * 100)
                );

                refereeLog.accept(
                    "Player #" + challenger.getJerseyNumber() +
                    " (" + challenger.getTeam() +
                    ") wins the ball!"
                );

            } catch (RedCardException e) {
                refereeLog.accept(
                    "REFEREE: " + e.getMessage()
                );
            }

        } else {
            refereeLog.accept(
                "Player #" + challenger.getJerseyNumber() +
                " (" + challenger.getTeam() +
                ") wins the ball!"
            );
        }

        ball.attachTo(challenger);
        return challenger;
    }

    private Player nearestChallenger(
        Team team, Set<Player> exclude
    ) {
        return nearestOf(
            team, exclude, ball.getX(), ball.getY()
        );
    }

    private Player nearestOf(
        Team team,
        Set<Player> exclude,
        int targetX,
        int targetY
    ) {
        ArrayList<Player> candidates = new ArrayList<>();

        for (Player p : players) {
            if (p.getTeam() != team || p.isGoalkeeper()) {
                continue;
            }

            if (p instanceof Defender &&
                ((Defender) p).isSentOff()) {
                continue;
            }

            if (exclude != null && exclude.contains(p)) {
                continue;
            }

            candidates.add(p);
        }

        Collections.shuffle(candidates);

        Player nearest = null;
        double best = Double.MAX_VALUE;

        for (Player p : candidates) {
            double d = p.distanceTo(targetX, targetY);

            if (d < best) {
                best = d;
                nearest = p;
            }
        }

        return nearest;
    }

    private void idleDriftTeammates(Player holder) {
        int direction = (holder.getTeam() == Team.RED)
            ? -1
            : 1;

        for (Player p : players) {
            if (p == holder) {
                continue;
            }

            if (p.isGoalkeeper()) {
                p.idleDriftToward(p.baseX, p.baseY);

            } else if (p.getTeam() == holder.getTeam()) {
                p.idleDriftToward(
                    p.baseX + direction * FORWARD_PUSH,
                    p.baseY
                );
            }
        }
    }

    private void applyFormationShift(Team attackingTeam) {
        for (Player p : players) {
            if (p == ball.getPossessor()) {
                continue;
            }

            if (p.isGoalkeeper()) {
                p.idleDriftToward(p.baseX, p.baseY);

            } else if (p.getTeam() != attackingTeam) {
                p.idleDriftToward(p.baseX, p.baseY);
            }
        }
    }

    private void forceGoalkeeperPass(
        Player keeper,
        Consumer<String> refereeLog
    ) {
        Player target = mostAdvancedForwardTeammate(
            keeper, GOALKEEPER_FORWARD_BIAS
        );

        if (target == null) {
            return;
        }

        ball.kick(
            keeper,
            target.getX() - keeper.getX(),
            target.getY() - keeper.getY()
        );

        refereeLog.accept(
            "Goalkeeper #" + keeper.getJerseyNumber() +
            " throws it out to #" +
            target.getJerseyNumber() + "."
        );
    }

    private boolean attemptPass(
        Player holder,
        Consumer<String> refereeLog
    ) {
        Team opponentTeam = (holder.getTeam() == Team.RED)
            ? Team.BLUE
            : Team.RED;

        Player nearestOpponent = nearestChallenger(
            opponentTeam, null
        );

        boolean underPressure =
            nearestOpponent != null &&
            nearestOpponent.distanceTo(
                holder.getX(), holder.getY()
            ) <= PRESSURE_RANGE;

        if (!underPressure && Math.random() > PASS_CHANCE) {
            return false;
        }

        Player target = mostAdvancedForwardTeammate(
            holder, FORWARD_BIAS
        );

        if (target == null) {
            return false;
        }

        ball.kick(
            holder,
            target.getX() - holder.getX(),
            target.getY() - holder.getY()
        );

        refereeLog.accept(
            "Player #" + holder.getJerseyNumber() +
            " (" + holder.getTeam() +
            ") passes forward to #" +
            target.getJerseyNumber() + "."
        );

        return true;
    }

    private void dribbleForward(Player holder) {
        int direction = (holder.getTeam() == Team.RED)
            ? -1
            : 1;

        int targetX = holder.getX() + direction * 40;

        holder.stepToward(
            targetX, 200, DRIBBLE_SPEED
        );
    }

    private double forwardnessOf(Player p) {
        if (p.getTeam() == Team.RED) {
            return PITCH_WIDTH - p.getX();
        } else {
            return p.getX();
        }
    }

    // Uses streams to find the most advanced teammate for a pass
    private Player mostAdvancedForwardTeammate(
        Player holder,
        int forwardBias
    ) {
        double minimumForwardness =
            forwardnessOf(holder) + forwardBias;

        return players.stream()

            // Only considers teammates who can receive the ball
            .filter(p -> p != holder &&
                         p.getTeam() == holder.getTeam() &&
                         !p.isGoalkeeper())

            // Excludes defenders who have been sent off
            .filter(p -> !(p instanceof Defender) ||
                         !((Defender) p).isSentOff())

            // Only considers players far enough forward
            .filter(p -> forwardnessOf(p) > minimumForwardness)

            // Chooses the furthest player, keeping the first in a tie
            .reduce((best, p) ->
                forwardnessOf(p) > forwardnessOf(best) ? p : best)

            .orElse(null);
    }

    public void receivePassesIfArrived() {
        if (ball.getPossessor() != null || ball.isLoose()) {
            return;
        }

        for (Player p : players) {
            if (p.distanceTo(
                ball.getX(), ball.getY()
            ) < 14) {
                ball.attachTo(p);
                return;
            }
        }
    }

    private boolean attemptShotIfInRange(Player holder) {
        int goalX = (holder.getTeam() == Team.RED)
            ? 0
            : 700;

        int goalY = 170;

        double distToGoal = holder.distanceTo(
            goalX, goalY
        );

        if (distToGoal <= SHOOTING_RANGE) {

            // Shoots towards the correct goal coordinates
            ball.shoot(
                holder,
                goalX,
                goalY,
                SHOT_ERROR_FACTOR
            );

            return true;
        }

        return false;
    }
}
