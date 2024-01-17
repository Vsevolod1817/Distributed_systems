package com.company;
import mpi.MPI;

public class lab5 {

    static class Graph {
        private int vertices;
        private int[][] adjacencyMatrix;

        public Graph(int vertices, int[][] adjacencyMatrix) {
            this.vertices = vertices;
            this.adjacencyMatrix = adjacencyMatrix;
        }

        private boolean isCyclicUtil(int v, boolean[] visited, int parent) {
            visited[v] = true;
            for (int neighbor = 0; neighbor < vertices; neighbor++) {
                if (adjacencyMatrix[v][neighbor] == 1) {
                    if (!visited[neighbor]) {
                        if (isCyclicUtil(neighbor, visited, v)) {
                            return true;
                        }
                    } else if (neighbor != parent) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isCyclic(int startVertex) {
            boolean[] visited = new boolean[vertices];
            return isCyclicUtil(startVertex, visited, -1);
        }

        public boolean isConnected() {
            boolean[] visited = new boolean[vertices];
            dfs(0, visited);

            for (boolean vertexVisited : visited) {
                if (!vertexVisited) {
                    return false;
                }
            }
            return true;
        }

        private void dfs(int v, boolean[] visited) {
            visited[v] = true;
            for (int neighbor = 0; neighbor < vertices; neighbor++) {
                if (adjacencyMatrix[v][neighbor] == 1 && !visited[neighbor]) {
                    dfs(neighbor, visited);
                }
            }
        }
    }

    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Пример графа (не дерева)
        int[][] adjacencyMatrix = {
                {0, 1, 0, 1, 0},
                {1, 0, 1, 0, 1},
                {0, 1, 0, 0, 0},
                {1, 0, 0, 0, 1},
                {0, 1, 0, 1, 0}
        };

        int vertices = adjacencyMatrix.length;

        // Определите размеры подграфов для каждого процесса
        int subGraphSize = vertices / size;
        int start = rank * subGraphSize;
        int end = (rank + 1) * subGraphSize;

        // Измерение времени начинается
        long startTime = System.currentTimeMillis();

        boolean isCyclic = false;

        // Проверьте свою часть графа на ацикличность
        for (int i = start; i < end; i++) {
            Graph subGraph = new Graph(vertices, adjacencyMatrix);
            if (subGraph.isCyclic(i)) {
                isCyclic = true;
                break;
            }
        }

        // Используйте операцию MPI_Reduce для агрегации результатов
        boolean[] globalIsCyclic = new boolean[1];
        MPI.COMM_WORLD.Reduce(new boolean[]{isCyclic}, 0, globalIsCyclic, 0, 1, MPI.BOOLEAN, MPI.LAND, 0);

        // Процесс с рангом 0 выведет результат
        if (rank == 0) {
            boolean isConnected = !globalIsCyclic[0];
            if (isConnected) {
                System.out.println("Isn't tree.");
            } else {
                System.out.println("Is tree.");
            }

            // Измерение времени завершается
            long endTime = System.currentTimeMillis();
            System.out.println("Execution time: " + (endTime - startTime) + " milliseconds. ");
        }

        MPI.Finalize();
    }
}
