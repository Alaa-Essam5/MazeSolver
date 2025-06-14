import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class MazeSolverVisualizer {
    // Maze object to store the maze structure
    private Maze maze = new Maze();
    private MazePanel mazePanel; // Panel to visualize the maze
    private JTextArea infoArea; // Area to display information and messages
    private String currentAlgorithm = "DFS"; // Default algorithm
    private long visualizationDelay = 50; // Delay in milliseconds between visual steps
    private boolean isPaused = false; // Flag to pause/resume visualization
    private boolean isRunning = false; // Flag to prevent multiple runs at once

    private JFrame frame; // Main window
    private JButton dfsButton, bfsButton, aStarButton, dijkstraButton; // Algorithm buttons
    private JButton leftHandButton, rightHandButton, deadEndButton, compareButton, loadButton; // Other control buttons
    private JButton pauseButton, stepButton; // Pause and Step controls

    public MazeSolverVisualizer() {
        initializeGUI(); // Set up GUI when object is created
    }

    // Initialize all GUI components
    private void initializeGUI() {
        frame = new JFrame("Advanced Maze Solver Visualizer"); // Window title
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit app when window closes
        frame.setLayout(new BorderLayout()); // Use border layout

        mazePanel = new MazePanel(); // Create maze drawing panel
        frame.add(mazePanel, BorderLayout.CENTER); // Add maze panel to center of window

        // Create control panel with grid layout: 3 rows (for 3 sets of buttons)
        JPanel controlPanel = new JPanel(new GridLayout(3, 1));
        JPanel buttonPanel1 = new JPanel();
        JPanel buttonPanel2 = new JPanel();
        JPanel buttonPanel3 = new JPanel();

        createButtons(); // Set up all buttons and their actions

        // Add algorithm buttons and load button to first row
        buttonPanel1.add(loadButton);
        buttonPanel1.add(dfsButton);
        buttonPanel1.add(bfsButton);
        buttonPanel1.add(aStarButton);
        buttonPanel1.add(dijkstraButton);

        // Add other solving strategies to second row
        buttonPanel2.add(leftHandButton);
        buttonPanel2.add(rightHandButton);
        buttonPanel2.add(deadEndButton);
        buttonPanel2.add(compareButton);

        // Add pause and step buttons to third row
        buttonPanel3.add(pauseButton);
        buttonPanel3.add(stepButton);

        // Add button panels to the main control panel
        controlPanel.add(buttonPanel1);
        controlPanel.add(buttonPanel2);
        controlPanel.add(buttonPanel3);

        // Info text area to display results and messages
        infoArea = new JTextArea(5, 40);
        infoArea.setEditable(false);
        JScrollPane infoScrollPane = new JScrollPane(infoArea); // Add scroll in case of long output

        // Bottom panel holds control buttons and info area
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(infoScrollPane, BorderLayout.CENTER);

        frame.add(bottomPanel, BorderLayout.SOUTH); // Add the bottom panel to the window

        frame.pack(); // Resize window to fit components
        frame.setLocationRelativeTo(null); // Center window on screen
        frame.setVisible(true); // Make the window visible
    }

    // Create all control buttons and assign actions
    private void createButtons() {
        loadButton = new JButton("Load Maze");
        loadButton.addActionListener(e -> loadMazeFile());

        dfsButton = new JButton("Run DFS");
        dfsButton.addActionListener(e -> runAlgorithm("DFS"));

        bfsButton = new JButton("Run BFS");
        bfsButton.addActionListener(e -> runAlgorithm("BFS"));

        aStarButton = new JButton("Run A*");
        aStarButton.addActionListener(e -> runAlgorithm("A*"));

        dijkstraButton = new JButton("Run Dijkstra");
        dijkstraButton.addActionListener(e -> runAlgorithm("Dijkstra"));

        leftHandButton = new JButton("Left Hand");
        leftHandButton.addActionListener(e -> runAlgorithm("LeftHand"));

        rightHandButton = new JButton("Right Hand");
        rightHandButton.addActionListener(e -> runAlgorithm("RightHand"));

        deadEndButton = new JButton("Dead-End Fill");
        deadEndButton.addActionListener(e -> runAlgorithm("DeadEnd"));

        compareButton = new JButton("Compare All");
        compareButton.addActionListener(e -> compareAlgorithms());

        pauseButton = new JButton("Pause");
        pauseButton.setEnabled(false); // Initially disabled
        pauseButton.addActionListener(e -> togglePause());

        stepButton = new JButton("Step");
        stepButton.setEnabled(false); // Initially disabled
        stepButton.addActionListener(e -> step());
    }

    // Load maze from file using file chooser
    private void loadMazeFile() {
        if (isRunning) {
            infoArea.setText("Please wait for current operation to finish");
            return;
        }

        JFileChooser fileChooser = new JFileChooser(); // Dialog to choose file
        int returnValue = fileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                maze.loadMaze(selectedFile); // Parse and load the maze file
                mazePanel.setPreferredSize(new Dimension(maze.getCols() * mazePanel.cellSize,
                        maze.getRows() * mazePanel.cellSize)); // Resize panel to fit maze

                mazePanel.setMazeData(maze, new boolean[maze.getRows()][maze.getCols()],
                        new ArrayList<>(), "Maze"); // Reset maze panel
                mazePanel.repaint();

                frame.pack(); // Resize frame
                infoArea.setText("Maze loaded successfully.\nStart: " + maze.getStartPos() +
                        ", End: " + maze.getEndPos() +
                        "\nSize: " + maze.getRows() + "x" + maze.getCols());
            } catch (Exception e) {
                infoArea.setText("Error loading maze: " + e.getMessage());
            }
        }
    }

    // Run the selected algorithm in a new thread
    private void runAlgorithm(String algorithm) {
        if (isRunning) {
            infoArea.setText("Already running an algorithm. Please wait or pause.");
            return;
        }

        if (maze.getRows() == 0 || maze.getCols() == 0) {
            infoArea.setText("Please load a maze first!");
            return;
        }

        currentAlgorithm = algorithm;
        setButtonsEnabled(false); // Disable all buttons during run
        pauseButton.setEnabled(true); // Enable pause
        isRunning = true;

        // Start algorithm execution in a separate thread
        new Thread(() -> {
            try {
                MazeSolver solver = createSolver(algorithm); // Create appropriate solver
                if (solver == null) return;

                // Reset visualization
                mazePanel.setMazeData(maze, new boolean[maze.getRows()][maze.getCols()],
                        new ArrayList<>(), algorithm);
                mazePanel.repaint();

                long startTime = System.currentTimeMillis(); // Start timer
                boolean success = false;

                // Choose correct visualization method based on solver type
                if (solver instanceof DFSSolver) {
                    success = runDFSWithVisualization((DFSSolver) solver);
                } else if (solver instanceof BFSSolver) {
                    success = runBFSWithVisualization((BFSSolver) solver);
                } else if (solver instanceof AStarSolver) {
                    success = runAStarWithVisualization((AStarSolver) solver);
                } else if (solver instanceof DijkstraSolver) {
                    success = runDijkstraWithVisualization((DijkstraSolver) solver);
                } else if (solver instanceof WallFollowerSolver) {
                    success = runWallFollowerWithVisualization((WallFollowerSolver) solver);
                } else if (solver instanceof DeadEndFillingSolver) {
                    success = runDeadEndFillingWithVisualization((DeadEndFillingSolver) solver);
                } else {
                    success = solver.solve(true) != -1; // Fallback case
                }

                long timeTaken = System.currentTimeMillis() - startTime; // End timer

                boolean finalSuccess = success;

                // Update UI from the Swing thread
                SwingUtilities.invokeLater(() -> {
                    if (finalSuccess) {
                        infoArea.setText(algorithm + " Results:\n" +
                                "Path found in " + solver.getSteps() + " steps\n" +
                                "Time taken: " + timeTaken + "ms\n" +
                                "Path length: " + solver.getPath().size() + " cells\n" +
                                "Visited cells: " + countVisitedCells(solver.getVisited()));
                    } else {
                        infoArea.setText(algorithm + " found no path!\n" +
                                "Time taken: " + timeTaken + "ms\n" +
                                "Visited cells: " + countVisitedCells(solver.getVisited()));
                    }

                    setButtonsEnabled(true); // Re-enable controls
                    pauseButton.setEnabled(false);
                    stepButton.setEnabled(false);
                    isRunning = false;
                    isPaused = false;
                });
            } catch (Exception e) {
                // Handle any runtime exceptions and update UI
                SwingUtilities.invokeLater(() -> {
                    infoArea.setText("Error during " + algorithm + ": " + e.getMessage());
                    setButtonsEnabled(true);
                    pauseButton.setEnabled(false);
                    stepButton.setEnabled(false);
                    isRunning = false;
                    isPaused = false;
                });
            }
        }).start();
    }

    // Counts how many cells in the 'visited' array are true (i.e., visited during the solving process)
    private int countVisitedCells(boolean[][] visited) {
        int count = 0;
        for (boolean[] row : visited) {
            for (boolean cell : row) {
                if (cell) count++; // If the cell is visited, increment the counter
            }
        }
        return count;
    }

    // Creates an instance of the appropriate maze-solving algorithm based on user selection
    private MazeSolver createSolver(String algorithm) {
        switch (algorithm) {
            case "DFS":
                return new DFSSolver(maze);                 // Depth-First Search
            case "BFS":
                return new BFSSolver(maze);                 // Breadth-First Search
            case "A*":
                return new AStarSolver(maze);                // A* Search Algorithm
            case "Dijkstra":
                return new DijkstraSolver(maze);       // Dijkstra's Algorithm
            case "LeftHand":
                return new WallFollowerSolver(maze, true);  // Left-hand wall-following
            case "RightHand":
                return new WallFollowerSolver(maze, false); // Right-hand wall-following
            case "DeadEnd":
                return new DeadEndFillingSolver(maze);  // Dead-end filling method
            default:
                return null;                                   // If algorithm not recognized
        }
    }

    // Toggles between paused and running states; updates button text accordingly
    private void togglePause() {
        isPaused = !isPaused; // Flip the paused state
        pauseButton.setText(isPaused ? "Resume" : "Pause"); // Change button text
        stepButton.setEnabled(isPaused); // Enable the step button only when paused
    }

    // If paused, this method will resume execution by notifying the waiting thread
    private void step() {
        if (isPaused) {
            synchronized (this) {
                this.notify(); // Resume waiting thread
            }
        }
    }

    // Causes the algorithm to pause at each step if 'isPaused' is true
    private void waitIfPaused() {
        if (isPaused) {
            stepButton.setEnabled(true); // Allow stepping while paused
            synchronized (this) {
                try {
                    this.wait(); // Wait until notified (via step or resume)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle interruption
                }
            }
        }
    }

    // Runs DFS algorithm with visualization enabled
    private boolean runDFSWithVisualization(DFSSolver solver) {
        Stack<DFSSolver.PathNode> stack = new Stack<>();
        // Start with the maze's start position
        stack.push(new DFSSolver.PathNode(maze.getStartPos().x, maze.getStartPos().y, new ArrayList<>()));

        while (!stack.isEmpty()) {
            waitIfPaused(); // Check for pause
            if (!isRunning) return false; // Exit if solving is stopped

            DFSSolver.PathNode node = stack.pop(); // Get next node
            int x = node.x;
            int y = node.y;

            updateVisualization(solver, x, y); // Visual feedback on screen

            // If we reached the end point, construct the final path
            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                solver.path = new ArrayList<>(node.path);
                solver.path.add(new Point(x, y));
                solver.steps = solver.path.size() - 1 + solver.penaltySteps;
                return true;
            }

            // If the cell has not been visited yet
            if (!solver.visited[y][x]) {
                solver.visited[y][x] = true; // Mark cell as visited
                List<Point> newPath = new ArrayList<>(node.path);
                newPath.add(new Point(x, y)); // Add current point to path

                // Handle teleportation or special tiles
                Point teleportPos = solver.handleSpecialTile(x, y);
                if (teleportPos != null) {
                    stack.push(new DFSSolver.PathNode(teleportPos.x, teleportPos.y, newPath));
                    continue; // Skip normal neighbors if teleport used
                }

                // Explore all unvisited neighbors by pushing them onto the stack
                for (Point neighbor : maze.getNeighbors(x, y, solver.visited)) {
                    stack.push(new DFSSolver.PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }
        return false; // No path found
    }

    // Runs BFS algorithm with visualization enabled
    private boolean runBFSWithVisualization(BFSSolver solver) {
        Queue<BFSSolver.PathNode> queue = new LinkedList<>();
        // Start with the start position and mark it visited
        queue.add(new BFSSolver.PathNode(maze.getStartPos().x, maze.getStartPos().y, new ArrayList<>()));
        solver.visited[maze.getStartPos().y][maze.getStartPos().x] = true;

        while (!queue.isEmpty()) {
            waitIfPaused(); // Check for pause
            if (!isRunning) return false; // Exit if solving is stopped

            BFSSolver.PathNode node = queue.poll(); // Get next node
            int x = node.x;
            int y = node.y;

            updateVisualization(solver, x, y); // Show current cell

            // If reached the end, construct the path
            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                solver.path = new ArrayList<>(node.path);
                solver.path.add(new Point(x, y));
                solver.steps = solver.path.size() - 1 + solver.penaltySteps;
                return true;
            }

            List<Point> newPath = new ArrayList<>(node.path);
            newPath.add(new Point(x, y));

            // Handle teleportation or special tiles
            Point teleportPos = solver.handleSpecialTile(x, y);
            if (teleportPos != null && !solver.visited[teleportPos.y][teleportPos.x]) {
                solver.visited[teleportPos.y][teleportPos.x] = true;
                queue.add(new BFSSolver.PathNode(teleportPos.x, teleportPos.y, newPath));
                continue; // Skip regular neighbors
            }

            // Visit all unvisited neighbors
            for (Point neighbor : maze.getNeighbors(x, y, solver.visited)) {
                if (!solver.visited[neighbor.y][neighbor.x]) {
                    solver.visited[neighbor.y][neighbor.x] = true;
                    queue.add(new BFSSolver.PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }
        return false; // No path found
    }

    // Runs A* algorithm with visualization enabled
    private boolean runAStarWithVisualization(AStarSolver solver) {
        PriorityQueue<AStarSolver.Node> openSet = new PriorityQueue<>();
        // Start with the starting point and its heuristic
        openSet.add(new AStarSolver.Node(
                maze.getStartPos().x,
                maze.getStartPos().y,
                null,
                0,
                heuristic(maze.getStartPos().x, maze.getStartPos().y)
        ));

        while (!openSet.isEmpty()) {
            waitIfPaused(); // Pause check
            if (!isRunning) return false;

            AStarSolver.Node current = openSet.poll(); // Get node with lowest f = g + h
            int x = current.x;
            int y = current.y;

            solver.currentNode = current; // Track current node for visualization
            updateVisualization(solver, x, y); // Display step

            // If reached goal, reconstruct path
            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                solver.path = solver.reconstructPath(current);
                solver.steps = solver.path.size() - 1 + solver.penaltySteps;
                return true;
            }

            // Skip already visited cells
            if (solver.visited[y][x]) continue;
            solver.visited[y][x] = true;

            // Handle teleportation
            Point teleportPos = solver.handleSpecialTile(x, y);
            if (teleportPos != null) {
                double newG = current.g + 1;
                double newH = heuristic(teleportPos.x, teleportPos.y);
                openSet.add(new AStarSolver.Node(
                        teleportPos.x,
                        teleportPos.y,
                        current,
                        newG,
                        newH
                ));
                continue; // Skip normal neighbors
            }

            // Add all neighbors with updated cost and heuristic to the priority queue
            for (Point neighbor : maze.getNeighbors(x, y, solver.visited)) {
                double newG = current.g + 1;
                double newH = heuristic(neighbor.x, neighbor.y);
                openSet.add(new AStarSolver.Node(
                        neighbor.x,
                        neighbor.y,
                        current,
                        newG,
                        newH
                ));
            }
        }
        return false; // No path found
    }

    // Runs Dijkstra's algorithm with visual feedback
    private boolean runDijkstraWithVisualization(DijkstraSolver solver) {
        // Initialize priority queue with the start node (x, y, distance = 0, no parent)
        PriorityQueue<DijkstraSolver.Node> queue = new PriorityQueue<>();
        queue.add(new DijkstraSolver.Node(maze.getStartPos().x, maze.getStartPos().y, 0, null));

        // Loop until there are no more nodes to explore
        while (!queue.isEmpty()) {
            waitIfPaused(); // Wait if visualization is paused
            if (!isRunning) return false; // Stop if the algorithm is canceled

            // Retrieve node with lowest cost (priority)
            DijkstraSolver.Node current = queue.poll();
            int x = current.x;
            int y = current.y;

            // Set current node for tracking and update visualization
            solver.currentNode = current;
            updateVisualization(solver, x, y);

            // Check if we have reached the end of the maze
            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                solver.path = solver.reconstructPath(current); // Build final path
                solver.steps = solver.path.size() - 1 + solver.penaltySteps; // Calculate steps
                return true; // Algorithm finished successfully
            }

            // Skip if already visited
            if (solver.visited[y][x]) continue;
            solver.visited[y][x] = true; // Mark as visited

            // Handle teleport tiles if any, jump to teleport destination
            Point teleportPos = solver.handleSpecialTile(x, y);
            if (teleportPos != null) {
                queue.add(new DijkstraSolver.Node(teleportPos.x, teleportPos.y, current.distance + 1, current));
                continue;
            }

            // Add all valid neighboring cells to the queue
            for (Point neighbor : maze.getNeighbors(x, y, solver.visited)) {
                queue.add(new DijkstraSolver.Node(neighbor.x, neighbor.y, current.distance + 1, current));
            }
        }
        return false; // No path found
    }

    // Runs the Wall Follower algorithm (left-hand or right-hand rule) with visualization
    private boolean runWallFollowerWithVisualization(WallFollowerSolver solver) {
        int x = maze.getStartPos().x;
        int y = maze.getStartPos().y;
        List<Point> path = new ArrayList<>(); // Store the path

        while (true) {
            waitIfPaused(); // Pause handling
            if (!isRunning) return false;

            updateVisualization(solver, x, y); // Visual feedback
            path.add(new Point(x, y)); // Add current position to path

            // Check if goal is reached
            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                solver.path = path;
                solver.steps = path.size() - 1 + solver.penaltySteps;
                return true;
            }

            // Determine the next position to move
            Point next = solver.findNextMove(x, y);
            if (next == null) break; // No valid moves (maze trapped or done)

            x = next.x;
            y = next.y;
        }
        return false; // Algorithm failed or got stuck
    }

    // Runs Dead-End Filling algorithm with visualization
    private boolean runDeadEndFillingWithVisualization(DeadEndFillingSolver solver) {
        solver.solve(false); // Execute the algorithm without visualization

        // Update the maze panel on the GUI thread with the visited path and solution
        SwingUtilities.invokeLater(() -> {
            mazePanel.setMazeData(maze, solver.getVisited(), solver.getPath(), currentAlgorithm);
            mazePanel.repaint(); // Redraw the maze panel
        });

        // Return true if a valid path is found
        return solver.getPath() != null && !solver.getPath().isEmpty();
    }

    // Handles the visual update of the maze during an algorithm run
    private void updateVisualization(MazeSolver solver, int x, int y) {
        SwingUtilities.invokeLater(() -> {
            List<Point> currentPath = new ArrayList<>();

            // Get current path depending on the solver type
            if (solver instanceof AStarSolver) {
                currentPath = ((AStarSolver) solver).getCurrentPath();
            } else if (solver instanceof DijkstraSolver) {
                currentPath = ((DijkstraSolver) solver).getCurrentPath();
            } else if (solver.getPath() != null) {
                currentPath = new ArrayList<>(solver.getPath());
            }

            // Add current position to the path if it's valid
            if (x != -1 && y != -1) {
                currentPath.add(new Point(x, y));
            }

            // Update and repaint the maze with the current path and visited nodes
            mazePanel.setMazeData(maze, solver.getVisited(), currentPath, currentAlgorithm);
            mazePanel.repaint();
        });

        try {
            Thread.sleep(visualizationDelay); // Wait for delay between steps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle interruption safely
        }
    }

    // Manhattan distance heuristic used by A* algorithm
    private double heuristic(int x, int y) {
        return Math.abs(x - maze.getEndPos().x) + Math.abs(y - maze.getEndPos().y);
    }

    // Runs all algorithms several times and compares their performance
    private void compareAlgorithms() {
        if (isRunning) {
            infoArea.setText("Already running an algorithm. Please wait.");
            return;
        }

        if (maze.getRows() == 0 || maze.getCols() == 0) {
            infoArea.setText("Please load a maze first!");
            return;
        }

        setButtonsEnabled(false); // Disable UI buttons
        isRunning = true;

        // Start new thread for comparison to avoid freezing the UI
        new Thread(() -> {
            String[] algorithms = {"DFS", "BFS", "A*", "Dijkstra", "LeftHand", "RightHand", "DeadEnd"};
            Map<String, Double> avgTimes = new HashMap<>();
            Map<String, Double> avgSteps = new HashMap<>();
            Map<String, Double> avgPathLengths = new HashMap<>();
            Map<String, Double> avgVisitedCells = new HashMap<>();

            int numRuns = 5; // Number of repetitions per algorithm

            for (String algo : algorithms) {
                long totalTime = 0;
                long totalSteps = 0;
                long totalPathLength = 0;
                long totalVisitedCells = 0;
                int successfulRuns = 0;

                for (int i = 0; i < numRuns; i++) {
                    MazeSolver solver = createSolver(algo); // Instantiate solver based on algorithm
                    long timeTaken = solver.solve(false); // Run solver (no visualization)

                    if (timeTaken != -1) { // Check if solver succeeded
                        totalTime += timeTaken;
                        totalSteps += solver.getSteps();
                        totalPathLength += solver.getPath().size();
                        totalVisitedCells += countVisitedCells(solver.getVisited());
                        successfulRuns++;
                    }
                }

                // Store averages or mark as -1 if all runs failed
                if (successfulRuns > 0) {
                    avgTimes.put(algo, (double) totalTime / successfulRuns);
                    avgSteps.put(algo, (double) totalSteps / successfulRuns);
                    avgPathLengths.put(algo, (double) totalPathLength / successfulRuns);
                    avgVisitedCells.put(algo, (double) totalVisitedCells / successfulRuns);
                } else {
                    avgTimes.put(algo, -1.0);
                    avgSteps.put(algo, -1.0);
                    avgPathLengths.put(algo, -1.0);
                    avgVisitedCells.put(algo, -1.0);
                }
            }

            // Display results and re-enable UI buttons
            SwingUtilities.invokeLater(() -> {
                showComparisonResults(algorithms, avgTimes, avgSteps, avgPathLengths, avgVisitedCells, numRuns);
                setButtonsEnabled(true);
                isRunning = false;
            });
        }).start(); // Start background thread
    }


    // This method displays the results of algorithm comparisons in a textual format
    private void showComparisonResults(String[] algorithms, Map<String, Double> avgTimes,
                                       Map<String, Double> avgSteps, Map<String, Double> avgPathLengths,
                                       Map<String, Double> avgVisitedCells, int numRuns) {
        // StringBuilder to construct the text output
        StringBuilder comparisonText = new StringBuilder();

        // Add header information showing number of runs used in averaging
        comparisonText.append(String.format("Algorithm Comparison (averaged over %d runs):\n", numRuns));
        comparisonText.append("Algorithm\tTime(ms)\tSteps\tPathLen\tVisited\n");
        comparisonText.append("------------------------------------------------\n");

        // Loop over all algorithms and append their performance metrics
        for (String algo : algorithms) {
            comparisonText.append(String.format("%-8s\t%8.1f\t%5.1f\t%5.1f\t%5.1f\n",
                    algo, avgTimes.get(algo), avgSteps.get(algo),
                    avgPathLengths.get(algo), avgVisitedCells.get(algo)));
        }

        // Display the textual data in the infoArea (a JTextArea)
        infoArea.setText(comparisonText.toString());

        // Also show the data visually in a bar chart format
        showComparisonChart(avgTimes, avgSteps, avgPathLengths, avgVisitedCells);
    }

    // This method displays four different charts to compare algorithm performance
    private void showComparisonChart(Map<String, Double> avgTimes, Map<String, Double> avgSteps,
                                     Map<String, Double> avgPathLengths, Map<String, Double> avgVisitedCells) {
        // Create a new JFrame to hold the charts
        JFrame chartFrame = new JFrame("Algorithm Comparison");

        // Set dimensions and layout of the frame: 2 rows, 2 columns of charts
        chartFrame.setSize(1200, 600);
        chartFrame.setLayout(new GridLayout(2, 2));

        // Create four chart panels for different metrics
        JPanel timePanel = createChartPanel("Time (ms)", avgTimes, Color.BLUE);
        JPanel stepsPanel = createChartPanel("Steps", avgSteps, Color.GREEN);
        JPanel pathPanel = createChartPanel("Path Length", avgPathLengths, Color.RED);
        JPanel visitedPanel = createChartPanel("Visited Cells", avgVisitedCells, Color.ORANGE);

        // Add the panels to the frame
        chartFrame.add(timePanel);
        chartFrame.add(stepsPanel);
        chartFrame.add(pathPanel);
        chartFrame.add(visitedPanel);

        // Center the chart frame on screen and make it visible
        chartFrame.setLocationRelativeTo(frame);
        chartFrame.setVisible(true);
    }

    // This method creates a JPanel with a custom chart for a single metric
    private JPanel createChartPanel(String title, Map<String, Double> values, Color color) {
        // Create an anonymous JPanel with overridden paintComponent to draw the bar chart
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g, this, title, values, color);
            }
        };

        // Set a border with the chart title
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    // This method draws a single bar chart inside the provided JPanel
    private void drawBarChart(Graphics g, JPanel panel, String title, Map<String, Double> values, Color baseColor) {
        // Get width and height of the panel
        int width = panel.getWidth();
        int height = panel.getHeight();

        // Define padding and spacing for layout
        int padding = 60;
        int barWidth = 40;
        int spacing = 20;

        // Find the maximum value in the data to normalize bar heights
        double maxValue = values.values().stream()
                .filter(v -> v >= 0)
                .max(Double::compare)
                .orElse(1.0) * 1.2; // Add 20% padding to top

        // Draw x and y axes
        g.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
        g.drawLine(padding, height - padding, padding, padding); // Y-axis

        // Draw title above chart
        g.drawString(title, width / 2 - 30, padding / 2);

        // Algorithms in order to display
        int colorIndex = 0;
        String[] algorithms = {"DFS", "BFS", "A*", "Dijkstra", "LeftHand", "RightHand", "DeadEnd"};

        int xPos = padding + spacing;

        // Loop through each algorithm and draw its bar
        for (String algo : algorithms) {
            double value = values.get(algo);
            if (value < 0) continue; // Skip if failed to run

            // Change bar color slightly for each algorithm
            Color barColor = new Color(
                    Math.min(255, baseColor.getRed() + colorIndex * 30),
                    Math.min(255, baseColor.getGreen() + colorIndex * 30),
                    Math.min(255, baseColor.getBlue() + colorIndex * 30)
            );

            // Calculate height of the bar
            int barHeight = (int) ((value / maxValue) * (height - 2 * padding));

            // Draw the bar
            g.setColor(barColor);
            g.fillRect(xPos, height - padding - barHeight, barWidth, barHeight);

            // Draw value label and algorithm label under each bar
            g.setColor(Color.BLACK);
            g.drawString(String.format("%.1f", value), xPos, height - padding - barHeight - 5);
            g.drawString(algo, xPos, height - padding + 15);

            xPos += barWidth + spacing;
            colorIndex++;
        }

        // Draw labels on the y-axis (5 intervals)
        for (int i = 0; i <= 5; i++) {
            int y = height - padding - (i * (height - 2 * padding) / 5);
            g.drawString(String.format("%.0f", maxValue * i / 5), padding - 30, y + 5);
            g.drawLine(padding - 5, y, padding, y); // tick mark
        }
    }

    // This method enables or disables all control buttons based on the state
    private void setButtonsEnabled(boolean enabled) {
        dfsButton.setEnabled(enabled);
        bfsButton.setEnabled(enabled);
        aStarButton.setEnabled(enabled);
        dijkstraButton.setEnabled(enabled);
        leftHandButton.setEnabled(enabled);
        rightHandButton.setEnabled(enabled);
        deadEndButton.setEnabled(enabled);
        compareButton.setEnabled(enabled);
        loadButton.setEnabled(enabled);
    }

    // Main method to launch the Maze Solver Visualizer GUI
    public static void main(String[] args) {
        // Run GUI creation on the Swing event dispatch thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the GUI look and feel to match the system's native style
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace(); // Log any errors in setting the look and feel
            }

            // Instantiate the visualizer window
            new MazeSolverVisualizer();
        });
    }
}



