package com.company;
import mpi.*;
import java.util.Random;
import java.util.Arrays;

public class lab3 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int TAG = 0;
        int centre = size / 2;
        Random rand = new Random();
        Request[] r;
        Status[] st = new Status[2];

        if (rank == 0) {
            st[0] = MPI.COMM_WORLD.Probe(centre, 1);
            st[1] = MPI.COMM_WORLD.Probe(size - 1, 2);
            int[] buf1 = new int[st[0].Get_count(MPI.INT)];
            int[] buf2 = new int[st[1].Get_count(MPI.INT)];
            int[] buf3 = new int[size - 3];
            r = new Request[2];
            r[0] = MPI.COMM_WORLD.Irecv(buf1, 0, buf1.length, MPI.INT, st[0].source, st[0].tag);
            r[1] = MPI.COMM_WORLD.Irecv(buf2, 0, buf2.length, MPI.INT, st[1].source, st[1].tag);
            Request.Waitall(r);
            System.arraycopy(buf1, 0, buf3, 0, buf1.length);
            System.arraycopy(buf2, 0, buf3, buf1.length, buf2.length);
            Arrays.sort(buf3);
            System.out.println("Result array: " + Arrays.toString(buf3));
        }
        else if (rank < centre) {
            int randomNumber = rand.nextInt(1000);
            MPI.COMM_WORLD.Isend(new int[]{randomNumber}, 0, 1, MPI.INT, centre, TAG);
        }
        else if (rank > centre && rank < size - 1) {
            int randomNumber = rand.nextInt(1000);
            MPI.COMM_WORLD.Isend(new int[]{randomNumber}, 0, 1, MPI.INT, size - 1, TAG);
        }
        else if (rank == centre) {
            int[] tempBuf = new int[centre - 1];
            r = new Request[tempBuf.length];
            for (int i = 0; i < tempBuf.length; i++) {
                r[i] = MPI.COMM_WORLD.Irecv(tempBuf, i, 1, MPI.INT, MPI.ANY_SOURCE, TAG);
            }
            Request.Waitall(r);

            int[] firstBuf = new int[tempBuf.length];
            System.arraycopy(tempBuf, 0, firstBuf, 0, tempBuf.length);
            Arrays.sort(firstBuf);
            MPI.COMM_WORLD.Isend(firstBuf, 0, firstBuf.length, MPI.INT, 0, 1);
        }
        else {
            int[] tempBuf = new int[size - centre - 2];
            r = new Request[tempBuf.length];
            for (int i = 0; i < tempBuf.length; i++) {
                r[i] = MPI.COMM_WORLD.Irecv(tempBuf, i, 1, MPI.INT, MPI.ANY_SOURCE, TAG);
            }
            Request.Waitall(r);

            int[] lastBuf = new int[tempBuf.length];
            System.arraycopy(tempBuf, 0, lastBuf, 0, tempBuf.length);
            Arrays.sort(lastBuf);
            MPI.COMM_WORLD.Isend(lastBuf, 0, lastBuf.length, MPI.INT, 0, 2);
        }
        MPI.Finalize();
    }
}