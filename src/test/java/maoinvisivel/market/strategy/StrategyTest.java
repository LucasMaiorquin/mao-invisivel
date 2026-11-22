package maoinvisivel.market.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyTest {
    private static final double TOL = 1e-9;

    @Test
    void budgetShareGastaAFracaoDoOrcamento() {
        // 50% de 100 = 50 de gasto, a preço 5 -> 10 unidades
        assertEquals(10.0, new BudgetShareStrategy(0.5).quantidadeDesejada(5, 100), TOL);
    }

    @Test
    void budgetShareSemOrcamentoOuPrecoInvalidoRetornaZero() {
        BudgetShareStrategy s = new BudgetShareStrategy(0.5);
        assertEquals(0.0, s.quantidadeDesejada(5, 0), TOL);
        assertEquals(0.0, s.quantidadeDesejada(0, 100), TOL);
    }

    @Test
    void sensivelAoPrecoCompraMenosQuandoOPrecoSobe() {
        PriceSensitiveStrategy s = new PriceSensitiveStrategy(10, 10, 1);
        assertEquals(10.0, s.quantidadeDesejada(10, 1000), TOL);
        assertEquals(5.0, s.quantidadeDesejada(20, 1000), TOL);
        assertTrue(s.quantidadeDesejada(5, 1000) > 10.0);
    }

    @Test
    void sensivelAoPrecoNaoPassaDoOrcamento() {
        PriceSensitiveStrategy s = new PriceSensitiveStrategy(10, 10, 1);
        // orçamento 20 a preço 10 só paga 2 unidades
        assertEquals(2.0, s.quantidadeDesejada(10, 20), TOL);
    }

    @Test
    void producaoConstanteIgnoraOPreco() {
        ConstantProduction p = new ConstantProduction(7);
        assertEquals(7.0, p.quantidadeAOfertar(1), TOL);
        assertEquals(7.0, p.quantidadeAOfertar(1000), TOL);
    }

    @Test
    void rejeitaParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new BudgetShareStrategy(1.5));
        assertThrows(IllegalArgumentException.class, () -> new PriceSensitiveStrategy(10, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new ConstantProduction(-1));
    }
}
