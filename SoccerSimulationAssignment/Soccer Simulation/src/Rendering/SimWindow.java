
package Rendering;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.util.ArrayList;
import Model.*;

class SimWindow extends Menus {
    private Timer gameLoop;
    private int redScore = 0;
    private int blueScore = 0;
    private Match match;
    private JLabel clockLabel;
    private JLabel blueScoreLabel;
    private JLabel redScoreLabel;
    private JLabel goalBanner;
    private JButton endMatchButton;
    private JPanel fullTimePanel;
    private JLabel finalScoreLabel;
    private JLabel winnerLabel;
    private int goalBannerTicksLeft = 0;

    SimWindow(UIWindow window) {
        super(window);
        setLayout(null);
        window.pitch.setBounds(0, 60, 700, 340);

        // Keep an option to leave the match at any time.
        JButton mainMenuButton = new JButton("Main Menu");
        mainMenuButton.addActionListener(e -> {
            if (gameLoop != null) gameLoop.stop();
            window.setMenu(new MainMenu(window));
        });
        window.displayButtons(new JButton[]{mainMenuButton});
        mainMenuButton.setBounds(520, 5, 170, 40);

        JPanel scoreboard = new JPanel(null);
        scoreboard.setBounds(0, 0, 700, 55);
        scoreboard.setBackground(Color.BLACK);

        blueScoreLabel = new JLabel("BLUE TEAM   0", SwingConstants.CENTER);
        blueScoreLabel.setForeground(Color.CYAN);
        blueScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        blueScoreLabel.setBounds(20, 10, 200, 35);

        redScoreLabel = new JLabel("0   RED TEAM", SwingConstants.CENTER);
        redScoreLabel.setForeground(Color.RED);
        redScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        redScoreLabel.setBounds(420, 10, 200, 35);

        clockLabel = new JLabel("0'", SwingConstants.CENTER);
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        clockLabel.setBounds(280, 8, 90, 38);

        scoreboard.add(blueScoreLabel);
        scoreboard.add(clockLabel);
        scoreboard.add(redScoreLabel);

        // A smaller message fits across the pitch without being cut off.
        goalBanner = new JLabel("GOAL!", SwingConstants.CENTER);
        goalBanner.setForeground(Color.YELLOW);
        goalBanner.setFont(new Font("SansSerif", Font.BOLD, 34));
        goalBanner.setBounds(75, 155, 550, 65);
        goalBanner.setVisible(false);

        // Full-time results are displayed in a dark panel over the pitch.
        fullTimePanel = new JPanel(null);
        fullTimePanel.setBounds(145, 120, 410, 220);
        fullTimePanel.setBackground(new Color(20, 28, 34));
        fullTimePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        fullTimePanel.setVisible(false);

        JLabel fullTimeTitle = new JLabel("FULL TIME", SwingConstants.CENTER);
        fullTimeTitle.setFont(new Font("SansSerif", Font.BOLD, 23));
        fullTimeTitle.setForeground(Color.WHITE);
        fullTimeTitle.setBounds(15, 12, 380, 35);

        finalScoreLabel = new JLabel("", SwingConstants.CENTER);
        finalScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 27));
        finalScoreLabel.setForeground(Color.WHITE);
        finalScoreLabel.setBounds(10, 48, 390, 40);

        winnerLabel = new JLabel("", SwingConstants.CENTER);
        winnerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        winnerLabel.setBounds(10, 94, 390, 38);

        endMatchButton = new JButton("Back to Menu");
        // Use a consistent button appearance on macOS.
        endMatchButton.setUI(new BasicButtonUI());
        endMatchButton.setBounds(110, 157, 190, 43);
        endMatchButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        endMatchButton.setBackground(new Color(60, 82, 98));
        endMatchButton.setForeground(Color.WHITE);
        endMatchButton.setOpaque(true);
        endMatchButton.setContentAreaFilled(true);
        endMatchButton.setBorderPainted(false);
        endMatchButton.setFocusPainted(false);
        endMatchButton.addActionListener(e -> {
            if (gameLoop != null) gameLoop.stop();
            window.setMenu(new StartMenu(window));
        });

        fullTimePanel.add(fullTimeTitle);
        fullTimePanel.add(finalScoreLabel);
        fullTimePanel.add(winnerLabel);
        fullTimePanel.add(endMatchButton);

        String formation = window.formationCircle.getCurrent();
        ArrayList<Player> matchPlayers = new ArrayList<>();
        buildTeam(matchPlayers, formation, Team.BLUE, Color.BLUE, false);
        buildTeam(matchPlayers, formation, Team.RED, Color.RED, true);

        match = new Match();
        match.setPlayers(matchPlayers);
        Ball ball = new Ball(350, 200);
        match.setBall(ball);
        window.pitch.setMatch(match);

        add(scoreboard);
        add(mainMenuButton);
        add(goalBanner);
        add(fullTimePanel);
        add(window.pitch);

        setComponentZOrder(scoreboard, 0);
        setComponentZOrder(mainMenuButton, 1);
        setComponentZOrder(fullTimePanel, 2);
        setComponentZOrder(goalBanner, 3);
        setComponentZOrder(window.pitch, 4);

        gameLoop = new Timer(50, e -> {
            match.tick(msg -> System.out.println("REFEREE: " + msg));
            match.receivePassesIfArrived();

            String scoringTeam = match.consumeLastGoalTeam();
            if (scoringTeam != null) {
                if (scoringTeam.equals("RED")) {
                    redScore++;
                } else {
                    blueScore++;
                }

                blueScoreLabel.setText("BLUE TEAM   " + blueScore);
                redScoreLabel.setText(redScore + "   RED TEAM");

                goalBanner.setText(scoringTeam + " SCORES!");
                goalBanner.setVisible(true);
                goalBannerTicksLeft = 30;
            }

            if (goalBannerTicksLeft > 0) {
                goalBannerTicksLeft--;
                if (goalBannerTicksLeft == 0) {
                    goalBanner.setVisible(false);
                }
            }

            clockLabel.setText((int) match.getMatchMinute() + "'");

            if (match.isFullTime()) {
                clockLabel.setText("FT");
                goalBannerTicksLeft = 0;
                goalBanner.setVisible(false);

                finalScoreLabel.setText(
                    "BLUE " + blueScore + "  -  " + redScore + " RED"
                );

                if (blueScore > redScore) {
                    winnerLabel.setText("BLUE TEAM WINS!");
                    winnerLabel.setForeground(Color.CYAN);
                } else if (redScore > blueScore) {
                    winnerLabel.setText("RED TEAM WINS!");
                    winnerLabel.setForeground(new Color(255, 100, 100));
                } else {
                    winnerLabel.setText("IT'S A DRAW!");
                    winnerLabel.setForeground(Color.YELLOW);
                }

                fullTimePanel.setVisible(true);
                if (gameLoop.isRunning()) gameLoop.stop();
            }

            window.pitch.repaint();
        });

        gameLoop.start();
    }

    private void buildTeam(
        ArrayList<Player> out,
        String formation,
        Team team,
        Color colour,
        boolean mirrored
    ) {
        double[][] layout = formationLayout(formation);

        for (int i = 0; i < layout.length; i++) {
            double nx = layout[i][0];
            double ny = layout[i][1];

            if (mirrored) nx = 1.0 - nx;

            int x = (int) (nx * ScreenSize.width);
            int y = (int) (ny * ScreenSize.height);
            int jersey = i + 1;

            Player p;

            if (i == 1 || i == 2) {
                p = new Defender(
                    x, y, jersey, colour, team,
                    70 + (int) (Math.random() * 20)
                );
            } else {
                p = new Player(x, y, jersey, colour, team);
            }

            if (i == 0) {
                p.setGoalkeeper(true);
            }

            out.add(p);
        }
    }

    private double[][] formationLayout(String formation) {
        if (formation.equals("4-3-3")) {
            return new double[][] {
                {0.05, 0.50},
                {0.20, 0.20}, {0.20, 0.35},
                {0.20, 0.65}, {0.20, 0.80},
                {0.40, 0.30}, {0.42, 0.50},
                {0.40, 0.70},
                {0.65, 0.20}, {0.68, 0.50},
                {0.65, 0.80}
            };
        } else if (formation.equals("3-5-2")) {
            return new double[][] {
                {0.05, 0.50},
                {0.20, 0.30}, {0.20, 0.50},
                {0.20, 0.70},
                {0.38, 0.12}, {0.40, 0.35},
                {0.42, 0.50}, {0.40, 0.65},
                {0.38, 0.88},
                {0.65, 0.40}, {0.65, 0.60}
            };
        }

        return new double[][] {
            {0.05, 0.50},
            {0.20, 0.20}, {0.20, 0.40},
            {0.20, 0.60}, {0.20, 0.80},
            {0.42, 0.15}, {0.40, 0.38},
            {0.40, 0.62}, {0.42, 0.85},
            {0.65, 0.38}, {0.65, 0.62}
        };
    }

    @Override void next1() {}
    @Override void next2() {}
    @Override void next3() {}

    @Override
    void back() {
        window.setMenu(new SimWindowMenu(window));
    }
}
