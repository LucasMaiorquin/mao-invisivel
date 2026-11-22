# Mão Invisível

[🇬🇧 English](README.md) · 🇧🇷 Português

Motor de **simulação baseada em agentes** em **Java 21**, com um mercado em que os preços surgem da oferta e da demanda.

Arquitetura orientada a objetos (Strategy, Observer, Factory, Builder), choques econômicos configuráveis, métricas como o índice de Gini e testes com JUnit.

> **Nota:** os nomes das classes estão em inglês, mas os nomes dos métodos e os comentários estão em português (por exemplo `agir`, `rodar`, `aoFinalizarTick`).

## Como funciona

A simulação avança em **ticks**. Cada tick segue o mesmo ciclo:

1. **Início do tick** – o mercado zera os livros de ordens e aplica os choques agendados.
2. **Agentes agem** (em ordem embaralhada) – produtores pagam salários (uma fração da riqueza, dividida por igual entre os consumidores) e fazem ordens de venda; consumidores fazem ordens de compra.
3. **Liquidação** – só se negocia `min(demanda, oferta)`, ao preço do tick. O lado maior é racionado proporcionalmente, então cada ordem executa a mesma fração. Consumidores pagam e produtores recebem apenas pelo que foi realmente negociado.
4. **Atualização do preço** – o preço se move com o excesso de demanda:
   `preço = preço × (1 + k × (demanda − oferta) / oferta)`, com o excesso limitado a `[-1, 1]` e um piso de preço maior que zero.
5. **Observadores** rodam, por exemplo o coletor de métricas tira uma foto do estado.

Com a mesma semente, a simulação sempre produz o mesmo resultado.

### Choques

- `TaxShock` – aumenta o preço em uma fração num determinado tick.
- `ScarcityShock` – corta a oferta em uma fração a partir de um determinado tick.

### Limitações conhecidas

- O dinheiro é fechado por padrão: consumidores não têm renda externa, então o dinheiro total é conservado (a liquidação leva dos consumidores para os produtores, e os salários trazem de volta). Dá para injetar dinheiro extra com `rendaDoConsumidor(...)`.
- A fração de salários (`fracaoDeSalarios`, padrão 0,7) controla a desigualdade. Com `0`, os produtores nunca gastam e o dinheiro se acumula neles (Gini acima de 0,8); com `0,7` o Gini fica perto de 0,35 e o preço perto de 10; com `1,0` o Gini cai para cerca de 0,25.
- Produtores não consomem e consumidores só ganham salários, então ainda é um modelo simples de duas classes.

## Estrutura do projeto

```
src/main/java/maoinvisivel/
├── Main.java
├── core/      motor genérico de ticks (Agent, Environment, Simulation, SimulationObserver)
├── market/    mercado, agentes, estratégias, choques, builder de config e factory
└── metrics/   coleta de métricas, índice de Gini e exportação CSV
src/test/java/maoinvisivel/   testes JUnit
docs/ClassDiagram.png         diagrama de classes
```

## Como rodar

Requisitos: **JDK 21** e **Maven**.

```
mvn test                                    # roda os testes
mvn compile                                 # compila o projeto
java -cp target/classes maoinvisivel.Main   # roda os cenários e grava output/*.csv
```

O `Main` roda dois cenários de 150 ticks, um sem choques e outro com um choque de escassez (tick 50) e um de imposto (tick 100), e imprime um resumo:

```
[sem_choque]  150 ticks | preco 10.16 -> 10.37 | Gini 0.279 -> 0.350 | output/sem_choque.csv
[com_choques] 150 ticks | preco 10.16 -> 17.29 | Gini 0.279 -> 0.350 | output/com_choques.csv
```

Os arquivos CSV têm as colunas `tick,preco,demanda,oferta,gini`.

### Personalizando um cenário

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

Todos os parâmetros do builder têm valores padrão, calibrados para o preço ficar perto de 10 e o Gini perto de 0,35 sem choques.

## Fundamentação e referências

Este é um modelo educacional. Cada peça abaixo é classificada pelo quanto tem de base:
**Estabelecida** (padrão na literatura), **Adaptada** (uma ideia conhecida, alterada para este projeto) ou **Escolha própria** (decisão de modelagem sem fonte por trás).

