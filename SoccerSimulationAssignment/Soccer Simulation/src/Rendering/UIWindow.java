package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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