import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class WallFollowerSolver extends MazeSolver {
    // Determines whether to use left-hand or right-hand wall following
    private boolean isLeftHand;

    // Stores the current facing direction (0: North, 1: East, 2: South, 3: West)
    private int currentDirection;

    // Stores the current path taken through the maze
    private List<Point> currentPath;

    // Flag to check if the solver got stuck (no way forward)
    private boolean isStuck;

    // Constructor initializes the maze, method (left or right hand), and path data
    public WallFollowerSolver(Maze maze, boolean isLeftHand) {
        super(maze);
        this.isLeftHand = isLeftHand;
        this.algorithmName = isLeftHand ? "LeftHand" : "RightHand";
        this.currentPath = new ArrayList<>();
        this.isStuck = false;
    }

    // Solves the maze using the wall-following strategy
    @Override
    public long solve(boolean visualize) {
        reset(); // Clear any previous data
        long startTime = System.currentTimeMillis();

        // Start facing North if left-hand, South if right-hand
        currentDirection = isLeftHand ? 0 : 2;
        int x = maze.getStartPos().x;
        int y = maze.getStartPos().y;

        // Initialize path and visited position
        path.add(new Point(x, y));
        currentPath.add(new Point(x, y));
        visited[y][x] = true;
        steps = 1;

        // Continue moving until we reach the end position
        while (!(x == maze.getEndPos().x && y == maze.getEndPos().y)) {
            Point next = findNextMove(x, y);

            // No valid move found: solver is stuck
            if (next == null) {
                isStuck = true;
                break;
            }

            // Move to the next position
            x = next.x;
            y = next.y;

            // Record the move
            path.add(new Point(x, y));
            currentPath.add(new Point(x, y));
            visited[y][x] = true;
            steps++;

            // Failsafe: if the number of steps exceeds maze size, assume infinite loop
            if (steps > maze.getRows() * maze.getCols()) {
                isStuck = true;
                break;
            }

        }

        // If stuck, report failure and return -1
        if (isStuck) {
            System.out.println("Cannot solve maze - no valid path forward without revisiting cells");
            return -1;
        }

        // Calculate total steps including penalty
        steps = path.size() - 1 + penaltySteps;
        return System.currentTimeMillis() - startTime;
    }

    // Determines the next move based on wall-following rule
    protected Point findNextMove(int x, int y) {
        // Directions: 0 = North, 1 = East, 2 = South, 3 = West
        int[][] directions = {
                {0, -1},  // North
                {1, 0},   // East
                {0, 1},   // South
                {-1, 0}   // West
        };

        // Direction to check first depending on hand used (left: -1, right: +1)
        int turn = isLeftHand ? -1 : 1;

        // Order of directions to try: hand side -> forward -> opposite hand -> backward
        int[] tryOrder = {turn, 0, -turn, 2};

        // Check each direction in order
        for (int i = 0; i < 4; i++) {
            int dirIndex = (currentDirection + tryOrder[i] + 4) % 4;
            int newX = x + directions[dirIndex][0];
            int newY = y + directions[dirIndex][1];

            // If the move is valid and unvisited, take it and update direction
            if (maze.isValidMove(newX, newY, visited) && !visited[newY][newX]) {
                currentDirection = dirIndex;
                return new Point(newX, newY);
            }
        }

        // No valid direction found
        return null;
    }

    // Returns the current explored path so far
    public List<Point> getCurrentPath() {
        return new ArrayList<>(currentPath);
    }

    // Resets the solver state for a new run
    @Override
    public void reset() {
        super.reset();
        currentPath.clear();
        currentDirection = isLeftHand ? 0 : 2;
        isStuck = false;
    }

    // Returns true if solver got stuck and couldn't solve the maze
    public boolean isStuck() {
        return isStuck;
    }
}
