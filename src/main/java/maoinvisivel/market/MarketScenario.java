package maoinvisivel.market;

import maoinvisivel.core.Simulation;
import maoinvisivel.market.agent.Consumer;
import maoinvisivel.market.agent.EconomicAgent;
import maoinvisivel.market.agent.Producer;

import java.util.ArrayList;
import java.util.List;

/** Um cenário pronto para rodar: simulação, mercado, agentes e a config que os gerou. */
public record MarketScenario(
        Simulation simulacao,
        Market mercado,
        List<Consumer> consumidores,
        List<Producer> produtores,
        MarketConfig config) {

    public List<EconomicAgent> todosOsAgentes() {
        List<EconomicAgent> todos = new ArrayList<>(consumidores);
        todos.addAll(produtores);
        return todos;
    }

    /** Roda o número de ticks definido na config. */
    public void rodar() {
        simulacao.rodar(config.obterTicks());
    }
}
