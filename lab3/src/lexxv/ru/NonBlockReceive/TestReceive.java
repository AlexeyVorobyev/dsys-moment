package lexxv.ru.NonBlockReceive;

import mpi.*;

import java.util.Arrays;

public class TestReceive {

    static public void startTest(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank(); // Получаем номер процесса
        int size = MPI.COMM_WORLD.Size(); // Получаем общее количество процессов

        // Объявляем и инициализируем данные
        int[] data = new int[1];
        int[] buf = {1, 3, 5};
        int count, TAG = 0;
        Status st;

        if (rank == 0) {
            data[0] = 2016; // Значение для отправки от процесса 0
            MPI.COMM_WORLD.Send(data, 0, 1, MPI.INT, 2, TAG); // Отправляем процессу 2
            System.out.println("Process 0 send to 2");
        }
        else if (rank == 1) {
            MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, 2, TAG); // Отправляем буфер процессу 2
            System.out.println("Process 1 send to 2");
        }
        else if (rank == 2) {
            // Получаем данные от процесса 0
            st = MPI.COMM_WORLD.Probe(0, TAG); // Проверяем наличие данных от процесса 0
            count = st.Get_count(MPI.INT); // Получаем количество ожидаемых данных
            int[] back_buf = new int[count]; // Создаем буфер для получения данных
            MPI.COMM_WORLD.Recv(back_buf, 0, count, MPI.INT, 0, TAG); // Принимаем данные от процесса 0

            // Выводим данные
            System.out.print("Rank = 0: " + Arrays.toString(back_buf));
            System.out.println();

            // Получаем данные от процесса 1
            st = MPI.COMM_WORLD.Probe(1, TAG); // Проверяем наличие данных от процесса 1
            count = st.Get_count(MPI.INT); // Получаем количество ожидаемых данных
            int[] back_buf2 = new int[count]; // Создаем буфер для получения данных
            MPI.COMM_WORLD.Recv(back_buf2, 0, count, MPI.INT, 1, TAG); // Принимаем данные от процесса 1

            // Выводим данные
            System.out.print("Rank = 1: " + Arrays.toString(back_buf2));
            System.out.println();
        }

        MPI.Finalize(); // Завершаем MPI
    }
}
