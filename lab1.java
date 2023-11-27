import mpi.*;

public class Main {
    public static void
    main(String[] args){
        MPI.Init(args);
        int myrank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int message = myrank;
        int TAG = 0;

        if (myrank % 2 == 0) {
            if (myrank + 1 != size) {
                MPI.COMM_WORLD.Send(new int[]{message}, 0, 1, MPI.INT, myrank + 1, TAG);
                System.out.println("aaaa");
            }
        } else {
            if (myrank != 0) {
                int rec[] = new int[1];
                Status status = MPI.COMM_WORLD.Recv(rec, 0, 1, MPI.INT, MPI.ANY_SOURCE, TAG);

                System.out.println(myrank+" from " + rec[0]);
            }
        }
        MPI.Finalize();
    }
}
