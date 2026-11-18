package br.com.fiap.citizentech.dominio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Alerta {
    private Long id;
    private final String sensorId;
    private final String tipoSensor;
    private final NivelAlerta nivel;
    private final double valorPrevisto;
    private final double limiteUltrapassado;
    private final String estrategiaUtilizada;
    private final String justificativa;
    private final LocalDateTime dataHora;

    public Alerta(String sensorId, String tipoSensor, NivelAlerta nivel, double valorPrevisto,
                  double limiteUltrapassado, String estrategiaUtilizada, String justificativa) {
        this(null, sensorId, tipoSensor, nivel, valorPrevisto, limiteUltrapassado,
             estrategiaUtilizada, justificativa, LocalDateTime.now());
    }

    public Alerta(Long id, String sensorId, String tipoSensor, NivelAlerta nivel, double valorPrevisto,
                  double limiteUltrapassado, String estrategiaUtilizada, String justificativa,
                  LocalDateTime dataHora) {
        this.id = id;
        this.sensorId = sensorId;
        this.tipoSensor = tipoSensor;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limiteUltrapassado = limiteUltrapassado;
        this.estrategiaUtilizada = estrategiaUtilizada;
        this.justificativa = justificativa;
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

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiteUltrapassado() {
        return limiteUltrapassado;
    }

    public String getEstrategiaUtilizada() {
        return estrategiaUtilizada;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%s] [%s] Sensor %s (%s) | Previsto: %.2f (Limite: %.2f) | %s",
                dataHora.format(fmt), nivel.name(), sensorId, tipoSensor,
                valorPrevisto, limiteUltrapassado, justificativa);
    }
}
