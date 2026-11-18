package br.com.fiap.citizentech.servico;

import br.com.fiap.citizentech.dominio.Alerta;
import br.com.fiap.citizentech.dominio.Medicao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Funcionalidade Bonus (+0.5): Exportacao do historico consultado para arquivos CSV.
 */
public class ExportadorCSV {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void exportarMedicoes(List<Medicao> medicoes, String caminhoArquivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo, StandardCharsets.UTF_8))) {
            writer.write("id;sensor_id;tipo_sensor;valor;unidade;data_hora\n");
            for (Medicao m : medicoes) {
                writer.write(String.format("%s;%s;%s;%.2f;%s;%s\n",
                        m.getId() != null ? m.getId() : "",
                        m.getSensorId(),
                        m.getTipoSensor(),
                        m.getValor(),
                        m.getUnidade(),
                        m.getDataHora().format(FMT)));
            }
        }
    }

    public void exportarAlertas(List<Alerta> alertas, String caminhoArquivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo, StandardCharsets.UTF_8))) {
            writer.write("id;sensor_id;tipo_sensor;nivel;valor_previsto;limite_ultrapassado;estrategia;justificativa;data_hora\n");
            for (Alerta a : alertas) {
                writer.write(String.format("%s;%s;%s;%s;%.2f;%.2f;%s;\"%s\";%s\n",
                        a.getId() != null ? a.getId() : "",
                        a.getSensorId(),
                        a.getTipoSensor(),
                        a.getNivel().name(),
                        a.getValorPrevisto(),
                        a.getLimiteUltrapassado(),
                        a.getEstrategiaUtilizada(),
                        a.getJustificativa().replace("\"", "'"),
                        a.getDataHora().format(FMT)));
            }
        }
    }
}
