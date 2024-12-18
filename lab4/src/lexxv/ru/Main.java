package lexxv.ru;

import lexxv.ru.matrixoperations.MatrixMultiplication;
import mpi.*;
import com.google.common.base.Stopwatch;

import java.lang.*;
import java.time.Duration;

public class Main {

    public static void main(String[] args) throws Exception, MPIException {
//        testCorrectMultiply(args);
        timeMultiply(args);
    }

    public static void timeMultiply(String[] args) {
        int[] sizes = new int[]{100, 1000, 2500, 5000};
        for (int size : sizes) {
            MatrixMultiplication mult = new MatrixMultiplication(size, size, size);
            mult.fillMatrixByRandom();
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                mult.syncMultiply(args);
            } finally {
                int rank = MPI.COMM_WORLD.Rank();
                if (rank == 0) {
                    Duration duration = stopwatch.elapsed();
                    System.out.println("Time: " + duration);
                }
            }
        }
    }

    public static void testCorrectMultiply(String[] args) {
        // убедиться, что работает
        MatrixMultiplication mult = new MatrixMultiplication(3, 3, 3);
        mult.fillMatrix(
                new int[][]{new int[]{1, 1, 12}, new int[]{2, 2, 2}, new int[]{3, 3, 3}},
                new int[][]{new int[]{2, 1, 3}, new int[]{3, 2, 3}, new int[]{3, 3, 2}}
        );
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            mult.multiply_bcast(args);
        } finally {
            int rank = MPI.COMM_WORLD.Rank();
            if (rank == 0) {
                mult.printResultMatrix();
                Duration duration = stopwatch.elapsed();
                System.out.println("Time: " + duration);
            }
        }
    }

}