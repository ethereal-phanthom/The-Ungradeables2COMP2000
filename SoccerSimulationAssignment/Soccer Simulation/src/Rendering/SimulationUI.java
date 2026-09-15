package Rendering;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

import Model.SoccerPitch;
import Model.Player;
import Model.Defender;
import Model.Ball;
import Model.Match;
import Model.Team;

class UIWindow {
    JFrame frame;
    Menus currentMenu;
    CircleList<String> formationCircle;
    CircleList<String> sidesCircle;
    SoccerPitch pitch;
    VerticalPitch vertPitch;
    boolean debugMode = false;
    String backgroundColour = "#329632";
    String btnColour = "#404143";

    public UIWindow() {
        frame = new JFrame("Soccer Simulation");
        frame.setSize(ScreenSize.width, ScreenSize.height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        formationCircle = new CircleList<>(new String[] { "4-4-2", "4-3-3", "3-5-2" });
        sidesCircle = new CircleList<>(new String[] { "Home", "Away" });
        pitch = new SoccerPitch();
        vertPitch = new VerticalPitch();
    }

    void setMenu(Menus menu) {
        currentMenu = menu;
        frame.setContentPane(menu);
        frame.revalidate();
        frame.repaint();
        frame.getContentPane().setBackground(Color.decode(backgroundColour));
    }

    void start() {
        setMenu(new MainMenu(this));
        frame.setVisible(true);
    }

    boolean getDebug() { return this.debugMode; }
    void setDebug(boolean b) {
        this.debugMode = b;
        frame.setTitle("Soccer Simulation" + debugString());
    }

    private String debugString() {
        if (this.getDebug() == true) return " (Debug)";
        return "";
    }

    void displayButtons(JButton[] buttons) {
        for (JButton btn : buttons) {
            btn.setBackground(Color.decode(btnColour));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Lexend", Font.BOLD, 16));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    void displayCheckBox(JCheckBox[] boxes) {
        for (JCheckBox cbx : boxes) {
            cbx.setBackground(Color.decode(btnColour));
            cbx.setForeground(Color.WHITE);
            cbx.setFont(new Font("Lexend", Font.BOLD, 16));
            cbx.setBorderPainted(false);
            cbx.setFocusPainted(false);
            cbx.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    void displayLabel(JLabel[] labels) {
        for (JLabel lbl : labels) {
            lbl.setBackground(Color.decode(btnColour));
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Lexend", Font.BOLD, 16));
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setVerticalAlignment(JLabel.CENTER);
        }
    }
}


abstract class Menus extends JPanel {
    protected UIWindow window;
    Menus(UIWindow window) { this.window = window; }
    abstract void next1();
    abstract void next2();
    abstract void next3();
    abstract void back();
}


class MainMenu extends Menus {
    MainMenu(UIWindow window) {
        super(window);
        setLayout(null);
        window.pitch.setBounds(0, 0, 700, 400);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> next1());
        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> next2());
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> back());

        JButton[] buttons = { startButton, settingsButton, exitButton };
        window.displayButtons(buttons);

        startButton.setBounds(ScreenSize.width / 2 - 125, ScreenSize.height / 2 - 100, 250, 50);
        settingsButton.setBounds(ScreenSize.width / 2 - 125, ScreenSize.height / 2 - 30, 250, 50);
        exitButton.setBounds(ScreenSize.width / 2 - 125, ScreenSize.height / 2 + 40, 250, 50);

        add(startButton); add(settingsButton); add(exitButton); add(window.pitch);

        setComponentZOrder(startButton, 0);
        setComponentZOrder(settingsButton, 1);
        setComponentZOrder(exitButton, 2);
        setComponentZOrder(window.pitch, 3);
    }
    @Override void next1() { window.setMenu(new StartMenu(window)); }
    @Override void next2() { window.setMenu(new SettingsMenu(window)); }
    @Override void next3() {}
    @Override void back() { System.exit(0); }
}


class StartMenu extends Menus {
    StartMenu(UIWindow window) {
        super(window);
        setLayout(null);
        window.vertPitch.setBounds(0, 0, 700, 400);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> next1());
        JButton formationButton = new JButton("Formation Settings");
        formationButton.addActionListener(e -> next2());
        JButton matchSettingsButton = new JButton("Match Settings");
        matchSettingsButton.addActionListener(e -> next3());
        JButton exitButton = new JButton("Back");
        exitButton.addActionListener(e -> back());

