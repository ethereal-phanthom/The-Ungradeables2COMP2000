package Rendering;

import java.awt.*;
import javax.swing.*;
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
    private int goalBannerTicksLeft = 0;

    SimWindow(UIWindow window) {
        super(window);
        setLayout(null);
        window.pitch.setBounds(0, 60, 700, 340);

        // Direct one-click exit back to the main menu, available at any point in the match.
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
        blueScoreLabel.setFont(new Font("Lexend", Font.BOLD, 18));
        blueScoreLabel.setBounds(20, 10, 200, 35);

        redScoreLabel = new JLabel("0   RED TEAM", SwingConstants.CENTER);
        redScoreLabel.setForeground(Color.RED);
        redScoreLabel.setFont(new Font("Lexend", Font.BOLD, 18));
        redScoreLabel.setBounds(420, 10, 200, 35);

        clockLabel = new JLabel("0'", SwingConstants.CENTER);
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setFont(new Font("Lexend", Font.BOLD, 22));
        clockLabel.setBounds(280, 8, 90, 38);

        scoreboard.add(blueScoreLabel);
        scoreboard.add(clockLabel);
        scoreboard.add(redScoreLabel);

        goalBanner = new JLabel("GOAL!", SwingConstants.CENTER);
        goalBanner.setForeground(Color.YELLOW);
        goalBanner.setFont(new Font("Lexend", Font.BOLD, 60));
        goalBanner.setBounds(150, 150, 400, 80);
        goalBanner.setVisible(false);

        endMatchButton = new JButton("End Match");
        endMatchButton.setBounds(250, 250, 200, 60);
        endMatchButton.setFont(new Font("Lexend", Font.BOLD, 20));
        endMatchButton.setBackground(Color.decode("#404143"));
        endMatchButton.setForeground(Color.WHITE);
        endMatchButton.setFocusPainted(false);
        endMatchButton.setVisible(false);
        endMatchButton.addActionListener(e -> {
            if (gameLoop != null) gameLoop.stop();
            window.setMenu(new StartMenu(window));
        });

        String formation = window.formationCircle.getCurrent();
        ArrayList<Player> matchPlayers = new ArrayList<>();
        buildTeam(matchPlayers, formation, Team.BLUE, Color.BLUE, false);
        buildTeam(matchPlayers, formation, Team.RED, Color.RED, true);

        match = new Match();
        match.setPlayers(matchPlayers);
        Ball ball = new Ball(350, 200);
        match.setBall(ball);
        window.pitch.setMatch(match);

        add(scoreboard); add(mainMenuButton); add(goalBanner); add(endMatchButton); add(window.pitch);

        setComponentZOrder(scoreboard, 0);
        setComponentZOrder(mainMenuButton, 1);
        setComponentZOrder(goalBanner, 2);
        setComponentZOrder(endMatchButton, 3);
        setComponentZOrder(window.pitch, 4);

        gameLoop = new Timer(50, e -> {

            String goalMessage = match.tick(msg -> System.out.println("REFEREE: " + msg));

            match.receivePassesIfArrived();

            String scoringTeam = match.consumeLastGoalTeam();
            if (scoringTeam != null) {
                if (scoringTeam.equals("RED")) redScore++;
                else blueScore++;
                blueScoreLabel.setText("BLUE TEAM   " + blueScore);
                redScoreLabel.setText(redScore + "   RED TEAM");

                goalBanner.setText((scoringTeam.equals("RED") ? "RED" : "BLUE") + " SCORES!");
                goalBanner.setVisible(true);
                goalBannerTicksLeft = 30;
            }

            if (goalBannerTicksLeft > 0) {
                goalBannerTicksLeft--;
                if (goalBannerTicksLeft == 0) goalBanner.setVisible(false);
            }

            clockLabel.setText((int) match.getMatchMinute() + "'");

            if (match.isFullTime()) {
                clockLabel.setText("FT");
                endMatchButton.setVisible(true);
                if (gameLoop.isRunning()) gameLoop.stop();
            }

            window.pitch.repaint();
        });
        gameLoop.start();
    }

    private void buildTeam(ArrayList<Player> out, String formation, Team team, Color colour, boolean mirrored) {
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
                p = new Defender(x, y, jersey, colour, team, 70 + (int) (Math.random() * 20));
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
                {0.20, 0.20}, {0.20, 0.35}, {0.20, 0.65}, {0.20, 0.80},
                {0.40, 0.30}, {0.42, 0.50}, {0.40, 0.70},
                {0.65, 0.20}, {0.68, 0.50}, {0.65, 0.80}
            };
        } else if (formation.equals("3-5-2")) {
            return new double[][] {
                {0.05, 0.50},
                {0.20, 0.30}, {0.20, 0.50}, {0.20, 0.70},
                {0.38, 0.12}, {0.40, 0.35}, {0.42, 0.50}, {0.40, 0.65}, {0.38, 0.88},
                {0.65, 0.40}, {0.65, 0.60}
            };
        }
        return new double[][] {
            {0.05, 0.50},
            {0.20, 0.20}, {0.20, 0.40}, {0.20, 0.60}, {0.20, 0.80},
            {0.42, 0.15}, {0.40, 0.38}, {0.40, 0.62}, {0.42, 0.85},
            {0.65, 0.38}, {0.65, 0.62}
        };
    }

    @Override void next1() {} @Override void next2() {} @Override void next3() {}
    @Override void back() { window.setMenu(new SimWindowMenu(window)); }
}