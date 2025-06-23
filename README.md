# MazeSolver

A comprehensive Java-based application designed for the visualization and analysis of maze-solving algorithms.  
This tool provides both educational and practical insights into classical and heuristic-based search techniques, offering support for advanced features such as teleportation and penalty zones.

---

## 📘 Project Overview

This project demonstrates the implementation of various pathfinding algorithms in a grid-based maze environment. The application allows users to visualize how each algorithm explores and navigates through the maze in real time.

Developed using Java and Swing, the tool supports loading custom maze files, visual feedback during execution, and detailed metrics for comparison between algorithms.

---

## 🧭 Supported Algorithms

| Algorithm              | Description                                                                 |
|------------------------|-----------------------------------------------------------------------------|
| **Breadth-First Search (BFS)**   | Explores all neighbor nodes level-by-level. Guarantees shortest path.       |
| **Depth-First Search (DFS)**     | Explores as far as possible along each branch. Not guaranteed optimal.      |
| **Dijkstra’s Algorithm**         | Calculates the shortest path based on minimal cumulative cost.              |
| **Dead-End Filling + BFS**       | Prunes all dead-end paths before solving with BFS. Efficient for mazes with traps. |
| **Wall-Follower (Left/Right)**  | Follows one wall edge continuously. Simple but not always complete.         |

Each algorithm is implemented in a modular structure and inherits from a shared abstract base class for consistency and extensibility.

---

## 🧩 Maze Elements

| Symbol | Element Type | Description                                                   |
|--------|---------------|---------------------------------------------------------------|
| `#`    | Wall          | Impassable obstacle                                           |
| ` `    | Path          | Standard walkable tile                                        |
| `S`    | Start         | Entry point of the maze                                       |
| `E`    | End           | Target exit point of the maze                                 |
| `T`    | Teleport      | Instantly transports to another teleport tile (paired logic)  |
| `P`    | Penalty       | Adds extra step cost upon entry                               |

---

## 🖥️ Application Features

- ✅ **Real-time visualization** with graphical interface using Java Swing  
- 📁 **Maze file import** from `.txt` format  
- 🎨 **Color-coded cells**: differentiating path types, visited nodes, and solution path  
- 🔄 **Algorithm comparison** by step count and execution time  
- ⚠️ **Penalty & teleport mechanics** integrated into solver logic  
- ⛔ **Dead-end pruning** for optimization before solving  

---

## 🧪 Project Structure

```
src/
├── Maze.java                   # Maze parser and structure
├── MazeSolver.java             # Abstract base class for all solvers
├── BFSSolver.java              # Breadth-First Search
├── DFSSolver.java              # Depth-First Search
├── DijkstraSolver.java         # Dijkstra’s algorithm
├── DeadEndFillingSolver.java   # Dead-end filling + BFS
├── WallFollowerSolver.java     # Left and Right-hand wall-following
├── MazePanel.java              # Visualization using Java Swing
└── Main.java                   # Entry point and GUI logic
```

---

## 📂 Maze File Format

- File must be a rectangular `.txt` file.
- Valid characters: `S`, `E`, `#`, ` `, `T`, `P`
- Example:
```
##########
#S     T #
# # ### ##
#P     # #
# ### ## #
#   #   E#
##########
```
- Ensure exactly one `S` (start) and one `E` (end) are present.

---

## 🖼️ Visualization Color Mapping

| Cell Type     | Color         |
|---------------|---------------|
| Wall          | Black         |
| Path          | White         |
| Start (S)     | Green         |
| End (E)       | Red           |
| Teleport (T)  | Cyan          |
| Penalty (P)   | Pink          |
| Visited Cell  | Gray (semi-transparent) |
| Final Path    | Algorithm-specific color (e.g., Blue, Orange, Teal) |

---

## 🛠️ How to Run

1. Open the project in a Java IDE (e.g., IntelliJ IDEA, Eclipse).
2. Ensure JDK 8+ is configured.
3. Place maze files in the project directory.
4. Run `Main.java`.
5. Use the GUI to:
   - Load a maze file.
   - Choose a solving algorithm.
   - Observe real-time visualization.

---

## 📊 Performance Metrics

Upon solving, the system displays:
- ✅ Execution Time (in milliseconds)
- ✅ Total Steps Taken
- ✅ Penalty Steps (if applicable)
- ✅ Visual path rendering

These metrics allow comparison between the different solving strategies.

---

## 🧠 Technical Highlights

- Modular solver architecture
- Abstract base class with shared logic (`MazeSolver`)
- Teleport and penalty support integrated into all algorithms
- Dead-end detection loop based on neighbor analysis
- Visualization layer decoupled from algorithm logic


---
## 🎥 Project Demonstration

You can watch the demonstration video of the maze-solving algorithms here:

🔗 [Watch Video](https://github.com/user-attachments/assets/1a8f477d-0722-4fb6-a354-6827fe85efe6)

https://github.com/user-attachments/assets/1a8f477d-0722-4fb6-a354-6827fe85efe6

---


## 📜 License

This project is licensed under the [MIT License](LICENSE).  
Feel free to use, modify, and distribute it as per the license terms.
