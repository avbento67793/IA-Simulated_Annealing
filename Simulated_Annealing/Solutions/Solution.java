package Solutions;

import distanceMatrix.DistanceMatrix;
import java.util.ArrayList;

public class Solution {
    private ArrayList<String> path;
    private int cost;

    public Solution(ArrayList<String> path) {
        this.path = new ArrayList<>(path);
        this.cost = Integer.MAX_VALUE; // initial cost (very high value)
    }

    public void setCost(int i) {
        this.cost = i;
    }

    // Calculates the total cost of the solution
    public void evaluate(DistanceMatrix m) {
        int total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += m.distance(path.get(i), path.get(i + 1));
        }
        // Close the cycle (for the TSP: return to the starting city)
        total += m.distance(path.get(path.size() - 1), path.get(0));
        this.cost = total;
    }

    // Returns the total cost of the solution
    public int getCost() {
        return cost;
    }

    // Returns the path (sequence of cities)
    public ArrayList<String> getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
