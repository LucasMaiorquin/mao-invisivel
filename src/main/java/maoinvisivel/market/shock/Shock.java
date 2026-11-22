package maoinvisivel.market.shock;

import maoinvisivel.market.Market;

public interface Shock {
    void aplicar(Market mercado, long tick);
}