| Peça | Fórmula / regra | Base | Status |
|---|---|---|---|
| Simulação baseada em agentes, por ticks | agentes interagem em passos discretos | Epstein & Axtell (1996); Tesfatsion & Judd (2006) | Estabelecida |
| Ordem dos agentes embaralhada a cada tick | `Collections.shuffle` com um `Random` com semente | A ordem de atualização pode mudar os resultados emergentes em modelos baseados em indivíduos (Caron-Lormier et al., 2008); uma ordem aleatória e reproduzível evita um viés fixo | Ideia estabelecida |
| Preço segue o excesso de demanda | `p ← p × (1 + k × (D − S) / S)` | O preço sobe quando a demanda passa da oferta e cai no caso contrário: a ideia de ajuste de preços ("tâtonnement") formalizada por Samuelson (1941); há uma versão em log-preço em Farmer (2002) | **Adaptada**: a forma multiplicativa, a divisão por `S`, o limite `[-1, 1]` no excesso e o piso de preço são escolhas minhas. O `k = 0,1` não é calibrado com dados reais |
| Liquidação: negocia `min(D, S)` | negociado = `min(D, S)`, o lado maior é racionado | A regra do "lado curto" do mercado na teoria do desequilíbrio, com esquemas de racionamento em Drèze (1975) e Bénassy (1982) | Conceito estabelecido. Aqui se usa racionamento proporcional; não verifiquei a passagem exata nessas obras |
| Consumidor: fração fixa do orçamento | `q = f × orçamento / p` | Demanda Cobb-Douglas: uma fração constante da renda é gasta no bem ([notas de aula](https://www.amherst.edu/system/files/media/1622/Cobb-Douglas%20Consumer%20Theory.pdf)) | Estabelecida |
| Consumidor: demanda sensível ao preço | `q = q0 × (p0 / p)^ε`, limitada por `orçamento / p` | Demanda de elasticidade constante (isoelástica) ([Wikipedia](https://en.wikipedia.org/wiki/Isoelastic_function)) | Forma estabelecida. O limite pelo orçamento é acréscimo meu |
| Índice de Gini | `G = 2·Σ(i·xᵢ) / (n·Σxᵢ) − (n+1)/n` (valores em ordem crescente, i a partir de 1) | Gini (1912); forma para dados ordenados de Damgaard & Weiner (2000), também no [MathWorld](https://mathworld.wolfram.com/GiniCoefficient.html) | Estabelecida. Nota: com `n` agentes o máximo é `(n−1)/n`, não 1 |
| Salários (produtores pagam uma fração da riqueza aos consumidores) | `folha = fracaoDeSalarios × riqueza`, dividida por igual | Um circuito fechado de dinheiro em que todo pagamento é renda de alguém é a ideia por trás dos modelos estoque-fluxo consistentes (Godley & Lavoie, 2007) | **Escolha própria**. O padrão `0,7` vem de uma varredura desta simulação (preço ≈ 10, Gini ≈ 0,35), não de dados |
| `TaxShock` | `p ← p × (1 + t)` uma vez | Nenhuma. É um salto único e simplificado no preço, não um modelo de incidência de impostos | **Escolha própria** |
| `ScarcityShock` | oferta × `(1 − x)` a partir de um tick | Nenhuma. Um corte de oferta é um tipo padrão de choque, mas a regra aqui é uma simplificação | **Escolha própria** |

### Referências

- Epstein, J. M. & Axtell, R. L. (1996). *Growing Artificial Societies: Social Science from the Bottom Up*. Brookings Institution Press / MIT Press. [Brookings](https://www.brookings.edu/books/growing-artificial-societies)
- Tesfatsion, L. & Judd, K. L. (eds.) (2006). *Handbook of Computational Economics, Vol. 2: Agent-Based Computational Economics*. Elsevier. [Elsevier](https://shop.elsevier.com/books/handbook-of-computational-economics/tesfatsion/978-0-444-51253-6)
- Samuelson, P. A. (1941). The Stability of Equilibrium: Comparative Statics and Dynamics. *Econometrica*, 9(2), 97–120. [Econometric Society](https://www.econometricsociety.org/publications/econometrica/browse/volume/1941)
- Farmer, J. D. (2002). Market force, ecology and evolution. *Industrial and Corporate Change*, 11(5), 895–953. [arXiv](https://arxiv.org/abs/adap-org/9812005)
- Bénassy, J.-P. (1982). *The Economics of Market Disequilibrium*. Academic Press.
- Drèze, J. H. (1975). Trabalho sobre equilíbrio com preços rígidos e racionamento (título completo e páginas não conferidos). Visão geral dessa linha de pesquisa: [Disequilibrium macroeconomics](https://en.wikipedia.org/wiki/Disequilibrium_macroeconomics)
- Caron-Lormier, G., Humphry, R. W., Bohan, D. A., Hawes, C. & Thorbek, P. (2008). Asynchronous and synchronous updating in individual-based models. *Ecological Modelling*, 212(3), 522–527. [DOI](https://doi.org/10.1016/j.ecolmodel.2007.10.049)
- Gini, C. (1912). *Variabilità e mutabilità*. Bologna.
- Damgaard, C. & Weiner, J. (2000). Describing inequality in plant size or fecundity. *Ecology*, 81(4), 1139–1142. [Entrada do MathWorld que o cita](https://mathworld.wolfram.com/GiniCoefficient.html)
- Godley, W. & Lavoie, M. (2007). *Monetary Economics: An Integrated Approach to Credit, Money, Income, Production and Wealth*. Palgrave Macmillan. Revisão: [Nikiforos & Zezza, Levy Institute WP 891](https://levyinstitute.org/wp-content/uploads/2024/02/wp_891.pdf)
- Gamma, E., Helm, R., Johnson, R. & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley (Strategy, Observer, Factory, Builder).

> Os parâmetros (`k`, fração de salários, riqueza inicial, produção) **não são calibrados com dados reais**. Os resultados mostram como os mecanismos se comportam, não previsões sobre uma economia real.

## Padrões de projeto

- **Strategy**: `PurchaseStrategy` e `ProductionStrategy`
- **Observer**: `SimulationObserver`
- **Factory**: `MarketScenarioFactory`
- **Builder**: `MarketConfigBuilder`

## Estado atual

- [x] Núcleo do motor (`core`)
- [x] Mercado (`market`): preço, agentes, estratégias, choques, Builder e Factory
- [x] Métricas (`metrics`): snapshot, Gini, coletor e exportação CSV
- [x] `Main` executando um cenário completo
- [x] Liquidação no fim do tick
- [x] Testes unitários (JUnit)
