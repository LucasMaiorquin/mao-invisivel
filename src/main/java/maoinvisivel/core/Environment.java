package maoinvisivel.core;

//O ambiente em que os agentes vivem.

public interface Environment {
    void onTickStart(long tick);
    void onTickEnd(long tick);
}
