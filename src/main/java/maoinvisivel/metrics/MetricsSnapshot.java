package maoinvisivel.metrics;

/** Foto das métricas ao final de um tick. */
public record MetricsSnapshot(long tick, double preco, double demanda, double oferta, double gini) {
}
