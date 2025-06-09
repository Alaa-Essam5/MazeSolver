import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStarSolver extends MazeSolver {
    public static class Node implements Comparable<Node> {
        public int x, y;
        public Node parent;
        public double g; // Cost from start
        public double h; // Heuristic to end

        public Node(int x, int y, Node parent, double g, double h) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        public double getF() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.getF(), other.getF());
        }
    }

    protected Node currentNode;

    public AStarSolver(Maze maze) {
        super(maze);
        this.algorithmName = "A*";
    }

    public List<Point> getCurrentPath() {
        return reconstructPath(currentNode);
    }

    @Override
    public long solve(boolean visualize) {
        reset();
        long startTime = System.currentTimeMillis();

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<Point, Node> allNodes = new HashMap<>();

        Node startNode = new Node(maze.getStartPos().x, maze.getStartPos().y,
                null, 0, heuristic(maze.getStartPos().x, maze.getStartPos().y));
        openSet.add(startNode);
        allNodes.put(new Point(maze.getStartPos().x, maze.getStartPos().y), startNode);

        while (!openSet.isEmpty()) {
            currentNode = openSet.poll();

            if (currentNode.x == maze.getEndPos().x && currentNode.y == maze.getEndPos().y) {
                path = reconstructPath(currentNode);
                steps = path.size() - 1 + penaltySteps;
                return System.currentTimeMillis() - startTime;
            }

            visited[currentNode.y][currentNode.x] = true;

            Point teleportPos = handleSpecialTile(currentNode.x, currentNode.y);
            if (teleportPos != null) {
                double newG = currentNode.g + 1;
                Node teleportNode = allNodes.getOrDefault(new Point(teleportPos.x, teleportPos.y),
                        new Node(teleportPos.x, teleportPos.y, currentNode, newG, heuristic(teleportPos.x, teleportPos.y)));

                if (newG < teleportNode.g) {
                    teleportNode.g = newG;
                    teleportNode.parent = currentNode;
                    openSet.add(teleportNode);
                    allNodes.put(new Point(teleportPos.x, teleportPos.y), teleportNode);
                }
                continue;
            }

            for (Point neighbor : maze.getNeighbors(currentNode.x, currentNode.y, visited)) {
                if (visited[neighbor.y][neighbor.x]) continue;

                double tentativeG = currentNode.g + 1;
                Node neighborNode = allNodes.getOrDefault(neighbor,
                        new Node(neighbor.x, neighbor.y, null, Double.MAX_VALUE, heuristic(neighbor.x, neighbor.y)));

                if (tentativeG < neighborNode.g) {
                    neighborNode.g = tentativeG;
                    neighborNode.parent = currentNode;
                    openSet.add(neighborNode);
                    allNodes.put(neighbor, neighborNode);
                }
            }
        }

        return -1;
    }

    private double heuristic(int x, int y) {
        return Math.abs(x - maze.getEndPos().x) + Math.abs(y - maze.getEndPos().y);
    }

    protected List<Point> reconstructPath(Node node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(new Point(node.x, node.y));
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
}