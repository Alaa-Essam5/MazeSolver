import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MazePanel extends JPanel {
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;
    private static final Color RED = Color.RED;
    private static final Color GREEN = Color.GREEN;
    private static final Color BLUE = Color.BLUE;
    private static final Color YELLOW = Color.YELLOW;
    private static final Color TELEPORT_COLOR = new Color(0, 255, 255); // Cyan
    private static final Color PENALTY_COLOR = new Color(255, 192, 203); // Pink
    private static final Color A_STAR_COLOR = new Color(0, 128, 128); // Teal
    private static final Color DIJKSTRA_COLOR = new Color(255, 165, 0); // Orange
    private static final Color GRAY = new Color(200, 200, 200);

    private Maze maze;
    private boolean[][] visited;
    private List<Point> path;
    private String currentAlgorithm;
    public int cellSize = 30;

    public void setMazeData(Maze maze, boolean[][] visited, List<Point> path, String currentAlgorithm) {
        this.maze = maze;
        this.visited = visited;
        this.path = path;
        this.currentAlgorithm = currentAlgorithm;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (maze == null || maze.getMaze() == null) return;

        // Add bounds checking
        int rows = maze.getRows();
        int cols = maze.getCols();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // Skip if coordinates are out of bounds
                if (y < 0 || y >= rows || x < 0 || x >= cols) {
                    continue;
                }

                Color color = WHITE;
                char cell = maze.getMaze()[y][x];

                if (cell == Maze.WALL) {
                    color = BLACK;
                } else if (cell == Maze.START) {
                    color = GREEN;
                } else if (cell == Maze.END) {
                    color = RED;
                } else if (cell == Maze.TELEPORT) {
                    color = TELEPORT_COLOR;
                } else if (cell == Maze.PENALTY) {
                    color = PENALTY_COLOR;
                }

                // Draw cell background
                g.setColor(color);
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Draw visited cells - add null check for visited array
                if (visited != null && y < visited.length && x < visited[y].length &&
                        visited[y][x] && cell != Maze.START && cell != Maze.END &&
                        cell != Maze.TELEPORT && cell != Maze.PENALTY) {
                    g.setColor(GRAY);
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }

                // Draw path - add null check for path
                if (path != null && path.contains(new Point(x, y)) &&
                        cell != Maze.START && cell != Maze.END) {
                    Color pathColor = BLUE;
                    if ("A*".equals(currentAlgorithm)) {
                        pathColor = A_STAR_COLOR;
                    } else if ("Dijkstra".equals(currentAlgorithm)) {
                        pathColor = DIJKSTRA_COLOR;
                    }
                    g.setColor(pathColor);
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }

                // Draw grid
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        // Draw labels - add null checks
        if (maze.getStartPos() != null) {
            g.setColor(Color.BLACK);
            g.drawString("S", maze.getStartPos().x * cellSize + cellSize/2 - 3,
                    maze.getStartPos().y * cellSize + cellSize/2 + 5);
        }
        if (maze.getEndPos() != null) {
            g.setColor(Color.BLACK);
            g.drawString("E", maze.getEndPos().x * cellSize + cellSize/2 - 3,
                    maze.getEndPos().y * cellSize + cellSize/2 + 5);
        }

        if (maze.getTeleportPositions() != null) {
            for (Point tp : maze.getTeleportPositions()) {
                if (tp != null) {
                    g.setColor(Color.BLACK);
                    g.drawString("T", tp.x * cellSize + cellSize/2 - 3, tp.y * cellSize + cellSize/2 + 5);
                }
            }
        }

        if (maze.getPenaltyPositions() != null) {
            for (Point pp : maze.getPenaltyPositions()) {
                if (pp != null) {
                    g.setColor(Color.BLACK);
                    g.drawString("P", pp.x * cellSize + cellSize/2 - 3, pp.y * cellSize + cellSize/2 + 5);
                }
            }
        }

        // Draw current position if in wall follower algorithms
        if (path != null && !path.isEmpty() &&
                ("LeftHand".equals(currentAlgorithm) || "RightHand".equals(currentAlgorithm))) {
            Point current = path.get(path.size() - 1);
            if (current != null) {
                g.setColor(YELLOW);
                g.fillOval(current.x * cellSize + cellSize/4, current.y * cellSize + cellSize/4,
                        cellSize/2, cellSize/2);
            }
        }
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }
}