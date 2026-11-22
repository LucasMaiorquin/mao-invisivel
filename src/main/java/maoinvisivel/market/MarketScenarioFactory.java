package maoinvisivel.market;

import maoinvisivel.core.Simulation;
import maoinvisivel.market.agent.Consumer;
import maoinvisivel.market.agent.Producer;
import maoinvisivel.market.shock.Shock;
import maoinvisivel.market.strategy.BudgetShareStrategy;
import maoinvisivel.market.strategy.ConstantProduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Monta um cenário completo a partir de um MarketConfig. */
public class MarketScenarioFactory {

    public MarketScenario criar(MarketConfig config) {
        Objects.requireNonNull(config, "config");

        Market mercado = new Market(
                config.obterPrecoInicial(),
                config.obterSensibilidade(),
                config.obterPrecoMinimo());
        for (Shock choque : config.obterChoques()) {
            mercado.adicionarChoque(choque);
        }

        Simulation simulacao = new Simulation(mercado, new Random(config.obterSemente()));

        List<Consumer> consumidores = new ArrayList<>();
        for (int i = 1; i <= config.obterNumeroDeConsumidores(); i++) {
            Consumer consumidor = new Consumer(
                    "consumidor-" + i,
                    config.obterRiquezaInicialDoConsumidor(),
                    config.obterRendaDoConsumidor(),
                    mercado,
                    new BudgetShareStrategy(config.obterFracaoDeGasto()));
            consumidores.add(consumidor);
            simulacao.adicionarAgente(consumidor);
        }

        List<Producer> produtores = new ArrayList<>();
        for (int i = 1; i <= config.obterNumeroDeProdutores(); i++) {
            Producer produtor = new Producer(
                    "produtor-" + i,
                    config.obterRiquezaInicialDoProdutor(),
                    mercado,
                    new ConstantProduction(config.obterProducaoPorProdutor()));
            produtores.add(produtor);
            simulacao.adicionarAgente(produtor);
        }

        return new MarketScenario(simulacao, mercado, consumidores, produtores, config);
    }
}
