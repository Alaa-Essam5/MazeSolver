import java.awt.Point;
import java.util.List;

public abstract class MazeSolver {
    protected Maze maze;
    protected boolean[][] visited;
    protected List<Point> path;
    protected int steps;
    protected int penaltySteps;
    protected String algorithmName;

    public MazeSolver(Maze maze) {
        this.maze = maze;
        this.visited = new boolean[maze.getRows()][maze.getCols()];
        this.path = new java.util.ArrayList<>();
        this.steps = 0;
        this.penaltySteps = 0;
    }

    public abstract long solve(boolean visualize);

    protected Point handleSpecialTile(int x, int y) {
        Point current = new Point(x, y);
        for (Point teleport : maze.getTeleportPositions()) {
            if (teleport.equals(current) && maze.getTeleportPositions().size() >= 2) {
                steps++; // Count the teleport as a step
                for (Point other : maze.getTeleportPositions()) {
                    if (!other.equals(current)) {
                        return other;
                    }
                }
            }
        }

        for (Point penalty : maze.getPenaltyPositions()) {
            if (penalty.equals(current)) {
                penaltySteps += 2;
                steps++; // Count entering penalty as a step
            }
        }

        return null;
    }

    public List<Point> getPath() {
        return path;
    }

    public boolean[][] getVisited() {
        return visited;
    }

    public int getSteps() {
        return steps + penaltySteps;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    protected void reset() {
        this.visited = new boolean[maze.getRows()][maze.getCols()];
        this.path = new java.util.ArrayList<>();
        this.steps = 0;
        this.penaltySteps = 0;
    }
}