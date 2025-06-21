import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Maze {
    // Constants to represent different types of tiles in the maze
    public static final char WALL = '#';
    public static final char PATH = ' ';
    public static final char START = 'S';
    public static final char END = 'E';
    public static final char TELEPORT = 'T';
    public static final char PENALTY = 'P';

    private char[][] maze;  // 2D array to hold the maze layout
    private int rows, cols; // Number of rows and columns in the maze
    private Point startPos, endPos; // Start and end positions in the maze
    private List<Point> teleportPositions = new ArrayList<>(); // List of teleport tile positions
    private List<Point> penaltyPositions = new ArrayList<>();  // List of penalty tile positions

    // Loads the maze from a file
    public void loadMaze(File file) throws IOException {
        List<String> lines = new ArrayList<>();

        // Read the file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        // Throw error if file is empty
        if (lines.isEmpty()) {
            throw new IOException("Maze file is empty.");
        }

        // Set the dimensions of the maze
        rows = lines.size();
        cols = lines.get(0).length();

        // Ensure all lines are the same length (rectangular maze)
        for (int i = 0; i < rows; i++) {
            if (lines.get(i).length() != cols) {
                throw new IOException("All lines in the maze must have the same length. Line " + (i + 1) +
                        " has length " + lines.get(i).length() + " instead of " + cols + ".");
            }
        }

        // Initialize maze array and reset teleport/penalty/start/end
        maze = new char[rows][cols];
        teleportPositions.clear();
        penaltyPositions.clear();
        startPos = null;
        endPos = null;

        // Fill in the maze grid and record special tile positions
        for (int i = 0; i < rows; i++) {
            String line = lines.get(i);
            for (int j = 0; j < cols; j++) {
                char ch = line.charAt(j);
                maze[i][j] = ch;

                if (ch == START) {
                    startPos = new Point(j, i);
                } else if (ch == END) {
                    endPos = new Point(j, i);
                } else if (ch == TELEPORT) {
                    teleportPositions.add(new Point(j, i));
                } else if (ch == PENALTY) {
                    penaltyPositions.add(new Point(j, i));
                }
            }
        }

        // Ensure that both start and end positions exist
        if (startPos == null || endPos == null) {
            throw new IOException("Maze must contain both a start (S) and an end (E) position.");
        }
    }

    // Returns the full maze array
    public char[][] getMaze() {
        return maze;
    }

    // Returns the number of rows in the maze
    public int getRows() {
        return rows;
    }

    // Returns the number of columns in the maze
    public int getCols() {
        return cols;
    }

    // Returns the starting position
    public Point getStartPos() {
        return startPos;
    }

    // Returns the ending position
    public Point getEndPos() {
        return endPos;
    }

    // Returns list of teleport tile positions
    public List<Point> getTeleportPositions() {
        return teleportPositions;
    }

    // Returns list of penalty tile positions
    public List<Point> getPenaltyPositions() {
        return penaltyPositions;
    }

    // Checks if a move to (x, y) is within bounds, not a wall, and not already visited
    public boolean isValidMove(int x, int y, boolean[][] visited) {
        return x >= 0 && x < cols && y >= 0 && y < rows
                && maze[y][x] != WALL && !visited[y][x];
    }

    // Returns all valid neighbors (up, right, down, left) from current position
    public List<Point> getNeighbors(int x, int y, boolean[][] visited) {
        List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // Up, Right, Down, Left

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (isValidMove(newX, newY, visited)) {
                neighbors.add(new Point(newX, newY));
            }
        }

        return neighbors;
    }
}
