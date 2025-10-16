package SA;

import distanceMatrix.DistanceMatrix;
import Solutions.Solution;
import java.util.*;

public class SimulatedAnnealing {

    private final DistanceMatrix matrix;
    private final List<String> cities;

    // Parameters to use
    private double T0;
    private double alpha;
    private double minTemp;
    private int iterPerTemp;
    private int maxIter;
    private String decayMethod;
    private String iterMethod;

    // Temperature Decay Constants
    private final static double GRADUAL_BETA = 0.001;
    private final static int LOGARITHMIC_CONSTANT = 275;

    // Stop criteria parameters
    private final static int NO_IMPROVEMENT_LIMIT = 5000;
    private final static double MIN_ACCEPTANCE_RATE = 0.01;

    // Shared random generator
    private final Random rng;

    public SimulatedAnnealing(DistanceMatrix matrix) {
        this.matrix = matrix;
        this.cities = matrix.getCities();
        this.decayMethod = null;
        this.iterMethod = null;
        this.T0 = 0.0;
        this.alpha = 0.0;
        this.minTemp = 0.0;
        this.iterPerTemp = 0;
        this.maxIter = 0;
        this.rng = new Random();
    }

    // Generate initial solution (random permutation)
    private Solution createInitialSolution() {
        ArrayList<String> sol = new ArrayList<>(this.cities);
        Collections.shuffle(sol, this.rng);
        Solution s = new Solution(sol);
        s.evaluate(this.matrix);
        return s;
    }

    public void setInitialTemperature(double initialT0) {
        this.T0 = initialT0;
    }

    // Automatically adjust parameters based on the problem size
    private void autoAdjustParameters() {
        int n = this.cities.size();

        double avgDist = averageDistance();

        // Checks whether the initial temperature (T0) was provided by the user.
        // If not (T0 == 0.0), it is automatically calculated based on the average distance.
        if (this.T0 == 0.0) this.T0 = avgDist * 10.0;

        this.minTemp = this.T0 / 1000.0;

        if (n <= 7) this.alpha = 0.8;
        else if (n <= 14) this.alpha = 0.9;
        else this.alpha = 0.995;

        this.iterPerTemp = Math.max(100, n * 20);
        this.maxIter = Math.max(1000, n * 5000);

        System.out.println("\n==== Automatically Adjusted Parameters ====");
        System.out.printf("Cities: %d | Avg. Distance: %.2f%n", n, avgDist);
        System.out.printf("T0 = %.2f | alpha = %.4f | minTemp = %.4f%n", this.T0, this.alpha, this.minTemp);
        System.out.printf("Iterations/Temp = %d | Max Iterations = %d%n", this.iterPerTemp, this.maxIter);
    }

