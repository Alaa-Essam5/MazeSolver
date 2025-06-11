import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class DijkstraSolver extends MazeSolver {
    public static class Node implements Comparable<Node> {
        public int x, y;
        public Node parent;
        public double distance;

        // تم تصحيح معاملات البناء هنا
        public Node(int x, int y, double distance, Node parent) {
            this.x = x;
            this.y = y;
            this.distance = distance;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    protected Node currentNode;

    public DijkstraSolver(Maze maze) {
        super(maze);
        this.algorithmName = "Dijkstra";
    }

    public List<Point> getCurrentPath() {
        return reconstructPath(currentNode);
    }

    @Override
    public long solve(boolean visualize) {
        reset();
        long startTime = System.currentTimeMillis();

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        Map<Point, Node> allNodes = new HashMap<>();

        // تم تصحيح استدعاء البناء هنا
        Node startNode = new Node(maze.getStartPos().x, maze.getStartPos().y, 0, null);
        priorityQueue.add(startNode);
        allNodes.put(new Point(maze.getStartPos().x, maze.getStartPos().y), startNode);

        for (int y = 0; y < maze.getRows(); y++) {
            for (int x = 0; x < maze.getCols(); x++) {
                if (maze.getMaze()[y][x] != Maze.WALL && !(x == maze.getStartPos().x && y == maze.getStartPos().y)) {
                    Node node = new Node(x, y, Double.MAX_VALUE, null);
                    allNodes.put(new Point(x, y), node);
                }
            }
        }

        while (!priorityQueue.isEmpty()) {
            currentNode = priorityQueue.poll();

            if (currentNode.x == maze.getEndPos().x && currentNode.y == maze.getEndPos().y) {
                path = reconstructPath(currentNode);
                steps = path.size() - 1 + penaltySteps;
                return System.currentTimeMillis() - startTime;
            }

            visited[currentNode.y][currentNode.x] = true;

            Point teleportPos = handleSpecialTile(currentNode.x, currentNode.y);
            if (teleportPos != null) {
                Node teleportNode = allNodes.get(new Point(teleportPos.x, teleportPos.y));
                double newDistance = currentNode.distance + 1;

                if (newDistance < teleportNode.distance) {
                    priorityQueue.remove(teleportNode);
                    teleportNode.distance = newDistance;
                    teleportNode.parent = currentNode;
                    priorityQueue.add(teleportNode);
                }
                continue;
            }

            for (Point neighbor : maze.getNeighbors(currentNode.x, currentNode.y, visited)) {
                if (visited[neighbor.y][neighbor.x]) continue;

                Node neighborNode = allNodes.get(neighbor);
                double newDistance = currentNode.distance + 1;

                if (newDistance < neighborNode.distance) {
                    priorityQueue.remove(neighborNode);
                    neighborNode.distance = newDistance;
                    neighborNode.parent = currentNode;
                    priorityQueue.add(neighborNode);
                }
            }
        }

        return -1;
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
