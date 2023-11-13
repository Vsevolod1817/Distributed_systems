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

        System.out.println("Rank " + rank);
        
        System.out.println("Count of processors " + size);
        if (rank == 0)
        {
            MPI.COMM_WORLD.Send(buf, 0, 1, MPI.INT, (rank + 1) % size, TAG);
            MPI.COMM_WORLD.Recv(s, 0, 1, MPI.INT, (rank - 1 + size) % size, TAG);
            System.out.println("Processor " + rank + " has finished with sum: " + s[0]);
        }
        else
        {
            MPI.COMM_WORLD.Recv(s, 0, 1, MPI.INT, (rank - 1 + size) % size, TAG);
            buf[0] += s[0];
            MPI.COMM_WORLD.Send(buf, 0, 1, MPI.INT, (rank + 1) % size, TAG);

        }
        MPI.Finalize();
    }
}
