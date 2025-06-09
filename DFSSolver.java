import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DFSSolver extends MazeSolver {
    public static class PathNode {
        int x, y;
        List<Point> path;

        PathNode(int x, int y, List<Point> path) {
            this.x = x;
            this.y = y;
            this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
        }
    }

    public DFSSolver(Maze maze) {
        super(maze);
        this.algorithmName = "DFS";
    }

    @Override
    public long solve(boolean visualize) {
        reset();
        long startTime = System.currentTimeMillis();
        Stack<PathNode> stack = new Stack<>();
        stack.push(new PathNode(maze.getStartPos().x, maze.getStartPos().y, new ArrayList<>()));

        while (!stack.isEmpty()) {
            PathNode node = stack.pop();
            int x = node.x;
            int y = node.y;
            List<Point> currentPath = node.path;

            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                path = new ArrayList<>(currentPath);
                path.add(new Point(x, y));
                steps = path.size() - 1 + penaltySteps;
                return System.currentTimeMillis() - startTime;
            }

            if (!visited[y][x]) {
                visited[y][x] = true;
                List<Point> newPath = new ArrayList<>(currentPath);
                newPath.add(new Point(x, y));

                Point teleportPos = handleSpecialTile(x, y);
                if (teleportPos != null) {
                    stack.push(new PathNode(teleportPos.x, teleportPos.y, newPath));
                    continue;
                }

                for (Point neighbor : maze.getNeighbors(x, y, visited)) {
                    stack.push(new PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }

        return -1;
    }
}