import java.util.ArrayList;

public class Solution {

    private final ArrayList<Integer> turbineIndices = new ArrayList<>();

    public int[] getTurbineIndices() {
        return turbineIndices.stream().mapToInt(Integer::intValue).toArray();
    }
}
