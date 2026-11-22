package maoinvisivel.market;

import maoinvisivel.market.shock.Shock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Monta um MarketConfig. Todos os valores têm padrão, então
 * new MarketConfigBuilder().construir() já devolve um cenário válido.
 *
 * Os padrões formam um ciclo fechado de dinheiro (consumidor -> produtor -> consumidor,
 * pelos salários), sem renda externa. Foram calibrados medindo a simulação: com 70% da
 * riqueza paga em salários por tick, o preço se estabiliza perto de 10 e o Gini perto de 0,34.
 * Com salários menores o preço cai e o Gini sobe; com 0 o dinheiro se acumula nos produtores.
 */
public class MarketConfigBuilder {
    double precoInicial = 10.0;
    double sensibilidade = 0.1;
    double precoMinimo = 0.01;
    int numeroDeConsumidores = 100;
    int numeroDeProdutores = 20;
    double riquezaInicialDoConsumidor = 20.0;
    double rendaDoConsumidor = 0.0;
    double fracaoDeSalarios = 0.7;
    double fracaoDeGasto = 0.5;
    double riquezaInicialDoProdutor = 50.0;
    double producaoPorProdutor = 5.0;
    int ticks = 100;
    long semente = 42L;
    final List<Shock> choques = new ArrayList<>();

    public MarketConfigBuilder precoInicial(double valor) {
        this.precoInicial = valor;
        return this;
    }

    public MarketConfigBuilder sensibilidade(double valor) {
        this.sensibilidade = valor;
        return this;
    }

    public MarketConfigBuilder precoMinimo(double valor) {
        this.precoMinimo = valor;
        return this;
    }

    public MarketConfigBuilder numeroDeConsumidores(int valor) {
        this.numeroDeConsumidores = valor;
        return this;
    }

    public MarketConfigBuilder numeroDeProdutores(int valor) {
        this.numeroDeProdutores = valor;
        return this;
    }

    public MarketConfigBuilder riquezaInicialDoConsumidor(double valor) {
        this.riquezaInicialDoConsumidor = valor;
        return this;
    }

    public MarketConfigBuilder rendaDoConsumidor(double valor) {
        this.rendaDoConsumidor = valor;
        return this;
    }

    public MarketConfigBuilder fracaoDeGasto(double valor) {
        this.fracaoDeGasto = valor;
        return this;
    }

    /** Fração da riqueza que cada produtor paga em salários por tick (0 = nunca gasta). */
    public MarketConfigBuilder fracaoDeSalarios(double valor) {
        this.fracaoDeSalarios = valor;
        return this;
    }

    public MarketConfigBuilder riquezaInicialDoProdutor(double valor) {
        this.riquezaInicialDoProdutor = valor;
        return this;
    }

    public MarketConfigBuilder producaoPorProdutor(double valor) {
        this.producaoPorProdutor = valor;
        return this;
    }

    public MarketConfigBuilder ticks(int valor) {
        this.ticks = valor;
        return this;
    }

    public MarketConfigBuilder semente(long valor) {
        this.semente = valor;
        return this;
    }

    public MarketConfigBuilder adicionarChoque(Shock choque) {
        this.choques.add(Objects.requireNonNull(choque, "choque"));
        return this;
    }

    public MarketConfig construir() {
        if (precoMinimo <= 0) {
            throw new IllegalArgumentException("O preço mínimo deve ser > 0: " + precoMinimo);
        }
        if (precoInicial < precoMinimo) {
            throw new IllegalArgumentException("O preço inicial deve ser >= preço mínimo: " + precoInicial);
        }
        if (sensibilidade < 0) {
            throw new IllegalArgumentException("A sensibilidade deve ser >= 0: " + sensibilidade);
        }
        if (numeroDeConsumidores < 1) {
            throw new IllegalArgumentException("Precisa de pelo menos 1 consumidor: " + numeroDeConsumidores);
        }
        if (numeroDeProdutores < 1) {
            throw new IllegalArgumentException("Precisa de pelo menos 1 produtor: " + numeroDeProdutores);
        }
        if (riquezaInicialDoConsumidor < 0 || riquezaInicialDoProdutor < 0) {
            throw new IllegalArgumentException("A riqueza inicial deve ser >= 0");
        }
        if (fracaoDeSalarios < 0 || fracaoDeSalarios > 1) {
            throw new IllegalArgumentException("A fração de salários deve estar entre 0 e 1: " + fracaoDeSalarios);
        }
        if (rendaDoConsumidor < 0) {
            throw new IllegalArgumentException("A renda deve ser >= 0: " + rendaDoConsumidor);
        }
        if (fracaoDeGasto < 0 || fracaoDeGasto > 1) {
            throw new IllegalArgumentException("A fração de gasto deve estar entre 0 e 1: " + fracaoDeGasto);
        }
        if (producaoPorProdutor < 0) {
            throw new IllegalArgumentException("A produção deve ser >= 0: " + producaoPorProdutor);
        }
        if (ticks < 0) {
            throw new IllegalArgumentException("Os ticks devem ser >= 0: " + ticks);
        }
        return new MarketConfig(this);
    }
}
