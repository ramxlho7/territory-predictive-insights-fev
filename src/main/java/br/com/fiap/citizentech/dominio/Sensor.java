package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.excecao.FonteIndisponivelException;

public interface Sensor {
    String getId();
    String getTipo();
    String getUnidadeMedida();
    double lerValor() throws FonteIndisponivelException, DadosInvalidosException;
    boolean verificarFuncionamento();
    void setOperacional(boolean operacional);
}
