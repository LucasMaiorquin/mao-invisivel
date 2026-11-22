# Invisible Hand (Mão Invisível)

🇬🇧 English · [🇧🇷 Português](README.pt-BR.md)

An **agent-based simulation engine** written in **Java 21**, with a market where prices emerge from supply and demand.

Object-oriented design (Strategy, Observer, Factory, Builder), configurable economic shocks, metrics such as the Gini index, and JUnit tests.

> **Note:** class names are in English, but method names and comments are in Portuguese (e.g. `agir`, `rodar`, `aoFinalizarTick`).

## How it works

The simulation advances in **ticks**. Each tick follows the same cycle:

1. **Tick start** – the market resets its order books and applies any scheduled shocks.
2. **Agents act** (in a shuffled order) – consumers receive income and place buy orders; producers place sell orders.
3. **Settlement** – only `min(demand, supply)` is traded, at the tick's price. The larger side is rationed proportionally, so each order executes the same fraction. Consumers pay and producers earn only for what was actually traded.
4. **Price update** – the price moves with the excess demand:
   `price = price × (1 + k × (demand − supply) / supply)`, with the excess clamped to `[-1, 1]` and a price floor above zero.
5. **Observers** run, e.g. the metrics collector takes a snapshot.

Given the same seed, a simulation always produces the same result.

### Shocks

- `TaxShock` – raises the price by a fraction at a given tick.
- `ScarcityShock` – cuts supply by a fraction from a given tick on.

### Known limitations

- Producers never spend money, so wealth accumulates in them and the Gini index rises over time (about 0.33 → 0.83 in the default scenario). Making producers spend (e.g. wages or trading among themselves) would be a model change.
- Total money is conserved by the settlement; the only money entering the system is the consumers' income.

## Project structure

```
src/main/java/maoinvisivel/
├── Main.java
├── core/      generic tick engine (Agent, Environment, Simulation, SimulationObserver)
├── market/    market, agents, strategies, shocks, config builder and factory
└── metrics/   metrics collection, Gini index and CSV export
src/test/java/maoinvisivel/   JUnit tests
docs/ClassDiagram.png         class diagram
```

## Getting started

Requirements: **JDK 21** and **Maven**.

```
mvn test                                    # run the tests
mvn compile                                 # compile the project
java -cp target/classes maoinvisivel.Main   # run the scenarios and write output/*.csv
```

`Main` runs two 150-tick scenarios, one without shocks and one with a scarcity shock (tick 50) and a tax shock (tick 100), and prints a summary:

```
[sem_choque]  150 ticks | preco 10.00 -> 10.00 | Gini 0.333 -> 0.827 | output/sem_choque.csv
[com_choques] 150 ticks | preco 10.00 -> 16.69 | Gini 0.333 -> 0.827 | output/com_choques.csv
```

The CSV files have the columns `tick,preco,demanda,oferta,gini` (price, demand, supply, Gini).

### Customizing a scenario

```java
MarketConfig config = new MarketConfigBuilder()
        .numeroDeConsumidores(200)
        .ticks(150)
        .semente(7L)
        .adicionarChoque(new ScarcityShock(50, 0.4))
        .construir();

MarketScenario cenario = new MarketScenarioFactory().criar(config);
MetricsCollector coletor = new MetricsCollector(cenario.mercado(), cenario.todosOsAgentes());
cenario.simulacao().adicionarObservador(coletor);
cenario.rodar();
```

All builder parameters have defaults, calibrated so the price stays around 10 without shocks.

## Design patterns

- **Strategy**: `PurchaseStrategy` and `ProductionStrategy`
- **Observer**: `SimulationObserver`
- **Factory**: `MarketScenarioFactory`
- **Builder**: `MarketConfigBuilder`

## Status

- [x] Engine core (`core`)
- [x] Market (`market`): price, agents, strategies, shocks, Builder and Factory
- [x] Metrics (`metrics`): snapshot, Gini, collector and CSV export
- [x] `Main` running a full scenario
- [x] End-of-tick settlement
- [x] Unit tests (JUnit)
