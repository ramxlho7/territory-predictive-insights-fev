package br.com.fiap.citizentech.dominio;

public enum NivelAlerta {
    NORMAL("Normal", "Monitoramento de rotina; sem risco imediato."),
    ATENCAO("Atenção", "Acompanhar evolução das leituras e checar drenagem local."),
    ALERTA("Alerta", "Mobilizar equipes comunitárias e sinalizar pontos alagáveis."),
    CRITICO("Crítico", "Risco iminente de alagamento: acionar Defesa Civil e rotas seguras.");

    private final String rotulo;
    private final String orientacao;

    NivelAlerta(String rotulo, String orientacao) {
        this.rotulo = rotulo;
        this.orientacao = orientacao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getOrientacao() {
        return orientacao;
    }
}
