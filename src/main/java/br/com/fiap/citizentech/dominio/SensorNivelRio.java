package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.excecao.FonteIndisponivelException;

import java.util.Random;

public class SensorNivelRio implements Sensor {
    private final String id;
    private boolean operacional;
    private final Random random;

    public SensorNivelRio(String id) {
        this(id, true, new Random());
    }

    public SensorNivelRio(String id, boolean operacional, Random random) {
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
        return "NIVEL_RIO";
    }

    @Override
    public String getUnidadeMedida() {
        return "m";
    }

    @Override
    public double lerValor() throws FonteIndisponivelException, DadosInvalidosException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException(
                "Falha no sensor ultrassônico de nível de córrego [" + id + "]: sem resposta."
            );
        }
        // Simula cota hidrometrica entre 0.50 m e 4.80 m
        double leitura = Math.round((0.50 + random.nextDouble() * 4.30) * 100.0) / 100.0;
        if (leitura < 0.0 || leitura > 20.0) {
            throw new DadosInvalidosException("Nível do rio fora da faixa física [0..20 m]: " + leitura);
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
