package maoinvisivel.core;

//Qualquer entidade que age em cada tick da simulação

public interface Agent {
    String obterId();
    void agir(long tick);
}
