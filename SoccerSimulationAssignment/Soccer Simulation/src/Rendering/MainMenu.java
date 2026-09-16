package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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