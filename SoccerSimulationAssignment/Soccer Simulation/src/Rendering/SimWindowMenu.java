package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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