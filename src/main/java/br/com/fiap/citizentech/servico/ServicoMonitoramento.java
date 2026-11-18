package br.com.fiap.citizentech.servico;

import br.com.fiap.citizentech.dominio.Alerta;
import br.com.fiap.citizentech.dominio.EstrategiaPrevisao;
import br.com.fiap.citizentech.dominio.Medicao;
import br.com.fiap.citizentech.dominio.NivelAlerta;
import br.com.fiap.citizentech.dominio.Sensor;
import br.com.fiap.citizentech.dominio.TipoPrevisao;
import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.excecao.FonteIndisponivelException;
import br.com.fiap.citizentech.persistencia.AlertaDAO;
import br.com.fiap.citizentech.persistencia.MedicaoDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicoMonitoramento {
    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO) {
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
    }

    public Medicao coletarEPersistir(Sensor sensor)
            throws FonteIndisponivelException, DadosInvalidosException, SQLException {
        if (sensor == null) {
            throw new DadosInvalidosException("Sensor não pode ser nulo para coleta.");
        }
        double valorLido = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valorLido, sensor.getUnidadeMedida());
        return medicaoDAO.salvar(medicao);
    }

    public Medicao registrarMedicaoManual(Sensor sensor, double valorManual)
            throws FonteIndisponivelException, DadosInvalidosException, SQLException {
        if (sensor == null) {
            throw new DadosInvalidosException("Sensor não pode ser nulo.");
        }
        if (!sensor.verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor [" + sensor.getId() + "] está offline.");
        }
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valorManual, sensor.getUnidadeMedida());
        return medicaoDAO.salvar(medicao);
    }

    public ResultadoPrevisao executarPrevisao(Sensor sensor, TipoPrevisao tipoPrevisao)
            throws DadosInvalidosException, SQLException {
        List<Medicao> historico = medicaoDAO.buscarPorSensor(sensor.getId());
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(tipoPrevisao);

        double valorPrevisto = estrategia.prever(historico);
        String explicacao = estrategia.explicarPrevisao(historico, valorPrevisto);

        Optional<Alerta> alertaEmitido = avaliarEGerarAlerta(sensor, estrategia, valorPrevisto);
        return new ResultadoPrevisao(valorPrevisto, sensor.getUnidadeMedida(), explicacao, alertaEmitido);
    }

    public Optional<Alerta> avaliarEGerarAlerta(Sensor sensor, EstrategiaPrevisao estrategia, double valorPrevisto)
            throws SQLException {
        ConfiguracaoSistema cfg = ConfiguracaoSistema.getInstance();
        String tipo = sensor.getTipo();

        double limiarAtencao = cfg.getLimiarAtencao(tipo);
        double limiarAlerta = cfg.getLimiarAlerta(tipo);
        double limiarCritico = cfg.getLimiarCritico(tipo);

        NivelAlerta nivel = null;
        double limiteEstourado = 0.0;

        if (valorPrevisto >= limiarCritico) {
            nivel = NivelAlerta.CRITICO;
            limiteEstourado = limiarCritico;
        } else if (valorPrevisto >= limiarAlerta) {
            nivel = NivelAlerta.ALERTA;
            limiteEstourado = limiarAlerta;
        } else if (valorPrevisto >= limiarAtencao) {
            nivel = NivelAlerta.ATENCAO;
            limiteEstourado = limiarAtencao;
        }

        if (nivel == null) {
            return Optional.empty();
        }

        String justificativa = String.format(
            "Sensor %s (%s) atingiu previsão de %.2f %s, ultrapassando o limite de %s (%.2f %s) via %s. Ação: %s",
            sensor.getId(), tipo, valorPrevisto, sensor.getUnidadeMedida(),
            nivel.getRotulo(), limiteEstourado, sensor.getUnidadeMedida(),
            estrategia.getNomeEstrategia(), nivel.getOrientacao()
        );

        Alerta alerta = new Alerta(
                sensor.getId(),
                tipo,
                nivel,
                valorPrevisto,
                limiteEstourado,
                estrategia.getNomeEstrategia(),
                justificativa
        );
        alertaDAO.salvar(alerta);
        return Optional.of(alerta);
    }

    /**
     * Bonus (+0.5): Simulacao de ciclo de 24 horas de evento climatico extremo.
     */
    public List<String> simularCiclo24Horas(Sensor sensor)
            throws DadosInvalidosException, SQLException {
        double[] curvaChuva24h = {
            4.0, 5.5, 6.0, 8.0, 12.0, 18.0,
            24.0, 31.0, 38.0, 46.0, 55.0, 68.0,
            82.0, 91.0, 86.0, 74.0, 60.0, 48.0,
            35.0, 26.0, 19.0, 14.0, 9.0, 5.0
        };
        List<String> logEventos = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().minusHours(24);

        for (int hora = 0; hora < curvaChuva24h.length; hora++) {
            double valor = curvaChuva24h[hora];
            Medicao m = new Medicao(null, sensor.getId(), sensor.getTipo(), valor,
                    sensor.getUnidadeMedida(), base.plusHours(hora));
            medicaoDAO.salvar(m);

            if (hora >= 4) {
                ResultadoPrevisao res = executarPrevisao(sensor, TipoPrevisao.MEDIA_MOVEL);
                String statusAlerta = res.alerta()
                        .map(a -> " -> [" + a.getNivel().name() + "] " + a.getJustificativa())
                        .orElse(" -> [NORMAL] Sem alerta.");
                logEventos.add(String.format("Hora %02d:00 | Leitura: %5.1f %s | Previsão MM5: %5.2f %s%s",
                        hora, valor, sensor.getUnidadeMedida(), res.valorPrevisto(), res.unidade(), statusAlerta));
            } else {
                logEventos.add(String.format("Hora %02d:00 | Leitura: %5.1f %s | Acumulando janela mínima (silêncio preventivo)",
                        hora, valor, sensor.getUnidadeMedida()));
            }
        }
        return logEventos;
    }

    public List<Medicao> listarHistoricoPorSensor(String sensorId) throws SQLException, DadosInvalidosException {
        return medicaoDAO.buscarPorSensor(sensorId);
    }

    public List<Medicao> listarTodasMedicoes() throws SQLException, DadosInvalidosException {
        return medicaoDAO.buscarTodas();
    }

    public List<Alerta> listarAlertasEmitidos() throws SQLException {
        return alertaDAO.buscarTodos();
    }

    public record ResultadoPrevisao(
            double valorPrevisto,
            String unidade,
            String interpretacao,
            Optional<Alerta> alerta
    ) {}
}
