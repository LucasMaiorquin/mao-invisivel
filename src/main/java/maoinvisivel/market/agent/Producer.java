package maoinvisivel.market.agent;

import maoinvisivel.market.Liquidavel;
import maoinvisivel.market.Market;
import maoinvisivel.market.strategy.ProductionStrategy;

import java.util.Objects;

/** Produtor: a cada tick decide quanto ofertar e, na liquidação, recebe pelo que vendeu. */
public class Producer extends EconomicAgent implements Liquidavel {
    private final ProductionStrategy estrategia;

    public Producer(String id, double riquezaInicial, Market mercado, ProductionStrategy estrategia) {
        super(id, riquezaInicial, mercado);
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
    }

    @Override
    public void agir(long tick) {
        double preco = mercado.obterPreco();
        double quantidade = estrategia.quantidadeAOfertar(preco);
        if (quantidade <= 0) {
            return;
        }

        // A receita só chega na liquidação, pelo que foi realmente vendido.
        mercado.registrarOferta(quantidade, this);
    }

    @Override
    public void aoLiquidar(double quantidadeExecutada, double preco) {
        receber(quantidadeExecutada * preco);
    }
}
