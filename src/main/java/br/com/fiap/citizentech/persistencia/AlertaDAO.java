package br.com.fiap.citizentech.persistencia;

import br.com.fiap.citizentech.dominio.Alerta;
import br.com.fiap.citizentech.dominio.NivelAlerta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {
    private final ConnectionFactory connectionFactory;

    public AlertaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Alerta salvar(Alerta alerta) throws SQLException {
        String sql = "INSERT INTO alertas (sensor_id, tipo_sensor, nivel, valor_previsto, " +
                     "limite_ultrapassado, estrategia_utilizada, justificativa, data_hora) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, alerta.getSensorId());
            ps.setString(2, alerta.getTipoSensor());
            ps.setString(3, alerta.getNivel().name());
            ps.setDouble(4, alerta.getValorPrevisto());
            ps.setDouble(5, alerta.getLimiteUltrapassado());
            ps.setString(6, alerta.getEstrategiaUtilizada());
            ps.setString(7, alerta.getJustificativa());
            ps.setTimestamp(8, Timestamp.valueOf(alerta.getDataHora()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    alerta.setId(keys.getLong(1));
                }
            }
        }
        return alerta;
    }

    public List<Alerta> buscarTodos() throws SQLException {
        String sql = "SELECT id, sensor_id, tipo_sensor, nivel, valor_previsto, limite_ultrapassado, " +
                     "estrategia_utilizada, justificativa, data_hora " +
                     "FROM alertas ORDER BY data_hora DESC, id DESC";
        List<Alerta> lista = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Alerta(
                        rs.getLong("id"),
                        rs.getString("sensor_id"),
                        rs.getString("tipo_sensor"),
                        NivelAlerta.valueOf(rs.getString("nivel")),
                        rs.getDouble("valor_previsto"),
                        rs.getDouble("limite_ultrapassado"),
                        rs.getString("estrategia_utilizada"),
                        rs.getString("justificativa"),
                        rs.getTimestamp("data_hora").toLocalDateTime()
                ));
            }
        }
        return lista;
    }
}
