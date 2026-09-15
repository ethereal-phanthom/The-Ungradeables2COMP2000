package Model;

import java.awt.Color;
import java.awt.Graphics;

public class Ball extends Actor {

    private static final int KICK_SPEED = 9;
    private static final int PASS_DISTANCE = 130;
    private static final int SHOT_DISTANCE = 400;
    private static final int GOAL_TOP = 140;
    private static final int GOAL_BOTTOM = 260;

    private double velocityX = 0;
    private double velocityY = 0;
    private double distanceRemaining = 0;
    private boolean moving = false;
    private boolean isShot = false;

    private Player possessor = null;

    public Ball(int x, int y) {
        super(x, y, Color.WHITE);
    }

    public Player getPossessor() { return possessor; }
    public boolean isMoving() { return moving; }
    public boolean isLoose() { return possessor == null && !moving; }

    public void attachTo(Player player) {
        this.possessor = player;
        stop();
    }

    public void releaseFrom() {
        this.possessor = null;
    }

    public void kick(Player kicker, int dirX, int dirY) {
        launch(kicker, dirX, dirY, PASS_DISTANCE, false);
    }

    /**
     * A shot on goal, aimed at (targetX, targetY) with a random vertical error applied.
     * The error is narrow enough that most on-target attempts still land inside the goal
     * mouth (GOAL_TOP..GOAL_BOTTOM), for both teams equally - errorFactor only controls
     * how wide the miss can be, not which side is more accurate.
     */
    public void shoot(Player kicker, int targetX, int targetY, double errorFactor) {
        double error = (Math.random() - 0.5) * 2 * errorFactor;
        int erroredTargetY = targetY + (int) (error * 55);
        int dirX = targetX - this.x;
        int dirY = erroredTargetY - this.y;
        launch(kicker, dirX, dirY, SHOT_DISTANCE, true);
    }

    private void launch(Player kicker, int dirX, int dirY, int distance, boolean shot) {
        this.possessor = null;
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        if (length == 0) {
            dirX = (kicker.getTeam() == Team.RED) ? -1 : 1;
            dirY = 0;
            length = 1;
        }
        this.velocityX = (dirX / length) * KICK_SPEED;
        this.velocityY = (dirY / length) * KICK_SPEED;
        this.distanceRemaining = distance;
        this.moving = true;
        this.isShot = shot;
    }

    /**
     * Goal-line crossing is checked identically for both x<=0 (RED's target) and x>=700
     * (BLUE's target), using the same GOAL_TOP/GOAL_BOTTOM window both times, so neither
     * side has an easier or harder goal mouth to hit.
     */
    public String updatePhysics() {
        if (possessor != null) {
            followPossessor();
            return null;
        }
        if (!moving) return null;

        this.x += (int) velocityX;
        this.y += (int) velocityY;
        double stepLength = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        distanceRemaining -= stepLength;

        if (this.x <= 0) {
            boolean scored = this.y >= GOAL_TOP && this.y <= GOAL_BOTTOM;
            String result = scored ? "GOAL FOR RED TEAM!" : null;
            resetToCenter();
            return result;
        }
        if (this.x >= 700) {
            boolean scored = this.y >= GOAL_TOP && this.y <= GOAL_BOTTOM;
            String result = scored ? "GOAL FOR BLUE TEAM!" : null;
            resetToCenter();
            return result;
        }

        if (distanceRemaining <= 0 || this.y <= 0 || this.y >= 400) {
            stop();
        }

        return null;
    }

    private void followPossessor() {
        this.x = possessor.getX();
        this.y = possessor.getY();
    }

    private void stop() {
        this.moving = false;
        this.isShot = false;
        this.velocityX = 0;
        this.velocityY = 0;
        this.distanceRemaining = 0;
    }

    private void resetToCenter() {
        this.x = 350;
        this.y = 200;
        this.possessor = null;
        stop();
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(colour);
        g.fillOval(x - 5, y - 5, 10, 10);
    }
}