package lexxv.ru.NonBlockReceive;

import mpi.*;

import java.util.Arrays;

public class FilteringAndProbe {
    public static void start(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int TAG = 0;

        if (size < 9) {
            if (rank == 0) {
                System.err.println("At least 9 processes are required to run.");
            }
            MPI.Finalize();
            return;
        }

        if (rank >= 1 && rank <= 3) {
            // Процессы 1-3 отправляют случайные числа в процесс 4
            int[] numbers = {rank * 10 + 1, rank * 10 + 2, rank * 10 + 3}; // Пример данных
            System.out.println("Rank " + rank + " send: " + Arrays.toString(numbers));
            for (int i = 0; i < 3; i++) {
                MPI.COMM_WORLD.Isend(new int[]{numbers[i]}, 0, 1, MPI.INT, 4, TAG);
            }
            System.out.println("Rank " + rank + " finished sending.");
        } else if (rank == 4) {
            // Процесс 4 принимает данные и сортирует
            int[] received = new int[9]; // 9 ожидаемых чисел
            int count = 0;

            for (int i = 1; i <= 3; i++) {
                count = receivedAndGetCount(TAG, received, count, i);
            }
            Arrays.sort(received, 0, count); // Сортируем полученные числа
            System.out.println("Rank 4 sorted and send: " + Arrays.toString(Arrays.copyOf(received, count)));
            MPI.COMM_WORLD.Isend(received, 0, count, MPI.INT, 0, TAG);
        } else if (rank >= 5 && rank <= 7) {
            // Процессы 5-7 отправляют случайные числа в процесс 8
            int[] numbers = {rank * 10 + 1, rank * 10 + 2}; // Пример данных
            System.out.println("Rank " + rank + " send: " + Arrays.toString(numbers));
            for (int i = 0; i < 2; i++) {
                MPI.COMM_WORLD.Isend(new int[]{numbers[i]}, 0, 1, MPI.INT, 8, TAG);
            }
            System.out.println("Rank " + rank + " finished sending.");
        } else if (rank == 8) {
            // Процесс 8 принимает данные и сортирует
            int[] received = new int[6]; // 6 ожидаемых чисел
            int count = 0;

            for (int i = 5; i <= 7; i++) {
                count = receivedAndGetCount(TAG, received, count, i);
            }
            Arrays.sort(received, 0, count); // Сортируем полученные числа
            System.out.println("Rank 8 sorted and send: " + Arrays.toString(Arrays.copyOf(received, count)));
            MPI.COMM_WORLD.Isend(received, 0, count, MPI.INT, 0, TAG);
        } else if (rank == 0) {
            // Процесс 0 принимает данные от 4 и 8 и сортирует их
            int[] finalReceived = new int[15]; // 15 ожидаемых чисел (9 от 4 и 6 от 8)
            int count = 0;

            // Получаем данные от процесса 4
            Status st = MPI.COMM_WORLD.Probe(4, TAG);
            int numCount = st.Get_count(MPI.INT);
            int[] buf4 = new int[numCount];
            MPI.COMM_WORLD.Recv(buf4, 0, numCount, MPI.INT, 4, TAG);
            System.arraycopy(buf4, 0, finalReceived, count, numCount);
            count += numCount;

            // Получаем данные от процесса 8
            st = MPI.COMM_WORLD.Probe(8, TAG);
            numCount = st.Get_count(MPI.INT);
            int[] buf8 = new int[numCount];
            MPI.COMM_WORLD.Recv(buf8, 0, numCount, MPI.INT, 8, TAG);
            System.arraycopy(buf8, 0, finalReceived, count, numCount);
            count += numCount;

            // Сортируем финальные данные
            Arrays.sort(finalReceived, 0, count);
            System.out.println("Rank 0 final result: " + Arrays.toString(Arrays.copyOf(finalReceived, count)));
        }

        MPI.Finalize(); // Завершение MPI
    }

    private static int receivedAndGetCount(int TAG, int[] received, int count, int i) {
        Status st = MPI.COMM_WORLD.Probe(i, TAG);
        int num = st.Get_count(MPI.INT);
        for (int j = 0; j < num; j++) {
            int[] buf = new int[1];
            MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.INT, i, TAG);
            received[count++] = buf[0];
        }
        return count;
    }
}