/*
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.PriorityQueue;

public class MazeSolverVisualizer {
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;
    private static final Color RED = Color.RED;
    private static final Color GREEN = Color.GREEN;
    private static final Color BLUE = Color.BLUE;
    private static final Color YELLOW = Color.YELLOW;
    private static final Color PURPLE = new Color(128, 0, 128);
    private static final Color GRAY = new Color(200, 200, 200);
    private static final Color TELEPORT_COLOR = new Color(0, 255, 255);
    private static final Color PENALTY_COLOR = new Color(255, 192, 203);
    private static final Color A_STAR_COLOR = new Color(0, 128, 128);
    private static final Color DIJKSTRA_COLOR = new Color(255, 165, 0);

    private static final char WALL = '#';
    private static final char PATH = ' ';
    private static final char START = 'S';
    private static final char END = 'E';
    private static final char TELEPORT = 'T';
    private static final char PENALTY = 'P';

    private char[][] maze;
    private int rows, cols;
    private Point startPos, endPos;
    private List<Point> teleportPositions = new ArrayList<>();
    private List<Point> penaltyPositions = new ArrayList<>();
    private boolean[][] visited;
    private List<Point> path = new ArrayList<>();
    private int steps = 0;
    private int penaltySteps = 0;
    private int cellSize = 30;
    private String currentAlgorithm = "DFS";
    private long visualizationDelay = 50;

    private JFrame frame;
    private MazePanel mazePanel;
    private JTextArea infoArea;
    private JButton dfsButton, bfsButton, aStarButton, dijkstraButton,
            leftHandButton, rightHandButton, deadEndButton,
            compareButton, loadButton;

    public MazeSolverVisualizer() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Advanced Maze Solver Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        mazePanel = new MazePanel();
        frame.add(mazePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        JPanel buttonPanel1 = new JPanel();
        JPanel buttonPanel2 = new JPanel();

        // Initialize buttons
        dfsButton = new JButton("Run DFS");
        bfsButton = new JButton("Run BFS");
        aStarButton = new JButton("Run A*");
        dijkstraButton = new JButton("Run Dijkstra");
        leftHandButton = new JButton("Left Hand");
        rightHandButton = new JButton("Right Hand");
        deadEndButton = new JButton("Dead-End Fill");
        compareButton = new JButton("Compare All");
        loadButton = new JButton("Load Maze");

        // Add action listeners
        dfsButton.addActionListener(e -> {
            currentAlgorithm = "DFS";
            new Thread(this::runAlgorithm).start();
        });

        bfsButton.addActionListener(e -> {
            currentAlgorithm = "BFS";
            new Thread(this::runAlgorithm).start();
        });

        aStarButton.addActionListener(e -> {
            currentAlgorithm = "A*";
            new Thread(this::runAlgorithm).start();
        });

        dijkstraButton.addActionListener(e -> {
            currentAlgorithm = "Dijkstra";
            new Thread(this::runAlgorithm).start();
        });

        leftHandButton.addActionListener(e -> {
            currentAlgorithm = "LeftHand";
            new Thread(this::runAlgorithm).start();
        });

        rightHandButton.addActionListener(e -> {
            currentAlgorithm = "RightHand";
            new Thread(this::runAlgorithm).start();
        });

        deadEndButton.addActionListener(e -> {
            currentAlgorithm = "DeadEnd";
            new Thread(this::runAlgorithm).start();
        });

        compareButton.addActionListener(e -> new Thread(this::compareAlgorithms).start());
        loadButton.addActionListener(e -> loadMazeFile());

        // Add buttons to panels
        buttonPanel1.add(loadButton);
        buttonPanel1.add(dfsButton);
        buttonPanel1.add(bfsButton);
        buttonPanel1.add(aStarButton);
        buttonPanel1.add(dijkstraButton);

        buttonPanel2.add(leftHandButton);
        buttonPanel2.add(rightHandButton);
        buttonPanel2.add(deadEndButton);
        buttonPanel2.add(compareButton);

        controlPanel.add(buttonPanel1);
        controlPanel.add(buttonPanel2);

        // Info panel
        infoArea = new JTextArea(5, 40);
        infoArea.setEditable(false);
        JScrollPane infoScrollPane = new JScrollPane(infoArea);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(infoScrollPane, BorderLayout.CENTER);

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void loadMazeFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                loadMaze(selectedFile);
                mazePanel.setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
                frame.pack();
                infoArea.setText("Maze loaded successfully.\nStart: " + startPos + ", End: " + endPos);
            } catch (IOException e) {
                infoArea.setText("Error loading maze: " + e.getMessage());
            }
        }
    }

    private void loadMaze(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            throw new IOException("Maze file is empty.");
        }

        rows = lines.size();
        cols = lines.get(0).length();

        for (int i = 0; i < rows; i++) {
            if (lines.get(i).length() != cols) {
                throw new IOException("All lines in the maze must have the same length.");
            }
        }

        maze = new char[rows][cols];
        teleportPositions.clear();
        penaltyPositions.clear();
        startPos = null;
        endPos = null;

        for (int i = 0; i < rows; i++) {
            String line = lines.get(i);
            for (int j = 0; j < cols; j++) {
                char ch = line.charAt(j);
                maze[i][j] = ch;

                if (ch == START) {
                    startPos = new Point(j, i);
                } else if (ch == END) {
                    endPos = new Point(j, i);
                } else if (ch == TELEPORT) {
                    teleportPositions.add(new Point(j, i));
                } else if (ch == PENALTY) {
                    penaltyPositions.add(new Point(j, i));
                }
            }
        }

        if (startPos == null || endPos == null) {
            throw new IOException("Maze must contain both a start (S) and an end (E) position.");
        }

        reset();
    }

    private void reset() {
        visited = new boolean[rows][cols];
        path.clear();
        steps = 0;
        penaltySteps = 0;
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows
                && maze[y][x] != WALL && !visited[y][x];
    }

    private List<Point> getNeighbors(int x, int y) {
        List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (isValidMove(newX, newY)) {
                neighbors.add(new Point(newX, newY));
            }
        }

        return neighbors;
    }

    private Point handleSpecialTile(int x, int y, String algorithm) {
        Point current = new Point(x, y);
        for (Point teleport : teleportPositions) {
            if (teleport.equals(current) && teleportPositions.size() >= 2) {
                steps++; // Count the teleport as a step
                for (Point other : teleportPositions) {
                    if (!other.equals(current)) {
                        return other;
                    }
                }
            }
        }

        for (Point penalty : penaltyPositions) {
            if (penalty.equals(current)) {
                penaltySteps += 2;
                steps++; // Count entering penalty as a step
            }
        }

        return null;
    }

    private long dfs(boolean visualize) {
        reset();
        long startTime = System.nanoTime();
        Stack<PathNode> stack = new Stack<>();
        stack.push(new PathNode(startPos.x, startPos.y, new ArrayList<>()));

        while (!stack.isEmpty()) {
            PathNode node = stack.pop();
            int x = node.x;
            int y = node.y;
            List<Point> currentPath = node.path;

            if (visualize) {
                path = new ArrayList<>(currentPath);
                mazePanel.repaint();
                try {
                    Thread.sleep(visualizationDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }

            if (x == endPos.x && y == endPos.y) {
                path = new ArrayList<>(currentPath);
                path.add(new Point(x, y));
                steps = path.size() - 1 + penaltySteps;
                return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            }

            if (!visited[y][x]) {
                visited[y][x] = true;
                List<Point> newPath = new ArrayList<>(currentPath);
                newPath.add(new Point(x, y));

                Point teleportPos = handleSpecialTile(x, y, "DFS");
                if (teleportPos != null) {
                    stack.push(new PathNode(teleportPos.x, teleportPos.y, newPath));
                    continue;
                }

                for (Point neighbor : getNeighbors(x, y)) {
                    stack.push(new PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }

        return -1;
    }

    private long bfs(boolean visualize) {
        reset();
        long startTime = System.nanoTime();
        Queue<PathNode> queue = new LinkedBlockingQueue<>();
        queue.add(new PathNode(startPos.x, startPos.y, new ArrayList<>()));
        visited[startPos.y][startPos.x] = true;

        while (!queue.isEmpty()) {
            PathNode node = queue.poll();
            int x = node.x;
            int y = node.y;
            List<Point> currentPath = node.path;

            if (visualize) {
                path = new ArrayList<>(currentPath);
                mazePanel.repaint();
                try {
                    Thread.sleep(visualizationDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }

            if (x == endPos.x && y == endPos.y) {
                path = new ArrayList<>(currentPath);
                path.add(new Point(x, y));
                steps = path.size() - 1 + penaltySteps;
                return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            }

            List<Point> newPath = new ArrayList<>(currentPath);
            newPath.add(new Point(x, y));

            Point teleportPos = handleSpecialTile(x, y, "BFS");
            if (teleportPos != null && !visited[teleportPos.y][teleportPos.x]) {
                visited[teleportPos.y][teleportPos.x] = true;
                queue.add(new PathNode(teleportPos.x, teleportPos.y, newPath));
                continue;
            }

            for (Point neighbor : getNeighbors(x, y)) {
                if (!visited[neighbor.y][neighbor.x]) {
                    visited[neighbor.y][neighbor.x] = true;
                    queue.add(new PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }

        return -1;
    }

    private int heuristic(int x, int y) {
        // Improved heuristic that accounts for teleports
        int minDistance = Math.abs(x - endPos.x) + Math.abs(y - endPos.y);

        // Consider teleport possibilities
        for (Point teleport : teleportPositions) {
            int teleportDist = Math.abs(x - teleport.x) + Math.abs(y - teleport.y);
            for (Point otherTeleport : teleportPositions) {
                if (!otherTeleport.equals(teleport)) {
                    int exitDist = Math.abs(otherTeleport.x - endPos.x) +
                            Math.abs(otherTeleport.y - endPos.y);
                    minDistance = Math.min(minDistance, teleportDist + exitDist);
                }
            }
        }

        return minDistance;
    }

    private long aStarSearch(boolean visualize) {
        reset();
        long startTime = System.nanoTime();

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingInt(AStarNode::getF));
        Map<Point, AStarNode> allNodes = new HashMap<>();

        AStarNode startNode = new AStarNode(startPos.x, startPos.y, null, 0, heuristic(startPos.x, startPos.y));
        openSet.add(startNode);
        allNodes.put(new Point(startPos.x, startPos.y), startNode);

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();

            if (visualize) {
                path = reconstructPath(current);
                mazePanel.repaint();
                try {
                    Thread.sleep(visualizationDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }

            if (current.x == endPos.x && current.y == endPos.y) {
                path = reconstructPath(current);
                steps = path.size() - 1 + penaltySteps;
                return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            }

            visited[current.y][current.x] = true;

            // Check for teleport
            Point teleportPos = handleSpecialTile(current.x, current.y, "A*");
            if (teleportPos != null) {
                int newG = current.g + 1;
                AStarNode teleportNode = allNodes.getOrDefault(new Point(teleportPos.x, teleportPos.y),
                        new AStarNode(teleportPos.x, teleportPos.y, current, newG, heuristic(teleportPos.x, teleportPos.y)));

                if (newG < teleportNode.g) {
                    teleportNode.g = newG;
                    teleportNode.parent = current;
                    openSet.add(teleportNode);
                    allNodes.put(new Point(teleportPos.x, teleportPos.y), teleportNode);
                }
                continue;
            }

            // Explore neighbors
            for (Point neighbor : getNeighbors(current.x, current.y)) {
                if (visited[neighbor.y][neighbor.x]) continue;

                int tentativeG = current.g + 1;
                AStarNode neighborNode = allNodes.getOrDefault(neighbor,
                        new AStarNode(neighbor.x, neighbor.y, null, Integer.MAX_VALUE, heuristic(neighbor.x, neighbor.y)));

                if (tentativeG < neighborNode.g) {
                    neighborNode.g = tentativeG;
                    neighborNode.parent = current;
                    openSet.add(neighborNode);
                    allNodes.put(neighbor, neighborNode);
                }
            }
        }

        return -1;
    }

    private long dijkstra(boolean visualize) {
        reset();
        long startTime = System.nanoTime();

        PriorityQueue<DijkstraNode> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(DijkstraNode::getDistance));
        Map<Point, DijkstraNode> allNodes = new HashMap<>();

        DijkstraNode startNode = new DijkstraNode(startPos.x, startPos.y, null, 0);
        priorityQueue.add(startNode);
        allNodes.put(new Point(startPos.x, startPos.y), startNode);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (maze[y][x] != WALL && !(x == startPos.x && y == startPos.y)) {
                    DijkstraNode node = new DijkstraNode(x, y, null, Integer.MAX_VALUE);
                    allNodes.put(new Point(x, y), node);
                }
            }
        }

        while (!priorityQueue.isEmpty()) {
            DijkstraNode current = priorityQueue.poll();

            if (visualize) {
                path = reconstructDijkstraPath(current);
                mazePanel.repaint();
                try {
                    Thread.sleep(visualizationDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }

            if (current.x == endPos.x && current.y == endPos.y) {
                path = reconstructDijkstraPath(current);
                steps = path.size() - 1 + penaltySteps;
                return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            }

            visited[current.y][current.x] = true;

            Point teleportPos = handleSpecialTile(current.x, current.y, "Dijkstra");
            if (teleportPos != null) {
                DijkstraNode teleportNode = allNodes.get(new Point(teleportPos.x, teleportPos.y));
                int newDistance = current.distance + 1;

                if (newDistance < teleportNode.distance) {
                    priorityQueue.remove(teleportNode);
                    teleportNode.distance = newDistance;
                    teleportNode.parent = current;
                    priorityQueue.add(teleportNode);
                }
                continue;
            }

            for (Point neighbor : getNeighbors(current.x, current.y)) {
                if (visited[neighbor.y][neighbor.x]) continue;

                DijkstraNode neighborNode = allNodes.get(neighbor);
                int newDistance = current.distance + 1;

                if (newDistance < neighborNode.distance) {
                    priorityQueue.remove(neighborNode);
                    neighborNode.distance = newDistance;
                    neighborNode.parent = current;
                    priorityQueue.add(neighborNode);
                }
            }
        }

        return -1;
    }

    private long rightHandSearch(boolean visualize) {
        reset();
        long startTime = System.nanoTime();

        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        int currentDir = 0;

        int x = startPos.x;
        int y = startPos.y;
        path.add(new Point(x, y));
        visited[y][x] = true;

        while (!(x == endPos.x && y == endPos.y)) {
            if (visualize) {
                mazePanel.repaint();
                try {
                    Thread.sleep(visualizationDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }

            // Improved teleport handling
            Point teleportPos = null;
            for (Point tp : teleportPositions) {
                if (tp.x == x && tp.y == y) {
                    for (Point otherTp : teleportPositions) {
                        if (!otherTp.equals(tp)) {
                            teleportPos = otherTp;
                            break;
                        }
                    }
                    break;
                }
            }

            if (teleportPos != null) {
                x = teleportPos.x;
                y = teleportPos.y;
                if (!visited[y][x]) {
                    path.add(new Point(x, y));
                    visited[y][x] = true;
                }
                continue;
            }

            // Try to turn right
            int rightDir = (currentDir + 1) % 4;
            int newX = x + directions[rightDir][0];
            int newY = y + directions[rightDir][1];

            if (isValidMove(newX, newY)) {
                currentDir = rightDir;
                x = newX;
                y = newY;
            } else {
                // Try to go straight
                newX = x + directions[currentDir][0];
                newY = y + directions[currentDir][1];

                if (isValidMove(newX, newY)) {
                    x = newX;
                    y = newY;
                } else {
                    // Try to turn left
                    int leftDir = (currentDir + 3) % 4;
                    newX = x + directions[leftDir][0];
                    newY = y + directions[leftDir][1];

                    if (isValidMove(newX, newY)) {
                        currentDir = leftDir;
                        x = newX;
                        y = newY;
                    } else {
                        // Turn around
                        currentDir = (currentDir + 2) % 4;
                    }
                }
            }

            path.add(new Point(x, y));
            visited[y][x] = true;
            steps++;

            if (steps > rows * cols * 10) {
                return -1;
            }
        }

        steps = path.size() - 1 + penaltySteps;
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private long leftHandSearch(boolean visualize) {
        reset();
        long startTime = System.nanoTime();

        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        int currentDir = 0;

        int x = startPos.x;
        int y = startPos.y;
        path.add(new Point(x, y));
        visited[y][x] = true;

        while (!(x == endPos.x && y == endPos.y)) {
            if (visualize) {
                mazePanel.repaint();
                try {
                    Thread.sleep(visualizationDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            }

            // Improved teleport handling
            Point teleportPos = null;
            for (Point tp : teleportPositions) {
                if (tp.x == x && tp.y == y) {
                    for (Point otherTp : teleportPositions) {
                        if (!otherTp.equals(tp)) {
                            teleportPos = otherTp;
                            break;
                        }
                    }
                    break;
                }
            }

            if (teleportPos != null) {
                x = teleportPos.x;
                y = teleportPos.y;
                if (!visited[y][x]) {
                    path.add(new Point(x, y));
                    visited[y][x] = true;
                }
                continue;
            }

            // Try to turn left
            int leftDir = (currentDir + 1) % 4;
            int newX = x + directions[leftDir][0];
            int newY = y + directions[leftDir][1];

            if (isValidMove(newX, newY)) {
                currentDir = leftDir;
                x = newX;
                y = newY;
            } else {
                // Try to go straight
                newX = x + directions[currentDir][0];
                newY = y + directions[currentDir][1];

                if (isValidMove(newX, newY)) {
                    x = newX;
                    y = newY;
                } else {
                    // Try to turn right
                    int rightDir = (currentDir + 3) % 4;
                    newX = x + directions[rightDir][0];
                    newY = y + directions[rightDir][1];

                    if (isValidMove(newX, newY)) {
                        currentDir = rightDir;
                        x = newX;
                        y = newY;
                    } else {
                        // Turn around
                        currentDir = (currentDir + 2) % 4;
                    }
                }
            }

            path.add(new Point(x, y));
            visited[y][x] = true;
            steps++;

            if (steps > rows * cols * 10) {
                return -1;
            }
        }

        steps = path.size() - 1 + penaltySteps;
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private long deadEndFilling(boolean visualize) {
        reset();
        long startTime = System.nanoTime();

        char[][] mazeCopy = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(maze[i], 0, mazeCopy[i], 0, cols);
        }

        boolean changed;
        do {
            changed = false;

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (mazeCopy[y][x] == PATH || mazeCopy[y][x] == PENALTY) {
                        int openNeighbors = 0;
                        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

                        for (int[] dir : directions) {
                            int nx = x + dir[0];
                            int ny = y + dir[1];

                            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows &&
                                    (mazeCopy[ny][nx] == PATH || mazeCopy[ny][nx] == PENALTY ||
                                            mazeCopy[ny][nx] == START || mazeCopy[ny][nx] == END ||
                                            mazeCopy[ny][nx] == TELEPORT)) {
                                openNeighbors++;
                            }
                        }

                        if (openNeighbors == 1 && !(x == startPos.x && y == startPos.y) &&
                                !(x == endPos.x && y == endPos.y)) {
                            mazeCopy[y][x] = WALL;
                            changed = true;

                            if (visualize) {
                                for (int i = 0; i < rows; i++) {
                                    for (int j = 0; j < cols; j++) {
                                        if (mazeCopy[i][j] == WALL && maze[i][j] != WALL) {
                                            visited[i][j] = true;
                                        }
                                    }
                                }
                                mazePanel.repaint();
                                try {
                                    Thread.sleep(visualizationDelay / 2);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return -1;
                                }
                            }
                        }
                    }
                }
            }
        } while (changed);

        long timeTaken = bfs(visualize);
        if (timeTaken != -1) {
            int filledCells = 0;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (mazeCopy[y][x] == WALL && maze[y][x] != WALL) {
                        filledCells++;
                    }
                }
            }
            steps += filledCells;
        }

        return timeTaken;
    }

    private List<Point> reconstructPath(AStarNode node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(0, new Point(node.x, node.y));
            node = node.parent;
        }
        return path;
    }

    private List<Point> reconstructDijkstraPath(DijkstraNode node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(0, new Point(node.x, node.y));
            node = node.parent;
        }
        return path;
    }

    private void runAlgorithm() {
        SwingUtilities.invokeLater(() -> {
            dfsButton.setEnabled(false);
            bfsButton.setEnabled(false);
            aStarButton.setEnabled(false);
            dijkstraButton.setEnabled(false);
            leftHandButton.setEnabled(false);
            rightHandButton.setEnabled(false);
            deadEndButton.setEnabled(false);
            compareButton.setEnabled(false);
            loadButton.setEnabled(false);
        });

        long timeTaken = -1;
        switch (currentAlgorithm) {
            case "DFS":
                timeTaken = dfs(true);
                break;
            case "BFS":
                timeTaken = bfs(true);
                break;
            case "A*":
                timeTaken = aStarSearch(true);
                break;
            case "Dijkstra":
                timeTaken = dijkstra(true);
                break;
            case "LeftHand":
                timeTaken = leftHandSearch(true);
                break;
            case "RightHand":
                timeTaken = rightHandSearch(true);
                break;
            case "DeadEnd":
                timeTaken = deadEndFilling(true);
                break;
        }

        final long finalTime = timeTaken;
        SwingUtilities.invokeLater(() -> {
            if (finalTime != -1) {
                infoArea.setText(currentAlgorithm + " found path in " + steps + " steps (Time: " + finalTime + "ms)\n");
                drawFinalPath();
            } else {
                infoArea.setText(currentAlgorithm + " found no path!\n");
            }

            dfsButton.setEnabled(true);
            bfsButton.setEnabled(true);
            aStarButton.setEnabled(true);
            dijkstraButton.setEnabled(true);
            leftHandButton.setEnabled(true);
            rightHandButton.setEnabled(true);
            deadEndButton.setEnabled(true);
            compareButton.setEnabled(true);
            loadButton.setEnabled(true);
        });
    }

    private void drawFinalPath() {
        for (int i = 0; i < path.size(); i++) {
            mazePanel.repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void compareAlgorithms() {
        SwingUtilities.invokeLater(() -> {
            dfsButton.setEnabled(false);
            bfsButton.setEnabled(false);
            aStarButton.setEnabled(false);
            dijkstraButton.setEnabled(false);
            leftHandButton.setEnabled(false);
            rightHandButton.setEnabled(false);
            deadEndButton.setEnabled(false);
            compareButton.setEnabled(false);
            loadButton.setEnabled(false);
        });

        int numRuns = 5;
        Map<String, List<Long>> times = new HashMap<>();
        Map<String, List<Integer>> steps = new HashMap<>();

        String[] algorithms = {"DFS", "BFS", "A*", "Dijkstra", "LeftHand", "RightHand", "DeadEnd"};

        for (String algo : algorithms) {
            times.put(algo, new ArrayList<>());
            steps.put(algo, new ArrayList<>());
        }

        for (int i = 0; i < numRuns; i++) {
            for (String algo : algorithms) {
                currentAlgorithm = algo;
                long timeTaken = -1;

                switch (algo) {
                    case "DFS":
                        timeTaken = dfs(false);
                        break;
                    case "BFS":
                        timeTaken = bfs(false);
                        break;
                    case "A*":
                        timeTaken = aStarSearch(false);
                        break;
                    case "Dijkstra":
                        timeTaken = dijkstra(false);
                        break;
                    case "LeftHand":
                        timeTaken = leftHandSearch(false);
                        break;
                    case "RightHand":
                        timeTaken = rightHandSearch(false);
                        break;
                    case "DeadEnd":
                        timeTaken = deadEndFilling(false);
                        break;
                }

                if (timeTaken != -1) {
                    times.get(algo).add(timeTaken);
                    steps.get(algo).add(this.steps);
                }
            }
        }

        // Calculate averages
        Map<String, Double> avgTimes = new HashMap<>();
        Map<String, Double> avgSteps = new HashMap<>();

        for (String algo : algorithms) {
            avgTimes.put(algo, times.get(algo).stream().mapToLong(Long::longValue).average().orElse(0));
            avgSteps.put(algo, steps.get(algo).stream().mapToInt(Integer::intValue).average().orElse(0));
        }

        // Show comparison results
        StringBuilder comparisonText = new StringBuilder();
        comparisonText.append(String.format("Algorithm Comparison (averaged over %d runs):\n", numRuns));

        for (String algo : algorithms) {
            comparisonText.append(String.format(
                    "%s - Time: %.2fms, Steps: %.2f\n",
                    algo, avgTimes.get(algo), avgSteps.get(algo)
            ));
        }

        SwingUtilities.invokeLater(() -> {
            infoArea.setText(comparisonText.toString());

            dfsButton.setEnabled(true);
            bfsButton.setEnabled(true);
            aStarButton.setEnabled(true);
            dijkstraButton.setEnabled(true);
            leftHandButton.setEnabled(true);
            rightHandButton.setEnabled(true);
            deadEndButton.setEnabled(true);
            compareButton.setEnabled(true);
            loadButton.setEnabled(true);
        });

        // Show comparison chart
        showComparisonChart(avgTimes, avgSteps);
    }

    private void showComparisonChart(Map<String, Double> avgTimes, Map<String, Double> avgSteps) {
        JFrame chartFrame = new JFrame("Algorithm Comparison");
        chartFrame.setSize(1000, 500);
        chartFrame.setLayout(new GridLayout(1, 2));

        JPanel timePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g, this, "Time (ms)", avgTimes);
            }
        };

        timePanel.setBorder(BorderFactory.createTitledBorder("Average Time Taken"));

        JPanel stepsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g, this, "Steps", avgSteps);
            }
        };

        stepsPanel.setBorder(BorderFactory.createTitledBorder("Average Steps Taken"));

        chartFrame.add(timePanel);
        chartFrame.add(stepsPanel);
        chartFrame.setLocationRelativeTo(frame);
        chartFrame.setVisible(true);
    }

    private void drawBarChart(Graphics g, JPanel panel, String title, Map<String, Double> values) {
        int width = panel.getWidth();
        int height = panel.getHeight();
        int padding = 60;
        int barWidth = 40;
        int spacing = 20;

        double maxValue = values.values().stream().max(Double::compare).orElse(1.0) * 1.2;

        // Draw axes
        g.drawLine(padding, height - padding, width - padding, height - padding);
        g.drawLine(padding, height - padding, padding, padding);

        // Draw title
        g.drawString(title, width / 2 - 30, padding / 2);

        Color[] colors = {Color.BLUE, Color.ORANGE, Color.GREEN, Color.RED, Color.MAGENTA, Color.CYAN, Color.PINK};
        int colorIndex = 0;

        int xPos = padding + spacing;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            String algo = entry.getKey();
            double value = entry.getValue();

            int barHeight = (int)((value / maxValue) * (height - 2 * padding));

            g.setColor(colors[colorIndex % colors.length]);
            g.fillRect(xPos, height - padding - barHeight, barWidth, barHeight);

            g.setColor(Color.BLACK);
            g.drawString(String.format("%.1f", value), xPos, height - padding - barHeight - 5);
            g.drawString(algo, xPos, height - padding + 15);

            xPos += barWidth + spacing;
            colorIndex++;
        }

        // Draw y-axis labels
        for (int i = 0; i <= 5; i++) {
            int y = height - padding - (i * (height - 2 * padding) / 5);
            g.drawString(String.format("%.0f", maxValue * i / 5), padding - 30, y + 5);
            g.drawLine(padding - 5, y, padding, y);
        }
    }

    private static class AStarNode {
        int x, y;
        AStarNode parent;
        int g;
        int h;

        AStarNode(int x, int y, AStarNode parent, int g, int h) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        int getF() {
            return g + h;
        }
    }

    private static class DijkstraNode {
        int x, y;
        DijkstraNode parent;
        int distance;

        DijkstraNode(int x, int y, DijkstraNode parent, int distance) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.distance = distance;
        }

        int getDistance() {
            return distance;
        }
    }

    private static class PathNode {
        int x, y;
        List<Point> path;

        PathNode(int x, int y, List<Point> path) {
            this.x = x;
            this.y = y;
            this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
        }
    }

    private class MazePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (maze == null) return;

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    Color color = WHITE;
                    char cell = maze[y][x];

                    if (cell == WALL) {
                        color = BLACK;
                    } else if (cell == START) {
                        color = GREEN;
                    } else if (cell == END) {
                        color = RED;
                    } else if (cell == TELEPORT) {
                        color = TELEPORT_COLOR;
                    } else if (cell == PENALTY) {
                        color = PENALTY_COLOR;
                    }

                    g.setColor(color);
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                    if (visited[y][x] && cell != START && cell != END && cell != TELEPORT && cell != PENALTY) {
                        g.setColor(GRAY);
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }

                    if (path.contains(new Point(x, y)) && cell != START && cell != END) {
                        Color pathColor = BLUE;
                        if (currentAlgorithm.equals("A*")) {
                            pathColor = A_STAR_COLOR;
                        } else if (currentAlgorithm.equals("Dijkstra")) {
                            pathColor = DIJKSTRA_COLOR;
                        }
                        g.setColor(pathColor);
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }

                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }

            // Draw labels
            if (startPos != null) {
                g.setColor(Color.BLACK);
                g.drawString("S", startPos.x * cellSize + cellSize/2 - 3, startPos.y * cellSize + cellSize/2 + 5);
            }
            if (endPos != null) {
                g.setColor(Color.BLACK);
                g.drawString("E", endPos.x * cellSize + cellSize/2 - 3, endPos.y * cellSize + cellSize/2 + 5);
            }

            for (Point tp : teleportPositions) {
                g.setColor(Color.BLACK);
                g.drawString("T", tp.x * cellSize + cellSize/2 - 3, tp.y * cellSize + cellSize/2 + 5);
            }

            for (Point pp : penaltyPositions) {
                g.setColor(Color.BLACK);
                g.drawString("P", pp.x * cellSize + cellSize/2 - 3, pp.y * cellSize + cellSize/2 + 5);
            }

            if ((currentAlgorithm.equals("LeftHand") || currentAlgorithm.equals("RightHand"))) {
                Point current = path.isEmpty() ? startPos : path.get(path.size() - 1);
                g.setColor(YELLOW);
                g.fillOval(current.x * cellSize + cellSize/4, current.y * cellSize + cellSize/4,
                        cellSize/2, cellSize/2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MazeSolverVisualizer();
        });
    }
}

*/