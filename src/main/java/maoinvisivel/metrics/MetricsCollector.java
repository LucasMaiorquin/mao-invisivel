package maoinvisivel.metrics;

import maoinvisivel.core.SimulationObserver;
import maoinvisivel.market.MarketView;
import maoinvisivel.market.agent.EconomicAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Observador que guarda um MetricsSnapshot ao final de cada tick. */
public class MetricsCollector implements SimulationObserver {
    private final MarketView mercado;
    private final List<? extends EconomicAgent> agentes;
    private final List<MetricsSnapshot> snapshots = new ArrayList<>();

    public MetricsCollector(MarketView mercado, List<? extends EconomicAgent> agentes) {
        this.mercado = Objects.requireNonNull(mercado, "mercado");
        this.agentes = Objects.requireNonNull(agentes, "agentes");
    }

    @Override
    public void aoFinalizarTick(long tick) {
        List<Double> riquezas = new ArrayList<>();
        for (EconomicAgent agente : agentes) {
            riquezas.add(agente.obterRiqueza());
        }
        snapshots.add(new MetricsSnapshot(
                tick,
                mercado.obterPreco(),
                mercado.obterUltimaDemanda(),
                mercado.obterUltimaOferta(),
                GiniCalculator.calcular(riquezas)));
    }

    public List<MetricsSnapshot> obterSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }
}
