package maoinvisivel.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Índice de Gini: 0 = igualdade total, perto de 1 = uma pessoa concentra tudo. */
public final class GiniCalculator {

    private GiniCalculator() {
    }

    /**
     * Com os valores ordenados (x1 <= ... <= xn):
     * G = (2 * soma(i * xi)) / (n * soma(xi)) - (n + 1) / n
     * Lista vazia ou com soma zero devolve 0.
     */
    public static double calcular(List<Double> riquezas) {
        List<Double> ordenadas = new ArrayList<>(riquezas);
        for (double valor : ordenadas) {
            if (valor < 0) {
                throw new IllegalArgumentException("A riqueza não pode ser negativa: " + valor);
            }
        }
        int n = ordenadas.size();
        if (n == 0) {
            return 0.0;
        }
        Collections.sort(ordenadas);

        double soma = 0.0;
        double somaPonderada = 0.0;
        for (int i = 0; i < n; i++) {
            soma += ordenadas.get(i);
            somaPonderada += (i + 1) * ordenadas.get(i);
        }
        if (soma == 0.0) {
            return 0.0;
        }
        return (2.0 * somaPonderada) / (n * soma) - (n + 1.0) / n;
    }
}
