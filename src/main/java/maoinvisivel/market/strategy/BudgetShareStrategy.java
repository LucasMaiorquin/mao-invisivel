package maoinvisivel.market.strategy;

/** Gasta uma fração fixa do orçamento, não importa o preço. */
public class BudgetShareStrategy implements PurchaseStrategy {
    private final double fracao;

    public BudgetShareStrategy(double fracao) {
        if (fracao < 0 || fracao > 1) {
            throw new IllegalArgumentException("A fração deve estar entre 0 e 1: " + fracao);
        }
        this.fracao = fracao;
    }

    @Override
    public double quantidadeDesejada(double preco, double orcamento) {
        if (preco <= 0 || orcamento <= 0) {
            return 0;
        }
        return fracao * orcamento / preco;
    }
}
