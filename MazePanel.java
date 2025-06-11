import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MazePanel extends JPanel {
    protected int cellSize = 30;
    private Maze maze;
    private boolean[][] visited;
    private List<Point> path;
    private String algorithm;

    public void setMazeData(Maze maze, boolean[][] visited, List<Point> path, String algorithm) {
        this.maze = maze;
        this.visited = visited;
        this.path = path;
        this.algorithm = algorithm;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (maze == null) return;

        for (int y = 0; y < maze.getRows(); y++) {
            for (int x = 0; x < maze.getCols(); x++) {
                // Draw cell background
                char cell = maze.getMaze()[y][x];
                Color color = getCellColor(cell);
                g.setColor(color);
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Draw visited cells
                if (visited != null && visited[y][x] && !isSpecialCell(cell)) {
                    g.setColor(new Color(200, 200, 200, 150));
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }

                // Draw path
                if (path != null && path.contains(new Point(x, y)) && !isSpecialCell(cell)) {
                    g.setColor(getPathColor());
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }

                // Draw cell border
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Draw cell label
                drawCellLabel(g, x, y, cell);
            }
        }

        // Draw current position for wall followers
        if (algorithm != null && (algorithm.equals("LeftHand") || algorithm.equals("RightHand")) && path != null && !path.isEmpty()) {
            Point current = path.get(path.size() - 1);
            g.setColor(Color.YELLOW);
            g.fillOval(current.x * cellSize + cellSize/4, current.y * cellSize + cellSize/4,
                    cellSize/2, cellSize/2);
        }
    }

    private Color getCellColor(char cell) {
        switch (cell) {
            case Maze.WALL: return Color.BLACK;
            case Maze.START: return Color.GREEN;
            case Maze.END: return Color.RED;
            case Maze.TELEPORT: return new Color(0, 255, 255); // Cyan for teleport
            case Maze.PENALTY: return new Color(255, 192, 203); // Pink for penalty
            default: return Color.WHITE;
        }
    }

    private Color getPathColor() {
        if (algorithm == null) return Color.BLUE;
        switch (algorithm) {
            case "A*": return new Color(0, 128, 128); // Teal
            case "Dijkstra": return new Color(255, 165, 0); // Orange
            default: return Color.BLUE;
        }
    }

    private boolean isSpecialCell(char cell) {
        return cell == Maze.START || cell == Maze.END || cell == Maze.TELEPORT || cell == Maze.PENALTY;
    }

    private void drawCellLabel(Graphics g, int x, int y, char cell) {
        g.setColor(Color.BLACK);
        String label = "";
        if (cell == Maze.START) label = "S";
        else if (cell == Maze.END) label = "E";
        else if (cell == Maze.TELEPORT) label = "T";
        else if (cell == Maze.PENALTY) label = "P";

        if (!label.isEmpty()) {
            g.drawString(label, x * cellSize + cellSize/2 - 3, y * cellSize + cellSize/2 + 5);
        }
    }



    @Override
    public Dimension getPreferredSize() {
        if (maze == null) return new Dimension(300, 300);
        return new Dimension(maze.getCols() * cellSize, maze.getRows() * cellSize);
    }
}