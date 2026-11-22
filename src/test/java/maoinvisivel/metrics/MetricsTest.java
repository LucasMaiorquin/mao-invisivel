package maoinvisivel.metrics;

import maoinvisivel.market.MarketConfigBuilder;
import maoinvisivel.market.MarketScenario;
import maoinvisivel.market.MarketScenarioFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsTest {
    private static final double TOL = 1e-9;

    // ---------- Gini ----------

    @Test
    void igualdadeTotalDaZero() {
        assertEquals(0.0, GiniCalculator.calcular(List.of(1.0, 1.0, 1.0)), TOL);
    }

    @Test
    void concentracaoTotalChegaPertoDeUm() {
        // com n = 3 o máximo possível é (n-1)/n = 2/3
        assertEquals(2.0 / 3.0, GiniCalculator.calcular(List.of(0.0, 0.0, 10.0)), TOL);
    }

    @Test
    void valorConhecido() {
        assertEquals(0.25, GiniCalculator.calcular(List.of(1.0, 2.0, 3.0, 4.0)), TOL);
    }

    @Test
    void naoDependeDaOrdemDeEntrada() {
        assertEquals(
                GiniCalculator.calcular(List.of(1.0, 2.0, 3.0, 4.0)),
                GiniCalculator.calcular(List.of(4.0, 1.0, 3.0, 2.0)), TOL);
    }

    @Test
    void listaVaziaOuSomaZeroDaZero() {
        assertEquals(0.0, GiniCalculator.calcular(List.of()), TOL);
        assertEquals(0.0, GiniCalculator.calcular(List.of(0.0, 0.0)), TOL);
    }

    @Test
    void rejeitaRiquezaNegativa() {
        assertThrows(IllegalArgumentException.class, () -> GiniCalculator.calcular(List.of(1.0, -1.0)));
    }

    // ---------- Collector ----------

    private MarketScenario cenarioComColetor(int ticks, MetricsCollector[] saida) {
        MarketScenario cenario = new MarketScenarioFactory().criar(
                new MarketConfigBuilder().ticks(ticks).numeroDeConsumidores(10).numeroDeProdutores(2).construir());
        saida[0] = new MetricsCollector(cenario.mercado(), cenario.todosOsAgentes());
        cenario.simulacao().adicionarObservador(saida[0]);
        return cenario;
    }

    @Test
    void coletorGuardaUmSnapshotPorTick() {
        MetricsCollector[] coletor = new MetricsCollector[1];
        MarketScenario cenario = cenarioComColetor(10, coletor);
        cenario.rodar();

        List<MetricsSnapshot> snapshots = coletor[0].obterSnapshots();
        assertEquals(10, snapshots.size());
        assertEquals(1L, snapshots.get(0).tick());
        assertEquals(10L, snapshots.get(9).tick());
    }

    @Test
    void ultimoSnapshotBateComOEstadoFinalDoMercado() {
        MetricsCollector[] coletor = new MetricsCollector[1];
        MarketScenario cenario = cenarioComColetor(5, coletor);
        cenario.rodar();

        MetricsSnapshot ultimo = coletor[0].obterSnapshots().get(4);
        assertEquals(cenario.mercado().obterPreco(), ultimo.preco(), TOL);
        assertEquals(cenario.mercado().obterUltimaDemanda(), ultimo.demanda(), TOL);
        assertEquals(cenario.mercado().obterUltimaOferta(), ultimo.oferta(), TOL);
        assertTrue(ultimo.gini() >= 0 && ultimo.gini() < 1);
    }

    @Test
    void listaDeSnapshotsNaoPodeSerAlteradaDeFora() {
        MetricsCollector[] coletor = new MetricsCollector[1];
        cenarioComColetor(1, coletor);
        assertThrows(UnsupportedOperationException.class,
                () -> coletor[0].obterSnapshots().add(new MetricsSnapshot(1, 1, 1, 1, 0)));
    }

    // ---------- CSV ----------

    @Test
    void exportaCabecalhoELinhasComPontoDecimal(@TempDir Path pasta) throws IOException {
        Path arquivo = pasta.resolve("saida").resolve("metricas.csv");
        List<MetricsSnapshot> snapshots = List.of(
                new MetricsSnapshot(1, 10.5, 100.0, 90.0, 0.25),
                new MetricsSnapshot(2, 11.0, 95.5, 90.0, 0.3));

        new CsvExporter().exportar(snapshots, arquivo);

        List<String> linhas = Files.readAllLines(arquivo);
        assertEquals(3, linhas.size());
        assertEquals("tick,preco,demanda,oferta,gini", linhas.get(0));
        assertEquals("1,10.5000,100.0000,90.0000,0.2500", linhas.get(1));
        assertEquals("2,11.0000,95.5000,90.0000,0.3000", linhas.get(2));
    }
}
