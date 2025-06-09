import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BFSSolver extends MazeSolver {
    public static class PathNode {
        int x, y;
        List<Point> path;

        PathNode(int x, int y, List<Point> path) {
            this.x = x;
            this.y = y;
            this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
        }
    }

    public BFSSolver(Maze maze) {
        super(maze);
        this.algorithmName = "BFS";
    }

    @Override
    public long solve(boolean visualize) {
        reset();
        long startTime = System.currentTimeMillis();
        Queue<PathNode> queue = new LinkedList<>();
        queue.add(new PathNode(maze.getStartPos().x, maze.getStartPos().y, new ArrayList<>()));
        visited[maze.getStartPos().y][maze.getStartPos().x] = true;

        while (!queue.isEmpty()) {
            PathNode node = queue.poll();
            int x = node.x;
            int y = node.y;
            List<Point> currentPath = node.path;

            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                path = new ArrayList<>(currentPath);
                path.add(new Point(x, y));
                steps = path.size() - 1 + penaltySteps;
                return System.currentTimeMillis() - startTime;
            }

            List<Point> newPath = new ArrayList<>(currentPath);
            newPath.add(new Point(x, y));

            Point teleportPos = handleSpecialTile(x, y);
            if (teleportPos != null && !visited[teleportPos.y][teleportPos.x]) {
                visited[teleportPos.y][teleportPos.x] = true;
                queue.add(new PathNode(teleportPos.x, teleportPos.y, newPath));
                continue;
            }

            for (Point neighbor : maze.getNeighbors(x, y, visited)) {
                if (!visited[neighbor.y][neighbor.x]) {
                    visited[neighbor.y][neighbor.x] = true;
                    queue.add(new PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }

        return -1;
    }
}