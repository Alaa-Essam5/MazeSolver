import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MazePanel extends JPanel {
    // Size of each cell in pixels
    protected int cellSize = 30;

    private Maze maze;                // The maze data structure
    private boolean[][] visited;     // Tracks visited cells
    private List<Point> path;        // The path from start to end
    private String algorithm;        // The algorithm used for solving

    // Set the data required to draw the maze
    public void setMazeData(Maze maze, boolean[][] visited, List<Point> path, String algorithm) {
        this.maze = maze;
        this.visited = visited;
        this.path = path;
        this.algorithm = algorithm;
    }

    // Custom painting of the maze panel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (maze == null) return; // Nothing to draw if maze is not set

        for (int y = 0; y < maze.getRows(); y++) {
            for (int x = 0; x < maze.getCols(); x++) {
                // Get the current cell character
                char cell = maze.getMaze()[y][x];

                // Set color based on cell type
                Color color = getCellColor(cell);
                g.setColor(color);
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Shade visited cells (except special ones)
                if (visited != null && visited[y][x] && !isSpecialCell(cell)) {
                    g.setColor(new Color(200, 200, 200, 150)); // Transparent gray
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }

                // Color the solution path
                if (path != null && path.contains(new Point(x, y)) && !isSpecialCell(cell)) {
                    g.setColor(getPathColor());
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }

                // Draw cell border
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Draw text label for special cells (S, E, T, P)
                drawCellLabel(g, x, y, cell);
            }
        }

        // Highlight the current position for wall follower algorithms
        if (algorithm != null && (algorithm.equals("LeftHand") || algorithm.equals("RightHand")) && path != null && !path.isEmpty()) {
            Point current = path.get(path.size() - 1);
            g.setColor(Color.YELLOW);
            g.fillOval(current.x * cellSize + cellSize/4, current.y * cellSize + cellSize/4,
                    cellSize/2, cellSize/2);
        }
    }

    // Determines the color to use for each cell type
    private Color getCellColor(char cell) {
        switch (cell) {
            case Maze.WALL: return Color.BLACK;               // Wall
            case Maze.START: return Color.GREEN;              // Start point
            case Maze.END: return Color.RED;                  // End point
            case Maze.TELEPORT: return new Color(0, 255, 255); // Cyan for teleport
            case Maze.PENALTY: return new Color(255, 192, 203); // Pink for penalty
            default: return Color.WHITE;                      // Normal path
        }
    }

    // Chooses path color based on algorithm used
    private Color getPathColor() {
        if (algorithm == null) return Color.BLUE;
        switch (algorithm) {
            case "A*": return new Color(0, 128, 128);       // Teal for A*
            case "Dijkstra": return new Color(255, 165, 0); // Orange for Dijkstra
            default: return Color.BLUE;                     // Default path color
        }
    }

    // Checks if the cell is a special type (not to be overridden visually)
    private boolean isSpecialCell(char cell) {
        return cell == Maze.START || cell == Maze.END || cell == Maze.TELEPORT || cell == Maze.PENALTY;
    }

    // Draws label text for special cells
    private void drawCellLabel(Graphics g, int x, int y, char cell) {
        g.setColor(Color.BLACK);
        String label = "";
        if (cell == Maze.START) label = "S";
        else if (cell == Maze.END) label = "E";
        else if (cell == Maze.TELEPORT) label = "T";
        else if (cell == Maze.PENALTY) label = "P";

        // Draw the label centered inside the cell
        if (!label.isEmpty()) {
            g.drawString(label, x * cellSize + cellSize/2 - 3, y * cellSize + cellSize/2 + 5);
        }
    }

    // Returns the preferred panel size based on maze dimensions
    @Override
    public Dimension getPreferredSize() {
        if (maze == null) return new Dimension(300, 300);
        return new Dimension(maze.getCols() * cellSize, maze.getRows() * cellSize);
    }
}
