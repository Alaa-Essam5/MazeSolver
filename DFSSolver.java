import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// DFSSolver class extends MazeSolver and implements the Depth-First Search (DFS) algorithm
public class DFSSolver extends MazeSolver {

    // Inner class to represent a node in the search path
    public static class PathNode {
        int x, y; // Coordinates of the node
        List<Point> path; // The path taken to reach this node

        // Constructor for PathNode
        PathNode(int x, int y, List<Point> path) {
            this.x = x;
            this.y = y;
            // Copy the current path if it exists, or create a new empty one
            this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
        }
    }

    // Constructor for DFSSolver that sets the algorithm name
    public DFSSolver(Maze maze) {
        super(maze);
        this.algorithmName = "DFS";
    }

    // Main function to solve the maze using DFS
    @Override
    public long solve(boolean visualize) {
        reset(); // Reset visited matrix, steps, and path
        long startTime = System.currentTimeMillis(); // Record start time

        Stack<PathNode> stack = new Stack<>(); // Create a stack for DFS
        // Push the starting position onto the stack with an empty path
        stack.push(new PathNode(maze.getStartPos().x, maze.getStartPos().y, new ArrayList<>()));

        // Loop until there are no more nodes to explore
        while (!stack.isEmpty()) {
            // Pop the last node (LIFO) from the stack
            PathNode node = stack.pop();
            int x = node.x;
            int y = node.y;
            List<Point> currentPath = node.path;

            // Check if the end position is reached
            if (x == maze.getEndPos().x && y == maze.getEndPos().y) {
                path = new ArrayList<>(currentPath); // Copy the successful path
                path.add(new Point(x, y)); // Add the final point to the path
                steps = path.size() - 1 + penaltySteps; // Update steps (subtract 1 because we include the start)
                return System.currentTimeMillis() - startTime; // Return total time taken
            }

            // If the current node has not been visited yet
            if (!visited[y][x]) {
                visited[y][x] = true; // Mark it as visited

                // Create a new path that includes the current node
                List<Point> newPath = new ArrayList<>(currentPath);
                newPath.add(new Point(x, y));

                // Check for special tiles (e.g., teleport)
                Point teleportPos = handleSpecialTile(x, y);
                if (teleportPos != null) {
                    // If teleporting, push the teleport destination to the stack and skip neighbors
                    stack.push(new PathNode(teleportPos.x, teleportPos.y, newPath));
                    continue;
                }

                // Get all valid neighbors that haven't been visited
                for (Point neighbor : maze.getNeighbors(x, y, visited)) {
                    // Push each neighbor to the stack with the updated path
                    stack.push(new PathNode(neighbor.x, neighbor.y, newPath));
                }
            }
        }

        // If no path was found, return -1 to indicate failure
        return -1;
    }
}
