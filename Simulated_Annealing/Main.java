import distanceMatrix.DistanceMatrix;
import SA.SimulatedAnnealing;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("Choose a set of cities to use:");
        System.out.println("1 - Example E1");
        System.out.println("2 - Example E2");
        System.out.println("3 - Example E3");
        System.out.println("4 - All cities");
        System.out.print("Option: ");
        int optMatrix = in.nextInt();

        SimulatedAnnealing sa = new SimulatedAnnealing(getCitiesMatrix(optMatrix));

        // Ask user to choose weather he prefers to set the initial temperature or not
        System.out.println("\nDo you want to set an initial temperature manually? (y/n): ");
        String setT0 = in.next();

        if (setT0.equalsIgnoreCase("y")) {
            System.out.println("Enter the initial temperature (T0): ");
            double initialT0 = in.nextDouble();
            sa.setInitialTemperature(initialT0);
        }

        // Ask user to choose the temperature decay method
        System.out.println("\nChoose the temperature decay method:");
        System.out.println("geometric");
        System.out.println("linear");
        System.out.println("gradual");
        System.out.println("logarithmic");
        System.out.print("Option: ");
        String optTemperatureDecay = in.next();
        sa.setTemperatureDecayMethod(optTemperatureDecay);

        // Ask user to choose how to vary the number of iterations per temperature
        System.out.println("\nChoose the method for varying the number of iterations per temperature:");
        System.out.println("linear");
        System.out.println("exponential");
        System.out.println("random");
        System.out.println("constant");
        System.out.print("Option: ");
        String optIterPerTemp = in.next();
        sa.setIterVariationMethod(optIterPerTemp);

        // Simulated Annealing execution
        sa.run();
    }

    private static DistanceMatrix getCitiesMatrix(int optCities) {
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

        // Example E4 -> All Cities

        switch(optCities) {
            case 1:
                return mE1;// mE1;
            case 2:
                return mE2; // mE2;
            case 3:
                return mE3; // mE3
            case 4:
            default:
                return m; // case 4 = default = all cities = m
        }
    }
}
