package maoinvisivel.market.agent;

import maoinvisivel.market.Market;
import maoinvisivel.market.strategy.BudgetShareStrategy;
import maoinvisivel.market.strategy.ConstantProduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentTest {
    private static final double TOL = 1e-9;

    private Market novoMercado() {
        return new Market(10.0, 0.5, 0.01);
    }

    @Test
    void consumidorRegistraDemandaEPagaPeloQueComprou() {
        Market mercado = novoMercado();
        Consumer c = new Consumer("c1", 100, 0, mercado, new BudgetShareStrategy(0.5));
        Producer p = new Producer("p1", 0, mercado, new ConstantProduction(100));

        mercado.aoIniciarTick(1);
        c.agir(1);
        p.agir(1);
        mercado.aoFinalizarTick(1);

        // quer gastar 50 a preço 10 -> 5 unidades; há oferta de sobra, então compra tudo
        assertEquals(5.0, mercado.obterUltimaDemanda(), TOL);
        assertEquals(50.0, c.obterRiqueza(), TOL);
        assertEquals(50.0, p.obterRiqueza(), TOL);
    }

    @Test
    void consumidorSemOfertaNaoPagaNada() {
        Market mercado = novoMercado();
        Consumer c = new Consumer("c1", 100, 0, mercado, new BudgetShareStrategy(0.5));

        mercado.aoIniciarTick(1);
        c.agir(1);
        mercado.aoFinalizarTick(1);

        assertEquals(5.0, mercado.obterUltimaDemanda(), TOL);
        assertEquals(100.0, c.obterRiqueza(), TOL);
    }

    @Test
    void consumidorRecebeRendaAntesDeComprar() {
        Market mercado = novoMercado();
        Consumer c = new Consumer("c1", 0, 20, mercado, new BudgetShareStrategy(1.0));
        Producer p = new Producer("p1", 0, mercado, new ConstantProduction(100));

        mercado.aoIniciarTick(1);
        c.agir(1);
        p.agir(1);
        mercado.aoFinalizarTick(1);

        assertEquals(2.0, mercado.obterUltimaDemanda(), TOL);
        assertEquals(0.0, c.obterRiqueza(), TOL);
    }

    @Test
    void consumidorSemDinheiroNaoCompra() {
        Market mercado = novoMercado();
        Consumer c = new Consumer("c1", 0, 0, mercado, new BudgetShareStrategy(0.5));

        mercado.aoIniciarTick(1);
        c.agir(1);
        mercado.aoFinalizarTick(1);

        assertEquals(0.0, mercado.obterUltimaDemanda(), TOL);
        assertEquals(0.0, c.obterRiqueza(), TOL);
    }

    @Test
    void produtorRegistraOfertaESoRecebePeloQueVendeu() {
        Market mercado = novoMercado();
        Producer p = new Producer("p1", 0, mercado, new ConstantProduction(4));

        mercado.aoIniciarTick(1);
        p.agir(1);
        mercado.aoFinalizarTick(1);

        // sem comprador, nada é vendido
        assertEquals(4.0, mercado.obterUltimaOferta(), TOL);
        assertEquals(0.0, p.obterRiqueza(), TOL);
    }

    @Test
    void produtorVendeTudoQuandoHaDemandaDeSobra() {
        Market mercado = novoMercado();
        Producer p = new Producer("p1", 0, mercado, new ConstantProduction(4));
        Consumer c = new Consumer("c1", 1000, 0, mercado, new BudgetShareStrategy(1.0));

        mercado.aoIniciarTick(1);
        c.agir(1);
        p.agir(1);
        mercado.aoFinalizarTick(1);

        // vendeu 4 unidades a preço 10
        assertEquals(40.0, p.obterRiqueza(), TOL);
        assertEquals(960.0, c.obterRiqueza(), TOL);
    }

    @Test
    void produtorPagaSalariosDivididosPorIgual() {
        Market mercado = novoMercado();
        Consumer c1 = new Consumer("c1", 0, 0, mercado, new BudgetShareStrategy(0.0));
        Consumer c2 = new Consumer("c2", 0, 0, mercado, new BudgetShareStrategy(0.0));
        Producer p = new Producer("p1", 100, mercado, new ConstantProduction(0),
                List.of(c1, c2), 0.5);

        mercado.aoIniciarTick(1);
        p.agir(1);
        mercado.aoFinalizarTick(1);

        assertEquals(50.0, p.obterRiqueza(), TOL);
        assertEquals(25.0, c1.obterRiqueza(), TOL);
        assertEquals(25.0, c2.obterRiqueza(), TOL);
    }

    @Test
    void produtorSemAssalariadosNaoPagaNada() {
        Market mercado = novoMercado();
        Producer p = new Producer("p1", 100, mercado, new ConstantProduction(0));

        mercado.aoIniciarTick(1);
        p.agir(1);
        mercado.aoFinalizarTick(1);

        assertEquals(100.0, p.obterRiqueza(), TOL);
    }

    @Test
    void produtorRejeitaFracaoDeSalariosInvalida() {
        Market mercado = novoMercado();
        assertThrows(IllegalArgumentException.class,
                () -> new Producer("p", 0, mercado, new ConstantProduction(1), List.of(), 1.5));
    }

    @Test
    void agenteTemIdentidade() {
        Producer p = new Producer("p1", 0, novoMercado(), new ConstantProduction(1));
        assertEquals("p1", p.obterId());
    }

    @Test
    void naoPermiteGastarMaisQueARiqueza() {
        Producer p = new Producer("p1", 10, novoMercado(), new ConstantProduction(1));
        assertThrows(IllegalArgumentException.class, () -> p.gastar(11));
    }

    @Test
    void rejeitaParametrosInvalidos() {
        Market mercado = novoMercado();
        assertThrows(IllegalArgumentException.class,
                () -> new Producer("p", -1, mercado, new ConstantProduction(1)));
        assertThrows(IllegalArgumentException.class,
                () -> new Consumer("c", 0, -1, mercado, new BudgetShareStrategy(0.5)));
        assertThrows(NullPointerException.class,
                () -> new Consumer("c", 0, 0, mercado, null));
    }
}
