package br.com.fiap.citizentech.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
    private static final String DEFAULT_URL = "jdbc:h2:file:./data/territorydb;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private final String jdbcUrl;

    public ConnectionFactory() {
        this(DEFAULT_URL);
    }

    public ConnectionFactory(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, USER, PASSWORD);
    }

    public void inicializarBanco() throws SQLException {
        String sqlMedicoes =
                "CREATE TABLE IF NOT EXISTS medicoes (" +
                "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "    sensor_id VARCHAR(50) NOT NULL," +
                "    tipo_sensor VARCHAR(50) NOT NULL," +
                "    valor DOUBLE NOT NULL," +
                "    unidade VARCHAR(20) NOT NULL," +
                "    data_hora TIMESTAMP NOT NULL" +
                ")";

        String sqlAlertas =
                "CREATE TABLE IF NOT EXISTS alertas (" +
                "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "    sensor_id VARCHAR(50) NOT NULL," +
                "    tipo_sensor VARCHAR(50) NOT NULL," +
                "    nivel VARCHAR(20) NOT NULL," +
                "    valor_previsto DOUBLE NOT NULL," +
                "    limite_ultrapassado DOUBLE NOT NULL," +
                "    estrategia_utilizada VARCHAR(100) NOT NULL," +
                "    justificativa VARCHAR(500) NOT NULL," +
                "    data_hora TIMESTAMP NOT NULL" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlMedicoes);
            stmt.execute(sqlAlertas);
        }
    }
}
