package maoinvisivel.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import maoinvisivel.core.Environment;
import maoinvisivel.market.shock.Shock;
public class Market implements Environment, MarketView{
    private double preco;
    private final double sensibilidade;
    private final double precoMinimo;

    private double demandaAtual;
    private double ofertaAtual;
    private double ultimaDemanda;
    private double ultimaOferta;

    private double fatorOferta = 1.0; // Multiplicador da oferta. 1.0 é normal. Escassez reduz o valor.

    private final List<Shock> choques = new ArrayList<>();

    public Market(double precoInicial, double sensibilidade, double precoMinimo) {
        if (precoMinimo <= 0) {
            throw new IllegalArgumentException("O preço mínimo deve ser maior que 0: " + precoMinimo);
        }
        if (precoInicial < precoMinimo) {
            throw new IllegalArgumentException("O preço inicial deve ser maior ou igual ao preço minimo: " + precoInicial);
        }
        if (sensibilidade < 0) {
            throw new IllegalArgumentException("A sensibilidade deve ser maior ou igual à 0: " + sensibilidade);
        }
        this.preco = precoInicial;
        this.sensibilidade = sensibilidade;
        this.precoMinimo = precoMinimo;
    }

    @Override
    public void aoIniciarTick(long tick) {
        demandaAtual = 0;
        ofertaAtual = 0;
        for (Shock choque : choques) { 
            choque.aplicar(this, tick);
        }
    }

    @Override
    public void aoFinalizarTick(long tick) {
        ultimaDemanda = demandaAtual;
        ultimaOferta = ofertaAtual;
        preco = calcularNovoPreco(demandaAtual, ofertaAtual);
    }

    private double calcularNovoPreco(double demanda, double oferta) {
        double excesso;
        if (oferta == 0) {
            excesso = demanda > 0 ? 1.0 : 0.0;
        } else { 
            excesso = (demanda - oferta) / oferta;
        }
        excesso = Math.max(-1.0, Math.min(1.0, excesso));

        double novoPreco = preco * (1 + sensibilidade * excesso);
        return Math.max(precoMinimo, novoPreco);
    }

    public void registrarDemanda(double quantidade) {
        validarQuantidade(quantidade);
        demandaAtual += quantidade;
    }

    public void registrarOferta(double quantidade) {
        validarQuantidade(quantidade);
        ofertaAtual += quantidade * fatorOferta;
    }

    private void validarQuantidade(double quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior ou igual a 0: " + quantidade);
        }
    }

    public void adicionarChoque(Shock choque) {
        choques.add(Objects.requireNonNull(choque, "choque"));
    }

    public void definirPreco(double novoPreco) {
        this.preco = Math.max(precoMinimo, novoPreco);
    }

    public void reduzirOferta(double fracao) {
        if (fracao < 0 || fracao > 1) {
            throw new IllegalArgumentException("A fração deve estar entre 0 e 1: " + fracao);
        }
        fatorOferta *= (1 -fracao);
    }

    @Override
    public double obterPreco() {
        return preco;
    }

    @Override
    public double obterUltimaDemanda() {
        return ultimaDemanda;
    }

    @Override
    public double obterUltimaOferta() {
        return ultimaOferta;
    }
}
