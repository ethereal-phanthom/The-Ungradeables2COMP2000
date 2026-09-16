package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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