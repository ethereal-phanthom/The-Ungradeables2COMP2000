package Rendering;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import Model.*;

class VerticalPitch extends JPanel {
    private int width; private int height;
    private ArrayList<Player> players;
    private Color sideColour = Color.BLUE;
    private Team sideTeam = Team.BLUE;
    private int rectX, rectY, rectW, rectH;

    VerticalPitch() {
        setPreferredSize(new Dimension(ScreenSize.width, ScreenSize.height));
        setOpaque(false);
        this.height = ScreenSize.height / 2 + 130;
        this.width = (int) (this.height * 0.65);
        players = new ArrayList<>();
        rectX = ScreenSize.width - width - 50; rectY = height - 310; rectW = width; rectH = height;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(50, 150, 50)); g.fillRect(rectX, rectY, rectW, rectH);
        g.setColor(Color.WHITE); g.drawRect(rectX, rectY, rectW, rectH); g.drawLine(rectX, rectY + rectH / 2, rectX + rectW, rectY + rectH / 2);

        int penaltySpan = (int) (rectW * 0.59); int penaltyDepth = (int) (rectH * 0.16);
        int penaltyX = rectX + (rectW - penaltySpan) / 2;
        g.drawRect(penaltyX, rectY, penaltySpan, penaltyDepth); g.drawRect(penaltyX, rectY + rectH - penaltyDepth, penaltySpan, penaltyDepth);

        int centerX = rectX + rectW / 2; int centerY = rectY + rectH / 2; int circleRadius = (int) (rectW * 0.135);
        g.drawOval(centerX - circleRadius, centerY - circleRadius, circleRadius * 2, circleRadius * 2); g.fillOval(centerX - 4, centerY - 4, 8, 8);

        int goalSpan = (int) (rectW * 0.27); int goalDepth = (int) (rectH * 0.05); int goalX = rectX + (rectW - goalSpan) / 2;
        g.drawRect(goalX, rectY, goalSpan, goalDepth); g.drawRect(goalX, rectY + rectH - goalDepth, goalSpan, goalDepth);

        int spotOffset = (int) (rectH * 0.105);
        g.fillOval(centerX - 4, rectY + spotOffset - 4, 8, 8); g.fillOval(centerX - 4, rectY + rectH - spotOffset - 4, 8, 8);

        for (Player p : players) { p.draw(g); }
    }

     private void addPlayer(int jerseyNumber, double nx, double ny) {
        int x = rectX + (int) (nx * rectW); int y = rectY + (int) (ny * rectH);
        players.add(new Player(x, y, jerseyNumber, sideColour, sideTeam));
    }

    void drawPlayers(UIWindow window){
        players.clear();
        String formation = window.formationCircle.getCurrent();
        addPlayer(1, 0.5, 0.92);
        if (formation.equals("4-4-2")) {
            addPlayer(2, 0.15, 0.75); addPlayer(3, 0.38, 0.75); addPlayer(4, 0.62, 0.75); addPlayer(5, 0.85, 0.75); addPlayer(6, 0.15, 0.50); addPlayer(7, 0.38, 0.50); addPlayer(8, 0.62, 0.50); addPlayer(9, 0.85, 0.50); addPlayer(10, 0.35, 0.22); addPlayer(11, 0.65, 0.22);
        } else if (formation.equals("4-3-3")) {
            addPlayer(2, 0.15, 0.75); addPlayer(3, 0.38, 0.75); addPlayer(4, 0.62, 0.75); addPlayer(5, 0.85, 0.75); addPlayer(6, 0.25, 0.50); addPlayer(7, 0.50, 0.50); addPlayer(8, 0.75, 0.50); addPlayer(9, 0.15, 0.20); addPlayer(10, 0.50, 0.18); addPlayer(11, 0.85, 0.20);
        } else if (formation.equals("3-5-2")) {
            addPlayer(2, 0.25, 0.78); addPlayer(3, 0.50, 0.78); addPlayer(4, 0.75, 0.78); addPlayer(5, 0.08, 0.52); addPlayer(6, 0.30, 0.52); addPlayer(7, 0.50, 0.52); addPlayer(8, 0.70, 0.52); addPlayer(9, 0.92, 0.52); addPlayer(10, 0.35, 0.22); addPlayer(11, 0.65, 0.22);
        }
        repaint();
    }

    void swapSides(UIWindow window){
        if(window.sidesCircle.getCurrent().equals("Home")){
            sideColour = Color.RED; sideTeam = Team.RED; window.sidesCircle.next();
        } else {
            sideColour = Color.BLUE; sideTeam = Team.BLUE; window.sidesCircle.next();
        }
    }
}