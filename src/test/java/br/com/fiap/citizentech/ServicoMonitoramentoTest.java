package br.com.fiap.citizentech;

import br.com.fiap.citizentech.dominio.Alerta;
import br.com.fiap.citizentech.dominio.Medicao;
import br.com.fiap.citizentech.dominio.NivelAlerta;
import br.com.fiap.citizentech.dominio.Sensor;
import br.com.fiap.citizentech.dominio.SensorPluviometrico;
import br.com.fiap.citizentech.dominio.TipoPrevisao;
import br.com.fiap.citizentech.persistencia.AlertaDAO;
import br.com.fiap.citizentech.persistencia.MedicaoDAO;
import br.com.fiap.citizentech.servico.ServicoMonitoramento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoDAO medicaoDAO;

    @Mock
    private AlertaDAO alertaDAO;

    private ServicoMonitoramento servico;

    @BeforeEach
    void setUp() {
        servico = new ServicoMonitoramento(medicaoDAO, alertaDAO);
    }

    @Test
    void deveEmitirEPersistirAlertaCriticoQuandoPrevisaoUltrapassaLimiteCritico() throws Exception {
        // Arrange
        Sensor sensor = new SensorPluviometrico("PLUV-01");
        List<Medicao> historicoCritico = List.of(
                new Medicao("PLUV-01", "PLUVIOMETRO", 85.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 80.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 78.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 82.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 80.0, "mm/h")
        );
        when(medicaoDAO.buscarPorSensor("PLUV-01")).thenReturn(historicoCritico);

        // Act
        ServicoMonitoramento.ResultadoPrevisao resultado =
                servico.executarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert
        assertTrue(resultado.alerta().isPresent());
        assertEquals(NivelAlerta.CRITICO, resultado.alerta().get().getNivel());
        assertEquals(81.0, resultado.valorPrevisto(), 0.001);
        verify(alertaDAO, times(1)).salvar(any(Alerta.class));
    }

    @Test
    void naoDeveEmitirAlertaQuandoPrevisaoEstaAbaixoDoLimiarAtencao() throws Exception {
        // Arrange
        Sensor sensor = new SensorPluviometrico("PLUV-01");
        List<Medicao> historicoNormal = List.of(
                new Medicao("PLUV-01", "PLUVIOMETRO", 10.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 12.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 8.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 11.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 9.0, "mm/h")
        );
        when(medicaoDAO.buscarPorSensor("PLUV-01")).thenReturn(historicoNormal);

        // Act
        ServicoMonitoramento.ResultadoPrevisao resultado =
                servico.executarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);

        // Assert
        assertTrue(resultado.alerta().isEmpty());
        assertEquals(10.0, resultado.valorPrevisto(), 0.001);
        verify(alertaDAO, never()).salvar(any(Alerta.class));
    }
}
