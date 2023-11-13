package com.company;
import mpi.*;

public class lab2 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int[] buf = {rank};
        int[] s = new int[1];
        int TAG = 0;
        Request r;
        if (rank == 0)
        {
            MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.INT, (rank + 1) % size, TAG);
            r = MPI.COMM_WORLD.Irecv(s, 0, 1, MPI.INT, (rank - 1 + size) % size, TAG);
            r.Wait();
            System.out.println("Processor " + rank + " has finished with sum: " + s[0]);
        }
        else
        {
            r = MPI.COMM_WORLD.Irecv(s, 0, 1, MPI.INT, (rank - 1 + size) % size, TAG);
            r.Wait();
            buf[0] += s[0];
            MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.INT, (rank + 1) % size, TAG);

        }
        MPI.Finalize();
    }
}
