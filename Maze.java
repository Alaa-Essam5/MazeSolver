import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Maze {
    public static final char WALL = '#';
    public static final char PATH = ' ';
    public static final char START = 'S';
    public static final char END = 'E';
    public static final char TELEPORT = 'T';
    public static final char PENALTY = 'P';

    private char[][] maze;
    private int rows, cols;
    private Point startPos, endPos;
    private List<Point> teleportPositions = new ArrayList<>();
    private List<Point> penaltyPositions = new ArrayList<>();

    public void loadMaze(File file) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            throw new IOException("Maze file is empty.");
        }

        rows = lines.size();
        cols = lines.get(0).length();

        for (int i = 0; i < rows; i++) {
            if (lines.get(i).length() != cols) {
                throw new IOException("All lines in the maze must have the same length. Line " + (i + 1) +
                        " has length " + lines.get(i).length() + " instead of " + cols + ".");
            }
        }

        maze = new char[rows][cols];
        teleportPositions.clear();
        penaltyPositions.clear();
        startPos = null;
        endPos = null;

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

        if (startPos == null || endPos == null) {
            throw new IOException("Maze must contain both a start (S) and an end (E) position.");
        }
    }

    public char[][] getMaze() {
        return maze;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Point getStartPos() {
        return startPos;
    }

    public Point getEndPos() {
        return endPos;
    }

    public List<Point> getTeleportPositions() {
        return teleportPositions;
    }

    public List<Point> getPenaltyPositions() {
        return penaltyPositions;
    }

    public boolean isValidMove(int x, int y, boolean[][] visited) {
        return x >= 0 && x < cols && y >= 0 && y < rows
                && maze[y][x] != WALL && !visited[y][x];
    }

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
