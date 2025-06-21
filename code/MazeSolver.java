import java.awt.Point;
import java.util.List;

public abstract class MazeSolver {
    // Holds the maze to be solved
    protected Maze maze;

    // Tracks which cells have been visited
    protected boolean[][] visited;

    // Stores the path from start to end
    protected List<Point> path;

    // Number of steps taken in the solution
    protected int steps;

    // Extra steps added due to penalty tiles
    protected int penaltySteps;

    // The name of the algorithm (e.g., A*, Dijkstra)
    protected String algorithmName;

    // Constructor initializes maze and tracking variables
    public MazeSolver(Maze maze) {
        this.maze = maze;
        this.visited = new boolean[maze.getRows()][maze.getCols()];
        this.path = new java.util.ArrayList<>();
        this.steps = 0;
        this.penaltySteps = 0;
    }

    // Abstract method to be implemented by subclasses for solving the maze
    public abstract long solve(boolean visualize);

    // Handles special tiles: teleport and penalty
    protected Point handleSpecialTile(int x, int y) {
        Point current = new Point(x, y);

        // Handle teleport tiles: move to the other teleport
        for (Point teleport : maze.getTeleportPositions()) {
            if (teleport.equals(current) && maze.getTeleportPositions().size() >= 2) {
                steps++; // Count teleport as a step
                for (Point other : maze.getTeleportPositions()) {
                    if (!other.equals(current)) {
                        return other; // Return the destination teleport
                    }
                }
            }
        }

        // Handle penalty tiles: add 2 penalty steps
        for (Point penalty : maze.getPenaltyPositions()) {
            if (penalty.equals(current)) {
                penaltySteps += 2;
                steps++; // Count stepping into penalty
            }
        }

        return null; // No special tile found
    }

    // Getter for the solved path
    public List<Point> getPath() {
        return path;
    }

    // Getter for visited cells
    public boolean[][] getVisited() {
        return visited;
    }

    // Getter for total steps including penalties
    public int getSteps() {
        return steps + penaltySteps;
    }

    // Getter for algorithm name
    public String getAlgorithmName() {
        return algorithmName;
    }

    // Resets all tracking variables for a fresh solve
    protected void reset() {
        this.visited = new boolean[maze.getRows()][maze.getCols()];
        this.path = new java.util.ArrayList<>();
        this.steps = 0;
        this.penaltySteps = 0;
    }
}
