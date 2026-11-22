package maoinvisivel.market.agent;

import maoinvisivel.market.Liquidavel;
import maoinvisivel.market.Market;
import maoinvisivel.market.strategy.ProductionStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Produtor: a cada tick paga salários, decide quanto ofertar e, na liquidação,
 * recebe pelo que vendeu.
 *
 * Os salários são uma fração da riqueza do produtor, dividida por igual entre
 * os assalariados. É isso que devolve o dinheiro dos produtores para a economia.
 */
public class Producer extends EconomicAgent implements Liquidavel {
    private final ProductionStrategy estrategia;
    private final List<? extends EconomicAgent> assalariados;
    private final double fracaoDeSalarios;

    /** Produtor que não paga salários. */
    public Producer(String id, double riquezaInicial, Market mercado, ProductionStrategy estrategia) {
        this(id, riquezaInicial, mercado, estrategia, List.of(), 0.0);
    }

    public Producer(String id, double riquezaInicial, Market mercado, ProductionStrategy estrategia,
                    List<? extends EconomicAgent> assalariados, double fracaoDeSalarios) {
        super(id, riquezaInicial, mercado);
        if (fracaoDeSalarios < 0 || fracaoDeSalarios > 1) {
            throw new IllegalArgumentException("A fração de salários deve estar entre 0 e 1: " + fracaoDeSalarios);
        }
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
        this.assalariados = List.copyOf(Objects.requireNonNull(assalariados, "assalariados"));
        this.fracaoDeSalarios = fracaoDeSalarios;
    }

    @Override
    public void agir(long tick) {
        pagarSalarios();

        double preco = mercado.obterPreco();
        double quantidade = estrategia.quantidadeAOfertar(preco);
        if (quantidade <= 0) {
            return;
        }

        // A receita só chega na liquidação, pelo que foi realmente vendido.
        mercado.registrarOferta(quantidade, this);
    }

    private void pagarSalarios() {
        if (assalariados.isEmpty() || fracaoDeSalarios == 0) {
            return;
        }
        double folha = fracaoDeSalarios * obterRiqueza();
        if (folha <= 0) {
            return;
        }
        gastar(folha);
        double parcela = folha / assalariados.size();
        for (EconomicAgent assalariado : assalariados) {
            assalariado.receber(parcela);
        }
    }

    @Override
    public void aoLiquidar(double quantidadeExecutada, double preco) {
        receber(quantidadeExecutada * preco);
    }
}
