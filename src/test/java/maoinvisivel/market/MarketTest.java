package maoinvisivel.market;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarketTest {
    private static final double TOLERANCIA = 1e-9;

    private Market novoMercado() {
        return new Market(10.0, 0.5, 0.01);
    }

    private void rodarTick(Market mercado, long tick, double demanda, double oferta) {
        mercado.aoIniciarTick(tick);
        mercado.registrarDemanda(demanda);
        mercado.registrarOferta(oferta);
        mercado.aoFinalizarTick(tick);
    }

    @Test
    void demandaMaiorQueOfertaFazOPrecoSubir() {
        Market mercado = novoMercado();
        rodarTick(mercado, 1, 15, 10);
        assertEquals(12.5, mercado.obterPreco(), TOLERANCIA);
    }

    @Test 
    void ofertaMaiorQueDemandaFazOPrecoCair() {
        Market mercado = novoMercado();
        rodarTick(mercado, 1, 5, 10);
        assertEquals(7.5, mercado.obterPreco(), TOLERANCIA);
    }

    @Test
    void equilibrioMantemOPreco() {
        Market mercado = novoMercado();
        rodarTick(mercado, 1, 10, 10);
        assertEquals(10.0, mercado.obterPreco(), TOLERANCIA);
    }

    @Test
    void precoNuncaFicaAbaixoDoMinimo() {
        Market mercado = novoMercado();
        for (long tick = 1; tick <= 200; tick++) {
            rodarTick(mercado, tick, 0, 100);
        }
        assertTrue(mercado.obterPreco() >= 0.01);
    }

    @Test
    void semOfertaEComDemandaOPrecoSobe() {
        Market mercado = novoMercado();
        rodarTick(mercado, 1, 10, 0);
        assertTrue(mercado.obterPreco() > 10);
    }

    @Test 
    void semOfertaESemDemandaOPrecoFicaParado() {
        Market mercado = novoMercado();
        rodarTick(mercado, 1, 0, 0);
        assertEquals(10.0, mercado.obterPreco(), TOLERANCIA);
    }

    @Test
    void acumularesZeramACadaTick() {
        Market mercado = novoMercado();
        rodarTick(mercado, 1, 50, 50);
        rodarTick(mercado, 2, 1, 1);
        assertEquals(1.0, mercado.obterUltimaDemanda(), TOLERANCIA);
        assertEquals(1.0, mercado.obterUltimaOferta(), TOLERANCIA);
    }

    @Test
    void somaAsDemandasDeVariosAgentesNoMesmoTick() {
        Market mercado = novoMercado();
        mercado.aoIniciarTick(1);
        mercado.registrarDemanda(3);
        mercado.registrarDemanda(4);
        mercado.registrarOferta(7);
        mercado.aoFinalizarTick(1);
        assertEquals(7.0, mercado.obterUltimaDemanda(), TOLERANCIA);
    }

    @Test
    void reduzirOfertaCortaAOfertaRegistrada() {
        Market mercado = novoMercado();
        mercado.reduzirOferta(0.5);
        rodarTick(mercado, 1, 10, 10);
        assertEquals(5.0, mercado.obterUltimaOferta(), TOLERANCIA);
    }

    @Test
    void choqueEAplicadoNoInicioDoTick() {
        Market mercado = novoMercado();
        mercado.adicionarChoque((m, tick) -> {
            if (tick == 2) m.definirPreco(100.0);
        });
        mercado.aoIniciarTick(1);
        assertEquals(10.0, mercado.obterPreco(), TOLERANCIA);
        mercado.aoFinalizarTick(1);
        mercado.aoIniciarTick(2);
        assertEquals(100.0, mercado.obterPreco(), TOLERANCIA);
    }

    // ---------- Liquidação ----------

    private static class Registro implements Liquidavel {
        double quantidade = -1;
        double preco = -1;

        @Override
        public void aoLiquidar(double quantidadeExecutada, double preco) {
            this.quantidade = quantidadeExecutada;
            this.preco = preco;
        }
    }

    @Test
    void demandaMaiorQueOfertaRacionaOsCompradores() {
        Market mercado = novoMercado();
        Registro comprador1 = new Registro();
        Registro comprador2 = new Registro();
        Registro vendedor = new Registro();

        mercado.aoIniciarTick(1);
        mercado.registrarDemanda(10, comprador1);
        mercado.registrarDemanda(10, comprador2);
        mercado.registrarOferta(10, vendedor);
        mercado.aoFinalizarTick(1);

        // demanda 20, oferta 10: cada comprador executa 50%
        assertEquals(5.0, comprador1.quantidade, TOLERANCIA);
        assertEquals(5.0, comprador2.quantidade, TOLERANCIA);
        assertEquals(10.0, vendedor.quantidade, TOLERANCIA);
    }

    @Test
    void ofertaMaiorQueDemandaRacionaOsVendedores() {
        Market mercado = novoMercado();
        Registro comprador = new Registro();
        Registro vendedor1 = new Registro();
        Registro vendedor2 = new Registro();

        mercado.aoIniciarTick(1);
        mercado.registrarDemanda(10, comprador);
        mercado.registrarOferta(15, vendedor1);
        mercado.registrarOferta(15, vendedor2);
        mercado.aoFinalizarTick(1);

        assertEquals(10.0, comprador.quantidade, TOLERANCIA);
        assertEquals(5.0, vendedor1.quantidade, TOLERANCIA);
        assertEquals(5.0, vendedor2.quantidade, TOLERANCIA);
    }

    @Test
    void liquidaAoPrecoDoTickENaoAoPrecoNovo() {
        Market mercado = novoMercado();
        Registro comprador = new Registro();

        mercado.aoIniciarTick(1);
        mercado.registrarDemanda(15, comprador);
        mercado.registrarOferta(10, new Registro());
        mercado.aoFinalizarTick(1);

        assertEquals(10.0, comprador.preco, TOLERANCIA);
        assertEquals(12.5, mercado.obterPreco(), TOLERANCIA);
    }

    @Test
    void escassezReduzOQueOProdutorPodeVender() {
        Market mercado = novoMercado();
        mercado.reduzirOferta(0.5);
        Registro comprador = new Registro();
        Registro vendedor = new Registro();

        mercado.aoIniciarTick(1);
        mercado.registrarDemanda(10, comprador);
        mercado.registrarOferta(10, vendedor);
        mercado.aoFinalizarTick(1);

        // oferta efetiva = 5
        assertEquals(5.0, vendedor.quantidade, TOLERANCIA);
        assertEquals(5.0, comprador.quantidade, TOLERANCIA);
    }

    @Test
    void semOrdemDeUmDosLadosNaoNegociaNada() {
        Market mercado = novoMercado();
        Registro vendedor = new Registro();

        mercado.aoIniciarTick(1);
        mercado.registrarOferta(10, vendedor);
        mercado.aoFinalizarTick(1);

        assertEquals(0.0, vendedor.quantidade, TOLERANCIA);
    }

    @Test
    void rejeitaQuantidadeNegativa() {
        Market mercado = novoMercado();
        assertThrows(IllegalArgumentException.class, () -> mercado.registrarDemanda(-1));
        assertThrows(IllegalArgumentException.class, () -> mercado.registrarOferta(-1));
    }

    @Test
    void rejeitaConfiguracaoInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new Market(10, 0.5, 0));
        assertThrows(IllegalArgumentException.class, () -> new Market(0.001, 0.5, 0.01));
        assertThrows(IllegalArgumentException.class, () -> new Market(10, -1, 0.01));
    }
}
