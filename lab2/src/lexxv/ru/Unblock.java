package lexxv.ru;

import mpi.MPI;
import mpi.MPIException;
import mpi.Request;

public class Unblock {
    /**
     * Неблокирующая отправка
     */
    public void unblockTaskCircleMessage() {
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int nextRank = (rank + 1 == size) ? 0 : rank + 1;
        int prevRank = (rank == 0) ? size - 1 : rank - 1;
        int msg[] = {rank};
        int tag = 0;
        if (rank == 0) {
            MPI.COMM_WORLD.Isend(msg, 0, 1, MPI.INT, nextRank, tag);
            System.out.printf("Process <%d> sends %d to <%d>%n", rank, msg[0], nextRank);
            Request r = MPI.COMM_WORLD.Irecv(msg, 0, 1, MPI.INT, prevRank, tag);
            r.Wait();
            System.out.printf("Process <%d> receives %d from <%d>%n", rank, msg[0],prevRank);
        } else {
            Request r = MPI.COMM_WORLD.Irecv(msg, 0, 1, MPI.INT, prevRank, tag);
            r.Wait();
            System.out.printf("Process <%d> receives %d from <%d>%n", rank, msg[0],prevRank);
            msg[0] += rank;
            MPI.COMM_WORLD.Isend(msg, 0, 1, MPI.INT, nextRank, tag);
            System.out.printf("Process <%d> sends %d to <%d>%n", rank, msg[0], nextRank);
        }
    }
}
