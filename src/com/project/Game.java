package com.project;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class Game implements Runnable {

    public void run() {

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         * 		General Formatting and GUI Setup                                                                             *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        // Top-level frame in which game components live
        final JFrame frame = new JFrame("Minesweeper");
        frame.setLocation(300, 300);

        // Style settings to be used throughout
        // Adding padding to elements
        EmptyBorder empty = new EmptyBorder(0, 0, 0, 0);
        EmptyBorder paddingWide = new EmptyBorder(6, 18, 6, 18);
        EmptyBorder padding = new EmptyBorder(18, 18, 18, 18);
        // Setting default font and font size
        Font normalFont = new Font("Sans Serif", Font.PLAIN, 18);

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         * 		Formatting for Parts of Labels & Buttons at the Bottom                                                       *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        // Wrapper JPanel to contain status panel & information panel
        final JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        // Status panel to contain game status, mines remaining, and time duration
        final JPanel status_panel = new JPanel();

        // Displays game status
        final JLabel status = new JLabel("Game Not Started");
        // General styling
        status.setBorder(paddingWide);
        status.setFont(normalFont);
        status_panel.add(status);

        // Displays number of mines - number of marked boxes
        final JLabel minesRemaining = new JLabel("Mines left: 10");
        minesRemaining.setBorder(paddingWide);
        minesRemaining.setFont(normalFont);
        status_panel.add(minesRemaining);

        // Displays time since the game has been started (generated)
        final JLabel timeStatus = new JLabel("Time: 0");
        timeStatus.setBorder(paddingWide);
        timeStatus.setFont(normalFont);
        status_panel.add(timeStatus);

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         * 		Formatting Grid                                                                                              *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        // Wrapper panel for the grid of the game
        final JPanel gridPanel = new JPanel();
        // Gives a margin to the grid itself
        gridPanel.setBorder(paddingWide);

        final Grid grid = new Grid(Grid.Difficulty.BEGINNER, status, minesRemaining, timeStatus);
        grid.setBorder(empty);

        // Added to frame to stick to center
        gridPanel.add(grid);
        frame.add(gridPanel, BorderLayout.CENTER);

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         * 		Formatting for Buttons at the Top to Set Difficulty & New Games                                              *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        // Wrapper for top buttons
        final JPanel control_panel = new JPanel();
        control_panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        // Added to frame to stick to top border
        frame.add(control_panel, BorderLayout.NORTH);

        // Reset button resets the grid upon being pressed, generating a new game
        final JButton reset = new JButton("New Game");
        reset.setFont(new Font("Sans Serif", Font.PLAIN, 16));
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.reset();
            }
        });

        /*
         * Create buttons
         */

        // Creates a new game with difficulty beginner
        final JButton beginner = new JButton("Beginner");
        beginner.setFont(new Font("Sans Serif", Font.PLAIN, 16));

        // Creates a new game with difficulty intermediate
        final JButton intermediate = new JButton("Intermediate");
        intermediate.setFont(new Font("Sans Serif", Font.PLAIN, 16));

        // Creates a new game with difficulty expert
        final JButton expert = new JButton("Expert");
        expert.setFont(new Font("Sans Serif", Font.PLAIN, 16));

        // Creates a new game with difficulty custom, determined by user input
        final JButton custom = new JButton("Custom");
        custom.setFont(new Font("Sans Serif", Font.PLAIN, 16));

        // Darkens beginner button to signify it is selected by default
        toggleDifficultyButtons(beginner, intermediate, expert, custom);

        /*
         * Add button functionality
         */

        beginner.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.reset(Grid.Difficulty.BEGINNER);
                frame.pack();
                // Darkens the beginner button to show that it is the current difficulty
                toggleDifficultyButtons(beginner, intermediate, expert, custom);
            }
        });

        intermediate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.reset(Grid.Difficulty.INTERMEDIATE);
                frame.pack();
                // Darkens the intermediate button to show that it is the current difficulty
                toggleDifficultyButtons(intermediate, beginner, expert, custom);
            }
        });

        expert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grid.reset(Grid.Difficulty.EXPERT);
                frame.pack();
                // Darkens the expert button to show that it is the current difficulty
                toggleDifficultyButtons(expert, beginner, intermediate, custom);
            }
        });

        custom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create and display a plane asking for number of rows, columns, and mines for the new board
                JLabel r1 = new JLabel("Number of rows:  ");
                r1.setFont(normalFont);
                JLabel c1 = new JLabel("Number of columns:  ");
                c1.setFont(normalFont);
                JLabel m1 = new JLabel("Number of mines:  ");
                m1.setFont(normalFont);

                JTextField r2 = new JTextField(2);
                JTextField c2 = new JTextField(2);
                JTextField m2 = new JTextField(2);

                JPanel rs = new JPanel();
                rs.add(r1);
                rs.add(r2);

                JPanel cs = new JPanel();
                cs.add(c1);
                cs.add(c2);

                JPanel ms = new JPanel();
                ms.add(m1);
                ms.add(m2);

                JPanel customDif = new JPanel();
                customDif.setLayout(new BoxLayout(customDif, BoxLayout.Y_AXIS));

                customDif.add(rs);
                customDif.add(cs);
                customDif.add(ms);

                // Handles user input into the pane
                int result = JOptionPane.showConfirmDialog(
                        frame,
                        customDif,
                        "Custom Difficulty",
                        JOptionPane.OK_CANCEL_OPTION
                );
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        int row = Integer.parseInt(r2.getText());
                        int col = Integer.parseInt(c2.getText());
                        int mines = Integer.parseInt(m2.getText());
                        // Asserts values entered are correct
                        if (row < 4 || row > 48 || col < 4 || col > 48 || mines < 0 || mines > row * col - 9)
                            throw new IllegalArgumentException();
                        // Resets the grid with the new parameters
                        grid.reset(row, col, mines);
                        // Adjusts frame size to the new grid size
                        frame.pack();
                        // Darkens the custom button to show that it is the current difficulty
                        toggleDifficultyButtons(custom, beginner, intermediate, expert);
                    } catch (IllegalArgumentException error) {
                        // Displays a pane showing an input exception if input in the last pane was invalid
                        JOptionPane.showMessageDialog(frame,
                                "Input Exception.",
                                "Inane error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
                else
                    return;
            }
        });

        // Add buttons to the wrapper control_panel

        control_panel.add(reset);
        control_panel.add(beginner);
        control_panel.add(intermediate);
        control_panel.add(expert);
        control_panel.add(custom);

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         * 		Added Features for Bottom Section                                                                            *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        // Creates a highscores button
        final JButton highscores = new JButton("Highscores");
        highscores.setFont(new Font("Sans Serif", Font.PLAIN, 18));

        // Displays a new frame listing highscores for each difficulty
        highscores.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create frame
                final JFrame highScoresFrame = new JFrame("Highscores");
                // Open the frame at the user's mouse position
                highScoresFrame.setLocation(frame.getMousePosition());

                // Title added at the top of the pane
                final JLabel title = new JLabel("Minesweeper Highscores:");
                title.setBorder(paddingWide);
                title.setFont(new Font("Sans Serif", Font.BOLD, 20));
                highScoresFrame.add(title, BorderLayout.NORTH);

                // Wrapper panel for the scores corresponding to each difficulty
                final JPanel difficulties = new JPanel();

                // Create a label for each difficulty and add it to the difficulties panel
                // Set the text to the output text for highscores conducted in the Game class

                final JLabel beginnerScores = new JLabel(Game.highScoreOutput(Grid.Difficulty.BEGINNER));
                beginnerScores.setBorder(paddingWide);
                beginnerScores.setFont(normalFont);
                difficulties.add(beginnerScores);

                final JLabel intermediateScores = new JLabel(Game.highScoreOutput(Grid.Difficulty.INTERMEDIATE));
                intermediateScores.setBorder(paddingWide);
                intermediateScores.setFont(normalFont);
                difficulties.add(intermediateScores);

                final JLabel expertScores = new JLabel(Game.highScoreOutput(Grid.Difficulty.EXPERT));
                expertScores.setBorder(paddingWide);
                expertScores.setFont(normalFont);
                expertScores.setVerticalAlignment(JLabel.TOP);
                expertScores.setVerticalTextPosition(JLabel.TOP);
                difficulties.add(expertScores);

                final JLabel customScores = new JLabel(Game.highScoreOutput(Grid.Difficulty.CUSTOM));
                customScores.setBorder(paddingWide);
                customScores.setFont(normalFont);
                customScores.setVerticalAlignment(JLabel.TOP);
                customScores.setVerticalTextPosition(JLabel.TOP);
                difficulties.add(customScores);

                // Add the difficulties pane, set the frame size to the size of components, and make the frame visible.
                highScoresFrame.add(difficulties);

                // Create a button which closes the instructions panel
                JPanel playContainer = new JPanel();
                playContainer.setBorder(padding);
                JButton play = new JButton("Let's Beat the Records!");
                play.setFont(new Font("Sans Serif", Font.ITALIC, 22));
                play.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        highScoresFrame.dispose();
                    }
                });

                playContainer.add(play);
                // Fix the play button to the bottom of the frame
                highScoresFrame.add(playContainer, BorderLayout.SOUTH);

                highScoresFrame.pack();
                highScoresFrame.setVisible(true);
            }
        });

        // Create a button for displaying game instructions
        final JButton instructions = new JButton("Instructions");
        instructions.setFont(normalFont);

        // Displays a frame showing the instructions of the game
        instructions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFrame instr = new JFrame("Instructions");
                instr.setLocation(frame.getMousePosition());

                // Text for game instructions
                final JLabel text = new JLabel(
                        "<html>"
                                + "<b>M I N E S W E E P E R</b><br><br>"
                                + "Built by Cameron Cabo for his CIS 120 final project. Enjoy!<br><br>"
                                + "<b>Overview:</b><br>"
                                + "The objective of the game is to reveal all boxes which do not contain mines. The<br>"
                                + "number within a box is equal to the number of mines it is touching. Complete this<br>"
                                + "as quickly as possible to get a highscore!<br><br>"
                                + "<b>Instructions:</b><br>"
                                + "<ol>"
                                + "<li>Left click anywhere on the board to start the game.</li>"
                                + "<li>From there, left click any unrevealed box to see what's below!</li>"
                                + "<li>Right click unrevealed boxes to toggle marked, unsure, and unmarked states.</li>"
                                + "<li>If you double left click a box already touching as many marked mines as<br> "
                                + "the box is touching all actual mines, then all adjacent unmarked boxes will<br>"
                                + "revealed...but make sure you marked the right mines!</li>"
                                + "<li>If the double clicked box is not touching the right number of mines, it<br>"
                                + "will flash a red cross</li>"
                                + "</ol><br><br>"
                                + "<b>Additional Settings:</b><br>"
                                + "<ul>"
                                + "<li>Choose your difficulty from the buttons on the top. A custom difficulty will<br>"
                                + "prompt you to enter the number of rows, columns, and mines of your choosing.</li>"
                                + "<li>On the bottom you will see the game status, how many mines are left, and the<br>"
                                + "duration of the game!</li>"
                                + "<li>Click the \"Highscores\" button to see highscores for each difficulty.</li>"
                                + "<li>If you win quickly enough, you will be prompted to enter your name for the<br>"
                                + "highscores board</li>"
                                + "<li>If you lose, you will be given the options to replay the game you just<br>"
                                + "played, to play a new game of the same difficulty, or to quit.</li>"
                                + "</ul><br>"
                                + "</html>"
                );

                // Formatting for the text above
                text.setBorder(padding);
                text.setFont(normalFont);

                // Create a button which closes the instructions panel
                JPanel playContainer = new JPanel();
                playContainer.setBorder(padding);
                JButton play = new JButton("Let's Play!");
                play.setFont(new Font("Sans Serif", Font.ITALIC, 20));
                play.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        instr.dispose();
                    }
                });

                instr.add(text);
                playContainer.add(play);
                // Fix the play button to the bottom of the frame
                instr.add(playContainer, BorderLayout.SOUTH);

                // Set the frame size to the collective size of its contents
                instr.pack();
                // Make the frame visible
                instr.setVisible(true);
            }
        });

        // Creates a wrapper panel to go on the bottom of the instructions and highscores buttons
        JPanel info = new JPanel();
        info.setBorder(new EmptyBorder(0, 0, 12, 0));
        info.add(instructions);
        info.add(highscores);

        // Add the status_panel and info panel to the wrapper panel at the bottom
        bottom.add(status_panel);
        bottom.add(info);
        // Fix the bottom panel to the button of the frame
        frame.add(bottom, BorderLayout.SOUTH);

        // Set the frame size to the collective size of its components
        frame.pack();
        // Terminate the program upon closing the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Make the frame visible
        frame.setVisible(true);
    }

    // Helper method to format output for the highscores for each difficulty
    private static String highScoreOutput(Grid.Difficulty d) {
        //Outputs difficulty with only the first letter capitalized
        String diff = d.toString().toLowerCase();
        diff = diff.substring(0, 1).toUpperCase() + diff.substring(1);
        // Constructs an ordered list for the high score usernames and times
        String output = "<html><b>" + diff + ":</b><ol>";
        try {
            // Gets lists of names of highscore holders and scores of highscore holders from the Grid class
            List<String> all = Grid.highScoresToStrings(d);
            List<String> names = Grid.highScoresToNames(all);
            List<Integer> scores = Grid.highScoresToInts(all);
            // Formats each name and corresponding score into a list item
            for (int i = 0; i < names.size(); i++)
                output += "<li>" + names.get(i) + ":  " + scores.get(i) + "</li>";
            // If there are not 5 high scores, fill the remainder of the list with dashes
            int index = names.size();
            while (index < 5) {
                index++;
                output += "<li>-</li>";
            }
        } catch (IOException e) {
            System.out.println("Caught an IOException: " + e.getMessage());
        }
        // CLose the html elements
        output += "</ol></html>";

        // Return the constructed ouput String
        return output;
    }

    // Helper method to show which difficulty is currently selected depending on which is passed in
    private static void toggleDifficultyButtons(JButton a, JButton b, JButton c, JButton d) {
        a.setBackground(Color.LIGHT_GRAY);
        // Default Java button fill formatting
        b.setBackground(null);
        c.setBackground(null);
        d.setBackground(null);
    }

    // Main method to make the game run:
    // Initializes the GUI elements specified in Game and runs it
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Game());
    }
}