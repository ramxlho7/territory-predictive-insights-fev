package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;

public abstract class ComponenteTerritorial {
    private final String id;
    private String nome;
    private String regiao;
    protected String status;

    protected ComponenteTerritorial(String id, String nome, String regiao, String statusInicial)
            throws DadosInvalidosException {
        if (id == null || id.isBlank()) {
            throw new DadosInvalidosException("O ID do componente territorial não pode ser vazio.");
        }
        this.id = id.trim();
        setNome(nome);
        setRegiao(regiao);
        this.status = (statusInicial == null || statusInicial.isBlank()) ? "INICIALIZADO" : statusInicial;
    }

    public abstract String verificarStatus();

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws DadosInvalidosException {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do componente territorial não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A região do componente territorial não pode ser vazia.");
        }
        this.regiao = regiao.trim();
    }

    public String getStatus() {
        return status;
    }
}
