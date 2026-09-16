package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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