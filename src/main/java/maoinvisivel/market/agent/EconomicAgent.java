package maoinvisivel.market.agent;

import maoinvisivel.core.Agent;
import maoinvisivel.market.Market;

import java.util.Objects;

/** Base de todo agente do mercado: tem identidade, riqueza e acesso ao mercado. */
public abstract class EconomicAgent implements Agent {
    private final String id;
    private double riqueza;
    protected final Market mercado;

    protected EconomicAgent(String id, double riquezaInicial, Market mercado) {
        if (riquezaInicial < 0) {
            throw new IllegalArgumentException("A riqueza inicial deve ser >= 0: " + riquezaInicial);
        }
        this.id = Objects.requireNonNull(id, "id");
        this.riqueza = riquezaInicial;
        this.mercado = Objects.requireNonNull(mercado, "mercado");
    }

    @Override
    public String obterId() {
        return id;
    }

    public double obterRiqueza() {
        return riqueza;
    }

    public void receber(double valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("O valor deve ser >= 0: " + valor);
        }
        riqueza += valor;
    }

    public void gastar(double valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("O valor deve ser >= 0: " + valor);
        }
        if (valor > riqueza) {
            throw new IllegalArgumentException("Riqueza insuficiente: " + riqueza + " < " + valor);
        }
        riqueza -= valor;
    }
}
