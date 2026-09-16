package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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