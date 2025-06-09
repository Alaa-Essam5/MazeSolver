import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class WallFollowerSolver extends MazeSolver {
    private boolean isLeftHand;
    private int currentDirection;
    private List<Point> currentPath;

    public WallFollowerSolver(Maze maze, boolean isLeftHand) {
        super(maze);
        this.isLeftHand = isLeftHand;
        this.algorithmName = isLeftHand ? "LeftHand" : "RightHand";
        this.currentPath = new ArrayList<>();
    }

    @Override
    public long solve(boolean visualize) {
        reset();
        long startTime = System.currentTimeMillis();

        // Initialize direction based on hand rule
        currentDirection = isLeftHand ? 0 : 2; // 0:Left, 1:Down, 2:Right, 3:Up

        int x = maze.getStartPos().x;
        int y = maze.getStartPos().y;

        path.add(new Point(x, y));
        currentPath.add(new Point(x, y));
        visited[y][x] = true;
        steps = 1;

        while (!(x == maze.getEndPos().x && y == maze.getEndPos().y)) {
            Point next = getNextPosition(x, y);
            if (next == null) {
                return -1; // No path found
            }

            x = next.x;
            y = next.y;

            path.add(new Point(x, y));
            currentPath.add(new Point(x, y));
            visited[y][x] = true;
            steps++;

            // Prevent infinite loops
            if (steps > maze.getRows() * maze.getCols() * 2) {
                return -1;
            }
        }

        steps = path.size() - 1 + penaltySteps;
        return System.currentTimeMillis() - startTime;
    }

    public Point getNextPosition(int x, int y) {
        // Direction order: Left, Down, Right, Up (for left hand)
        // or Right, Down, Left, Up (for right hand)
        int[][] directions = isLeftHand ?
                new int[][]{{-1, 0}, {0, 1}, {1, 0}, {0, -1}} :
                new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

        // Check for teleport first
        Point teleportPos = handleSpecialTile(x, y);
        if (teleportPos != null) {
            return teleportPos;
        }

        // Try to follow the wall
        for (int i = 0; i < 4; i++) {
            // Check in preferred direction (current direction + offset)
            int checkDir = (currentDirection + i) % 4;
            int newX = x + directions[checkDir][0];
            int newY = y + directions[checkDir][1];

            if (maze.isValidMove(newX, newY, visited)) {
                currentDirection = checkDir;
                return new Point(newX, newY);
            }
        }

        // If no move found (shouldn't happen in a valid maze)
        return null;
    }

    public List<Point> getCurrentPath() {
        return new ArrayList<>(currentPath);
    }

    @Override
    public void reset() {
        super.reset();
        currentPath.clear();
        currentDirection = isLeftHand ? 0 : 2;
    }
}