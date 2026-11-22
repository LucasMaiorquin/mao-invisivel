package maoinvisivel.market;

import maoinvisivel.market.shock.Shock;

import java.util.List;

/** Parâmetros imutáveis de um cenário de mercado. Use o MarketConfigBuilder para criar. */
public class MarketConfig {
    private final double precoInicial;
    private final double sensibilidade;
    private final double precoMinimo;
    private final int numeroDeConsumidores;
    private final int numeroDeProdutores;
    private final double riquezaInicialDoConsumidor;
    private final double rendaDoConsumidor;
    private final double fracaoDeGasto;
    private final double riquezaInicialDoProdutor;
    private final double producaoPorProdutor;
    private final int ticks;
    private final long semente;
    private final List<Shock> choques;

    MarketConfig(MarketConfigBuilder b) {
        this.precoInicial = b.precoInicial;
        this.sensibilidade = b.sensibilidade;
        this.precoMinimo = b.precoMinimo;
        this.numeroDeConsumidores = b.numeroDeConsumidores;
        this.numeroDeProdutores = b.numeroDeProdutores;
        this.riquezaInicialDoConsumidor = b.riquezaInicialDoConsumidor;
        this.rendaDoConsumidor = b.rendaDoConsumidor;
        this.fracaoDeGasto = b.fracaoDeGasto;
        this.riquezaInicialDoProdutor = b.riquezaInicialDoProdutor;
        this.producaoPorProdutor = b.producaoPorProdutor;
        this.ticks = b.ticks;
        this.semente = b.semente;
        this.choques = List.copyOf(b.choques);
    }

    public double obterPrecoInicial() { return precoInicial; }
    public double obterSensibilidade() { return sensibilidade; }
    public double obterPrecoMinimo() { return precoMinimo; }
    public int obterNumeroDeConsumidores() { return numeroDeConsumidores; }
    public int obterNumeroDeProdutores() { return numeroDeProdutores; }
    public double obterRiquezaInicialDoConsumidor() { return riquezaInicialDoConsumidor; }
    public double obterRendaDoConsumidor() { return rendaDoConsumidor; }
    public double obterFracaoDeGasto() { return fracaoDeGasto; }
    public double obterRiquezaInicialDoProdutor() { return riquezaInicialDoProdutor; }
    public double obterProducaoPorProdutor() { return producaoPorProdutor; }
    public int obterTicks() { return ticks; }
    public long obterSemente() { return semente; }
    public List<Shock> obterChoques() { return choques; }
}
