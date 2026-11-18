package br.com.fiap.citizentech;

import br.com.fiap.citizentech.dominio.DadosTerritorio;
import br.com.fiap.citizentech.dominio.Medicao;
import br.com.fiap.citizentech.dominio.SensorPluviometrico;
import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedicaoTest {

    @Test
    void deveCriarMedicaoValidaQuandoDadosEstaoDentroDaFaixa() throws DadosInvalidosException {
        // Arrange
        String sensorId = "PLUV-01";
        String tipo = "PLUVIOMETRO";
        double valor = 42.5;
        String unidade = "mm/h";

        // Act
        Medicao medicao = new Medicao(sensorId, tipo, valor, unidade);

        // Assert
        assertEquals("PLUV-01", medicao.getSensorId());
        assertEquals(42.5, medicao.getValor(), 0.001);
        assertNotNull(medicao.getDataHora());
    }

    @Test
    void deveLancarDadosInvalidosExceptionQuandoMedicaoNegativa() {
        // Arrange
        String sensorId = "PLUV-01";
        double valorNegativo = -12.0;

        // Act & Assert
        assertThrows(DadosInvalidosException.class, () ->
                new Medicao(sensorId, "PLUVIOMETRO", valorNegativo, "mm/h")
        );
    }

    @Test
    void deveLancarDadosInvalidosExceptionQuandoLimiaresTerritorioForaDeOrdem() {
        // Arrange
        double atencao = 60.0;
        double alerta = 40.0; // menor que atencao -> invalido
        double critico = 80.0;

        // Act & Assert
        assertThrows(DadosInvalidosException.class, () ->
                new DadosTerritorio("Zona Sul", 10000, 3.5, atencao, alerta, critico)
        );
    }

    @Test
    void deveLancarFonteIndisponivelExceptionQuandoSensorEstaOffline() {
        // Arrange
        SensorPluviometrico sensor = new SensorPluviometrico("PLUV-OFF");
        sensor.setOperacional(false);

        // Act & Assert
        assertThrows(FonteIndisponivelException.class, sensor::lerValor);
    }
}
