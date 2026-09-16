package Model;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Match {

    private static final int CHASE_SPEED = 4;
    private static final int RETURN_SPEED = 3;
    private static final int DRIBBLE_SPEED = 3;
    private static final int FORWARD_PUSH = 60;
    private static final int CHALLENGE_RANGE = 32;
    private static final int SECOND_DEFENDER_RANGE = 45; 
    private static final int SHOOTING_RANGE = 220; // Increased slightly so they shoot more often
    private static final double SHOT_ERROR_FACTOR = 0.4;
    private static final int PRESSURE_RANGE = 55;
    
    // Fixed: Lowered significantly so they dribble instead of playing "hot potato"
    private static final double PASS_CHANCE = 0.02; 
    
    private static final int FORWARD_BIAS = 30;
    private static final int GOALKEEPER_FORWARD_BIAS = 10;
    private static final int GOALKEEPER_BOX_RADIUS = 100;
    private static final int DEFENSIVE_THIRD_RANGE = 260; 
    private static final int PITCH_WIDTH = 700;

    private static final double SIM_MINUTES_PER_TICK = 90.0 / 2400.0;
    private static final int FULL_TIME_MINUTE = 90;
    private static final int HALF_TIME_MINUTE = 45;

    private ArrayList<Player> players;
    private Ball ball;
    private int maxPlayers = 22;

    private double matchMinute = 0;
    private boolean halfTimeAnnounced = false;
    private boolean fullTimeReached = false;

    private Set<Player> beatenDefenders = new HashSet<>();
    private Player lastKnownHolder = null;

    private String lastGoalScorerTeam = null;

    public Match() {
        players = new ArrayList<>();
    }

    public ArrayList<Player> getPlayers() { return players; }

    public void setPlayers(ArrayList<Player> players) {
        if (players.size() > maxPlayers) {
            throw new IllegalArgumentException("Cannot field more than " + maxPlayers + " players");
        }
        this.players = players;
    }

    public void setBall(Ball ball) { this.ball = ball; }

    public double getMatchMinute() { return matchMinute; }
    public boolean isFullTime() { return fullTimeReached; }

    public void drawAll(Graphics g) {
        for (Player player : players) {
            player.draw(g);
        }
        if (ball != null) ball.draw(g);
    }

    public String tick(Consumer<String> refereeLog) {
        if (ball == null || fullTimeReached) return null;

        advanceClock(refereeLog);
        if (fullTimeReached) return null;

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
            // Fixed: Safely handles lowercase/uppercase goals so Red gets their points!
            lastGoalScorerTeam = goalMessage.toUpperCase().contains("RED") ? "RED" : "BLUE";
            resetToKickoff();
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
            refereeLog.accept("FULL-TIME. Referee blows the final whistle.");
        }
    }

    private void chaseLooseBall() {
        Player blueKeeper = findGoalkeeper(Team.BLUE);
        Player redKeeper = findGoalkeeper(Team.RED);

        boolean blueKeeperClaims = isBallInOwnBox(blueKeeper);
        boolean redKeeperClaims = isBallInOwnBox(redKeeper);

        Player nearestBlue = blueKeeperClaims ? blueKeeper : nearestOutfielder(Team.BLUE);
        Player nearestRed = redKeeperClaims ? redKeeper : nearestOutfielder(Team.RED);

        for (Player p : players) {
            if (p == nearestRed || p == nearestBlue) {
                p.stepToward(ball.getX(), ball.getY(), CHASE_SPEED);
                if (p.distanceTo(ball.getX(), ball.getY()) < 12) {
                    ball.attachTo(p);
                }
            } else {
                p.idleDriftToward(p.baseX, p.baseY);
            }
        }
    }

    private boolean isBallInOwnBox(Player keeper) {
        if (keeper == null) return false;
        return keeper.distanceTo(ball.getX(), ball.getY()) <= GOALKEEPER_BOX_RADIUS;
    }

    private Player findGoalkeeper(Team team) {
        for (Player p : players) {
            if (p.getTeam() == team && p.isGoalkeeper()) return p;
        }
        return null;
    }

    private Player nearestOutfielder(Team team) {
        return nearestOf(team, null, ball.getX(), ball.getY());
    }

    private void runChallengeAgainstPossessor(Consumer<String> refereeLog) {
        Player holder = ball.getPossessor();
        Team opponentTeam = (holder.getTeam() == Team.RED) ? Team.BLUE : Team.RED;
        Player challenger = nearestChallenger(opponentTeam, beatenDefenders);

        boolean nearOpponentGoal = isNearGoal(holder, opponentTeam);
        Player secondDefender = null;
        if (nearOpponentGoal && challenger != null) {
            Set<Player> excludeBoth = new HashSet<>(beatenDefenders);
            excludeBoth.add(challenger);
            secondDefender = nearestChallenger(opponentTeam, excludeBoth);
        }

        for (Player p : players) {
            if (p == holder) continue;
            if (p == challenger || p == secondDefender) {
                p.stepToward(holder.getX(), holder.getY(), CHASE_SPEED);
            } else if (p.isGoalkeeper()) {
                p.idleDriftToward(p.baseX, p.baseY);
            } else if (p.getTeam() != holder.getTeam()) {
                p.idleDriftToward(p.baseX, p.baseY);
            }
        }

        Player winner = resolveChallenge(challenger, holder, refereeLog);
        if (winner == null && secondDefender != null) {
            winner = resolveChallenge(secondDefender, holder, refereeLog);
        }
    }

    private boolean isNearGoal(Player holder, Team defendingTeam) {
        int goalX = (defendingTeam == Team.RED) ? 700 : 0;
        return holder.distanceTo(goalX, 200) <= DEFENSIVE_THIRD_RANGE;
    }

    private Player resolveChallenge(Player challenger, Player holder, Consumer<String> refereeLog) {
        if (challenger == null) return null;
        if (challenger.distanceTo(holder.getX(), holder.getY()) > CHALLENGE_RANGE) return null;
        if (ball.getPossessor() != holder) return null; 

        boolean challengerWins = Math.random() < 0.5;

        if (!challengerWins) {
            beatenDefenders.add(challenger);
            refereeLog.accept("Player #" + holder.getJerseyNumber() + " (" + holder.getTeam() + ") gets past #" + challenger.getJerseyNumber() + " (" + challenger.getTeam() + ")!");
            return null;
        }

        ball.releaseFrom();
        if (challenger instanceof Defender) {
            Defender d = (Defender) challenger;
            try {
                d.attemptTackle((int) (Math.random() * 100));
                refereeLog.accept("Player #" + challenger.getJerseyNumber() + " (" + challenger.getTeam() + ") wins the ball!");
            } catch (RedCardException e) {
                refereeLog.accept("REFEREE: " + e.getMessage());
            }
        } else {
            refereeLog.accept("Player #" + challenger.getJerseyNumber() + " (" + challenger.getTeam() + ") wins the ball!");
        }
        ball.attachTo(challenger);
        return challenger;
    }

    private Player nearestChallenger(Team team, Set<Player> exclude) {
        return nearestOf(team, exclude, ball.getX(), ball.getY());
    }

    private Player nearestOf(Team team, Set<Player> exclude, int targetX, int targetY) {
        ArrayList<Player> candidates = new ArrayList<>();
        for (Player p : players) {
            if (p.getTeam() != team || p.isGoalkeeper()) continue;
            if (p instanceof Defender && ((Defender) p).isSentOff()) continue;
            if (exclude != null && exclude.contains(p)) continue;
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
        int direction = (holder.getTeam() == Team.RED) ? -1 : 1;
        for (Player p : players) {
            if (p == holder) continue;
            if (p.isGoalkeeper()) {
                p.idleDriftToward(p.baseX, p.baseY);
            } else if (p.getTeam() == holder.getTeam()) {
                p.idleDriftToward(p.baseX + direction * FORWARD_PUSH, p.baseY);
            }
        }
    }

    private void applyFormationShift(Team attackingTeam) {
        for (Player p : players) {
            if (p == ball.getPossessor()) continue;
            if (p.isGoalkeeper()) {
                p.idleDriftToward(p.baseX, p.baseY);
            } else if (p.getTeam() != attackingTeam) {
                p.idleDriftToward(p.baseX, p.baseY);
            }
        }
    }

    private void forceGoalkeeperPass(Player keeper, Consumer<String> refereeLog) {
        Player target = mostAdvancedForwardTeammate(keeper, GOALKEEPER_FORWARD_BIAS);
        if (target == null) return;

        ball.kick(keeper, target.getX() - keeper.getX(), target.getY() - keeper.getY());
        refereeLog.accept("Goalkeeper #" + keeper.getJerseyNumber() + " throws it out to #" + target.getJerseyNumber() + ".");
    }

    private boolean attemptPass(Player holder, Consumer<String> refereeLog) {
        Team opponentTeam = (holder.getTeam() == Team.RED) ? Team.BLUE : Team.RED;
        Player nearestOpponent = nearestChallenger(opponentTeam, null);
        boolean underPressure = nearestOpponent != null &&
                nearestOpponent.distanceTo(holder.getX(), holder.getY()) <= PRESSURE_RANGE;

        if (!underPressure && Math.random() > PASS_CHANCE) return false;

        Player target = mostAdvancedForwardTeammate(holder, FORWARD_BIAS);
        if (target == null) return false;

        ball.kick(holder, target.getX() - holder.getX(), target.getY() - holder.getY());
        refereeLog.accept("Player #" + holder.getJerseyNumber() + " (" + holder.getTeam() + ") passes forward to #" + target.getJerseyNumber() + ".");
        return true;
    }

    private void dribbleForward(Player holder) {
        int direction = (holder.getTeam() == Team.RED) ? -1 : 1;
        int targetX = holder.getX() + direction * 40;
        holder.stepToward(targetX, 200, DRIBBLE_SPEED);
    }

    private double forwardnessOf(Player p) {
        if (p.getTeam() == Team.RED) {
            return PITCH_WIDTH - p.getX();
        } else {
            return p.getX();
        }
    }

    private Player mostAdvancedForwardTeammate(Player holder, int forwardBias) {
        double holderForwardness = forwardnessOf(holder);
        double bestForwardness = holderForwardness + forwardBias;

        Player best = null;
        for (Player p : players) {
            if (p == holder || p.getTeam() != holder.getTeam() || p.isGoalkeeper()) continue;
            if (p instanceof Defender && ((Defender) p).isSentOff()) continue;
            double forwardness = forwardnessOf(p);
            if (forwardness > bestForwardness) {
                bestForwardness = forwardness;
                best = p;
            }
        }
        return best;
    }

    public void receivePassesIfArrived() {
        if (ball.getPossessor() != null || ball.isLoose()) return;
        for (Player p : players) {
            if (p.distanceTo(ball.getX(), ball.getY()) < 14) {
                ball.attachTo(p);
                return;
            }
        }
    }

    private boolean attemptShotIfInRange(Player holder) {
        int goalX = (holder.getTeam() == Team.RED) ? 0 : 700;
        int goalY = 170; 
        double distToGoal = holder.distanceTo(goalX, goalY);

        if (distToGoal <= SHOOTING_RANGE) {
            // Fixed: Changed back to exact coordinates instead of vectors so they don't aim off the pitch!
            ball.shoot(holder, goalX, goalY, SHOT_ERROR_FACTOR);
            return true;
        }
        return false;
    }
}