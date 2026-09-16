package Model;
import java.awt.Color;
import java.awt.Graphics;

public abstract class Actor implements Drawable {
    protected int x, y;
    protected int baseX, baseY;
    protected Color colour;

    public Actor(int x, int y, Color colour) {
        this.x = x;
        this.y = y;
        this.baseX = x;
        this.baseY = y;
        this.colour = colour;
    }

    public abstract void draw(Graphics g);

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public double distanceTo(int targetX, int targetY) {
        return Math.sqrt(Math.pow(this.x - targetX, 2) + Math.pow(this.y - targetY, 2));
    }

    public double distanceTo(Actor other) {
        return distanceTo(other.getX(), other.getY());
    }
}