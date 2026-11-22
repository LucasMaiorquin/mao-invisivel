# Invisible Hand (Mão Invisível)

🇬🇧 English · [🇧🇷 Português](README.pt-BR.md)

An **agent-based simulation engine** written in **Java 21**, with a market where prices emerge from supply and demand.

Object-oriented design (Strategy, Observer, Factory, Builder), configurable economic shocks, metrics such as the Gini index, and JUnit tests.

> **Note:** class names are in English, but method names and comments are in Portuguese (e.g. `agir`, `rodar`, `aoFinalizarTick`).

## How it works

The simulation advances in **ticks**. Each tick follows the same cycle:

1. **Tick start** – the market resets its order books and applies any scheduled shocks.
2. **Agents act** (in a shuffled order) – producers pay wages (a share of their wealth, split equally among consumers) and place sell orders; consumers place buy orders.
3. **Settlement** – only `min(demand, supply)` is traded, at the tick's price. The larger side is rationed proportionally, so each order executes the same fraction. Consumers pay and producers earn only for what was actually traded.
4. **Price update** – the price moves with the excess demand:
   `price = price × (1 + k × (demand − supply) / supply)`, with the excess clamped to `[-1, 1]` and a price floor above zero.
5. **Observers** run, e.g. the metrics collector takes a snapshot.

Given the same seed, a simulation always produces the same result.

### Shocks

- `TaxShock` – raises the price by a fraction at a given tick.
- `ScarcityShock` – cuts supply by a fraction from a given tick on.

### Known limitations

- Money is closed by default: consumers have no outside income, so total money is conserved (settlement moves it from consumers to producers, wages move it back). You can inject extra money with `rendaDoConsumidor(...)`.
- The wage share (`fracaoDeSalarios`, default 0.7) drives inequality. With `0`, producers never spend and money piles up in them (Gini above 0.8); at `0.7` the Gini stays around 0.35 and the price around 10; at `1.0` the Gini drops to about 0.25.
- Producers do not consume and consumers only earn wages, so this is still a simple two-class model.

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
[sem_choque]  150 ticks | preco 10.16 -> 10.37 | Gini 0.279 -> 0.350 | output/sem_choque.csv
[com_choques] 150 ticks | preco 10.16 -> 17.29 | Gini 0.279 -> 0.350 | output/com_choques.csv
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

All builder parameters have defaults, calibrated so the price stays around 10 and the Gini around 0.35 without shocks.

## Foundations and references

This is an educational model. Each piece below is labeled by how well it is grounded:
**Established** (standard in the literature), **Adapted** (a known idea, changed to fit this project) or **Own choice** (a modeling decision with no source behind it).

