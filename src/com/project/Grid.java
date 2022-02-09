package com.project;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class Grid extends JPanel {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Instance Variables                                                                                           *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // 2D array of Box elements make up the grid
    private Box[][] grid;
    // Count of number of mines in the grid
    private int numMines;
    // Count of number of marked mines in the grid
    private int numMarked;
    // Count of revealed nums in the grid
    private int numRevealed;
    // Count of duration of the current game in seconds
    private int time;
    // User's name, changed upon the user achieving a highscore
    private String name = "user";
    // Status of the game
    public enum GameStatus {NOT_STARTED, IN_PROGRESS, LOST, WON};
    private GameStatus gameStatus;
    // JLabel showing the status to the user
    private JLabel status;
    // JLabel showing the number of mines remaining to the user
    private JLabel minesRemaining;
    // JLabel showing the time duration to the user
    private JLabel timeStatus;
    // Difficulty of the game
    public enum Difficulty {BEGINNER, INTERMEDIATE, EXPERT, CUSTOM};
    private Difficulty difficulty;

    // Timer which increments the time instance variable
    private Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            incTime();
        }
    });

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		General Formatting and Helper Methods                                                                        *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


    // Helper method for constructing a grid to default values and updating various JLabels
    private void jLabelHelper(JLabel status, JLabel minesRemaining, JLabel timeStatus) {
        this.numMarked = 0;
        this.gameStatus = GameStatus.NOT_STARTED;
        this.status = status;
        this.updateStatus();
        this.minesRemaining = minesRemaining;
        this.updateMinesRemaining();
        this.time = 0;
        this.timeStatus = timeStatus;
        this.updateTime();
        this.numRevealed = 0;
    }

    // Helper method to output a beep sound
    private void beepHelper(String s) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(s).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (IOException exception) {
            System.out.println("Caught IO Exception: " + exception.getMessage());
        } catch (UnsupportedAudioFileException exception) {
            System.out.println("Caught Unsupported Audio File Exception: " + exception.getMessage());
        } catch (LineUnavailableException exception) {
            System.out.println("Caught Line Unavailable Exception: " + exception.getMessage());
        }
    }

    // Sets width and height of each grid depending on the size of the grid
    public int scale() {
        if (this.getRows() < 16)
            return 64;
        if (this.getRows() < 32)
            return 48;
        return 32;
    }

    // Output a beep
    private void beep() {
        beepHelper("beep.wav");
    }

    // Output a different toned beep
    private void beep2() {
        beepHelper("beep2.wav");
    }

    // Helper method to add mouse listener to the grid
    public void mouseListenerHelper() {
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (won() || lost())
                    return;
                // Find c, the column, and r, the row, in which the click event occurred
                int x = e.getX();
                int c = (int) (x / scale());
                int y = e.getY();
                int r = (int) (y / scale());
                // Handle left, right, and double clicks
                // Makes "beep" sound for left click, "beep2" sound for right click
                if (e.getClickCount() == 2) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        beep();
                        doubleLeftClick(r, c);
                    }
                    else if (SwingUtilities.isRightMouseButton(e)) {
                        rightClick(r, c);
                        beep2();
                    }
                }
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    leftClick(r, c);
                    beep();
                }
                else if (SwingUtilities.isRightMouseButton(e)) {
                    rightClick(r, c);
                    beep2();
                }
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }
        });
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Constructors                                                                                                 *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // Constructor for custom grid creation
    public Grid(int rows, int columns, int numMines, JLabel status, JLabel minesRemaining, JLabel timeStatus) {
        // Creates an empty grid to be generated upon the first left click
        this.grid = new Box[rows][columns];
        this.numMines = numMines;
        this.difficulty = Difficulty.CUSTOM;
        // Calls helper methods to execute JLabel and mouse listener configuration
        this.jLabelHelper(status, minesRemaining, timeStatus);
        this.mouseListenerHelper();
    }

    // Constructor for any preset difficulty
    public Grid(Difficulty d, JLabel status, JLabel minesRemaining, JLabel timeStatus) {
        // Sets difficulty, grid size, and number of mines depending on the passed in difficulty
        if (d == Difficulty.BEGINNER) {
            this.grid = new Box[8][8];
            this.numMines = 10;
        }
        else if (d == Difficulty.INTERMEDIATE) {
            this.grid = new Box[16][16];
            this.numMines = 40;
        }
        else {
            this.grid = new Box[16][32];
            this.numMines = 99;
        }
        this.difficulty = d;
        this.jLabelHelper(status, minesRemaining, timeStatus);
        this.mouseListenerHelper();
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Methods to reset the grid                                                                                    *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // Helper method for standardizing a grid when resetting
    private void resetHelper() {
        this.time = 0;
        this.numMarked = 0;
        this.numRevealed = 0;
        this.gameStatus = GameStatus.NOT_STARTED;
        this.timer.stop();
        this.name = "user";
        this.updateStatus();
        this.updateMinesRemaining();
        this.updateTime();
        this.repaint();
    }

    // General method to reset the grid, creating a new, empty one, ready to play again
    public void reset() {
        this.grid = new Box[this.getRows()][this.getCols()];
        this.resetHelper();
    }

    // Reset method for a new difficulty
    public void reset(Difficulty d) {
        // If the difficulty is not being changed, call the general reset method
        if (this.difficulty == d)
            this.reset();
        // Otherwise, create a new grid with the new difficulty
        Grid g = new Grid(d, new JLabel("Game Not Started"), new JLabel("Mines left: 10"), new JLabel("Time: 0"));
        this.grid = g.grid;
        this.difficulty = g.difficulty;
        this.numMines = g.numMines;
        this.resetHelper();
    }

    // Reset method for a custom difficulty
    public void reset(int row, int col, int mines) {
        this.grid = new Box[row][col];
        this.difficulty = Difficulty.CUSTOM;
        this.numMines = mines;
        this.resetHelper();
    }


    // Adapted reset method to replay a copy of the past game (same mines and number locations)
    public void replay() throws IOException {
        // Reads in the past grid
        char[][] arr = Grid.readGrid();
        // Sets the grid to a blank grid of the read in grid's size
        this.grid = new Box[arr.length][arr[0].length];
        // Fills in the grid based on the char array read in
        for (int r = 0; r < arr.length; r++) {
            for (int c = 0; c < arr[0].length; c++) {
                char s = arr[r][c];
                Position p = new Position(r, c);
                if (s == 'M')
                    this.grid[r][c] = new Mine(this, p);
                // Increments numbers to check that the char value is equal to the char value passed in
                for (int num = 0; num <= 8; num++) {
                    // Shifts the number value to its corresponding unicode value
                    if (s == 48 + num)
                        this.grid[r][c] = new Num(this, p, num);
                }
            }
        }
        // Finishes reset with the reset helper method
        this.resetHelper();
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Setters, Getters, and Modifiers                                                                              *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // Increment the number of revealed numbers
    public void incNumRevealed() {
        this.numRevealed ++;
    }

    public int getNumRevealed() {
        return this.numRevealed;
    }

    // Increment the number of marked boxes
    public void incNumMarked() {
        this.numMarked++;
    }

    // Decrement the number of marked boxes
    public void decNumMarked() {
        this.numMarked--;
    }

    public int getNumMarked() {
        return this.numMarked;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Increments the time of the game, capping it at 999
    private void incTime() {
        if (this.time == 999)
            return;
        this.time++;
        this.updateTime();
    }

    // Helper method to return the box located at a certain position in the grid
    private Box getBox(Position pos) {
        return this.grid[pos.getRow()][pos.getCol()];
    }

    public int getNumMines() {
        return this.numMines;
    }

    public int getRows() {
        return this.grid.length;
    }

    public int getCols() {
        Box[] arr = grid[0];
        if (arr == null)
            return 0;
        return arr.length;
    }

    public int getTime() {
        return time;
    }

    public boolean won() {
        return this.gameStatus == GameStatus.WON;
    }

    public boolean lost() {
        return this.gameStatus == GameStatus.LOST;
    }

    public boolean notStarted() {
        return this.gameStatus == GameStatus.NOT_STARTED;
    }

    public boolean inProgress() {
        return this.gameStatus == GameStatus.IN_PROGRESS;
    }

    // Returns the game status capitalized for status updates
    public String gameStatusToString() {
        if (this.gameStatus == GameStatus.NOT_STARTED)
            return "Not Started";
        if (this.gameStatus == GameStatus.IN_PROGRESS)
            return "In Progress";
        if (this.gameStatus == GameStatus.WON)
            return "Won";
        else
            return "Lost";
    }

    // Returns the difficulty with its first letter capitalized
    public String difficultyToString() {
        if (this.difficulty == Difficulty.BEGINNER)
            return "Beginner";
        if (this.difficulty == Difficulty.INTERMEDIATE)
            return "Intermediate";
        if (this.difficulty == Difficulty.EXPERT)
            return "Expert";
        else
            return "Custom";
    }

    // Update the status JLabel's text
    public void updateStatus() {
        this.status.setText("Game " + this.gameStatusToString());
    }

    // Update the text of the mines left JLabel
    public void updateMinesRemaining() {
        int minesLeft = this.numMines - this.numMarked;
        this.minesRemaining.setText("Mines left: " + minesLeft);
    }

    // Update the text of the time left JLabel
    public void updateTime() {
        this.timeStatus.setText("Time: " + this.time);
    }

    // Returns a set of the positions of all boxes within the grid adjacent to the passed in position
    private Set<Position> getSurroundings(Position p) {
        // The spans for each direction ensure that only positions within the array are checked
        int spanUp = 1, spanDown = 1, spanRight = 1, spanLeft = 1;
        if (p.getRow() - 1 < 0)
            spanUp = 0;
        if (p.getCol() - 1 < 0)
            spanLeft = 0;
        if (p.getRow() + 1 >= grid.length)
            spanDown = 0;
        if (p.getCol() + 1 >= grid[0].length)
            spanRight = 0;
        Set<Position> positions = new TreeSet<Position>();
        // Iterates through the subset of the grid array determined by the distances of the span from the position
        for (int r = p.getRow() - spanUp; r <= p.getRow() + spanDown; r++)
            for (int c = p.getCol() - spanLeft; c <= p.getCol() + spanRight; c++)
                // Adds this position to the set of surrounding positions
                positions.add(new Position(r, c));
        return positions;
    }

    // Returns the number of mines adjacent to the passed in position
    public int mineCount(Position pos) {
        int mineCount = 0;
        Set<Position> surroundings = this.getSurroundings(pos);
        // Iterates through the set of surroundings
        for (Position p : surroundings) {
            Box b = this.grid[p.getRow()][p.getCol()];
            if (!(b == null) && b instanceof Mine)
                // Increments the count if it is an instance of the mine class
                mineCount++;
        }
        return mineCount;
    }

    // Returns the number of marked boxes adjacent to the passed in position
    public int markedCount(Position pos) {
        int markedCount = 0;
        Set<Position> surroundings = this.getSurroundings(pos);
        for (Position p : surroundings) {
            Box b = this.grid[p.getRow()][p.getCol()];
            if (!(b == null) && b.marked())
                markedCount++;
        }
        return markedCount;
    }

    // Returns if the user has won determined by if the number of revealed nums is equal to the number of nums in total
    public boolean hasWon() {
        if (this.lost()) {
            return false;
        }
        int nums = this.getRows() * this.getCols() - this.numMines;
        return nums == this.numRevealed;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Click Handling and Related Methods                                                                           *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // Handles a left click in the grid
    public void leftClick(int row, int col) {
        // Ensure the click is within the grid
        if (row >= 0 && col >= 0 && row < this.getRows() && col < this.getCols()) {
            Box b = this.grid[row][col];
            // Generates the board if not already generated
            if (b == null) {
                this.generate(row, col);
                // Reassigns b
                b = this.grid[row][col];
            }
            // Starts the game if it is not already started, will only happen in the case of replaying a game
            if (this.notStarted()) {
                this.gameStatus = GameStatus.IN_PROGRESS;
                this.timer.start();
            }
            // Dispatches left click to the box itself
            b.leftClick();
        }
    }

    // Handles a double left click in the grid
    public void doubleLeftClick(int row, int col) {
        if (row >= 0 && col >= 0 && row < this.getRows() && col < this.getCols()) {
            Box b = this.grid[row][col];
            if (b == null) {
                this.generate(row, col);
                b = this.grid[row][col];
            }
            b.doubleLeftClick();
        }
    }

    // Handles a right click in the grid
    public void rightClick(int row, int col) {
        if (row >= 0 && col >= 0 && row < this.getRows() && col < this.getCols()) {
            Box b = this.grid[row][col];
            // Does nothing if the grid is not yet generated
            if (b != null)
                b.rightClick();
        }
    }

    // Generates the grid with random mine distribution
    public void generate(int row, int col) {
        Set<Position> surroundings = getSurroundings(new Position(row, col));

        // Creates a list of positions where mines could be placed.
        // This excludes the surroundings of the positions the user clicks at because where the user clicks must be
        // touching zero mines.
        List<Position> openPositions = new ArrayList<Position>();
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[0].length; c++) {
                Position p = new Position(r, c);
                if (!surroundings.contains(p))
                    openPositions.add(p);
            }

        // Adds all mines at random positions within the open positions until
        int count = 0;
        while (count < this.numMines) {
            int randomIndex = (int) Math.floor(Math.random() * openPositions.size());
            Position randomPos = openPositions.get(randomIndex);
            grid[randomPos.getRow()][randomPos.getCol()] = new Mine(this, randomPos);
            // Removes the position where the mine was added from the list of open positions
            openPositions.remove(randomIndex);
            count++;
        }
        // Iterates through the grid filling null boxes with numbers with the correct mine count
        for (int r = 0; r < this.getRows(); r++)
            for (int c = 0; c < this.getCols(); c++) {
                Box b = this.grid[r][c];
                if (b == null) {
                    Position p = new Position(r, c);
                    // Mine count is handled when the mine is instantiated
                    this.grid[r][c] = new Num(this, p);
                }
            }
        this.gameStatus = GameStatus.IN_PROGRESS;
        // Starts the timer, as the game has now been started
        this.timer.start();
    }

    // Reveals all boxes adjacent to a clicked box which are hidden
    public void cascade(Position pos) {
        // Do nothing if the game is finished
        if (this.won() || this.lost())
            return;
        Set<Position> cascadeSetPositions = this.getSurroundings(pos);
        cascadeSetPositions.remove(pos);
        Set<Box> cascadeSet = new TreeSet<Box>();
        for (Position p : cascadeSetPositions) {
            Box b = this.getBox(p);
            if (b != null && b.hidden())
                cascadeSet.add(b);
        }
        for (Box b : cascadeSet) {
            b.leftClick();
        }
    }

    // Handles the user losing a game
    public void lose() {
        // Reveals all boxes in the grid
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[0].length; c++) {
                Box b = this.grid[r][c];
                b.reveal();
            }
        this.timer.stop();
        this.gameStatus = GameStatus.LOST;
        this.repaint();

        // Outputs boom noise
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("boom.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (IOException e) {
            System.out.println("Caught IO Exception: " + e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Caught Unsupported Audio File Exception: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("Caught Line Unavailable Exception: " + e.getMessage());
        }

        try {
            this.writeGrid();
        } catch (IOException e) {
            System.out.println("Caught IO Exception: " + e.getMessage());
        }

        // Creates a frame telling the loser they lost and allowing the user to replay game, start a new game, or quit
        JFrame lose = new JFrame();
        lose.setLocationRelativeTo(this);

        // Tells user that they lost
        JPanel ohNoWrapper = new JPanel();
        JLabel ohNo = new JLabel("<html><b><i>Oh no!</i></b> You hit a mine.</html>");
        ohNo.setFont(new Font("Sans Serif", Font.PLAIN, 20));
        ohNo.setBorder(new EmptyBorder(18, 18, 0, 18));
        ohNoWrapper.add(ohNo);

        // Creates a button allowing the user to replay the game
        JButton replay = new JButton("Replay");
        replay.setFont(new Font("Sans Serif", Font.PLAIN, 18));
        replay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    replay();
                } catch (IOException exception) {
                    System.out.println("Caught IO Exception: " + exception.getMessage());
                }
                lose.dispose();
            }
        });

        // Creates a button allowing the user to play a new game
        JButton reset = new JButton("New Game");
        reset.setFont(new Font("Sans Serif", Font.PLAIN, 18));
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reset();
                lose.dispose();
            }
        });

        // Creates a button allowign the user the quit the application
        JButton quit = new JButton("Quit");
        quit.setFont(new Font("Sans Serif", Font.PLAIN, 18));
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Adds the above buttons to a wrapper panel called buttons
        JPanel buttons = new JPanel();
        buttons.setBorder(new EmptyBorder(18, 18, 18, 18));
        buttons.add(replay);
        buttons.add(reset);
        buttons.add(quit);

        // Creates an overarching wrapper and aligns the panels vertically
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(ohNoWrapper);
        wrapper.add(buttons);
        lose.add(wrapper);

        lose.pack();
        lose.setVisible(true);
    }

    // Handles the user wining a game
    public void win() throws IOException {
        this.gameStatus = GameStatus.WON;
        this.updateStatus();
        this.timer.stop();
        this.repaint();

        // Outputs a tada sound
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("tada.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (IOException e) {
            System.out.println("Caught IO Exception: " + e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Caught Unsupported Audio File Exception: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("Caught Line Unavailable Exception: " + e.getMessage());
        }

        // Creates a pane asking the user to enter their name if they set a highscore
        try {
            if (this.name.equals("user") && this.isHighScore()) {
                JLabel nameLabel = new JLabel("<html>Congrats! You got a high score.<br>"
                        + "Please enter your name:</html>");
                nameLabel.setFont(new Font("Sans Serif", Font.PLAIN, 18));

                JTextField nameField = new JTextField(8);

                JPanel namePanel = new JPanel();
                namePanel.add(nameLabel);
                namePanel.add(nameField);

                int result = JOptionPane.showConfirmDialog(
                        null,
                        namePanel,
                        "Enter Your Name",
                        JOptionPane.OK_CANCEL_OPTION
                );
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String name = nameField.getText();

                        if (name == null || name.length() < 2 || name.length() > 18)
                            throw new IllegalArgumentException();
                        else
                            this.setName(name);
                    } catch (Exception error) {
                        JOptionPane.showMessageDialog(null, "Input Exception.", "Inane error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Caught an IO Exception: " + e.getMessage());
        }
        // Rewrite the highscores file corresponding to the difficulty with the user's name and score
        this.writeHighScores();
        this.repaint();
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Handling Drawing the Grid                                                                                    *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.gray);
        for (int r = 0; r < this.getRows(); r++) {
            for (int c = 0; c < this.getCols(); c++) {
                Box b = grid[r][c];
                // Draws the grid with the hidden image if elements are null
                if (b == null) {
                    int x = c * scale();
                    int y = r * scale();
                    g.drawImage(Box.createImage("hidden.png"), x, y, scale(), scale(), null);
                }
                // Otherwise draws each box
                else
                    b.draw(g);
            }
        }
        // Draws a partly transparent red rectangle over everything if the game is lost
        if (this.lost()) {
            g.setColor(new Color(255, 0, 0, 48));
            g.fillRect(0, 0, this.getCols() * scale(), this.getRows() * scale());
        }
        // Draws a partly transparent green rectangle over everything if the game is won
        else if (this.won()) {
            g.setColor(new Color(0, 255, 0, 48));
            g.fillRect(0, 0, this.getCols() * scale(), this.getRows() * scale());
        }
    }

    // Returns the size of the grid dependent on the scale of each box
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.getCols() * scale(), this.getRows() * scale());
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Methods for Reading and Writing the Previous Game                                                                   *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // Writes the current grid to the previous.txt file
    public void writeGrid() throws IOException {
        PrintWriter writer = new PrintWriter("previous.txt");
        // First line is an int for the number of rows
        writer.println(this.getRows());
        // Second line is an int for the number of columns
        writer.println(this.getCols());
        // Iterates through the grid
        for (int r = 0; r < this.getRows(); r++) {
            for (int c = 0; c < this.getCols(); c++) {
                // Prints the toString() representation of each box in the grid
                writer.print(this.grid[r][c].toString());
            }
            // Prints to a new line with each new row
            writer.println();
        }
        writer.close();
    }

    // Reads the grid from previous.txt into a 2D char array
    public static char[][] readGrid() throws IOException {
        try {
            BufferedReader r = new BufferedReader(new FileReader("previous.txt"));

            // First line of the file is the number of rows
            String rows = r.readLine();
            int row = Integer.parseInt(rows);
            // Second line of the file is the number of columns
            String cols = r.readLine();
            int col = Integer.parseInt(cols);

            // Creates an array of the size outlined above
            char[][] arr = new char[row][col];

            // Fills in the array row by row with a char from each string
            boolean done = false;
            int currRow = 0;
            while(!done) {
                String str = r.readLine();
                // Stops looping when the read line is null
                if (str == null)
                    done = true;
                else {
                    for (int i = 0; i < str.length(); i++)
                        arr[currRow][i] = str.charAt(i);
                    currRow ++;
                }
            }
            r.close();
            return arr;
        } catch (NoSuchFileException e) {
            System.out.println("Caught No Such File Exception: " + e.getMessage());
        }
        // Returns an empty char array if the reading fails
        return new char[0][0];
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * 		Methods for Reading and Writing Highscores                                                                   *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    // Returns the game status formatted to highScore output
    private String scoreToString() {
        return "" + this.name + "," + this.time;
    }

    // Returns a list of highscores formated as: name,score
    public static List<String> highScoresToStrings(Difficulty d) throws IOException {
        List<String> l = new LinkedList<String>();
        // Reads from the file corresponding to the difficulty
        BufferedReader r = new BufferedReader(new FileReader("" + d.toString().toLowerCase() + ".txt"));
        try {
            boolean done = false;
            while (!done) {
                String line = r.readLine();
                // Stops looping when the read line is null
                if (line == null)
                    done = true;
                else
                    l.add(line);
            }
        }
        catch (IOException e) {
            System.out.println("Caught an IO Exception: " + e.getMessage());
        } finally {
            r.close();
        }
        return l;
    }

    // Returns a list of highscores with just the scores from a string list
    public static List<Integer> highScoresToInts(List<String> strings) {
        List<Integer> ints = new LinkedList<Integer>();
        try {
            for (String s : strings) {
                int i = s.indexOf(",");
                if (i == -1)
                    throw new IOException();
                // Finds the string after the comma and converts it to an integer
                Integer score = Integer.parseInt(s.substring(i + 1));
                ints.add(score);
            }
        }
        catch (IOException e) {
            System.out.println("Caught an IOException: " + e.getMessage());
        }
        return ints;
    }

    // Helper method that returns a list of highscores with just the scores from a difficulty
    private static List<Integer> highScoresToInts(Difficulty d) throws IOException {
        List<String> strings = highScoresToStrings(d);
        return highScoresToInts(strings);
    }

    // Returns a list of highscores with just the names from a string list
    public static List<String> highScoresToNames(List<String> strings) throws IOException {
        List<String> names = new LinkedList<String>();
        try {
            for (String s : strings) {
                int i = s.indexOf(",");
                // Throw an input output exception if there is no comma in the line
                if (i == -1)
                    throw new IOException();
                // Add the portion of the string before the comma to the list
                String name = s.substring(0, i);
                names.add(name);
            }
        }
        catch (IOException e) {
            System.out.println("Caught an IO Exception: " + e.getMessage());
        }
        return names;
    }

    // Returns if a user's score is a high score
    public boolean isHighScore() throws IOException {
        // Irrelevant if the user has not yet won
        if (!this.won())
            return false;
        List<Integer> scores = Grid.highScoresToInts(this.difficulty);
        // As five high scores are kept, the user's score must be a highscore if there aren't five scores yet
        if (scores.size() < 5)
            return true;
        // Otherwise, check if the user's time is above the time stored as the fifth value in the list
        return this.time < scores.get(4);
    }

    // Helper method which returns a list of highscores updated with that of the user
    private List<String> newHighScoresToString() throws IOException {
        List<String> strings = Grid.highScoresToStrings(this.difficulty);
        List<Integer> ints = Grid.highScoresToInts(strings);
        // Returns just the users name and score if the current highscore list is null or empty
        if (strings == null || strings.isEmpty()) {
            List<String> l = new LinkedList<String>();
            l.add(this.scoreToString());
            return l;
        }
        // Otherwise checks at what index the user's score is greater than the current score
        int index = 0;
        for (int i : ints) {
            if (this.time >= i)
                index++;
        }
        // If the index is within the list size, then add the user's score at that index, and remove the last item in
        // the list if the list is now six items long.
        if (index < strings.size()) {
            strings.add(index, "" + this.scoreToString());
            if (strings.size() == 6)
                strings.remove(5);
        }
        // Otherwise add the string to the end of the list
        else if (index < 5)
            strings.add(this.scoreToString());
        return strings;
    }

    // Writes the highscores to the file corresponding to the current difficulty
    public void writeHighScores() throws IOException {
        // No need to overwrite the file if the user's score is not a highscore
        if (!this.isHighScore())
            return;
        // Otherwise get the updated list of highscores
        List<String> strings = newHighScoresToString();
        // Write the list to the file
        PrintWriter writer = new PrintWriter(this.difficultyToString().toLowerCase() + ".txt");
        for (String s : strings)
            writer.println(s);
        writer.close();
    }
}
