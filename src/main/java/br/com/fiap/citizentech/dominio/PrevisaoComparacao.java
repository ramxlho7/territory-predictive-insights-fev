package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.util.List;

public class PrevisaoComparacao extends EstrategiaPrevisao {
    private static final int TAMANHO_BLOCO = 3;

    public PrevisaoComparacao() {
        super("Comparação de Períodos (3 recentes vs. 3 anteriores)", TAMANHO_BLOCO * 2);
    }

    @Override
    public double prever(List<Medicao> historico) throws DadosInvalidosException {
        // Reutiliza validacao da superclasse via super
        List<Double> valores = super.validarEExtrairValores(historico);

        // Bloco 0..2 = periodo mais recente; Bloco 3..5 = periodo anterior
        double mediaRecente = (valores.get(0) + valores.get(1) + valores.get(2)) / TAMANHO_BLOCO;
        double mediaAnterior = (valores.get(3) + valores.get(4) + valores.get(5)) / TAMANHO_BLOCO;

        double tendencia = mediaRecente - mediaAnterior;
        double projecao = Math.max(0.0, mediaRecente + (tendencia * 0.5));
        return Math.round(projecao * 100.0) / 100.0;
    }

    @Override
    public String explicarPrevisao(List<Medicao> historico, double valorPrevisto)
            throws DadosInvalidosException {
        List<Double> valores = super.validarEExtrairValores(historico);
        double mediaRecente = (valores.get(0) + valores.get(1) + valores.get(2)) / TAMANHO_BLOCO;
        double mediaAnterior = (valores.get(3) + valores.get(4) + valores.get(5)) / TAMANHO_BLOCO;
        double variacao = mediaRecente - mediaAnterior;
        String unidade = historico.get(0).getUnidade();
        String direcao = (variacao >= 0) ? "aceleração (+" : "desaceleração (";

        return String.format(
            "Comparação entre blocos aponta %s%.2f %s) entre o período anterior (%.2f) e o atual (%.2f), projetando %.2f %s.",
            direcao, variacao, unidade, mediaAnterior, mediaRecente, valorPrevisto, unidade
        );
    }
}
