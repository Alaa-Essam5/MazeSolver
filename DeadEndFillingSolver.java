// This class implements the Dead-End Filling algorithm to solve a maze.
// It removes dead-ends before solving the maze using BFS.
public class DeadEndFillingSolver extends MazeSolver {

    // We reuse BFSSolver to find the final path after removing dead-ends
    private BFSSolver bfsSolver;

    // Constructor initializes the solver and sets the algorithm name
    public DeadEndFillingSolver(Maze maze) {
        super(maze);
        this.algorithmName = "DeadEnd";
        this.bfsSolver = new BFSSolver(maze);
    }

    // Main solve method: performs dead-end filling, then runs BFS
    @Override
    public long solve(boolean visualize) {
        // Reset internal state before solving
        reset();

        // Start timing the algorithm
        long startTime = System.currentTimeMillis();

        // Make a copy of the maze to work on it without modifying the original
        char[][] mazeCopy = new char[maze.getRows()][maze.getCols()];
        for (int i = 0; i < maze.getRows(); i++) {
            System.arraycopy(maze.getMaze()[i], 0, mazeCopy[i], 0, maze.getCols());
        }

        boolean changed;

        // Keep looping until no more dead ends can be filled
        do {
            changed = false;

            for (int y = 0; y < maze.getRows(); y++) {
                for (int x = 0; x < maze.getCols(); x++) {
                    // Check if this is a normal path or penalty tile
                    if (mazeCopy[y][x] == Maze.PATH || mazeCopy[y][x] == Maze.PENALTY) {
                        // Count how many open neighbors this cell has
                        int openNeighbors = countOpenNeighbors(mazeCopy, x, y);

                        // If it's a dead-end, fill it by turning it into a wall
                        if (isDeadEnd(openNeighbors, x, y)) {
                            mazeCopy[y][x] = Maze.WALL;
                            changed = true;

                            // Optional visualization of filling
                            if (visualize) {
                                updateVisitedForVisualization(mazeCopy);
                            }
                        }
                    }
                }
            }
        } while (changed); // Repeat until no changes

        // After dead-end filling, use BFS to find the shortest path
        long timeTaken = bfsSolver.solve(visualize);
        if (timeTaken != -1) {
            // Copy path and visited data from BFS
            this.path = bfsSolver.getPath();
            this.visited = bfsSolver.getVisited();
            this.penaltySteps = bfsSolver.penaltySteps;

            // Add the number of filled dead-end cells to the total step count
            int filledCells = countFilledCells(mazeCopy);
            steps = bfsSolver.getSteps() + filledCells;
        }

        // Return total time taken to solve the maze
        return timeTaken;
    }

    // Helper method: counts how many open (walkable) neighbors a cell has
    private int countOpenNeighbors(char[][] mazeCopy, int x, int y) {
        int openNeighbors = 0;
        // Up, Right, Down, Left directions
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            // Check if neighbor is within bounds and is walkable
            if (nx >= 0 && nx < maze.getCols() && ny >= 0 && ny < maze.getRows() &&
                    (mazeCopy[ny][nx] == Maze.PATH || mazeCopy[ny][nx] == Maze.PENALTY ||
                            mazeCopy[ny][nx] == Maze.START || mazeCopy[ny][nx] == Maze.END ||
                            mazeCopy[ny][nx] == Maze.TELEPORT)) {
                openNeighbors++;
            }
        }
        return openNeighbors;
    }

    // Helper method: returns true if a cell is a dead-end
    private boolean isDeadEnd(int openNeighbors, int x, int y) {
        // A dead-end has only one open neighbor and is not the start or end point
        return openNeighbors == 1 &&
                !(x == maze.getStartPos().x && y == maze.getStartPos().y) &&
                !(x == maze.getEndPos().x && y == maze.getEndPos().y);
    }

    // For visualization: mark newly added walls as visited
    private void updateVisitedForVisualization(char[][] mazeCopy) {
        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                if (mazeCopy[i][j] == Maze.WALL && maze.getMaze()[i][j] != Maze.WALL) {
                    visited[i][j] = true;
                }
            }
        }
    }

    // Count how many cells were filled (turned into walls) during dead-end filling
    private int countFilledCells(char[][] mazeCopy) {
        int filledCells = 0;
        for (int y = 0; y < maze.getRows(); y++) {
            for (int x = 0; x < maze.getCols(); x++) {
                if (mazeCopy[y][x] == Maze.WALL && maze.getMaze()[y][x] != Maze.WALL) {
                    filledCells++;
                }
            }
        }
        return filledCells;
    }
}
