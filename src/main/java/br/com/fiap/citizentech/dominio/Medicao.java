package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Medicao {
    private Long id;
    private final String sensorId;
    private final String tipoSensor;
    private final double valor;
    private final String unidade;
    private final LocalDateTime dataHora;

    public Medicao(String sensorId, String tipoSensor, double valor, String unidade)
            throws DadosInvalidosException {
        this(null, sensorId, tipoSensor, valor, unidade, LocalDateTime.now());
    }

    public Medicao(Long id, String sensorId, String tipoSensor, double valor, String unidade, LocalDateTime dataHora)
            throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("ID do sensor é obrigatório para registrar uma medição.");
        }
        if (tipoSensor == null || tipoSensor.isBlank()) {
            throw new DadosInvalidosException("Tipo do sensor é obrigatório para registrar uma medição.");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 0.0) {
            throw new DadosInvalidosException(
                String.format("Medição inválida para sensor %s: valor negativo ou indefinido (%.2f).", sensorId, valor)
            );
        }
        if ("UMIDADE_SOLO".equalsIgnoreCase(tipoSensor) && valor > 100.0) {
            throw new DadosInvalidosException(
                String.format("Medição de umidade do solo acima de 100%% é inválida: %.2f%%", valor)
            );
        }
        if ("PLUVIOMETRO".equalsIgnoreCase(tipoSensor) && valor > 300.0) {
            throw new DadosInvalidosException(
                String.format("Medição pluviométrica acima do teto físico (300 mm/h): %.2f", valor)
            );
        }
        if ("NIVEL_RIO".equalsIgnoreCase(tipoSensor) && valor > 20.0) {
            throw new DadosInvalidosException(
                String.format("Medição de nível do rio acima do teto físico (20 m): %.2f", valor)
            );
        }

        this.id = id;
        this.sensorId = sensorId.trim();
        this.tipoSensor = tipoSensor.trim();
        this.valor = valor;
        this.unidade = (unidade == null) ? "" : unidade.trim();
        this.dataHora = (dataHora == null) ? LocalDateTime.now() : dataHora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%s] Sensor: %-10s | Tipo: %-13s | Valor: %6.2f %-4s",
                dataHora.format(fmt), sensorId, tipoSensor, valor, unidade);
    }
}
