package maoinvisivel.market;

import maoinvisivel.market.shock.ScarcityShock;
import maoinvisivel.market.shock.TaxShock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketScenarioTest {
    private final MarketScenarioFactory fabrica = new MarketScenarioFactory();

    // ---------- Builder ----------

    @Test
    void builderSemParametrosDevolveConfigValida() {
        MarketConfig config = new MarketConfigBuilder().construir();
        assertEquals(100, config.obterNumeroDeConsumidores());
        assertEquals(20, config.obterNumeroDeProdutores());
        assertEquals(10.0, config.obterPrecoInicial());
    }

    @Test
    void builderAplicaOsValoresInformados() {
        MarketConfig config = new MarketConfigBuilder()
                .precoInicial(20).numeroDeConsumidores(7).ticks(30).semente(5L).construir();
        assertEquals(20.0, config.obterPrecoInicial());
        assertEquals(7, config.obterNumeroDeConsumidores());
        assertEquals(30, config.obterTicks());
        assertEquals(5L, config.obterSemente());
    }

    @Test
    void builderRejeitaValoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new MarketConfigBuilder().numeroDeConsumidores(0).construir());
        assertThrows(IllegalArgumentException.class, () -> new MarketConfigBuilder().numeroDeProdutores(0).construir());
        assertThrows(IllegalArgumentException.class, () -> new MarketConfigBuilder().fracaoDeGasto(1.5).construir());
        assertThrows(IllegalArgumentException.class, () -> new MarketConfigBuilder().ticks(-1).construir());
        assertThrows(IllegalArgumentException.class, () -> new MarketConfigBuilder().precoInicial(0.001).construir());
    }

    @Test
    void configNaoMudaDepoisDeConstruida() {
        MarketConfigBuilder builder = new MarketConfigBuilder();
        MarketConfig config = builder.construir();
        builder.numeroDeConsumidores(5).adicionarChoque(new TaxShock(1, 0.1));
        assertEquals(100, config.obterNumeroDeConsumidores());
        assertTrue(config.obterChoques().isEmpty());
    }

    // ---------- Factory ----------

    @Test
    void fabricaCriaAQuantidadeCertaDeAgentes() {
        MarketScenario cenario = fabrica.criar(
                new MarketConfigBuilder().numeroDeConsumidores(8).numeroDeProdutores(3).construir());
        assertEquals(8, cenario.consumidores().size());
        assertEquals(3, cenario.produtores().size());
        assertEquals(11, cenario.todosOsAgentes().size());
    }

    @Test
    void cenarioRodaOsTicksDaConfig() {
        MarketScenario cenario = fabrica.criar(new MarketConfigBuilder().ticks(10).construir());
        cenario.rodar();
        assertTrue(cenario.mercado().obterUltimaOferta() > 0);
        assertTrue(cenario.mercado().obterUltimaDemanda() > 0);
    }

    @Test
    void mesmaSementeGeraOMesmoResultado() {
        double a = precoFinal(7L);
        double b = precoFinal(7L);
        assertEquals(a, b);
    }

    private double precoFinal(long semente) {
        MarketScenario cenario = fabrica.criar(new MarketConfigBuilder().semente(semente).ticks(50).construir());
        cenario.rodar();
        return cenario.mercado().obterPreco();
    }

    @Test
    void cenarioPadraoFicaPertoDoEquilibrio() {
        MarketScenario cenario = fabrica.criar(new MarketConfigBuilder().ticks(200).construir());
        cenario.rodar();
        double preco = cenario.mercado().obterPreco();
        assertTrue(preco > 5 && preco < 20, "preço fora do esperado: " + preco);
    }

    @Test
    void escassezFazOPrecoFicarMaisAltoQueSemChoque() {
        double semChoque = precoFinalComChoque(false);
        double comChoque = precoFinalComChoque(true);
        assertTrue(comChoque > semChoque, comChoque + " deveria ser > " + semChoque);
    }

    private double precoFinalComChoque(boolean comChoque) {
        MarketConfigBuilder builder = new MarketConfigBuilder().ticks(100);
        if (comChoque) {
            builder.adicionarChoque(new ScarcityShock(20, 0.5));
        }
        MarketScenario cenario = fabrica.criar(builder.construir());
        cenario.rodar();
        return cenario.mercado().obterPreco();
    }
}
