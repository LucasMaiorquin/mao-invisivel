package maoinvisivel.core;

//O ambiente em que os agentes vivem.

public interface Environment {
    void aoIniciarTick(long tick);
    void aoFinalizarTick(long tick);
}
