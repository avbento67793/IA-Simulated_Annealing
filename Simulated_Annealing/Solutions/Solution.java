package Solutions;

import distanceMatrix.DistanceMatrix;
import java.util.ArrayList;

public class Solution {

    private ArrayList<String> path;
    private int cost;

    public Solution(ArrayList<String> path) {
        this.path = new ArrayList<>(path); // copy to avoid modifying the original list
        this.cost = Integer.MAX_VALUE;
    }

    // Set the path cost
    public void setCost(int i) {
        this.cost = i;
    }

    // Returns the total cost of the path
    public int getCost() {
        return cost;
    }

    // Evaluates the total cost of the solution using the provided distance matrix
    public void evaluate(DistanceMatrix m) {
        int total = 0;
        // Sum distances between consecutive cities
        for (int i = 0; i < path.size() - 1; i++) {
            total += m.distance(path.get(i), path.get(i + 1));
        }
        // Close the TSP cycle by adding the distance from last to first city
        total += m.distance(path.get(path.size() - 1), path.get(0));
        this.cost = total; // store the total cost
    }

    // Returns the current sequence of cities in the path
    public ArrayList<String> getPath() {
        return path;
    }

    // Returns a string representation of the path (sequence of cities)
    @Override
    public String toString() {
        return path.toString();
    }
}
