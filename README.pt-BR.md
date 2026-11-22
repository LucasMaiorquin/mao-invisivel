# Mão Invisível

[🇬🇧 English](README.md) · 🇧🇷 Português

Motor de **simulação baseada em agentes** em **Java 21**, com um mercado em que os preços surgem da oferta e da demanda.

Arquitetura orientada a objetos (Strategy, Observer, Factory, Builder), choques econômicos configuráveis, métricas como o índice de Gini e testes com JUnit.

> **Nota:** os nomes das classes estão em inglês, mas os nomes dos métodos e os comentários estão em português (por exemplo `agir`, `rodar`, `aoFinalizarTick`).

## Como funciona

A simulação avança em **ticks**. Cada tick segue o mesmo ciclo:

1. **Início do tick** – o mercado zera os livros de ordens e aplica os choques agendados.
2. **Agentes agem** (em ordem embaralhada) – consumidores recebem renda e fazem ordens de compra; produtores fazem ordens de venda.
3. **Liquidação** – só se negocia `min(demanda, oferta)`, ao preço do tick. O lado maior é racionado proporcionalmente, então cada ordem executa a mesma fração. Consumidores pagam e produtores recebem apenas pelo que foi realmente negociado.
4. **Atualização do preço** – o preço se move com o excesso de demanda:
   `preço = preço × (1 + k × (demanda − oferta) / oferta)`, com o excesso limitado a `[-1, 1]` e um piso de preço maior que zero.
5. **Observadores** rodam, por exemplo o coletor de métricas tira uma foto do estado.

Com a mesma semente, a simulação sempre produz o mesmo resultado.

### Choques

- `TaxShock` – aumenta o preço em uma fração num determinado tick.
- `ScarcityShock` – corta a oferta em uma fração a partir de um determinado tick.

### Limitações conhecidas

- Os produtores nunca gastam dinheiro, então a riqueza se acumula neles e o índice de Gini sobe ao longo do tempo (cerca de 0,33 → 0,83 no cenário padrão). Fazer os produtores gastarem (por exemplo salários ou trocas entre eles) seria uma mudança de modelo.
- O dinheiro total é conservado pela liquidação; o único dinheiro que entra no sistema é a renda dos consumidores.

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
[sem_choque]  150 ticks | preco 10.00 -> 10.00 | Gini 0.333 -> 0.827 | output/sem_choque.csv
[com_choques] 150 ticks | preco 10.00 -> 16.69 | Gini 0.333 -> 0.827 | output/com_choques.csv
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

Todos os parâmetros do builder têm valores padrão, calibrados para o preço ficar perto de 10 sem choques.

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
