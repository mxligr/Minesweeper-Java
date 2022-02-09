package com.project;
import java.awt.Graphics;

public class Mine extends Box implements Comparable<Box> {

    public Mine(Grid grid, Position position) {
        super(grid, position);
    }

    // Handles a left click on the mine
    public void leftClick() {
        // Do nothing if the game is finished
        if (this.grid.won() || this.grid.lost())
            return;
        this.reveal();
        // The game ends when a mine is left clicked
        this.grid.lose();
        // Update status and paint to reveal the changes to the user
        this.grid.updateStatus();
        this.paint();
    }

    // Handle a double left click on the mine
    // Performs the same as a left click
    public void doubleLeftClick() {
        this.leftClick();
    }

    public String toString() {
        return "M";
    }

    public int compareTo(Box b) {
        return super.compareTo(b);
    }

    // Draws the mine dependent on the box state
    public void draw(Graphics g) {
        // x and y variables represent the top left corner of the image to be drawn
        int x = this.getPosition().getCol() * grid.scale();
        int y = this.getPosition().getRow() * grid.scale();
        // Draws an image dependent on the state
        if (this.hidden())
            g.drawImage(createImage("hidden.png"), x, y, grid.scale(), grid.scale(), null);
        else if (this.marked())
            g.drawImage(createImage("marked.png"), x, y, grid.scale(), grid.scale(), null);
        else if (this.unsure())
            g.drawImage(createImage("unsure.png"), x, y, grid.scale(), grid.scale(), null);
        else
            g.drawImage(createImage("mine.png"), x, y, grid.scale(), grid.scale(), null);
    }
}
