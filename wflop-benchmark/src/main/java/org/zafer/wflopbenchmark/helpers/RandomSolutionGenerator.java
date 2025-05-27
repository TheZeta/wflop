package org.zafer.wflopbenchmark.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomSolutionGenerator {

    public static ArrayList<Integer> populateUniqueRandomListShuffle(int n, int M) {
        if (M <= 0 || n < 0) throw new IllegalArgumentException("Invalid input.");
        int maxVal = M * M;
        if (n > maxVal) throw new IllegalArgumentException("n cannot be greater than M*M.");

        List<Integer> allNumbers = new ArrayList<>(maxVal);
        for (int i = 0; i < maxVal; i++) {
            allNumbers.add(i);
        }
        Collections.shuffle(allNumbers);
        return new ArrayList<>(allNumbers.subList(0, n));
    }
}
