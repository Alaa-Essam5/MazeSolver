import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * AStarSolver is a maze solver that uses the A* search algorithm.
 * It extends the MazeSolver base class and finds the shortest path
 * from a start point to an end point using a combination of actual cost and heuristic.
 */
public class AStarSolver extends MazeSolver {

    /**
     * Inner Node class representing a cell in the maze grid.
     * Each node holds its coordinates, parent (for path tracking), and cost values:
     * g = cost from the start, h = estimated cost to the end (heuristic).
     */
    public static class Node implements Comparable<Node> {
        public int x, y;
        public Node parent;
        public double g; // Actual cost from start to this node
        public double h; // Heuristic cost from this node to goal

        public Node(int x, int y, Node parent, double g, double h) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        // Total estimated cost (f = g + h)
        public double getF() {
            return g + h;
        }

        // Compare nodes by their total cost (f-value) for priority queue sorting
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.getF(), other.getF());
        }
    }

    protected Node currentNode; // Current node being processed

    // Constructor sets the algorithm name and passes the maze to the parent class
    public AStarSolver(Maze maze) {
        super(maze);
        this.algorithmName = "A*";
    }

    // Returns the current path from the start to the current node
    public List<Point> getCurrentPath() {
        return reconstructPath(currentNode);
    }

    /**
     * The main A* solving function.
     * It searches the shortest path from start to end using open/closed lists.
     *
     * @param visualize Whether to enable visualization (not used here)
     * @return Time taken to solve the maze in milliseconds, or -1 if no path found
     */
    @Override
    public long solve(boolean visualize) {
        reset(); // Reset any previous state
        long startTime = System.currentTimeMillis();

        // Priority queue (min-heap) for nodes to explore, sorted by f = g + h
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        // Stores all visited or created nodes to avoid duplicates
        Map<Point, Node> allNodes = new HashMap<>();

        // Create the start node
        Node startNode = new Node(maze.getStartPos().x, maze.getStartPos().y,
                null, 0, heuristic(maze.getStartPos().x, maze.getStartPos().y));
        openSet.add(startNode);
        allNodes.put(new Point(maze.getStartPos().x, maze.getStartPos().y), startNode);

        // Main loop: continue while there are nodes to explore
        while (!openSet.isEmpty()) {
            currentNode = openSet.poll(); // Get node with the lowest f-value

            // Check if goal has been reached
            if (currentNode.x == maze.getEndPos().x && currentNode.y == maze.getEndPos().y) {
                path = reconstructPath(currentNode); // Rebuild the path
                steps = path.size() - 1 + penaltySteps;
                return System.currentTimeMillis() - startTime; // Return time taken
            }

            visited[currentNode.y][currentNode.x] = true; // Mark as visited

            // Handle teleport or special tiles (returns new position if teleportation occurred)
            Point teleportPos = handleSpecialTile(currentNode.x, currentNode.y);
            if (teleportPos != null) {
                double newG = currentNode.g + 1;
                Node teleportNode = allNodes.getOrDefault(new Point(teleportPos.x, teleportPos.y),
                        new Node(teleportPos.x, teleportPos.y, currentNode, newG, heuristic(teleportPos.x, teleportPos.y)));

                // Update the teleport node if a shorter path is found
                if (newG < teleportNode.g) {
                    teleportNode.g = newG;
                    teleportNode.parent = currentNode;
                    openSet.add(teleportNode);
                    allNodes.put(new Point(teleportPos.x, teleportPos.y), teleportNode);
                }
                continue; // Skip neighbor check for teleportation
            }

            // Explore neighbors of the current node
            for (Point neighbor : maze.getNeighbors(currentNode.x, currentNode.y, visited)) {
                if (visited[neighbor.y][neighbor.x]) continue; // Skip if already visited

                double tentativeG = currentNode.g + 1; // Cost to move to neighbor
                Node neighborNode = allNodes.getOrDefault(neighbor,
                        new Node(neighbor.x, neighbor.y, null, Double.MAX_VALUE, heuristic(neighbor.x, neighbor.y)));

                // Update neighbor if a shorter path is found
                if (tentativeG < neighborNode.g) {
                    neighborNode.g = tentativeG;
                    neighborNode.parent = currentNode;
                    openSet.add(neighborNode);
                    allNodes.put(neighbor, neighborNode);
                }
            }
        }

        return -1; // No path found
    }

    /**
     * Heuristic function: estimates cost from (x, y) to the goal.
     * Uses Manhattan distance (no diagonal movement allowed).
     */
    private double heuristic(int x, int y) {
        return Math.abs(x - maze.getEndPos().x) + Math.abs(y - maze.getEndPos().y);
    }

    /**
     * Reconstructs the path from end to start by following parent links.
     * @param node The end node (goal)
     * @return List of Points representing the path from start to goal
     */
    protected List<Point> reconstructPath(Node node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(new Point(node.x, node.y));
            node = node.parent;
        }
        Collections.reverse(path); // Reverse to get path from start to goal
        return path;
    }
}
