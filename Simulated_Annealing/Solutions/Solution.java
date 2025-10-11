package Solutions;

import distanceMatrix.DistanceMatrix;

import java.util.ArrayList;

public class Solution {
    private ArrayList<String> path;
    private int cost;

    public Solution(ArrayList<String> path) {
        this.path = new ArrayList<>(path);
        this.cost = Integer.MAX_VALUE; // inicial
    }

    public void evaluate(DistanceMatrix m) {
        int total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += m.distance(path.get(i), path.get(i + 1));
        }
        // fechar ciclo TSP
        total += m.distance(path.get(path.size() - 1), path.get(0));
        this.cost = total;
    }

    public int getCost() {
        return cost;
    }

    public ArrayList<String> getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
