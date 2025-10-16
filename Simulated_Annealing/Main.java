import distanceMatrix.DistanceMatrix;
import SA.SimulatedAnnealing;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Load the distance matrix file
        DistanceMatrix m = new DistanceMatrix("distancias.txt");

        // Example E1
        ArrayList<String> E1 = new ArrayList<>(Arrays.asList("Atroeira", "Douro", "Pinhal", "Teixoso", "Ulgueira", "Vilar"));
        DistanceMatrix mE1 = new DistanceMatrix(m, E1);

        // Example E2
        ArrayList<String> E2 = new ArrayList<>(Arrays.asList("Cerdeira", "Douro", "Gonta", "Infantado", "Lourel", "Nelas", "Oura", "Quebrada", "Roseiral", "Serra", "Teixoso", "Ulgueira"));
        DistanceMatrix mE2 = new DistanceMatrix(m, E2);

        // Example E3
        ArrayList<String> E3 = new ArrayList<>(Arrays.asList("Belmar", "Cerdeira", "Douro", "Encosta", "Freita", "Gonta", "Horta", "Infantado", "Lourel", "Monte", "Nelas", "Oura", "Pinhal", "Quebrada", "Roseiral", "Serra", "Teixoso", "Ulgueira"));
        DistanceMatrix mE3 = new DistanceMatrix(m, E3);

        Scanner in = new Scanner(System.in);

        // Ask user to choose the temperature decay method
        System.out.println("Choose the temperature decay method:");
        System.out.println("1 - Geometric");
        System.out.println("2 - Linear");
        System.out.println("3 - Gradual");
        System.out.println("4 - Logarithmic");
        System.out.print("Option: ");
        int optDecay = in.nextInt();

        String methodDecay = switch (optDecay) {
            case 1 -> "geometric";
            case 2 -> "linear";
            case 3 -> "gradual";
            case 4 -> "logarithmic";
            default -> "";
        };

        // Ask user to choose how to vary the number of iterations per temperature
        System.out.println("\nChoose the method for varying the number of iterations per temperature:");
        System.out.println("1 - Linear");
        System.out.println("2 - Exponential");
        System.out.println("3 - Random");
        System.out.println("4 - Constant");
        System.out.print("Option: ");
        int optIterPerTemp = in.nextInt();

        String methodIter = switch (optIterPerTemp) {
            case 1 -> "linear";
            case 2 -> "exponential";
            case 3 -> "random";
            case 4 -> "constant";
            default -> "";
        };

        // Create the Simulated Annealing object and configure methods
        SimulatedAnnealing sa = new SimulatedAnnealing(m);
        sa.setDecayMethod(methodDecay);
        sa.setIterMethod(methodIter);

        // Run the algorithm, measure total execution time, and display results
        long start = System.currentTimeMillis();
        sa.run();
        long end = System.currentTimeMillis();

        System.out.println("\nTotal Execution Time: " + (end - start) + " ms");
    }
}
