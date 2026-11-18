package br.com.fiap.citizentech.dominio;

public enum TipoPrevisao {
    MEDIA_MOVEL("Média Móvel das Últimas Leituras"),
    COMPARACAO_PERIODOS("Comparação entre Períodos Consecutivos");

    private final String descricao;

    TipoPrevisao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
