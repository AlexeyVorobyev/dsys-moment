package lexxv.ru;
import mpi.*;

public class Main {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int message = rank;

        if (rank % 2 == 0 && rank < size - 1) {
            System.out.println("Sending message from rank: " + message);

            MPI.COMM_WORLD.Send(new int[]{message}, 0, 1, MPI.INT, rank + 1, 0);
        } else if (rank % 2 == 1) {
            int[] receivedMessage = new int[1];

            MPI.COMM_WORLD.Recv(receivedMessage, 0, 1, MPI.INT, rank - 1, 0);

            System.out.println("Receiving message by rank " + rank + " from rank " + (rank - 1) + "\nreceivedMessage: " + receivedMessage[0]);
        }

        MPI.Finalize();
    }
}
