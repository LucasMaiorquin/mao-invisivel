package maoinvisivel.market.agent;

import maoinvisivel.market.Market;
import maoinvisivel.market.strategy.BudgetShareStrategy;
import maoinvisivel.market.strategy.ConstantProduction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentTest {
    private static final double TOL = 1e-9;

    private Market novoMercado() {
        return new Market(10.0, 0.5, 0.01);
    }

    @Test
    void consumidorRegistraDemandaEGastaORiqueza() {
        Market mercado = novoMercado();
        Consumer c = new Consumer("c1", 100, 0, mercado, new BudgetShareStrategy(0.5));

        mercado.aoIniciarTick(1);
        c.agir(1);
        mercado.aoFinalizarTick(1);

        // gasta 50 a preço 10 -> 5 unidades
        assertEquals(5.0, mercado.obterUltimaDemanda(), TOL);
        assertEquals(50.0, c.obterRiqueza(), TOL);
    }

    @Test
    void consumidorRecebeRendaAntesDeComprar() {
        Market mercado = novoMercado();
        Consumer c = new Consumer("c1", 0, 20, mercado, new BudgetShareStrategy(1.0));

        mercado.aoIniciarTick(1);
        c.agir(1);
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
    void produtorRegistraOfertaERecebeAReceita() {
        Market mercado = novoMercado();
        Producer p = new Producer("p1", 0, mercado, new ConstantProduction(4));

        mercado.aoIniciarTick(1);
        p.agir(1);
        mercado.aoFinalizarTick(1);

        assertEquals(4.0, mercado.obterUltimaOferta(), TOL);
        assertEquals(40.0, p.obterRiqueza(), TOL);
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
