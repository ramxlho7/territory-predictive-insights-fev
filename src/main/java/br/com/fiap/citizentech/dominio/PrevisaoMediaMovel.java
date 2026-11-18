package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.util.List;

public class PrevisaoMediaMovel extends EstrategiaPrevisao {
    private final int janela;

    public PrevisaoMediaMovel() {
        this(5);
    }

    public PrevisaoMediaMovel(int janela) {
        super("Média Móvel (" + janela + " últimas leituras)", janela);
        this.janela = janela;
    }

    @Override
    public double prever(List<Medicao> historico) throws DadosInvalidosException {
        // Reutiliza validacao da superclasse via super
        List<Double> valores = super.validarEExtrairValores(historico);

        // Assumindo historico ordenado do mais recente para o mais antigo
        double soma = 0.0;
        for (int i = 0; i < janela; i++) {
            soma += valores.get(i);
        }
        return Math.round((soma / janela) * 100.0) / 100.0;
    }

    @Override
    public String explicarPrevisao(List<Medicao> historico, double valorPrevisto)
            throws DadosInvalidosException {
        super.validarEExtrairValores(historico);
        String unidade = historico.get(0).getUnidade();
        return String.format(
            "A média móvel das últimas %d medições projeta %.2f %s, suavizando oscilações pontuais.",
            janela, valorPrevisto, unidade
        );
    }
}
