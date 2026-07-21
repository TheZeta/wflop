package org.zafer.wflopalgorithms.common;

public class Helper {

    public static int[] discretize(double[] vector, int cellCount) {
        int[] layout = new int[vector.length];
        boolean[] occupied = new boolean[cellCount];

        for (int i = 0; i < vector.length; i++) {
            int cell = (int) Math.floor(vector[i]);
            cell = Math.clamp(cell, 0, cellCount - 1);

            while (occupied[cell]) {
                cell = (cell + 1) % cellCount;
            }

            occupied[cell] = true;
            layout[i] = cell;
        }

        return layout;
    }
}
