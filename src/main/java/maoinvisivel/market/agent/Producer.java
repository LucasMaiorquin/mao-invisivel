package maoinvisivel.market.agent;

import maoinvisivel.market.Market;
import maoinvisivel.market.strategy.ProductionStrategy;

import java.util.Objects;

/** Produtor: a cada tick decide quanto ofertar e recebe a receita ao preço atual. */
public class Producer extends EconomicAgent {
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

        mercado.registrarOferta(quantidade);
        receber(quantidade * preco);
    }
}
