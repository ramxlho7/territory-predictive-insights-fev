package br.com.fiap.citizentech;

import br.com.fiap.citizentech.dominio.EstrategiaPrevisao;
import br.com.fiap.citizentech.dominio.Medicao;
import br.com.fiap.citizentech.dominio.PrevisaoComparacao;
import br.com.fiap.citizentech.dominio.PrevisaoMediaMovel;
import br.com.fiap.citizentech.dominio.TipoPrevisao;
import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.servico.FabricaPrevisao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrevisaoMediaMovelTest {

    @Test
    void deveCalcularMediaMovelDasUltimasCincoLeituras() throws DadosInvalidosException {
        // Arrange
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL);
        List<Medicao> historico = List.of(
                new Medicao("PLUV-01", "PLUVIOMETRO", 50.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 40.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 30.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 20.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 10.0, "mm/h")
        );

        // Act
        double resultado = estrategia.prever(historico);

        // Assert
        assertTrue(estrategia instanceof PrevisaoMediaMovel);
        assertEquals(30.0, resultado, 0.001);
    }

    @Test
    void deveCalcularPrevisaoComparacaoEntrePeriodosConsecutivos() throws DadosInvalidosException {
        // Arrange
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(TipoPrevisao.COMPARACAO_PERIODOS);
        // 3 recentes = media 40.0; 3 anteriores = media 20.0 -> tendencia +20 -> projecao = 40 + 10 = 50.0
        List<Medicao> historico = List.of(
                new Medicao("PLUV-01", "PLUVIOMETRO", 45.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 40.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 35.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 25.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 20.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 15.0, "mm/h")
        );

        // Act
        double resultado = estrategia.prever(historico);

        // Assert
        assertTrue(estrategia instanceof PrevisaoComparacao);
        assertEquals(50.0, resultado, 0.001);
    }

    @Test
    void deveLancarDadosInvalidosExceptionQuandoHistoricoInsuficiente() throws DadosInvalidosException {
        // Arrange
        EstrategiaPrevisao estrategia = new PrevisaoMediaMovel(5);
        List<Medicao> historicoCurto = List.of(
                new Medicao("PLUV-01", "PLUVIOMETRO", 25.0, "mm/h"),
                new Medicao("PLUV-01", "PLUVIOMETRO", 30.0, "mm/h")
        );

        // Act & Assert
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(historicoCurto));
    }
}
