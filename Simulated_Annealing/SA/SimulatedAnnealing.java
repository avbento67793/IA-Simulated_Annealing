package SA;

import distanceMatrix.DistanceMatrix;
import Solutions.Solution;
import java.util.*;

public class SimulatedAnnealing {

    private final DistanceMatrix matrix;
    private final List<String> cities;

    // Parâmetros a usar
    private double T0;
    private double alpha;
    private double minTemp;
    private int iterPerTemp;
    private int maxIter;
    private String decayMethod;
    private String iterMethod;


    // Parâmetros a usar para o stop criterion
    private final static int NO_IMPROVEMENT_LIMIT = 5000;
    private final static double MIN_ACCEPTANCE_RATE = 0.01;

    // Random partilhado
    private final Random rng = new Random();

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
    }

    // Gera solução inicial (permutação aleatória)
    private Solution createInitialSolution() {
        ArrayList<String> sol = new ArrayList<>(this.cities);
        Collections.shuffle(sol, this.rng);
        Solution s = new Solution(sol);
        s.evaluate(this.matrix);
        return s;
    }

    // Ajuste automático de parâmetros
    private void autoAdjustParameters() {
        int n = this.cities.size();
        if (n < 2) { // proteção
            this.T0 = 1.0;
            this.alpha = 0.9;
            this.minTemp = 1e-3;
            this.iterPerTemp = 10;
            this.maxIter = 100;
            return;
        }

        double avgDist = averageDistance();
        this.T0 = Math.max(1.0, avgDist * 10.0);
        this.minTemp = this.T0 / 1000.0;

        if (n <= 8) this.alpha = 0.95;
        else if (n <= 15) this.alpha = 0.98;
        else this.alpha = 0.995;

        this.iterPerTemp = Math.max(100, n * 20);
        this.maxIter = Math.max(1000, n * 5000);

        System.out.println("=== Parâmetros ajustados automaticamente ===");
        System.out.printf("Cidades: %d | Dist. média: %.2f%n", n, avgDist);
        System.out.printf("T0 = %.2f | alpha = %.4f | minTemp = %.4f%n", this.T0, this.alpha, this.minTemp);
        System.out.printf("Iterações/Temp = %d | Máx Iterações = %d%n", this.iterPerTemp, this.maxIter);
    }

    // Calcula a média das distâncias
    private double averageDistance() {
        int n = this.cities.size();
        if (n < 2) return 0.0;
        double total = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                total += this.matrix.distance(this.cities.get(i), this.cities.get(j));
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
        int i = this.rng.nextInt(n - 1);              // i pode ser qualquer posição exceto a última
        int j = i + 1 + this.rng.nextInt(n - i - 1);  // j é sempre maior que i, garantindo um intervalo válido

        // Movimento 2-opt:
        // Inverte o subcaminho entre as posições i e j (inclusive)
        // Exemplo: [A, B, C, D, E, F] → se i = 1, j = 4 → [A, E, D, C, B, F]
        Collections.reverse(path.subList(i, j + 1));


        Solution newSol = new Solution(path); // Cria uma nova solução com este novo caminho (vizinho)
        newSol.evaluate(this.matrix);              // Calcula o custo total (distância percorrida na nova rota)
        return newSol;
    }

    // Define o decaimento de temperatura
    public void setDecayMethod(String decay) {
        this.decayMethod = decay.toLowerCase();
    }

    // Calcula uma nova temperatura segundo o tipo de decaimento escolhido
    private double decayTemperature(double T, int iteration, String method) {
        switch (method.toLowerCase()) {
            case "geometric":
                return T * this.alpha;

            case "linear":
                double beta = (this.T0 - this.minTemp) / this.maxIter;
                return Math.max(this.minTemp, T - beta);

            case "logarithmic":
                return this.T0 / Math.log(2 + iteration);

            case "gradual":
                double fraction = (double) iteration / this.maxIter;
                double adaptiveAlpha = 1.0 - 0.5 * fraction;
                return Math.max(this.minTemp, T * adaptiveAlpha);

            default:
                return T * this.alpha; // Decaimento geométrico por ser o mais comum
        }
    }

    // Define o tipo de iterações por temperatura
    public void setIterMethod(String iter) {
        this.iterMethod = iter.toLowerCase();
    }

    // Calcula o número de iterações por temperatura com base na opção escolhida
    private int varyIterationsPerTemp(int baseIter, int iteration, String method) {
        switch (method.toLowerCase()) {
            case "linear":
                // Aumenta progressivamente com o número de iterações
                return baseIter + (iteration / 1000);
            case "exponential":
                // Aumenta exponencialmente
                return (int) (baseIter * Math.pow(1.02, iteration / 5000.0));
            case "random":
                // Adiciona uma pequena flutuação aleatória
                return baseIter + this.rng.nextInt(Math.max(1, baseIter / 5));
            case "constant":
                return baseIter;
            default:
                // Mantém constante
                return baseIter;
        }
    }

    // Verifica se algum critério de paragem foi ativado
    private boolean stopCriterionMethod(double T, int iteration, int acceptedMoves, int totalMoves, int noImprovementCount) {
        if (T <= this.minTemp) {
            return true;
        } else if (iteration == this.maxIter) {
            return true;
        }  else if ((double) acceptedMoves / totalMoves < MIN_ACCEPTANCE_RATE) {
            return true;
        } else {
            return noImprovementCount > NO_IMPROVEMENT_LIMIT;
        }
    }

    public void run() {
        // Configuração automática dos parâmetros do SA
        autoAdjustParameters();

        // Inicialização de soluções
        Solution current = createInitialSolution();
        current.evaluate(this.matrix);
        Solution best = current;
        Solution worst = current;
        Solution first = current;
        Solution last = current;

        // Variáveis locais para guardar info sobre cada tipo de solução
        double firstTemp = this.T0, lastTemp = 0.0, bestTemp = 0.0, worstTemp = 0.0;
        int firstIter = 0, lastIter = 0, bestIter = 0, worstIter = 0;

        double T = this.T0;
        int iteration = 0;

        int acceptedMoves = 0;
        int totalMoves = 0;
        int noImprovementCount = 0;

        long start = System.currentTimeMillis();

        System.out.println("\n=== Início da execução do Simulated Annealing ===");
        System.out.printf("Método de decaimento da temperatura: %s%n", this.decayMethod);
        System.out.printf("Método de variação de iterações por temperatura: %s%n", this.iterMethod);
        System.out.println("------------------------------------------------------------");


        // Loop principal — enquanto não atingir critério de paragem
        while (!stopCriterionMethod(T, iteration, acceptedMoves, totalMoves, noImprovementCount)) {

            // Varia o número de iterações por temperatura, consoante o tipo escolhido
            int currentIterPerTemp = varyIterationsPerTemp(this.iterPerTemp, iteration, this.iterMethod);

            for (int k = 0; k < currentIterPerTemp && iteration < this.maxIter; k++) {
                Solution next = neighbor(current);
                int delta = next.getCost() - current.getCost();
                totalMoves++;

                // Critério de aceitação de Metropolis
                if (delta < 0 || this.rng.nextDouble() < Math.exp(-delta / T)) {
                    current = next;
                    acceptedMoves++;
                } else {
                    noImprovementCount++;
                }

                // Atualizar melhor/pior solução
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

            // Atualiza temperatura conforme o tipo de decaimento escolhido
            T = decayTemperature(T, iteration, this.decayMethod);

            // Guardar última solução
            last = current;
            lastTemp = T;
            lastIter = iteration;
        }

        long end = System.currentTimeMillis();

        // Apresentação dos resultados finais
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