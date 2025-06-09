public class DeadEndFillingSolver extends MazeSolver {
    private BFSSolver bfsSolver;

    public DeadEndFillingSolver(Maze maze) {
        super(maze);
        this.algorithmName = "DeadEnd";
        this.bfsSolver = new BFSSolver(maze);
    }

    @Override
    public long solve(boolean visualize) {
        reset();
        long startTime = System.currentTimeMillis();

        char[][] mazeCopy = new char[maze.getRows()][maze.getCols()];
        for (int i = 0; i < maze.getRows(); i++) {
            System.arraycopy(maze.getMaze()[i], 0, mazeCopy[i], 0, maze.getCols());
        }

        boolean changed;
        do {
            changed = false;

            for (int y = 0; y < maze.getRows(); y++) {
                for (int x = 0; x < maze.getCols(); x++) {
                    if (mazeCopy[y][x] == Maze.PATH || mazeCopy[y][x] == Maze.PENALTY) {
                        int openNeighbors = countOpenNeighbors(mazeCopy, x, y);

                        if (isDeadEnd(openNeighbors, x, y)) {
                            mazeCopy[y][x] = Maze.WALL;
                            changed = true;

                            if (visualize) {
                                updateVisitedForVisualization(mazeCopy);
                            }
                        }
                    }
                }
            }
        } while (changed);

        // After filling dead ends, use BFS to find path
        long timeTaken = bfsSolver.solve(visualize);
        if (timeTaken != -1) {
            this.path = bfsSolver.getPath();
            this.visited = bfsSolver.getVisited();
            this.penaltySteps = bfsSolver.penaltySteps;

            // Count the number of filled cells for steps
            int filledCells = countFilledCells(mazeCopy);
            steps = bfsSolver.getSteps() + filledCells;
        }

        return timeTaken;
    }

    private int countOpenNeighbors(char[][] mazeCopy, int x, int y) {
        int openNeighbors = 0;
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && nx < maze.getCols() && ny >= 0 && ny < maze.getRows() &&
                    (mazeCopy[ny][nx] == Maze.PATH || mazeCopy[ny][nx] == Maze.PENALTY ||
                            mazeCopy[ny][nx] == Maze.START || mazeCopy[ny][nx] == Maze.END ||
                            mazeCopy[ny][nx] == Maze.TELEPORT)) {
                openNeighbors++;
            }
        }
        return openNeighbors;
    }

    private boolean isDeadEnd(int openNeighbors, int x, int y) {
        return openNeighbors == 1 &&
                !(x == maze.getStartPos().x && y == maze.getStartPos().y) &&
                !(x == maze.getEndPos().x && y == maze.getEndPos().y);
    }

    private void updateVisitedForVisualization(char[][] mazeCopy) {
        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                if (mazeCopy[i][j] == Maze.WALL && maze.getMaze()[i][j] != Maze.WALL) {
                    visited[i][j] = true;
                }
            }
        }
    }

    private int countFilledCells(char[][] mazeCopy) {
        int filledCells = 0;
        for (int y = 0; y < maze.getRows(); y++) {
            for (int x = 0; x < maze.getCols(); x++) {
                if (mazeCopy[y][x] == Maze.WALL && maze.getMaze()[y][x] != Maze.WALL) {
                    filledCells++;
                }
            }
        }
        return filledCells;
    }
}