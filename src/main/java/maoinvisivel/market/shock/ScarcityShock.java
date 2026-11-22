package maoinvisivel.market.shock;

import maoinvisivel.market.Market;

/** Escassez: no tick de disparo, a oferta é cortada em uma fração (0.3 = -30%). */
public class ScarcityShock implements Shock {
    private final long tickDeDisparo;
    private final double fracao;

    public ScarcityShock(long tickDeDisparo, double fracao) {
        if (fracao < 0 || fracao > 1) {
            throw new IllegalArgumentException("A fração deve estar entre 0 e 1: " + fracao);
        }
        this.tickDeDisparo = tickDeDisparo;
        this.fracao = fracao;
    }

    @Override
    public void aplicar(Market mercado, long tick) {
        if (tick == tickDeDisparo) {
            mercado.reduzirOferta(fracao);
        }
    }
}
