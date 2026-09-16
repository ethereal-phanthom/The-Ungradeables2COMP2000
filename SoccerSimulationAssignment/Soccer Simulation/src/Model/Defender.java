package Model;

import java.awt.Color;
import java.awt.Graphics;

public class Defender extends Player {

    private int tacklePower;
    private int yellowCards;
    private boolean isSentOff;

    public Defender(int x, int y, int jerseyNumber, Color colour, Team team) {
        super(x, y, jerseyNumber, colour, team);
        this.tacklePower = 70;
        this.yellowCards = 0;
        this.isSentOff = false;
    }

    public Defender(int x, int y, int jerseyNumber, Color colour, Team team, int tacklePower) {
        super(x, y, jerseyNumber, colour, team);
        this.tacklePower = tacklePower;
        this.yellowCards = 0;
        this.isSentOff = false;
    }

    public int getTacklePower() { return tacklePower; }
    public void setTacklePower(int tacklePower) { this.tacklePower = tacklePower; }
    public int getYellowCards() { return yellowCards; }
    public boolean isSentOff() { return isSentOff; }

    public boolean attemptTackle(int opponentDribble) throws RedCardException {
        if (isSentOff) {
            throw new RedCardException(jerseyNumber, "Player is already off the pitch");
        }
        if (this.tacklePower > 95) {
            this.isSentOff = true;
            throw new RedCardException(jerseyNumber, "Dangerous slide tackle straight red!");
        }
        if (this.tacklePower >= opponentDribble) {
            return true;
        } else {
            this.yellowCards++;
            if (this.yellowCards >= 2) {
                this.isSentOff = true;
                throw new RedCardException(jerseyNumber, "Second yellow card, player is gone!");
            }
            return false;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!isSentOff) {
            super.draw(g);
        }
    }

    @Override
    public String toString() {
        return "Defender [Tackle=" + tacklePower + ", Yellows=" + yellowCards + ", Out=" + isSentOff + "]";
    }
}