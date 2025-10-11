
import distanceMatrix.DistanceMatrix;
import SA.SimulatedAnnealing;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        DistanceMatrix m = new DistanceMatrix("distancias.txt"); // ficheiro com matriz

        // Exemplo E1
        ArrayList<String> E1 = new ArrayList<>(Arrays.asList("Atroeira", "Douro", "Pinhal", "Teixoso", "Ulgueira", "Vilar"));
        DistanceMatrix mE1 = new DistanceMatrix(m, E1);

        // Exemplo E2
        ArrayList<String> E2 = new ArrayList<>(Arrays.asList("Cerdeira", "Douro", "Gonta", "Infantado", "Lourel", "Nelas", "Oura", "Quebrada", "Roseiral", "Serra", "Teixoso", "Ulgueira"));
        DistanceMatrix mE2 = new DistanceMatrix(m, E2);

        // Exemplo E3
        ArrayList<String> E3 = new ArrayList<>(Arrays.asList("Belmar", "Cerdeira", "Douro", "Encosta", "Freita", "Gonta", "Horta", "Infantado", "Lourel", "Monte", "Nelas", "Oura", "Pinhal", "Quebrada", "Roseiral", "Serra", "Teixoso", "Ulgueira"));
        DistanceMatrix mE3 = new DistanceMatrix(m, E3);

        Scanner in = new Scanner(System.in);

        System.out.println("Escolha o método de decaimento da temperatura:");
        System.out.println("1 - Geometric");
        System.out.println("2 - Linear");
        System.out.println("3 - Gradual");
        System.out.println("4 - Logarithmic");
        System.out.print("Opção: ");
        int opt = in.nextInt();

        String method = switch (opt) {
            case 1 -> "geometric";
            case 2 -> "linear";
            case 3 -> "gradual";
            case 4 -> "logarithmic";
            default -> "geometric";
        };

        SimulatedAnnealing sa = new SimulatedAnnealing(m);
        sa.setDecayMethod(method);

        // Executar, medir tempo total e mostrar resultados
        long start = System.currentTimeMillis();
        sa.run();
        long end = System.currentTimeMillis();
        System.out.println("Tempo total: " + (end - start) + " ms");
    }
}

