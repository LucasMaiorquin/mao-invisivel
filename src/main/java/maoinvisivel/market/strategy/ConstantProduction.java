package maoinvisivel.market.strategy;

/** Oferta sempre a mesma quantidade, não importa o preço. */
public class ConstantProduction implements ProductionStrategy {
    private final double quantidade;

    public ConstantProduction(double quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade deve ser >= 0: " + quantidade);
        }
        this.quantidade = quantidade;
    }

    @Override
    public double quantidadeAOfertar(double preco) {
        return quantidade;
    }
}
