package br.com.fiap.citizentech.excecao;

public class FonteIndisponivelException extends Exception {
    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
