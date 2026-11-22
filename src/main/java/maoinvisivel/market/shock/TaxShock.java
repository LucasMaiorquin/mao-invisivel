package maoinvisivel.market.shock;

import maoinvisivel.market.Market;

/** Imposto: no tick de disparo, o preço sobe na proporção da taxa (0.2 = +20%). */
public class TaxShock implements Shock {
    private final long tickDeDisparo;
    private final double taxa;

    public TaxShock(long tickDeDisparo, double taxa) {
        if (taxa < 0) {
            throw new IllegalArgumentException("A taxa deve ser >= 0: " + taxa);
        }
        this.tickDeDisparo = tickDeDisparo;
        this.taxa = taxa;
    }

    @Override
    public void aplicar(Market mercado, long tick) {
        if (tick == tickDeDisparo) {
            mercado.definirPreco(mercado.obterPreco() * (1 + taxa));
        }
    }
}
