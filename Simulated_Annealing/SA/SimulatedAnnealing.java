package SA;

import distanceMatrix.DistanceMatrix;
import Solutions.Solution;
import java.util.*;

public class SimulatedAnnealing {

    private DistanceMatrix matrix;
    private List<String> cities;

    // parâmetros a usar
    private double T0;
    private double alpha;
    private double minTemp;
    private int iterPerTemp;
    private int maxIter;

    // Random partilhado
    private final Random rng = new Random();

    public SimulatedAnnealing(DistanceMatrix matrix) {
        this.matrix = matrix;
        this.cities = matrix.getCities();
    }

    // Gera solução inicial (permutação aleatória)
    private Solution createInitialSolution() {
        ArrayList<String> sol = new ArrayList<>(cities);
        Collections.shuffle(sol, rng);
        Solution s = new Solution(sol);
        s.evaluate(matrix);
        return s;
    }

    // Ajuste automático de parâmetros
    private void autoAdjustParameters() {
        int n = cities.size();
        if (n < 2) { // proteção
            T0 = 1.0;
            alpha = 0.9;
            minTemp = 1e-3;
            iterPerTemp = 10;
            maxIter = 100;
            return;
        }

        double avgDist = averageDistance();
        T0 = Math.max(1.0, avgDist * 10.0);
        minTemp = T0 / 1000.0;

        if (n <= 8) alpha = 0.95;
        else if (n <= 15) alpha = 0.98;
        else alpha = 0.995;

        iterPerTemp = Math.max(100, n * 20);
        maxIter = Math.max(1000, n * 5000);

        System.out.println("=== Parâmetros ajustados automaticamente ===");
        System.out.printf("Cidades: %d | Dist. média: %.2f%n", n, avgDist);
        System.out.printf("T0 = %.2f | alpha = %.4f | minTemp = %.4f%n", T0, alpha, minTemp);
        System.out.printf("Iterações/Temp = %d | Máx Iterações = %d%n", iterPerTemp, maxIter);
    }

    // Calcula a média das distâncias
    private double averageDistance() {
        int n = cities.size();
        if (n < 2) return 0.0;
        double total = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                total += matrix.distance(cities.get(i), cities.get(j));
                count++;
            }
        }
        return total / Math.max(1, count);
    }

    // Gera uma solução vizinha usando 2-opt
    private Solution neighbor(Solution current) {
        // Copia-se o caminho atual (lista de cidades) para não se alterar o original
        ArrayList<String> path = new ArrayList<>(current.getPath());

        // Número de cidades no caminho
        int n = path.size();

        // Escolhemos dois índices aleatórios com i < j
        // Estes índices definem o segmento da rota que será invertido
        int i = rng.nextInt(n - 1);              // i pode ser qualquer posição exceto a última
        int j = i + 1 + rng.nextInt(n - i - 1);  // j é sempre maior que i, garantindo um intervalo válido

        // Movimento 2-opt:
        // Inverte o subcaminho entre as posições i e j (inclusive)
        // Exemplo: [A, B, C, D, E, F]  → se i = 1, j = 4 → [A, E, D, C, B, F]
        Collections.reverse(path.subList(i, j + 1));


        Solution newSol = new Solution(path); // Cria uma nova solução com este novo caminho (vizinho)
        newSol.evaluate(matrix);              // Calcula o custo total (distância percorrida na nova rota)
        return newSol;
    }

    public void run() {
        // 1) configura automaticamente parâmetros
        autoAdjustParameters();

        // 2) inicializa soluções
        Solution current = createInitialSolution();
        current.evaluate(matrix);
        Solution best = current;
        double bestTemp = 0.0;
        int bestIter = 0;
        Solution worst = current;
        double worstTemp = 0.0;
        int worstIter = 0;
        Solution first = current;
        double firstTemp = T0;
        int firstIter = 0;
        Solution last = current;
        double lastTemp = 0.0;
        int lastIter = 0;

        double T = T0;
        int iteration = 0;

        long start = System.currentTimeMillis();

        // 3) loop principal: arrefecer até minTemp ou atingir maxIter
        while (T > minTemp && iteration < maxIter) {
            // por cada temperatura, fazer iterPerTemp iterações (ou até maxIter)
            for (int k = 0; k < iterPerTemp && iteration < maxIter; k++) {
                Solution next = neighbor(current);
                int delta = next.getCost() - current.getCost();

                // Aceitação: melhor é sempre aceite; pior é aceite apenas com probabilidade exp(-delta/T)
                if (delta < 0 || rng.nextDouble() < Math.exp(-delta / T)) {
                    current = next;
                }

                // Atualiza a melhor solução
                if (current.getCost() < best.getCost()) {
                    best = current.cloneSolution();
                    best.evaluate(matrix);
                    bestTemp = T;
                    bestIter = iteration;
                }

                // Atualiza a pior solução
                if (current.getCost() > worst.getCost()) {
                    worst = current.cloneSolution();
                    worst.evaluate(matrix);
                    worstTemp = T;
                    worstIter = iteration;
                }

                iteration++;
            }

            // Decaimento geométrico da temperatura
            T *= alpha;

            // Guarda a última solução
            last = current.cloneSolution();
            last.evaluate(matrix);
            lastTemp = T;
            lastIter = iteration;

            if (iteration % 1000 == 0 || T <= minTemp || iteration >= maxIter) {
                System.out.printf("Iter = %d | T = %.6f | Melhor = %d | Pior = %d | Atual = %d%n",
                        iteration, T, best.getCost(), worst.getCost(), current.getCost());
            }
        }

        long end = System.currentTimeMillis();

        // Dar print nos resultados finais
        System.out.println("\n===== RESULTADOS =====");
        System.out.printf("%-18s %-55s %-12s %-12s %-12s%n",
                "Tipo de Solução:", "Caminho (Percurso)", "Custo (Km)", "Iteração", "Temperatura");
        System.out.println("---------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-18s %-55s %-10d %-12d %-12.3f%n",
                "Primeira solução:", first.getPath(), first.getCost(), firstIter, firstTemp);
        System.out.printf("%-18s %-55s %-10d %-12d %-12.3f%n",
                "Última solução:", last.getPath(), last.getCost(), lastIter, lastTemp);
        System.out.printf("%-18s %-55s %-10d %-12d %-12.3f%n",
                "Melhor solução:", best.getPath(), best.getCost(), bestIter, bestTemp);
        System.out.printf("%-18s %-55s %-10d %-12d %-12.3f%n",
                "Pior solução:", worst.getPath(), worst.getCost(), worstIter, worstTemp);
        System.out.println("\nIterações totais: " + iteration);
        System.out.println("Tempo execução: " + (end - start) + " ms");
    }
}