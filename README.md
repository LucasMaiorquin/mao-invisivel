# Mão Invisível

Motor de simulação baseada em agentes em **Java 21**, com um mercado em que os preços surgem da oferta e da demanda.

Arquitetura orientada a objetos (Strategy, Observer, Factory, Builder), choques econômicos configuráveis, métricas como o índice de Gini e testes com JUnit.

## Estado atual

- [x] Núcleo do motor (`core`): `Agent`, `Environment`, `Simulation`, `SimulationObserver`
- [x] Testes do núcleo (`SimulationTest`)
- [x] Mercado (`market`): preço, agentes, estratégias, choques, Builder e Factory
- [x] Métricas (`metrics`): snapshot, Gini, coletor e exportação CSV
- [x] `Main` executando um cenário completo (gera `output/*.csv`)

## Estrutura

```
src/main/java/maoinvisivel/
├── Main.java
├── core/      motor genérico de ticks
├── market/    mercado, agentes, estratégias e choques
└── metrics/   coleta e exportação de métricas
src/test/java/maoinvisivel/   testes JUnit
docs/ClassDiagram.png         diagrama de classes
```

## Como rodar

Requisitos: JDK 21 e Maven.

```
mvn test      # roda os testes
mvn compile   # compila o projeto
java -cp target/classes maoinvisivel.Main   # roda os cenários e grava output/*.csv
```

## Padrões de projeto

- **Strategy**: `PurchaseStrategy` e `ProductionStrategy`
- **Observer**: `SimulationObserver`
- **Factory**: `MarketScenarioFactory`
- **Builder**: `MarketConfigBuilder`
