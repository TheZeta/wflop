package org.zafer.wflopbenchmark.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomSolutionGenerator {

    public static ArrayList<Integer> populateUniqueRandomListShuffle(int numberOfTurbines, int cellCount) {
        if (cellCount <= 0 || numberOfTurbines < 0) {
            throw new IllegalArgumentException("Invalid input.");
        }

        if (numberOfTurbines > cellCount) {
            throw new IllegalArgumentException("Number of turbines cannot be greater than number of cells.");
        }

        List<Integer> allNumbers = new ArrayList<>(cellCount);
        for (int i = 0; i < cellCount; i++) {
            allNumbers.add(i);
        }
        Collections.shuffle(allNumbers);
        return new ArrayList<>(allNumbers.subList(0, numberOfTurbines));
    }
}
