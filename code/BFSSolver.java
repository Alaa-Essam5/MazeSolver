import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// BFSSolver implements the Breadth-First Search algorithm for solving the maze
public class BFSSolver extends MazeSolver {

    // Inner class to keep track of a position (x, y) and the path taken to reach it
    public static class PathNode {
        int x, y;
        List<Point> path;

        // Constructor initializes the node's position and its path
        PathNode(int x, int y, List<Point> path) {
            this.x = x;
            this.y = y;
            // Make a copy of the path to avoid modifying the original list
            this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
        }
    }

    // Constructor initializes the solver and sets its algorithm name
    public BFSSolver(Maze maze) {
        super(maze);
        this.algorithmName = "BFS";
    }

    // The main function that performs the BFS algorithm
    @Override
    public long solve(boolean visualize) {
        // Reset the solver’s internal state before starting
        reset();

        // Track how long the solving process takes
        long startTime = System.currentTimeMillis();

        // Initialize BFS queue with the starting point of the maze
        Queue<PathNode> queue = new LinkedList<>();
        queue.add(new PathNode(maze.getStartPos().x, maze.getStartPos().y, new ArrayList<>()));

        // Mark the start point as visited
        visited[maze.getStartPos().y][maze.getStartPos().x] = true;

        // Start BFS loop
        while (!queue.isEmpty()) {
            // Get the current node from the queue
            PathNode node = queue.poll();
            int x = node.x;
            int y = node.y;
            List<Point> currentPath = node.path;

            // Check if we've reached the goal
            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                path = new ArrayList<>(currentPath);     // Store the final path
                path.add(new Point(x, y));              // Add the final point to the path
                steps = path.size() - 1 + penaltySteps; // Count the total steps
                return System.currentTimeMillis() - startTime; // Return execution time
            }

            // Create a new path list for neighbors
            List<Point> newPath = new ArrayList<>(currentPath);
            newPath.add(new Point(x, y));

            // Handle special tiles like teleporters or traps
            Point teleportPos = handleSpecialTile(x, y);
            if (teleportPos != null && !visited[teleportPos.y][teleportPos.x]) {
                visited[teleportPos.y][teleportPos.x] = true;
                queue.add(new PathNode(teleportPos.x, teleportPos.y, newPath));
                continue; // Skip normal neighbors if teleported
            }

            // Explore all valid and unvisited neighbors of the current node
            for (Point neighbor : maze.getNeighbors(x, y, visited)) {
                if (!visited[neighbor.y][neighbor.x]) {
                    visited[neighbor.y][neighbor.x] = true;
                    queue.add(new PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }

        // If no path is found, return -1 to indicate failure
        return -1;
    }
}
