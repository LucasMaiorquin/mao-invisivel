package maoinvisivel.market.agent;

import maoinvisivel.market.Market;
import maoinvisivel.market.strategy.PurchaseStrategy;

import java.util.Objects;

/** Consumidor: a cada tick recebe renda, decide quanto comprar e gasta. */
public class Consumer extends EconomicAgent {
    private final double renda;
    private final PurchaseStrategy estrategia;

    public Consumer(String id, double riquezaInicial, double renda,
                    Market mercado, PurchaseStrategy estrategia) {
        super(id, riquezaInicial, mercado);
        if (renda < 0) {
            throw new IllegalArgumentException("A renda deve ser >= 0: " + renda);
        }
        this.renda = renda;
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
    }

    @Override
    public void agir(long tick) {
        receber(renda);

        double preco = mercado.obterPreco();
        double quantidade = estrategia.quantidadeDesejada(preco, obterRiqueza());
        if (quantidade <= 0) {
            return;
        }

        mercado.registrarDemanda(quantidade);
        // min() evita estourar a riqueza por erro de arredondamento
        gastar(Math.min(quantidade * preco, obterRiqueza()));
    }
}
