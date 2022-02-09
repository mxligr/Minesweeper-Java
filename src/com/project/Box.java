package com.project;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class Box implements Comparable<Box> {

    // Must be aware of the grid it belongs to
    // Protected so that it can be directly accessed by instances of its subclasses
    protected final Grid grid;
    // Position within the grid
    private final Position position;
    // Current state
    public enum BoxState {HIDDEN, REVEALED, MARKED, UNSURE, CHECKED};
    private BoxState state;

    public Box(Grid grid, Position position) {
        this.grid = grid;
        this.position = position;
        this.state = BoxState.HIDDEN;
    }

    // Helper method to make it easier to draw images based on image name
    public static Image createImage(String imgFile) {
        Image img = null;
//			img = Toolkit.getDefaultToolkit().getImage(imgFile);
        try {
            if (img == null) {
                img = ImageIO.read(new File(imgFile));
            }
        } catch (IOException e) {
            System.out.println("Caught an IOException: " + e.getMessage());
        }
        return img;
    }

    // Abstract methods to be implemented by subclasses
    public abstract String toString();

    public abstract void leftClick();

    public abstract void doubleLeftClick();

    // Handles a right click on the box
    public void rightClick() {
        // Does nothing if the game is finished
        if (this.grid.won() || this.grid.lost())
            return;
        // Does nothing if the box is revealed
        if (this.state == BoxState.REVEALED)
            return;
            // Otherwise toggles between hidden, marked, and unsure
        else if (this.state == BoxState.HIDDEN) {
            this.state = BoxState.MARKED;
            this.grid.incNumMarked();
            this.grid.updateMinesRemaining();
        }
        else if (this.state == BoxState.MARKED) {
            this.state = BoxState.UNSURE;
            this.grid.decNumMarked();
            this.grid.updateMinesRemaining();
        }
        else if (this.state == BoxState.UNSURE)
            this.state = BoxState.HIDDEN;
        this.paint();
    }

    // Returns the position of the grid
    // Doesn't violate encapsulation because the state of the position cannot be changed
    public Position getPosition() {
        return position;
    }

    // Changes the state of the box
    public void setState(BoxState state) {
        this.state = state;
    }

    // Reveals the box
    public void reveal() {
        this.state = BoxState.REVEALED;
    }

    // The following methods return whether the box is in the specified state or not
    public boolean revealed() {
        return this.state == BoxState.REVEALED;
    }

    public boolean marked() {
        return this.state == BoxState.MARKED;
    }

    public boolean hidden() {
        return this.state == BoxState.HIDDEN;
    }

    public boolean unsure() {
        return this.state == BoxState.UNSURE;
    }

    public boolean checked() {
        return this.state == BoxState.CHECKED;
    }

    // Returns the box state as a String
    public String boxState() {
        if (this.state == BoxState.HIDDEN)
            return "hidden";
        else if (this.state == BoxState.MARKED)
            return "marked";
        else if (this.state == BoxState.UNSURE)
            return "unsure";
        else if (this.state == BoxState.CHECKED)
            return "checked";
        else
            return "revealed";
    }

    // Compares Boxes based on position
    public int compareTo(Box b) {
        return this.getPosition().compareTo(b.getPosition());
    }

    // Abstract method to draw the cell
    abstract public void draw(Graphics g);

    // Method to paint the Box
    public void paint() {
        Graphics g = grid.getGraphics();
        this.draw(g);
    }
}
