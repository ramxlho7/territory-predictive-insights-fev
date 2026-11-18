package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.excecao.FonteIndisponivelException;

import java.util.Random;

public class SensorUmidadeSolo implements Sensor {
    private final String id;
    private boolean operacional;
    private final Random random;

    public SensorUmidadeSolo(String id) {
        this(id, true, new Random());
    }

    public SensorUmidadeSolo(String id, boolean operacional, Random random) {
        this.id = id;
        this.operacional = operacional;
        this.random = random;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTipo() {
        return "UMIDADE_SOLO";
    }

    @Override
    public String getUnidadeMedida() {
        return "%";
    }

    @Override
    public double lerValor() throws FonteIndisponivelException, DadosInvalidosException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException(
                "Falha no tensiômetro de umidade do solo [" + id + "]: circuito aberto."
            );
        }
        // Simula saturacao do solo entre 25% e 98%
        double leitura = Math.round((25.0 + random.nextDouble() * 73.0) * 100.0) / 100.0;
        if (leitura < 0.0 || leitura > 100.0) {
            throw new DadosInvalidosException("Umidade do solo fora da faixa percentual [0..100%]: " + leitura);
        }
        return leitura;
    }

    @Override
    public boolean verificarFuncionamento() {
        return operacional;
    }

    @Override
    public void setOperacional(boolean operacional) {
        this.operacional = operacional;
    }
}
