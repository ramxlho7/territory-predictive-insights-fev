package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.excecao.FonteIndisponivelException;

import java.util.Random;

public class SensorPluviometrico implements Sensor {
    private final String id;
    private boolean operacional;
    private final Random random;

    public SensorPluviometrico(String id) {
        this(id, true, new Random());
    }

    public SensorPluviometrico(String id, boolean operacional, Random random) {
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
        return "PLUVIOMETRO";
    }

    @Override
    public String getUnidadeMedida() {
        return "mm/h";
    }

    @Override
    public double lerValor() throws FonteIndisponivelException, DadosInvalidosException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException(
                "Falha de telemetria no sensor pluviométrico [" + id + "]: equipamento indisponível."
            );
        }
        // Simula precipitacao realista entre 0.0 e 95.0 mm/h
        double leitura = Math.round((random.nextDouble() * 95.0) * 100.0) / 100.0;
        validarFaixa(leitura);
        return leitura;
    }

    public void validarFaixa(double valor) throws DadosInvalidosException {
        if (Double.isNaN(valor) || valor < 0.0 || valor > 300.0) {
            throw new DadosInvalidosException(
                String.format("Leitura pluviométrica fora da faixa física [0..300 mm/h]: %.2f", valor)
            );
        }
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
