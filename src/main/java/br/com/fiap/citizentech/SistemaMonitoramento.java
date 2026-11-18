package br.com.fiap.citizentech;

import br.com.fiap.citizentech.dominio.Alerta;
import br.com.fiap.citizentech.dominio.DadosTerritorio;
import br.com.fiap.citizentech.dominio.EstacaoHidrologica;
import br.com.fiap.citizentech.dominio.Medicao;
import br.com.fiap.citizentech.dominio.Sensor;
import br.com.fiap.citizentech.dominio.SensorNivelRio;
import br.com.fiap.citizentech.dominio.SensorPluviometrico;
import br.com.fiap.citizentech.dominio.SensorUmidadeSolo;
import br.com.fiap.citizentech.dominio.TipoPrevisao;
import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.excecao.FonteIndisponivelException;
import br.com.fiap.citizentech.persistencia.AlertaDAO;
import br.com.fiap.citizentech.persistencia.ConnectionFactory;
import br.com.fiap.citizentech.persistencia.MedicaoDAO;
import br.com.fiap.citizentech.servico.ExportadorCSV;
import br.com.fiap.citizentech.servico.RelatorioBuilder;
import br.com.fiap.citizentech.servico.ServicoMonitoramento;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class SistemaMonitoramento {
    private final EstacaoHidrologica estacao;
    private final DadosTerritorio dadosTerritorio;
    private final ServicoMonitoramento servico;
    private final ExportadorCSV exportadorCSV;
    private final Scanner scanner;

    public SistemaMonitoramento() throws SQLException, DadosInvalidosException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.inicializarBanco();

        MedicaoDAO medicaoDAO = new MedicaoDAO(connectionFactory);
        AlertaDAO alertaDAO = new AlertaDAO(connectionFactory);
        this.servico = new ServicoMonitoramento(medicaoDAO, alertaDAO);
        this.exportadorCSV = new ExportadorCSV();
        this.scanner = new Scanner(System.in);

        // Inicializa territorio e estacao hidrologica (sem dados pessoais de individuos)
        this.dadosTerritorio = new DadosTerritorio(
                "Microbacia Córrego Pirajuçara - Zona Sul",
                18500,
                4.2,
                25.0,
                50.0,
                75.0
        );

        this.estacao = new EstacaoHidrologica(
                "EST-PIR-01",
                "Estação Comunitária de Monitoramento Pluviométrico e Fluvial",
                dadosTerritorio
        );

        this.estacao.adicionarSensor(new SensorPluviometrico("PLUV-01"));
        this.estacao.adicionarSensor(new SensorNivelRio("RIO-01"));
        this.estacao.adicionarSensor(new SensorUmidadeSolo("SOLO-01"));
    }

    public static void main(String[] args) {
        try {
            SistemaMonitoramento app = new SistemaMonitoramento();
            app.executarMenu();
        } catch (SQLException e) {
            System.err.println("[ERRO CRÍTICO DE BANCO] Falha ao inicializar H2: " + e.getMessage());
        } catch (DadosInvalidosException e) {
            System.err.println("[ERRO DE CONFIGURAÇÃO INICIAL] " + e.getMessage());
        }
    }

    public void executarMenu() {
        boolean executando = true;
        while (executando) {
            exibirCabecalhoMenu();
            String opcao = scanner.nextLine().trim();

            try {
                switch (opcao) {
                    case "1" -> menuSensoresELeituras();
                    case "2" -> menuPrevisoesEAlertas();
                    case "3" -> menuHistoricoPersistido();
                    case "4" -> menuGerenciarTerritorio();
                    case "5" -> exibirStatusCompleto();
                    case "6" -> executarSimulacao24Horas();
                    case "7" -> exportarHistoricoCSV();
                    case "0" -> {
                        executando = false;
                        System.out.println("Encerrando Territory Predictive Insights. Histórico salvo em ./data/territorydb.");
                    }
                    default -> System.out.println("[AVISO] Opção inválida. Digite um número entre 0 e 7.");
                }
            } catch (DadosInvalidosException e) {
                System.out.println("\n[ERRO DE VALIDAÇÃO] " + e.getMessage());
            } catch (FonteIndisponivelException e) {
                System.out.println("\n[FALHA DE SENSOR/FONTE] " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("\n[ERRO DE ENTRADA] Valor numérico inválido. Utilize ponto (.) para decimais.");
            } catch (SQLException e) {
                System.out.println("\n[ERRO DE PERSISTÊNCIA] Falha ao acessar o banco de dados: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("\n[ERRO DE ARQUIVO] Falha ao exportar CSV: " + e.getMessage());
            }
        }
    }

    private void exibirCabecalhoMenu() {
        System.out.println("\n==============================================================");
        System.out.println("  CITIZENTECH | TERRITORY PREDICTIVE INSIGHTS (ALAGAMENTOS)   ");
        System.out.println("==============================================================");
        System.out.println("1. Verificar sensores e realizar novas leituras");
        System.out.println("2. Rodar previsões e gerar alertas automáticos");
        System.out.println("3. Ver histórico persistido (medições por sensor e alertas)");
        System.out.println("4. Gerenciar dados do território e limiares");
        System.out.println("5. Status completo do território (Relatório Consolidado)");
        System.out.println("6. [Bônus] Simular ciclo de 24 horas (chuva intensa)");
        System.out.println("7. [Bônus] Exportar histórico consultado em arquivo CSV");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private void menuSensoresELeituras()
            throws FonteIndisponivelException, DadosInvalidosException, SQLException {
        System.out.println("\n--- SENSORES DA ESTAÇÃO (" + estacao.verificarStatus() + ") ---");
        List<Sensor> sensores = estacao.getSensores();
        for (int i = 0; i < sensores.size(); i++) {
            Sensor s = sensores.get(i);
            System.out.printf("%d. [%s] %s (%s) - Status: %s%n",
                    i + 1, s.getId(), s.getTipo(), s.getUnidadeMedida(),
                    s.verificarFuncionamento() ? "ONLINE" : "OFFLINE");
        }
        System.out.println("\nAções:");
        System.out.println("A - Coletar leitura automática de TODOS os sensores");
        System.out.println("B - Registrar leitura MANUAL em um sensor (testar validação)");
        System.out.println("C - Alternar status ONLINE/OFFLINE de um sensor (testar FonteIndisponivelException)");
        System.out.print("Escolha (A/B/C): ");
        String acao = scanner.nextLine().trim().toUpperCase();

        switch (acao) {
            case "A" -> {
                for (Sensor s : sensores) {
                    try {
                        Medicao m = servico.coletarEPersistir(s);
                        System.out.println("  [OK] Gravado no banco: " + m);
                    } catch (FonteIndisponivelException | DadosInvalidosException e) {
                        System.out.println("  [FALHA NO SENSOR " + s.getId() + "] " + e.getMessage());
                    }
                }
            }
            case "B" -> {
                Sensor escolhido = selecionarSensor();
                System.out.print("Informe o valor da leitura (" + escolhido.getUnidadeMedida() + "): ");
                double valor = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
                Medicao m = servico.registrarMedicaoManual(escolhido, valor);
                System.out.println("[OK] Medição manual validada e persistida: " + m);
            }
            case "C" -> {
                Sensor escolhido = selecionarSensor();
                boolean novoEstado = !escolhido.verificarFuncionamento();
                escolhido.setOperacional(novoEstado);
                System.out.println("[OK] Sensor " + escolhido.getId() + " alterado para: "
                        + (novoEstado ? "ONLINE" : "OFFLINE"));
            }
            default -> System.out.println("[AVISO] Sub-opção inválida.");
        }
    }

    private void menuPrevisoesEAlertas() throws DadosInvalidosException, SQLException {
        Sensor sensor = selecionarSensor();
        System.out.println("\nEscolha a estratégia de previsão:");
        System.out.println("1. Média Móvel (últimas 5 leituras)");
        System.out.println("2. Comparação de Períodos (3 recentes vs. 3 anteriores)");
        System.out.print("Opção: ");
        String op = scanner.nextLine().trim();

        TipoPrevisao tipo = switch (op) {
            case "1" -> TipoPrevisao.MEDIA_MOVEL;
            case "2" -> TipoPrevisao.COMPARACAO_PERIODOS;
            default -> throw new DadosInvalidosException("Opção de estratégia inválida: " + op);
        };

        ServicoMonitoramento.ResultadoPrevisao resultado = servico.executarPrevisao(sensor, tipo);
        System.out.println("\n--- RESULTADO DA PREVISÃO ---");
        System.out.printf("Sensor: %s (%s)%n", sensor.getId(), sensor.getTipo());
        System.out.printf("Valor Previsto: %.2f %s%n", resultado.valorPrevisto(), resultado.unidade());
        System.out.println("Interpretação: " + resultado.interpretacao());

        if (resultado.alerta().isPresent()) {
            Alerta alerta = resultado.alerta().get();
            System.out.println("\n>>> ALERTA EMITIDO E GRAVADO NO BANCO <<<");
            System.out.println(alerta);
        } else {
            System.out.println("\n[STATUS NORMAL] Previsão dentro dos limites seguros. Nenhum alerta emitido.");
        }
    }

    private void menuHistoricoPersistido() throws SQLException, DadosInvalidosException {
        System.out.println("\n1. Listar histórico de medições por sensor");
        System.out.println("2. Listar todos os alertas já emitidos");
        System.out.print("Escolha: ");
        String op = scanner.nextLine().trim();

        if ("1".equals(op)) {
            Sensor sensor = selecionarSensor();
            List<Medicao> historico = servico.listarHistoricoPorSensor(sensor.getId());
            System.out.println("\n--- HISTÓRICO DO SENSOR " + sensor.getId() + " (" + historico.size() + " registros) ---");
            if (historico.isEmpty()) {
                System.out.println("Nenhuma medição encontrada no banco para este sensor.");
            } else {
                historico.forEach(System.out::println);
            }
        } else if ("2".equals(op)) {
            List<Alerta> alertas = servico.listarAlertasEmitidos();
            System.out.println("\n--- ALERTAS EMITIDOS (" + alertas.size() + " registros) ---");
            if (alertas.isEmpty()) {
                System.out.println("Nenhum alerta registrado no banco.");
            } else {
                alertas.forEach(System.out::println);
            }
        } else {
            System.out.println("[AVISO] Opção inválida.");
        }
    }

    private void menuGerenciarTerritorio() throws DadosInvalidosException {
        System.out.println("\n--- DADOS DO TERRITÓRIO ---");
        System.out.println("Região: " + dadosTerritorio.getRegiao());
        System.out.println("População Exposta: " + dadosTerritorio.getPopulacaoExposta());
        System.out.printf("Área Vulnerável: %.2f km²%n", dadosTerritorio.getAreaVulneravelKm2());
        System.out.printf("Limiares Pluviométricos (mm/h) -> Atenção: %.1f | Alerta: %.1f | Crítico: %.1f%n",
                dadosTerritorio.getLimiarAtencaoChuva(),
                dadosTerritorio.getLimiarAlertaChuva(),
                dadosTerritorio.getLimiarCriticoChuva());

        System.out.println("\n1. Alterar população exposta");
        System.out.println("2. Alterar limiares pluviométricos (Atenção / Alerta / Crítico)");
        System.out.println("0. Voltar");
        System.out.print("Escolha: ");
        String op = scanner.nextLine().trim();

        if ("1".equals(op)) {
            System.out.print("Nova população exposta: ");
            int pop = Integer.parseInt(scanner.nextLine().trim());
            dadosTerritorio.setPopulacaoExposta(pop);
            System.out.println("[OK] População exposta atualizada para " + pop + " habitantes.");
        } else if ("2".equals(op)) {
            System.out.print("Novo limiar ATENÇÃO (mm/h): ");
            double at = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            System.out.print("Novo limiar ALERTA (mm/h): ");
            double al = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            System.out.print("Novo limiar CRÍTICO (mm/h): ");
            double cr = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            dadosTerritorio.atualizarLimiaresChuva(at, al, cr);
            System.out.println("[OK] Limiares atualizados e sincronizados com ConfiguracaoSistema.");
        }
    }

    private void exibirStatusCompleto() throws SQLException, DadosInvalidosException {
        String relatorio = new RelatorioBuilder()
                .comEstacao(estacao)
                .comDadosTerritorio(dadosTerritorio)
                .comSensores(estacao.getSensores())
                .comUltimasMedicoes(servico.listarTodasMedicoes())
                .comUltimosAlertas(servico.listarAlertasEmitidos())
                .build();
        System.out.println("\n" + relatorio);
    }

    private void executarSimulacao24Horas() throws DadosInvalidosException, SQLException {
        Sensor pluviometro = estacao.getSensores().get(0);
        System.out.println("\n--- SIMULAÇÃO DE 24 HORAS (CICLO DE TEMPESTADE NO SENSOR " + pluviometro.getId() + ") ---");
        List<String> eventos = servico.simularCiclo24Horas(pluviometro);
        eventos.forEach(System.out::println);
        System.out.println("[OK] 24 medições e alertas correspondentes persistidos no banco H2.");
    }

    private void exportarHistoricoCSV() throws SQLException, DadosInvalidosException, IOException {
        List<Medicao> medicoes = servico.listarTodasMedicoes();
        List<Alerta> alertas = servico.listarAlertasEmitidos();
        exportadorCSV.exportarMedicoes(medicoes, "historico_medicoes.csv");
        exportadorCSV.exportarAlertas(alertas, "historico_alertas.csv");
        System.out.printf("[OK] Exportados %d medições para 'historico_medicoes.csv' e %d alertas para 'historico_alertas.csv'.%n",
                medicoes.size(), alertas.size());
    }

    private Sensor selecionarSensor() throws DadosInvalidosException {
        List<Sensor> sensores = estacao.getSensores();
        System.out.println("\nSelecione o sensor:");
        for (int i = 0; i < sensores.size(); i++) {
            Sensor s = sensores.get(i);
            System.out.printf("%d. [%s] %s (%s)%n", i + 1, s.getId(), s.getTipo(), s.getUnidadeMedida());
        }
        System.out.print("Número do sensor: ");
        int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
        if (idx < 0 || idx >= sensores.size()) {
            throw new DadosInvalidosException("Índice de sensor inválido.");
        }
        return sensores.get(idx);
    }
}
