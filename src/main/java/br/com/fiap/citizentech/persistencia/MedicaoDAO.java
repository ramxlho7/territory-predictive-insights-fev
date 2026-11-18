package br.com.fiap.citizentech.persistencia;

import br.com.fiap.citizentech.dominio.Medicao;
import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MedicaoDAO {
    private final ConnectionFactory connectionFactory;

    public MedicaoDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Medicao salvar(Medicao medicao) throws SQLException {
        String sql = "INSERT INTO medicoes (sensor_id, tipo_sensor, valor, unidade, data_hora) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicao.getSensorId());
            ps.setString(2, medicao.getTipoSensor());
            ps.setDouble(3, medicao.getValor());
            ps.setString(4, medicao.getUnidade());
            ps.setTimestamp(5, Timestamp.valueOf(medicao.getDataHora()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    medicao.setId(keys.getLong(1));
                }
            }
        }
        return medicao;
    }

    public List<Medicao> buscarPorSensor(String sensorId) throws SQLException, DadosInvalidosException {
        String sql = "SELECT id, sensor_id, tipo_sensor, valor, unidade, data_hora " +
                     "FROM medicoes WHERE sensor_id = ? ORDER BY data_hora DESC, id DESC";
        List<Medicao> lista = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sensorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMedicao(rs));
                }
            }
        }
        return lista;
    }

    public List<Medicao> buscarTodas() throws SQLException, DadosInvalidosException {
        String sql = "SELECT id, sensor_id, tipo_sensor, valor, unidade, data_hora " +
                     "FROM medicoes ORDER BY data_hora DESC, id DESC";
        List<Medicao> lista = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearMedicao(rs));
            }
        }
        return lista;
    }

    private Medicao mapearMedicao(ResultSet rs) throws SQLException, DadosInvalidosException {
        return new Medicao(
                rs.getLong("id"),
                rs.getString("sensor_id"),
                rs.getString("tipo_sensor"),
                rs.getDouble("valor"),
                rs.getString("unidade"),
                rs.getTimestamp("data_hora").toLocalDateTime()
        );
    }
}