| Piece | Formula / rule | Basis | Status |
|---|---|---|---|
| Agent-based, tick-driven simulation | agents interact in discrete steps | Epstein & Axtell (1996); Tesfatsion & Judd (2006) | Established |
| Shuffled agent order each tick | `Collections.shuffle` with a seeded `Random` | Update order can change emergent results in individual-based models (Caron-Lormier et al., 2008), so a random, reproducible order avoids a fixed bias | Established idea |
| Price follows excess demand | `p ← p × (1 + k × (D − S) / S)` | The price rises when demand exceeds supply and falls otherwise: the price-adjustment ("tâtonnement") idea formalized by Samuelson (1941); a log-price version is in Farmer (2002) | **Adapted**: the multiplicative form, the division by `S`, the `[-1, 1]` clamp on the excess and the price floor are my own choices. `k = 0.1` is not calibrated to real data |
| Settlement: trade `min(D, S)` | traded = `min(D, S)`, the larger side is rationed | The "short side" of the market rules in disequilibrium theory, with rationing schemes in Drèze (1975) and Bénassy (1982) | Established concept. Proportional rationing is used here; I did not verify the exact passage in those works |
| Consumer: fixed share of the budget | `q = f × budget / p` | Cobb-Douglas demand: a constant share of income is spent on the good ([lecture notes](https://www.amherst.edu/system/files/media/1622/Cobb-Douglas%20Consumer%20Theory.pdf)) | Established |
| Consumer: price-sensitive demand | `q = q0 × (p0 / p)^ε`, capped by `budget / p` | Constant-elasticity (isoelastic) demand ([Wikipedia](https://en.wikipedia.org/wiki/Isoelastic_function)) | Established form. The budget cap is my own addition |
| Gini index | `G = 2·Σ(i·xᵢ) / (n·Σxᵢ) − (n+1)/n` (values sorted ascending, i from 1) | Gini (1912); sorted-data form from Damgaard & Weiner (2000), also in [MathWorld](https://mathworld.wolfram.com/GiniCoefficient.html) | Established. Note: with `n` agents the maximum is `(n−1)/n`, not 1 |
| Wages (producers pay a share of wealth to consumers) | `wage bill = fracaoDeSalarios × wealth`, split equally | A closed circuit of money in which every payment is someone's income is the idea behind stock-flow consistent models (Godley & Lavoie, 2007) | **Own choice**. The default `0.7` comes from a sweep of this simulation (price ≈ 10, Gini ≈ 0.35), not from data |
| `TaxShock` | `p ← p × (1 + t)` once | None. It is a simplified one-time price jump, not a model of tax incidence | **Own choice** |
| `ScarcityShock` | supply × `(1 − x)` from a tick on | None. A supply cut is a standard kind of shock, but the rule here is a simplification | **Own choice** |

### References

- Epstein, J. M. & Axtell, R. L. (1996). *Growing Artificial Societies: Social Science from the Bottom Up*. Brookings Institution Press / MIT Press. [Brookings](https://www.brookings.edu/books/growing-artificial-societies)
- Tesfatsion, L. & Judd, K. L. (eds.) (2006). *Handbook of Computational Economics, Vol. 2: Agent-Based Computational Economics*. Elsevier. [Elsevier](https://shop.elsevier.com/books/handbook-of-computational-economics/tesfatsion/978-0-444-51253-6)
- Samuelson, P. A. (1941). The Stability of Equilibrium: Comparative Statics and Dynamics. *Econometrica*, 9(2), 97–120. [Econometric Society](https://www.econometricsociety.org/publications/econometrica/browse/volume/1941)
- Farmer, J. D. (2002). Market force, ecology and evolution. *Industrial and Corporate Change*, 11(5), 895–953. [arXiv](https://arxiv.org/abs/adap-org/9812005)
- Bénassy, J.-P. (1982). *The Economics of Market Disequilibrium*. Academic Press.
- Drèze, J. H. (1975). Work on equilibrium under price rigidities and rationing (full title and pages not checked). Overview of this line of work: [Disequilibrium macroeconomics](https://en.wikipedia.org/wiki/Disequilibrium_macroeconomics)
- Caron-Lormier, G., Humphry, R. W., Bohan, D. A., Hawes, C. & Thorbek, P. (2008). Asynchronous and synchronous updating in individual-based models. *Ecological Modelling*, 212(3), 522–527. [DOI](https://doi.org/10.1016/j.ecolmodel.2007.10.049)
- Gini, C. (1912). *Variabilità e mutabilità*. Bologna.
- Damgaard, C. & Weiner, J. (2000). Describing inequality in plant size or fecundity. *Ecology*, 81(4), 1139–1142. [MathWorld entry that cites it](https://mathworld.wolfram.com/GiniCoefficient.html)
- Godley, W. & Lavoie, M. (2007). *Monetary Economics: An Integrated Approach to Credit, Money, Income, Production and Wealth*. Palgrave Macmillan. Survey: [Nikiforos & Zezza, Levy Institute WP 891](https://levyinstitute.org/wp-content/uploads/2024/02/wp_891.pdf)
- Gamma, E., Helm, R., Johnson, R. & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley (Strategy, Observer, Factory, Builder).

> The parameters (`k`, wage share, initial wealth, production) are **not calibrated to real data**. Results show how the mechanisms behave, not predictions about a real economy.

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
