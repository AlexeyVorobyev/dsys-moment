package lexxv.ru;
import mpi.*;

public class Main {
    public static void main(String[] args) throws Exception, MPIException {
//        entrypointBlock(args);

        entrypointUnblock(args);
    }

    public static void entrypointBlock(String[] args) throws MPIException {
        MPI.Init(args);

        Block block = new Block();
        block.taskBlockMessageCircle();

        MPI.Finalize();
    }

    public static void entrypointUnblock(String[] args) throws MPIException {
        MPI.Init(args);

        Unblock unblocK = new Unblock();
        unblocK.unblockTaskCircleMessage();

        MPI.Finalize();
    }
}
