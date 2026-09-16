package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

abstract class Menus extends JPanel {
    protected UIWindow window;
    Menus(UIWindow window) { this.window = window; }
    abstract void next1();
    abstract void next2();
    abstract void next3();
    abstract void back();
}