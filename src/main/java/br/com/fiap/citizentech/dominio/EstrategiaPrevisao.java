package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.List;

public abstract class EstrategiaPrevisao {
    protected final String nomeEstrategia;
    protected final int minimoLeituras;

    protected EstrategiaPrevisao(String nomeEstrategia, int minimoLeituras) {
        this.nomeEstrategia = nomeEstrategia;
        this.minimoLeituras = minimoLeituras;
    }

    protected List<Double> validarEExtrairValores(List<Medicao> historico) throws DadosInvalidosException {
        if (historico == null || historico.size() < minimoLeituras) {
            int atual = (historico == null) ? 0 : historico.size();
            throw new DadosInvalidosException(
                String.format("Sistema em silêncio preventivo: histórico insuficiente (%d/%d leituras) para '%s'.",
                    atual, minimoLeituras, nomeEstrategia)
            );
        }
        List<Double> valores = new ArrayList<>();
        for (Medicao m : historico) {
            valores.add(m.getValor());
        }
        return valores;
    }

    public abstract double prever(List<Medicao> historico) throws DadosInvalidosException;

    public abstract String explicarPrevisao(List<Medicao> historico, double valorPrevisto)
            throws DadosInvalidosException;

    public String getNomeEstrategia() {
        return nomeEstrategia;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }
}
