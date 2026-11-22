package maoinvisivel.market.shock;

import maoinvisivel.market.Market;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShockTest {
    private static final double TOL = 1e-9;

    private Market novoMercado() {
        return new Market(10.0, 0.5, 0.01);
    }

    @Test
    void impostoAumentaOPrecoSoNoTickDeDisparo() {
        Market mercado = novoMercado();
        TaxShock imposto = new TaxShock(3, 0.2);

        imposto.aplicar(mercado, 2);
        assertEquals(10.0, mercado.obterPreco(), TOL);

        imposto.aplicar(mercado, 3);
        assertEquals(12.0, mercado.obterPreco(), TOL);
    }

    @Test
    void escassezCortaAOfertaSoNoTickDeDisparo() {
        Market mercado = novoMercado();
        ScarcityShock escassez = new ScarcityShock(2, 0.5);

        mercado.aoIniciarTick(1);
        escassez.aplicar(mercado, 1);
        mercado.registrarOferta(10);
        mercado.aoFinalizarTick(1);
        assertEquals(10.0, mercado.obterUltimaOferta(), TOL);

        mercado.aoIniciarTick(2);
        escassez.aplicar(mercado, 2);
        mercado.registrarOferta(10);
        mercado.aoFinalizarTick(2);
        assertEquals(5.0, mercado.obterUltimaOferta(), TOL);
    }

    @Test
    void funcionaRegistradoNoMercado() {
        Market mercado = novoMercado();
        mercado.adicionarChoque(new TaxShock(1, 1.0));
        mercado.aoIniciarTick(1);
        assertEquals(20.0, mercado.obterPreco(), TOL);
    }

    @Test
    void rejeitaParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new TaxShock(1, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new ScarcityShock(1, 1.5));
    }
}
