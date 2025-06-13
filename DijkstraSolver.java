import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * DijkstraSolver uses Dijkstra's algorithm to find the shortest path
 * through a maze from the start to the end point.
 */
public class DijkstraSolver extends MazeSolver {

    // Inner class to represent each node (cell) in the maze.
    public static class Node implements Comparable<Node> {
        public int x, y;             // Coordinates of the node
        public Node parent;          // Parent node used to reconstruct the path
        public double distance;      // Distance from the start node

        public Node(int x, int y, double distance, Node parent) {
            this.x = x;
            this.y = y;
            this.distance = distance;
            this.parent = parent;
        }

        // Compare nodes by their distance value (used by the priority queue)
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    protected Node currentNode;

    // Constructor to initialize the solver with a maze
    public DijkstraSolver(Maze maze) {
        super(maze);
        this.algorithmName = "Dijkstra";
    }

    // Returns the current shortest path as a list of points
    public List<Point> getCurrentPath() {
        return reconstructPath(currentNode);
    }

    /**
     * Solves the maze using Dijkstra’s algorithm.
     * @param visualize whether to visualize the steps (not used here)
     * @return time taken to solve in milliseconds, or -1 if no path found
     */
    @Override
    public long solve(boolean visualize) {
        reset();  // Reset the solver’s internal state
        long startTime = System.currentTimeMillis();

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        Map<Point, Node> allNodes = new HashMap<>();

        // Initialize the start node
        Node startNode = new Node(maze.getStartPos().x, maze.getStartPos().y, 0, null);
        priorityQueue.add(startNode);
        allNodes.put(new Point(startNode.x, startNode.y), startNode);

        // Initialize all other non-wall nodes with infinite distance
        for (int y = 0; y < maze.getRows(); y++) {
            for (int x = 0; x < maze.getCols(); x++) {
                if (maze.getMaze()[y][x] != Maze.WALL &&
                        !(x == maze.getStartPos().x && y == maze.getStartPos().y)) {
                    Node node = new Node(x, y, Double.MAX_VALUE, null);
                    allNodes.put(new Point(x, y), node);
                }
            }
        }

        // Main loop: keep exploring the closest unvisited node
        while (!priorityQueue.isEmpty()) {
            currentNode = priorityQueue.poll();  // Get node with smallest distance

            // If we've reached the goal, reconstruct and return the path
            if (currentNode.x == maze.getEndPos().x && currentNode.y == maze.getEndPos().y) {
                path = reconstructPath(currentNode);
                steps = path.size() - 1 + penaltySteps;
                return System.currentTimeMillis() - startTime;
            }

            visited[currentNode.y][currentNode.x] = true;  // Mark as visited

            // Check if the current tile has a teleport to another location
            Point teleportPos = handleSpecialTile(currentNode.x, currentNode.y);
            if (teleportPos != null) {
                Node teleportNode = allNodes.get(new Point(teleportPos.x, teleportPos.y));
                double newDistance = currentNode.distance + 1;

                // Update distance if teleporting gives a shorter path
                if (newDistance < teleportNode.distance) {
                    priorityQueue.remove(teleportNode);
                    teleportNode.distance = newDistance;
                    teleportNode.parent = currentNode;
                    priorityQueue.add(teleportNode);
                }
                continue;  // Skip processing normal neighbors if teleport exists
            }

            // Visit all valid neighboring cells
            for (Point neighbor : maze.getNeighbors(currentNode.x, currentNode.y, visited)) {
                if (visited[neighbor.y][neighbor.x]) continue;

                Node neighborNode = allNodes.get(neighbor);
                double newDistance = currentNode.distance + 1;

                // Update neighbor distance and path if a better route is found
                if (newDistance < neighborNode.distance) {
                    priorityQueue.remove(neighborNode);  // Update priority in queue
                    neighborNode.distance = newDistance;
                    neighborNode.parent = currentNode;
                    priorityQueue.add(neighborNode);
                }
            }
        }

        // No path was found
        return -1;
    }

    /**
     * Reconstructs the path from the end node back to the start node.
     * @param node the goal node
     * @return list of points representing the path from start to goal
     */
    protected List<Point> reconstructPath(Node node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(new Point(node.x, node.y));
            node = node.parent;
        }
        Collections.reverse(path);  // Path was built backwards
        return path;
    }
}
