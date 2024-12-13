package lexxv.ru.matrixoperations;

import mpi.MPI;
import mpi.Request;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MatrixMultiplication {
    int[][] matrixA;
    int[][] matrixB;
    int[][] resultMatrix;

    int n1, n2, n3;

    /**
     * Конструктор для операции перемножения матриц
     * Матрица A имеет размер n1 x n2
     * Матрица B имеет размер n2 x n3
     * Результирующая матрица будет иметь размер n1 x n3
     *
     */
    public MatrixMultiplication(int n1, int n2, int n3) {
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
        matrixA = new int[n1][n2];
        matrixB = new int[n2][n3];
        resultMatrix = new int[n1][n3];
    }

    public void fillMatrixByRandom() {
        Random rand = new Random();
        fillMatrixByRandom(matrixA, rand);
        fillMatrixByRandom(matrixB, rand);
    }

    public void fillMatrix(int[][] matrixA, int[][] matrixB) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
    }

    public void multiply(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Учитываем случай, когда n1 не делится на количество процессов
        int rowsPerProc = (n1 + (size - 2)) / (size - 1);

        if (rank == 0) {
            System.out.println("Process 0: Broadcasting matrices to workers.");

            // Отправка строк матрицы A процессам
            for (int i = 1; i < size; i++) {
                for (int j = 0; j < rowsPerProc; j++) {
                    int row = (i - 1) * rowsPerProc + j;
                    if (row < n1) { // Отправляем только существующие строки
                        MPI.COMM_WORLD.Send(matrixA[row], 0, n2, MPI.INT, i, 0);
                        System.out.println("Process 0: Sent row " + row + " of matrix A to process " + i);
                    }
                }
            }

            // Отправка всей матрицы B всем процессам
            for (int i = 1; i < size; i++) {
                for (int rowIdx = 0; rowIdx < n2; rowIdx++) {
                    MPI.COMM_WORLD.Send(matrixB[rowIdx], 0, n3, MPI.INT, i, 1);
                }
                System.out.println("Process 0: Sent entire matrix B to process " + i);
            }
        } else {
            // Получение строк матрицы A
            int[][] localRows = new int[rowsPerProc][n2];
            for (int j = 0; j < rowsPerProc; j++) {
                int globalRow = (rank - 1) * rowsPerProc + j;
                if (globalRow < n1) { // Получаем только существующие строки
                    MPI.COMM_WORLD.Recv(localRows[j], 0, n2, MPI.INT, 0, 0);
                    System.out.println("Process " + rank + ": Received row " + globalRow + " of matrix A.");
                }
            }

            // Получение всей матрицы B
            int[][] localB = new int[n2][n3];
            for (int i = 0; i < n2; i++) {
                MPI.COMM_WORLD.Recv(localB[i], 0, n3, MPI.INT, 0, 1);
            }
            System.out.println("Process " + rank + ": Received entire matrix B.");

            // Умножение строк матрицы A на столбцы матрицы B
            int[][] localResult = new int[rowsPerProc][n3];
            for (int i = 0; i < rowsPerProc; i++) {
                int globalRow = (rank - 1) * rowsPerProc + i;
                if (globalRow < n1) { // Умножаем только существующие строки
                    for (int j = 0; j < n3; j++) {
                        localResult[i][j] = 0;
                        for (int k = 0; k < n2; k++) {
                            localResult[i][j] += localRows[i][k] * localB[k][j];
                        }
                    }
                }
            }
            System.out.println("Process " + rank + ": Computed local result.");

            // Отправка локальных результатов обратно процессу 0
            for (int i = 0; i < rowsPerProc; i++) {
                int globalRow = (rank - 1) * rowsPerProc + i;
                if (globalRow < n1) {
                    MPI.COMM_WORLD.Send(localResult[i], 0, n3, MPI.INT, 0, 2);
                    System.out.println("Process " + rank + ": Sent local result for row " + globalRow + " to process 0.");
                }
            }
        }

        // Сбор результатов на процессе 0
        if (rank == 0) {
            for (int i = 1; i < size; i++) {
                for (int j = 0; j < rowsPerProc; j++) {
                    int row = (i - 1) * rowsPerProc + j;
                    if (row < n1) { // Собираем только существующие строки
                        MPI.COMM_WORLD.Recv(resultMatrix[row], 0, n3, MPI.INT, i, 2);
                        System.out.println("Process 0: Received result for row " + row + " from process " + i);
                    }
                }
            }
        }

        MPI.Finalize();
    }


    public void syncMultiply(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int rowsPerProc = (n1 + (size - 2)) / (size - 1); // Учёт возможного остатка строк
        if (rank == 0) {
            System.out.println("Process 0: Broadcasting matrices to workers.");

            // Отправка строк матрицы A и всей матрицы B процессам
            for (int i = 1; i < size; i++) {
                for (int j = 0; j < rowsPerProc; j++) {
                    int row = (i - 1) * rowsPerProc + j;
                    if (row < n1) { // Отправляем только существующие строки
                        MPI.COMM_WORLD.Ssend(matrixA[row], 0, n2, MPI.INT, i, 0);
                        System.out.println("Process 0: Sent row " + row + " of matrix A to process " + i);
                    }
                }
                // Отправка всей матрицы B
                for (int rowIdx = 0; rowIdx < n2; rowIdx++) {
                    MPI.COMM_WORLD.Ssend(matrixB[rowIdx], 0, n3, MPI.INT, i, 1);
                }
                System.out.println("Process 0: Sent entire matrix B to process " + i);
            }
        } else {
            // Получение строк матрицы A
            int[][] localRows = new int[rowsPerProc][n2];
            for (int j = 0; j < rowsPerProc; j++) {
                int globalRow = (rank - 1) * rowsPerProc + j;
                if (globalRow < n1) { // Проверка, чтобы не выйти за пределы матрицы
                    MPI.COMM_WORLD.Recv(localRows[j], 0, n2, MPI.INT, 0, 0);
                    System.out.println("Process " + rank + ": Received row " + globalRow + " of matrix A.");
                }
            }

            // Получение всей матрицы B
            int[][] localB = new int[n2][n3];
            for (int i = 0; i < n2; i++) {
                MPI.COMM_WORLD.Recv(localB[i], 0, n3, MPI.INT, 0, 1);
            }
            System.out.println("Process " + rank + ": Received entire matrix B.");

            // Умножение строк матрицы A на столбцы матрицы B
            int[][] localResult = new int[rowsPerProc][n3];
            for (int i = 0; i < rowsPerProc; i++) {
                int globalRow = (rank - 1) * rowsPerProc + i;
                if (globalRow < n1) { // Вычисляем только существующие строки
                    for (int j = 0; j < n3; j++) {
                        localResult[i][j] = 0;
                        for (int k = 0; k < n2; k++) {
                            localResult[i][j] += localRows[i][k] * localB[k][j];
                        }
                    }
                }
            }
            System.out.println("Process " + rank + ": Computed local result.");

            // Отправка локальных результатов обратно процессу 0
            for (int i = 0; i < rowsPerProc; i++) {
                int globalRow = (rank - 1) * rowsPerProc + i;
                if (globalRow < n1) {
                    MPI.COMM_WORLD.Ssend(localResult[i], 0, n3, MPI.INT, 0, 2);
                    System.out.println("Process " + rank + ": Sent local result for row " + globalRow + " to process 0.");
                }
            }
        }

        // Сбор результатов на процессе 0
        if (rank == 0) {
            for (int i = 1; i < size; i++) {
                for (int j = 0; j < rowsPerProc; j++) {
                    int row = (i - 1) * rowsPerProc + j;
                    if (row < n1) { // Проверка на существование строки
                        MPI.COMM_WORLD.Recv(resultMatrix[row], 0, n3, MPI.INT, i, 2);
                        System.out.println("Process 0: Received result for row " + row + " from process " + i);
                    }
                }
            }
        }

        MPI.Finalize();
    }


    public void imultiply(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Учитываем остаточные строки
        int rowsPerProc = (n1 + (size - 2)) / (size - 1);

        if (rank == 0) {
            System.out.println("Process 0: Broadcasting matrices to workers.");

            // Неблокирующее отправление строк матрицы A
            for (int i = 1; i < size; i++) {
                for (int j = 0; j < rowsPerProc; j++) {
                    int row = (i - 1) * rowsPerProc + j;
                    if (row < n1) {
                        MPI.COMM_WORLD.Isend(matrixA[row], 0, n2, MPI.INT, i, 0);
                        System.out.println("Process 0: Sent row " + row + " of matrix A to process " + i);
                    }
                }
            }

            // Неблокирующее отправление всей матрицы B
            for (int i = 1; i < size; i++) {
                for (int rowIdx = 0; rowIdx < n2; rowIdx++) {
                    MPI.COMM_WORLD.Isend(matrixB[rowIdx], 0, n3, MPI.INT, i, 1);
                }
                System.out.println("Process 0: Sent entire matrix B to process " + i);
            }
        } else {
            // Неблокирующее получение строк матрицы A
            int[][] localRows = new int[rowsPerProc][n2];
            Request[] requestsA = new Request[rowsPerProc];
            for (int j = 0; j < rowsPerProc; j++) {
                int globalRow = (rank - 1) * rowsPerProc + j;
                if (globalRow < n1) {
                    requestsA[j] = MPI.COMM_WORLD.Irecv(localRows[j], 0, n2, MPI.INT, 0, 0);
                }
            }

            // Неблокирующее получение всей матрицы B
            int[][] localB = new int[n2][n3];
            Request[] requestsB = new Request[n2];
            for (int i = 0; i < n2; i++) {
                requestsB[i] = MPI.COMM_WORLD.Irecv(localB[i], 0, n3, MPI.INT, 0, 1);
            }

            // Ожидание завершения всех операций приема
            Request.Waitall(requestsA);
            Request.Waitall(requestsB);

            System.out.println("Process " + rank + ": Received all data.");

            // Умножение строк матрицы A на столбцы матрицы B
            int[][] localResult = new int[rowsPerProc][n3];
            for (int i = 0; i < rowsPerProc; i++) {
                int globalRow = (rank - 1) * rowsPerProc + i;
                if (globalRow < n1) {
                    for (int j = 0; j < n3; j++) {
                        localResult[i][j] = 0;
                        for (int k = 0; k < n2; k++) {
                            localResult[i][j] += localRows[i][k] * localB[k][j];
                        }
                    }
                }
            }
            System.out.println("Process " + rank + ": Computed local result.");

            // Неблокирующее отправление локального результата
            Request[] requestsResult = new Request[rowsPerProc];
            for (int i = 0; i < rowsPerProc; i++) {
                int globalRow = (rank - 1) * rowsPerProc + i;
                if (globalRow < n1) {
                    requestsResult[i] = MPI.COMM_WORLD.Isend(localResult[i], 0, n3, MPI.INT, 0, 2);
                    System.out.println("Process " + rank + ": Sent local result for row " + globalRow + " to process 0.");
                }
            }

            // Ожидание завершения отправки всех результатов
            Request.Waitall(requestsResult);
        }

        // Сбор результатов на процессе 0
        if (rank == 0) {
            Request[] requestsRecv = new Request[(size - 1) * rowsPerProc];
            int requestIndex = 0;
            for (int i = 1; i < size; i++) {
                for (int j = 0; j < rowsPerProc; j++) {
                    int row = (i - 1) * rowsPerProc + j;
                    if (row < n1) {
                        requestsRecv[requestIndex++] = MPI.COMM_WORLD.Irecv(resultMatrix[row], 0, n3, MPI.INT, i, 2);
                    }
                }
            }

            // Ожидание завершения всех операций приема результатов
            Request.Waitall(requestsRecv);
            System.out.println("Process 0: Collected all results.");
        }

        MPI.Finalize();
    }


    public void printResultMatrix() {
        System.out.println("Result matrix C:");
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n3; j++) {
                System.out.print(resultMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    static private void fillMatrixByRandom(int[] @NotNull [] matrix, Random rand) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = rand.nextInt(100); // случайные числа от 0 до 99
            }
        }
    }
}
