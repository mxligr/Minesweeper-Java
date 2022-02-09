package com.project;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Num extends Box implements Comparable<Box> {
    private final int numMines;


    // General constructor used for board generation upon first click
    public Num(Grid grid, Position position) {
        super(grid, position);
        //Sets number of mines to the number of mines adjacent to the user
        this.numMines = grid.mineCount(position);
    }

    // Specific constructor used for replaying a game after losing
    public Num(Grid grid, Position position, int num) {
        super(grid, position);
        // Board is preset so there is no need to calculate the number of mines
        this.numMines = num;
    }

    // Handles a left click on the number
    // Exclusively reveals nums if they are not revealed
    public void leftClick() {
        // Do nothing if the game is finished
        if (this.grid.won() || this.grid.lost())
            return;
        // Reveal the num if it is hidden
        if (this.hidden()) {
            this.reveal();
            // Increment the count of revealed numbers in the grid
            this.grid.incNumRevealed();
            if (this.grid.hasWon()) {
                try {
                    grid.win();
                } catch (IOException e) {
                    System.out.println("Caught an IOException: " + e.getMessage());
                }
            }
            // Reveal all surrounding boxes if the box touches no mines
            // Works recursively by recalling this method (within grid.cascade) if an adjacent box also touches 0 mines
            if (this.numMines == 0)
                this.grid.cascade(this.getPosition());
        }
        // Update the game status for the grid
        this.grid.updateStatus();
        this.paint();
    }

    // Handles a double left click on the number
    public void doubleLeftClick() {
        // Do nothing if the game is finished
        if (this.grid.won() || this.grid.lost())
            return;
        if (this.revealed()) {
            // If the box is touching as many marked mines as it actually does, it will reveal all mines around itself
            // via the cascade method earlier.
            if (this.grid.markedCount(this.getPosition()) == this.numMines) {
                this.grid.cascade(this.getPosition());
                // The user may have won, so the game status must be updated
                this.grid.updateStatus();
            }
            // Otherwise, display a red x for 1/8 of a second to notify the user the above requirement is not met
            else {
                this.setState(BoxState.CHECKED);
                // Paint to display the x
                this.paint();
                // Wait .125 seconds
                try {
                    TimeUnit.MILLISECONDS.sleep(125);
                } catch (InterruptedException e) {
                    System.out.println("Caught and Interrupted Exception: " + e.getMessage());
                }
                // Reset the state to revealed
                this.setState(BoxState.REVEALED);
            }
        }
        // Repaint to the new state (will remove the x)
        this.paint();
    }

    public int getNumMines() {
        return this.numMines;
    }

    // Returns the number of mines as a string
    public String toString() {
        return Integer.toString(this.numMines);
    }

    public int compareTo(Box b) {
        return super.compareTo(b);
    }

    // Draws the num dependent on its boxState
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
        else if (this.checked()) {
            // Draws the red x
            g.setColor(Color.RED);
            // Uses 2D graphics to set stroke thickness
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(x + 2, y + 2, x - 2 + grid.scale(), y - 2 + grid.scale());
            g2.drawLine(x - 2 + grid.scale(), y + 2, x + 2, y - 2 + grid.scale());
        }
        // Otherwise the num is revealed
        else
            g.drawImage(createImage("" + this.numMines + ".png"), x, y, grid.scale(), grid.scale(), null);
    }
}
