package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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