        JButton[] buttons = { startButton, formationButton, matchSettingsButton, exitButton };
        window.displayButtons(buttons);

        startButton.setBounds(ScreenSize.width / 4 - 125, ScreenSize.height / 2 - 140, 250, 50);
        formationButton.setBounds(ScreenSize.width / 4 - 125, ScreenSize.height / 2 - 70, 250, 50);
        matchSettingsButton.setBounds(ScreenSize.width / 4 - 125, ScreenSize.height / 2, 250, 50);
        exitButton.setBounds(ScreenSize.width / 4 - 125, ScreenSize.height / 2 + 70, 250, 50);

        add(startButton); add(formationButton); add(matchSettingsButton); add(exitButton); add(window.vertPitch);
        window.vertPitch.drawPlayers(window);

        setComponentZOrder(startButton, 0);
        setComponentZOrder(formationButton, 1);
        setComponentZOrder(matchSettingsButton, 2);
        setComponentZOrder(exitButton, 3);
        setComponentZOrder(window.vertPitch, 4);
    }
    @Override void next1() { window.setMenu(new SimWindow(window)); }
    @Override void next2() { window.setMenu(new FormationMenu(window)); }
    @Override void next3() { window.setMenu(new MatchSettingsMenu(window)); }
    @Override void back() { window.setMenu(new MainMenu(window)); }
}


class SettingsMenu extends Menus {
    SettingsMenu(UIWindow window) {
        super(window);
        setLayout(null);

        JCheckBox debugButton = new JCheckBox("Debug Mode", window.getDebug());
        debugButton.addActionListener(e -> window.setDebug(debugButton.isSelected()));
        JButton exitButton = new JButton("Save");
        exitButton.addActionListener(e -> back());

        JButton[] buttons = { exitButton }; JCheckBox[] boxes = { debugButton };
        window.displayCheckBox(boxes); window.displayButtons(buttons);
        debugButton.setBounds(ScreenSize.width / 2 - 125, ScreenSize.height / 2 - 100, 250, 50);
        exitButton.setBounds(ScreenSize.width / 2 - 125, ScreenSize.height / 2 - 30, 250, 50);

        add(debugButton); add(exitButton);
    }
    @Override void next1() {} @Override void next2() {} @Override void next3() {}
    @Override void back() { window.setMenu(new MainMenu(window)); }
}


