package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

class CircleNode<T> {
    T value; CircleNode<T> next; CircleNode<T> prev;
    CircleNode(T value) { this.value = value; }
}