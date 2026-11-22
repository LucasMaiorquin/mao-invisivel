package maoinvisivel.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

/**
 * O comandante da simulação
 * Cada chamada de {@link #rodar(int)} conta os ticks a partir de 1.
 * Os agentes agem de forma embaralhada a cada tick. 
 * A mesma seed sempre produz a mesma simulação.
 */

public class Simulation {
    private final Environment environment;
    private final List<Agent> agents = new ArrayList<> ();
    private final List<SimulationObserver> observers = new ArrayList<>();
    private final Random random;

    public Simulation(Environment environment, Random random) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.random = Objects.requireNonNull(random, "random");
    }

    public void adicionarAgente(Agent agent) {
        agents.add(Objects.requireNonNull(agent, "agent"));
    }

    public void adicionarObservador(SimulationObserver observer) {
        this.observers.add(Objects.requireNonNull(observer, "observer"));
    }

    public void rodar(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("Os ticks deve ser >= 0" + ticks);
        }
        for (long tick = 1; tick <= ticks; tick++) {
            runTick(tick);
        }
    }

    private void runTick(long tick) {
        environment.aoIniciarTick(tick);

        List<Agent> order = new ArrayList<>(agents);
        Collections.shuffle(order, random);
        for (Agent agent : order) {
            agent.agir(tick);
        }

        environment.aoFinalizarTick(tick);

        for (SimulationObserver observer : observers) {
            observer.aoFinalizarTick(tick);
        }
    }
}