    // Calculates the average distance between all cities
    private double averageDistance() {
        int n = this.cities.size();
        if (n < 2) return 0.0;
        double total = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                total += this.matrix.distance(this.cities.get(i), this.cities.get(j));
                count++; // Count number of city pairs considered
            }
        }
        return total / count;
    }

    // Generates a neighbor solution using the 2-opt swap
    private Solution neighbor(Solution current) {
        // Copy current path to avoid modifying the original
        ArrayList<String> path = new ArrayList<>(current.getPath());
        int n = path.size();

        // Pick two random indices i < j
        int i = this.rng.nextInt(n - 1);
        int j = i + 1 + this.rng.nextInt(n - i - 1);

        // 2-opt move: reverse the segment between i and j (inclusive)
        // Example: [A, B, C, D, E, F] → if i = 1, j = 4 → [A, E, D, C, B, F]
        Collections.reverse(path.subList(i, j + 1));

        Solution newSol = new Solution(path);
        newSol.evaluate(this.matrix);
        return newSol;
    }

    // Set temperature decay method
    public void setTemperatureDecayMethod(String decay) {
        this.decayMethod = decay.toLowerCase();
    }

    // Compute new temperature based on selected decay type
    private double decayTemperature(double T, int iteration, String method) {
        switch (method.toLowerCase()) {
            case "linear":
                return T - this.minTemp;
            case "gradual":
                // Gradual cooling formula: Tk = Tk-1 / (1 + beta * Tk-1)
                return T / (1 + GRADUAL_BETA * T);
            case "logarithmic":
                // As decay only happens after initial iterations per temperature,
                // iteration will always be > 0, avoiding log(0)
                return this.T0 - LOGARITHMIC_CONSTANT * Math.log(iteration);
            case "geometric":
            default:
                return T * this.alpha; // Default = geometric decay
        }
    }

    // Set iteration variation method
    public void setIterVariationMethod(String iter) {
        this.iterMethod = iter.toLowerCase();
    }

    // Compute number of iterations per temperature based on chosen method
    private int varyIterationsPerTemp(int baseIter, int iteration, String method) {
        switch (method.toLowerCase()) {
            case "linear":
                return baseIter + (iteration / 1000);
            case "exponential":
                return (int) (baseIter * Math.pow(1.5, iteration / 5000.0));
            case "random":
                return baseIter + this.rng.nextInt(Math.max(1, baseIter / 5));
            case "constant":
            default:
                return baseIter;
        }
    }

    // Check if any stop criterion has been met
    private boolean stopCriterionMethod(double T, int iteration, int acceptedMoves, int totalMoves, int noImprovementCount) {
        double acceptance_rate = (double) acceptedMoves / totalMoves;
        if (T <= this.minTemp) {
            System.out.println("\n===== STOP CRITERION =====");
            System.out.printf("Minimum Temperature Reached: %-12.3f%n", T);
            return true;
        } else if (iteration == this.maxIter) {
            System.out.println("\n===== STOP CRITERION =====");
            System.out.println("Maximum Iteration Reached: " + iteration);
            return true;
        } else if (acceptance_rate < MIN_ACCEPTANCE_RATE) {
            System.out.println("\n===== STOP CRITERION =====");
            System.out.println("Accepted Moves: " + acceptedMoves);
            System.out.println("Total Moves: " + totalMoves);
            System.out.printf("Acceptance Rate: %-12.3f%n", acceptance_rate);
            return true;
        } else if (noImprovementCount > NO_IMPROVEMENT_LIMIT) {
            System.out.println("\n===== STOP CRITERION =====");
            System.out.println("No Improvement Count: " + noImprovementCount);
            return true;
        }
        return false;
    }

    // Main loop: Simulated Annealing
    public void run() {
        // Auto configuration
        if (this.cities.size() < 2) {
            System.out.println("There must be at least 2 cities.");
            return;
        }
        autoAdjustParameters();

        // Initialize solutions
        Solution current = createInitialSolution();
        current.evaluate(this.matrix);
        Solution best = current;
        Solution worst = current;
        Solution first = current;
        Solution last = current;

        // Variables to track each type of solution
        double firstTemp = this.T0, lastTemp = 0.0, bestTemp = 0.0, worstTemp = 0.0;
        int firstIter = 0, lastIter = 0, bestIter = 0, worstIter = 0;

        double T = this.T0;
        int iteration = 0;

        int acceptedMoves = 0;
        int totalMoves = 0;
        int noImprovementCount = 0;

        // Variable to check if a stopping criteria occurred inside the loop
        boolean exit = false;

        long start = System.currentTimeMillis();

        System.out.println("\n==== Starting Simulated Annealing ====");
        System.out.printf("Temperature Decay Method: %s%n", this.decayMethod);
        System.out.printf("Iteration Variation Method: %s%n", this.iterMethod);
        System.out.println("------------------------------------------------------------\n");
        System.out.println("Initial Iterations per Temperature: " + this.iterPerTemp);

        // Main loop
        while (!stopCriterionMethod(T, iteration, acceptedMoves, totalMoves, noImprovementCount)) {

            for (int k = 0; k < this.iterPerTemp; k++) {
                if (stopCriterionMethod(T, iteration, acceptedMoves, totalMoves, noImprovementCount)) {
                    exit = true;
                    break;
                }
                Solution next = neighbor(current);
                int delta = next.getCost() - current.getCost();
                totalMoves++;

                // Acceptance criterion
                if (delta < 0 || this.rng.nextDouble() < Math.exp(-delta / T)) {
                    current = next;
                    acceptedMoves++;
                } else {
                    noImprovementCount++;
                }

                // Update best/worst
                if (current.getCost() < best.getCost()) {
                    best = current;
                    bestTemp = T;
                    bestIter = iteration;
                }
                if (current.getCost() > worst.getCost()) {
                    worst = current;
                    worstTemp = T;
                    worstIter = iteration;
                }

                iteration++;
            }

            // Update last solution
            last = current;
            lastTemp = T;
            lastIter = iteration;

            if (exit) break;

            // Update number of iterations per temperature
            this.iterPerTemp = varyIterationsPerTemp(this.iterPerTemp, iteration, this.iterMethod);
            System.out.println("Current Iterations per Temperature: " + this.iterPerTemp);

            // Update temperature
            T = decayTemperature(T, iteration, this.decayMethod);
        }

        long end = System.currentTimeMillis();

        // Display results
        System.out.println("\n===== RESULTS =====");
        System.out.printf("%-18s %-55s %-12s %-12s %-12s%n",
                "Solution Type:", "Path", "Cost (Km)", "Iteration", "Temperature");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-18s %-55s %-10d %-12d %-12.2f%n",
                "First Solution:", first.getPath(), first.getCost(), firstIter, firstTemp);
        System.out.printf("%-18s %-55s %-10d %-12d %-12.2f%n",
                "Last Solution:", last.getPath(), last.getCost(), lastIter, lastTemp);
        System.out.printf("%-18s %-55s %-10d %-12d %-12.2f%n",
                "Best Solution:", best.getPath(), best.getCost(), bestIter, bestTemp);
        System.out.printf("%-18s %-55s %-10d %-12d %-12.2f%n",
                "Worst Solution:", worst.getPath(), worst.getCost(), worstIter, worstTemp);

        System.out.println("\nTotal Iterations: " + iteration);
        System.out.println("Execution Time: " + (end - start) + " ms");
    }
}
