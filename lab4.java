package com.company;
import mpi.MPI;
import mpi.MPIException;
import com.google.common.base.Stopwatch;

public class lab4 {
    public static void main(String[] args) throws MPIException {
        Stopwatch stopwatch = Stopwatch.createStarted(); // Создаем и запускаем секундомер

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int n1 = 10; // Количество строк в матрице A
        int n2 = 10; // Количество столбцов в матрице A и строк в матрице B
        int n3 = 10; // Количество столбцов в матрице B

        int[][] matrixA = new int[n1][n2];
        int[][] matrixB = new int[n2][n3];
        int[][] resultC = new int[n1][n3];

        // Инициализация матриц A и B
        if (rank == 0) {
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    matrixA[i][j] = i + j; // Здесь задайте значения элементов матрицы A
                }
            }

            for (int i = 0; i < n2; i++) {
                for (int j = 0; j < n3; j++) {
                    matrixB[i][j] = i + j; // Здесь задайте значения элементов матрицы B
                }
            }

            for (int i = 1; i < size; i++) {
                for (int row = 0; row < n1; row++) {
                    MPI.COMM_WORLD.Send(matrixA[row], 0, n2, MPI.INT, i, 0);
                }
                for (int row = 0; row < n2; row++) {
                    MPI.COMM_WORLD.Send(matrixB[row], 0, n3, MPI.INT, i, 0);
                }
            }
        } else {
            for (int row = 0; row < n1; row++) {
                int[] rowA = new int[n2];
                MPI.COMM_WORLD.Recv(rowA, 0, n2, MPI.INT, 0, 0);
                for (int j = 0; j < n2; j++) {
                    matrixA[row][j] = rowA[j];
                }
            }
            for (int row = 0; row < n2; row++) {
                int[] rowB = new int[n3];
                MPI.COMM_WORLD.Recv(rowB, 0, n3, MPI.INT, 0, 0);
                for (int j = 0; j < n3; j++) {
                    matrixB[row][j] = rowB[j];
                }
            }
        }

        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n3; j++) {
                resultC[i][j] = 0;
                for (int k = 0; k < n2; k++) {
                    resultC[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        if (rank != 0) {
            for (int row = 0; row < n1; row++) {
                MPI.COMM_WORLD.Send(resultC[row], 0, n3, MPI.INT, 0, 0);
            }
        } else {
            for (int i = 1; i < size; i++) {
                for (int row = 0; row < n1; row++) {
                    int[] receivedRow = new int[n3];
                    MPI.COMM_WORLD.Recv(receivedRow, 0, n3, MPI.INT, i, 0);
                    for (int j = 0; j < n3; j++) {
                        resultC[row][j] += receivedRow[j];
                    }
                }
            }

            System.out.println("Matrix A:");
            printMatrix(matrixA);
            System.out.println("Matrix B:");
            printMatrix(matrixB);
            System.out.println("Result: ");
            printScaledMatrix(resultC, 4);
        }

        MPI.Finalize();

        stopwatch.stop(); // Останавливаем секундомер
        long executionTime = stopwatch.elapsed().toMillis(); // Получаем время выполнения в миллисекундах
        System.out.println("Execution time: " + executionTime + " milliseconds");

        // Сохраняем время выполнения на каждом процессе в массив
        long[] executionTimes = new long[size];
        MPI.COMM_WORLD.Gather(new long[] { executionTime }, 0, 1, MPI.LONG, executionTimes, 0, 1, MPI.LONG, 0);

        // Выводим среднее время выполнения
        if (rank == 0) {
            long totalExecutionTime = 0;
            for (long time : executionTimes) {
                totalExecutionTime += time;
            }
            double averageExecutionTime = (double) totalExecutionTime / size;
            System.out.println("Average execution time: " + averageExecutionTime + " milliseconds");
        }
    }

    private static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static void printScaledMatrix(int[][] matrix, int scale) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] / scale + " "); // Делим каждый элемент на 4 перед выводом
            }
            System.out.println();
        }
    }
}