class FormationMenu extends Menus {
    FormationMenu(UIWindow window) {
        super(window);
        setLayout(null);
        window.vertPitch.setBounds(0, 0, 700, 400);

        JButton formPrevBtn = new JButton("<"); JButton formNextBtn = new JButton(">");
        JLabel formationLabel = new JLabel(window.formationCircle.getCurrent().toString());
        formPrevBtn.addActionListener(e -> { window.formationCircle.previous(); formationLabel.setText(window.formationCircle.getCurrent().toString()); window.vertPitch.drawPlayers(window); });
        formNextBtn.addActionListener(e -> { window.formationCircle.next(); formationLabel.setText(window.formationCircle.getCurrent().toString()); window.vertPitch.drawPlayers(window); });

        JButton swapPrevBtn = new JButton("<"); JButton swapNextBtn = new JButton(">");
        JLabel swapLabel = new JLabel(window.sidesCircle.getCurrent().toString());
        swapPrevBtn.addActionListener(e -> { window.vertPitch.swapSides(window); swapLabel.setText(window.sidesCircle.getCurrent().toString()); window.vertPitch.drawPlayers(window); });
        swapNextBtn.addActionListener(e -> { window.vertPitch.swapSides(window); swapLabel.setText(window.sidesCircle.getCurrent().toString()); window.vertPitch.drawPlayers(window); });

        JButton exitButton = new JButton("Save"); exitButton.addActionListener(e -> back());

        JButton[] buttons = { formPrevBtn, formNextBtn, swapPrevBtn, swapNextBtn, exitButton };
        JLabel[] labels = { formationLabel, swapLabel };
        window.displayLabel(labels); window.displayButtons(buttons);

        formPrevBtn.setBounds(ScreenSize.width / 4 - 125, ScreenSize.height / 2 - 140, 45, 50);
        formationLabel.setBounds(ScreenSize.width / 4 - 75, ScreenSize.height / 2 - 140, 150, 50);
        formNextBtn.setBounds(ScreenSize.width / 4 + 80, ScreenSize.height / 2 - 140, 45, 50);
        swapPrevBtn.setBounds(ScreenSize.width / 4 - 125, ScreenSize.height / 2 - 70, 45, 50);
        swapLabel.setBounds(ScreenSize.width / 4 - 75, ScreenSize.height / 2 - 70, 150, 50);
        swapNextBtn.setBounds(ScreenSize.width / 4 + 80, ScreenSize.height / 2 - 70, 45, 50);
        exitButton.setBounds(ScreenSize.width / 4 - 125, ScreenSize.height / 2, 250, 50);

        add(formPrevBtn); add(formationLabel); add(formNextBtn); add(swapPrevBtn); add(swapLabel); add(swapNextBtn); add(exitButton); add(window.vertPitch);
        window.vertPitch.drawPlayers(window);

        setComponentZOrder(formPrevBtn, 0);
        setComponentZOrder(formationLabel, 1);
        setComponentZOrder(formNextBtn, 2);
        setComponentZOrder(swapPrevBtn, 3);
        setComponentZOrder(swapLabel, 4);
        setComponentZOrder(swapNextBtn, 5);
        setComponentZOrder(exitButton, 6);
        setComponentZOrder(window.vertPitch, 7);
    }
    @Override void next1() {} @Override void next2() {} @Override void next3() {}
    @Override void back() { window.setMenu(new StartMenu(window)); }
}


class MatchSettingsMenu extends Menus {
    MatchSettingsMenu(UIWindow window) {
        super(window);
        setLayout(null);
        JButton exitButton = new JButton("Save"); exitButton.addActionListener(e -> back());
        JButton[] buttons = { exitButton }; window.displayButtons(buttons);
        exitButton.setBounds(ScreenSize.width / 2 - 125, ScreenSize.height / 2 - 100, 250, 50);
        add(exitButton);
    }
    @Override void next1() {} @Override void next2() {} @Override void next3() {}
    @Override void back() { window.setMenu(new StartMenu(window)); }
}


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


class SimWindowMenu extends Menus {
    SimWindowMenu(UIWindow window) {
        super(window);
        setLayout(null);
        JButton resumeButton = new JButton("Resume"); resumeButton.addActionListener(e -> next1());
        JButton exitButton = new JButton("Exit"); exitButton.addActionListener(e -> back());
        JButton[] buttons = { resumeButton, exitButton }; window.displayButtons(buttons);
        resumeButton.setBounds(ScreenSize.width / 2 - 75, ScreenSize.height / 2 - 100, 150, 50);
        exitButton.setBounds(ScreenSize.width / 2 - 75, ScreenSize.height / 2 - 30, 150, 50);
        add(resumeButton); add(exitButton);
    }
    @Override void next1() { window.setMenu(new SimWindow(window)); }
    @Override void next2() {} @Override void next3() {}
    @Override void back() { window.setMenu(new StartMenu(window)); }
}


class CircleNode<T> {
    T value; CircleNode<T> next; CircleNode<T> prev;
    CircleNode(T value) { this.value = value; }
}


class CircleList<T> {
    private CircleNode<T> current;
    CircleList(T[] values) {
        CircleNode<T> start = new CircleNode<T>(values[0]);
        CircleNode<T> prevNode = start;
        for (int i = 1; i < values.length; i++) {
            CircleNode<T> node = new CircleNode<>(values[i]);
            prevNode.next = node; node.prev = prevNode; prevNode = node;
        }
        prevNode.next = start; start.prev = prevNode;
        this.current = start;
    }
    T getCurrent() { return current.value; }
    void next() { this.current = this.current.next; }
    void previous() { this.current = this.current.prev; }
}


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


public class SimulationUI {
    public static void main(String[] args) {
        new UIWindow().start();
    }
}