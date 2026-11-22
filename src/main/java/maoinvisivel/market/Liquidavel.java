package maoinvisivel.market;

/** Quem coloca uma ordem no mercado e precisa saber quanto dela foi realmente executada. */
public interface Liquidavel {
    /**
     * Chamado no fim do tick, pela liquidação.
     *
     * @param quantidadeExecutada parte da ordem que foi comprada/vendida
     * @param preco               preço pelo qual o tick foi liquidado
     */
    void aoLiquidar(double quantidadeExecutada, double preco);
}
