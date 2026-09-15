package Model;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class Player extends Actor {

    protected int jerseyNumber;
    protected Team team;
    private boolean isGoalkeeper = false;
    private int lastMoveDx = 0;
    private int lastMoveDy = 0;
    private double stamina = 100.0;
    private double drift = Math.random() * Math.PI * 2;

    public Player(int x, int y, int jerseyNumber, Color colour, Team team) {
        super(x, y, colour);
        this.jerseyNumber = jerseyNumber;
        this.team = team;
    }

    public Team getTeam() { return team; }
    public int getJerseyNumber() { return jerseyNumber; }
    public double getStamina() { return stamina; }

    public boolean isGoalkeeper() { return isGoalkeeper; }
    public void setGoalkeeper(boolean isGoalkeeper) { this.isGoalkeeper = isGoalkeeper; }

    @Override
    public void move(int dx, int dy) {
        super.move(dx, dy);
        if (dx != 0 || dy != 0) {
            this.lastMoveDx = dx;
            this.lastMoveDy = dy;
        }
    }

    public int getLastMoveDx() { return lastMoveDx; }
    public int getLastMoveDy() { return lastMoveDy; }

    public void drainStamina(double amount) {
        stamina = Math.max(40.0, stamina - amount);
    }

    public double staminaSpeedFactor() {
        return 0.7 + 0.3 * (stamina / 100.0);
    }

    public void stepToward(int targetX, int targetY, int speed) {
        int adjustedSpeed = Math.max(1, (int) Math.round(speed * staminaSpeedFactor()));
        int dx = 0, dy = 0;
        if (this.x < targetX) dx = adjustedSpeed; else if (this.x > targetX) dx = -adjustedSpeed;
        if (this.y < targetY) dy = adjustedSpeed; else if (this.y > targetY) dy = -adjustedSpeed;
        move(dx, dy);
        if (dx != 0 || dy != 0) drainStamina(0.01);
    }

    public void idleDriftToward(int targetX, int targetY) {
        drift += 0.05;
        int wobbleX = (int) (Math.sin(drift) * 4);
        int wobbleY = (int) (Math.cos(drift * 0.8) * 4);
        stepToward(targetX + wobbleX, targetY + wobbleY, 1);
    }

    /** A quick step towards the centre of the pitch - used by a keeper advancing off the line. */
    public void stepUpfield(int speed) {
        int direction = (team == Team.RED) ? -1 : 1;
        move(direction * speed, 0);
    }

    public void resetToBase() {
        this.x = baseX;
        this.y = baseY;
    }

    @Override
    public void draw(Graphics g) {
        if (isGoalkeeper) {
            g.setColor(Color.YELLOW);
            g.fillRect(x - 10, y - 10, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRect(x - 10, y - 10, 20, 20);
        } else {
            g.setColor(colour);
            g.fillOval(x - 9, y - 9, 18, 18);
        }

        g.setColor(isGoalkeeper ? Color.BLACK : Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        String number = String.valueOf(jerseyNumber);
        FontMetrics metrics = g.getFontMetrics();
        int textX = x - metrics.stringWidth(number) / 2;
        int textY = y + metrics.getHeight() / 2 - metrics.getDescent();
        g.drawString(number, textX, textY);
    }
}