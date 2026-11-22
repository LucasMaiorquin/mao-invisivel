package maoinvisivel;

import maoinvisivel.market.MarketConfig;
import maoinvisivel.market.MarketConfigBuilder;
import maoinvisivel.market.MarketScenario;
import maoinvisivel.market.MarketScenarioFactory;
import maoinvisivel.market.shock.ScarcityShock;
import maoinvisivel.market.shock.TaxShock;
import maoinvisivel.metrics.CsvExporter;
import maoinvisivel.metrics.MetricsCollector;
import maoinvisivel.metrics.MetricsSnapshot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Roda dois cenários (sem choque e com choques) e exporta as métricas em CSV. */
public class Main {
    private static final int TICKS = 150;
    private static final Path PASTA = Path.of("output");

    public static void main(String[] args) throws IOException {
        MarketConfig semChoque = new MarketConfigBuilder()
                .ticks(TICKS)
                .construir();

        MarketConfig comChoques = new MarketConfigBuilder()
                .ticks(TICKS)
                .adicionarChoque(new ScarcityShock(50, 0.4))
                .adicionarChoque(new TaxShock(100, 0.3))
                .construir();

        executar("sem_choque", semChoque);
        executar("com_choques", comChoques);
    }

    private static void executar(String nome, MarketConfig config) throws IOException {
        MarketScenario cenario = new MarketScenarioFactory().criar(config);
        MetricsCollector coletor = new MetricsCollector(cenario.mercado(), cenario.todosOsAgentes());
        cenario.simulacao().adicionarObservador(coletor);

        cenario.rodar();

        List<MetricsSnapshot> snapshots = coletor.obterSnapshots();
        Path destino = PASTA.resolve(nome + ".csv");
        new CsvExporter().exportar(snapshots, destino);

        MetricsSnapshot primeiro = snapshots.get(0);
        MetricsSnapshot ultimo = snapshots.get(snapshots.size() - 1);
        System.out.printf(Locale.ROOT,
                "[%s] %d ticks | preco %.2f -> %.2f | Gini %.3f -> %.3f | %s%n",
                nome, snapshots.size(), primeiro.preco(), ultimo.preco(),
                primeiro.gini(), ultimo.gini(), destino);
    }
}
