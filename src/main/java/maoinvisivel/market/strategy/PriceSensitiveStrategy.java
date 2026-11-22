package maoinvisivel.market.strategy;

/**
 * Compra menos quando o preço sobe:
 * quantidade = base * (precoReferencia / preco) ^ elasticidade,
 * limitada ao que o orçamento permite pagar.
 */
public class PriceSensitiveStrategy implements PurchaseStrategy {
    private final double quantidadeBase;
    private final double precoReferencia;
    private final double elasticidade;

    public PriceSensitiveStrategy(double quantidadeBase, double precoReferencia, double elasticidade) {
        if (quantidadeBase < 0) {
            throw new IllegalArgumentException("A quantidade base deve ser >= 0: " + quantidadeBase);
        }
        if (precoReferencia <= 0) {
            throw new IllegalArgumentException("O preço de referência deve ser > 0: " + precoReferencia);
        }
        if (elasticidade < 0) {
            throw new IllegalArgumentException("A elasticidade deve ser >= 0: " + elasticidade);
        }
        this.quantidadeBase = quantidadeBase;
        this.precoReferencia = precoReferencia;
        this.elasticidade = elasticidade;
    }

    @Override
    public double quantidadeDesejada(double preco, double orcamento) {
        if (preco <= 0 || orcamento <= 0) {
            return 0;
        }
        double desejada = quantidadeBase * Math.pow(precoReferencia / preco, elasticidade);
        return Math.min(desejada, orcamento / preco);
    }
}
