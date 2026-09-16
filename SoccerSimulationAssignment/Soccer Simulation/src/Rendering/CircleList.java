package Rendering;

import java.awt.*;
import javax.swing.*;
import Model.*;

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