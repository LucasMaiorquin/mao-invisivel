package maoinvisivel.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {
    //Só anotam o que acontece em uma lista, classes falsas

    private static class FakeAgent implements Agent {
        private final String id;
        private final List<String> log;

        FakeAgent(String id, List<String> log) {
            this.id = id;
            this.log = log;
        }

        @Override 
        public String obterId() {
            return id;
        }

        @Override 
        public void agir(long tick) {
            log.add("step:" + id + ":" + tick );
        }
    }

    private static class FakeEnvironment implements Environment {
        private final List<String> log;

        FakeEnvironment(List<String> log) {
            this.log = log;
        }

        @Override
        public void aoIniciarTick(long tick) {
            log.add("start:" + tick);
        }    
        
        @Override 
        public void aoFinalizarTick(long tick) {
            log.add("end:" + tick);
        }
    }

    private static class FakeObserver implements SimulationObserver {
        private final List<String> log;

        FakeObserver(List<String> log) {
            this.log = log;
        } 

        @Override
        public void aoFinalizarTick(long tick) {
            log.add("observer:" + tick);
        }
    }

    private static long count(List<String> log, String prefix) {
        return log.stream().filter(entry -> entry.startsWith(prefix)).count();
    }

    //Testes

    @Test
    void chamaTudoUmaVezPorTick() {
        List<String> log = new ArrayList<>();
        Simulation simulation = new Simulation(new FakeEnvironment(log), new Random(1));
        simulation.adicionarAgente(new FakeAgent("a", log));
        simulation.adicionarObservador(new FakeObserver(log));

        simulation.rodar(5);

        assertEquals(5L, count(log, "start:"));
        assertEquals(5L, count(log, "step:"));
        assertEquals(5L, count(log, "end:"));
        assertEquals(5L, count(log, "observer:"));
    }

    @Test
    void respeitaAOrdemDentroDeUmTick() {
        List<String> log = new ArrayList<>();
        Simulation simulation = new Simulation(new FakeEnvironment(log), new Random(1));
        simulation.adicionarAgente(new FakeAgent("a", log));
        simulation.adicionarObservador(new FakeObserver(log));

        simulation.rodar(1);

        assertEquals(List.of("start:1", "step:a:1", "end:1", "observer:1"), log);
    }

    @Test
    void todosOsAgentesAgemUmaVezPorTick() {
        List<String> log = new ArrayList<>();
        Simulation simulation = new Simulation(new FakeEnvironment(log), new Random(1));
        simulation.adicionarAgente(new FakeAgent("a", log));
        simulation.adicionarAgente(new FakeAgent("b", log));
        simulation.adicionarAgente(new FakeAgent("c", log));

        simulation.rodar(1);

        assertEquals(1L, count(log, "step:a:"));
        assertEquals(1L, count(log, "step:b:"));
        assertEquals(1L, count(log, "step:c:"));
    }

    @Test
    void rodarZeroTicksNaoFazNada() {
        List<String> log = new ArrayList<>();
        Simulation simulation = new Simulation(new FakeEnvironment(log), new Random(1));
        simulation.adicionarAgente(new FakeAgent("a", log));
        simulation.adicionarObservador(new FakeObserver(log));

        simulation.rodar(0);

        assertTrue(log.isEmpty());
    }

    @Test
    void ticksNegativosLancamExcecao() {
        Simulation simulation = new Simulation(new FakeEnvironment(new ArrayList<>()), new Random(1));

        assertThrows(IllegalArgumentException.class, () -> simulation.rodar(-1));
    }

    @Test
    void naoAceitaAgenteNulo() {
        Simulation simulation = new Simulation(new FakeEnvironment(new ArrayList<>()), new Random(1));

        assertThrows(NullPointerException.class, () -> simulation.adicionarAgente(null));
    }

    @Test
    void mesmaSementeGeraAMesmaOrdemDeAgentes() {
        List<String> primeira = ordemDosAgentes(42L);
        List<String> segunda = ordemDosAgentes(42L);

        assertEquals(primeira, segunda);
    }

    private List<String> ordemDosAgentes(long seed) {
        List<String> log = new ArrayList<>();
        Simulation simulation = new Simulation(new FakeEnvironment(new ArrayList<>()), new Random(seed));
        for (int i = 0; i < 10; i++) {
            simulation.adicionarAgente(new FakeAgent("agente" + i, log));
        }
        simulation.rodar(20);
        return log;
    }
}
