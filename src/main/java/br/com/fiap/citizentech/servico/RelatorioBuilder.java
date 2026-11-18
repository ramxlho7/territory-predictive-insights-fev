package br.com.fiap.citizentech.servico;

import br.com.fiap.citizentech.dominio.Alerta;
import br.com.fiap.citizentech.dominio.DadosTerritorio;
import br.com.fiap.citizentech.dominio.EstacaoHidrologica;
import br.com.fiap.citizentech.dominio.Medicao;
import br.com.fiap.citizentech.dominio.Sensor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Padrao Criacional: Builder.
 * Monta passo a passo o relatorio consolidado de situacao territorial.
 */
public class RelatorioBuilder {
    private EstacaoHidrologica estacao;
    private DadosTerritorio dadosTerritorio;
    private List<Sensor> sensores = Collections.emptyList();
    private List<Medicao> medicoes = Collections.emptyList();
    private List<Alerta> alertas = Collections.emptyList();

    public RelatorioBuilder comEstacao(EstacaoHidrologica estacao) {
        this.estacao = estacao;
        return this;
    }

    public RelatorioBuilder comDadosTerritorio(DadosTerritorio dadosTerritorio) {
        this.dadosTerritorio = dadosTerritorio;
        return this;
    }

    public RelatorioBuilder comSensores(List<Sensor> sensores) {
        if (sensores != null) {
            this.sensores = sensores;
        }
        return this;
    }

    public RelatorioBuilder comUltimasMedicoes(List<Medicao> medicoes) {
        if (medicoes != null) {
            this.medicoes = medicoes;
        }
        return this;
    }

    public RelatorioBuilder comUltimosAlertas(List<Alerta> alertas) {
        if (alertas != null) {
            this.alertas = alertas;
        }
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ConfiguracaoSistema cfg = ConfiguracaoSistema.getInstance();

        sb.append("====================================================================\n");
        sb.append("       RELATÓRIO DE MONITORAMENTO TERRITORIAL - CITIZENTECH         \n");
        sb.append("       Gerado em: ").append(LocalDateTime.now().format(fmt)).append("\n");
        sb.append("====================================================================\n");

        if (estacao != null) {
            sb.append(String.format("Estação: %s (%s) | Região: %s\n",
                    estacao.getNome(), estacao.getId(), estacao.getRegiao()));
            sb.append("Status Operacional: ").append(estacao.verificarStatus()).append("\n");
        }

        if (dadosTerritorio != null) {
            sb.append(String.format("População Exposta: %d habitantes | Área Vulnerável: %.2f km²\n",
                    dadosTerritorio.getPopulacaoExposta(), dadosTerritorio.getAreaVulneravelKm2()));
        }

        sb.append("--------------------------------------------------------------------\n");
        sb.append("1. SENSORES INSTALADOS E LIMIARES ATIVOS:\n");
        for (Sensor s : sensores) {
            String estado = s.verificarFuncionamento() ? "ONLINE" : "OFFLINE";
            sb.append(String.format("   - [%s] %-13s (%s) | Limiares (At/Al/Cr): %.1f / %.1f / %.1f %s\n",
                    s.getId(), s.getTipo(), estado,
                    cfg.getLimiarAtencao(s.getTipo()),
                    cfg.getLimiarAlerta(s.getTipo()),
                    cfg.getLimiarCritico(s.getTipo()),
                    s.getUnidadeMedida()));
        }

        sb.append("--------------------------------------------------------------------\n");
        sb.append("2. ÚLTIMAS MEDIÇÕES PERSISTIDAS (até 5 mais recentes):\n");
        if (medicoes.isEmpty()) {
            sb.append("   Nenhuma medição registrada no banco de dados.\n");
        } else {
            medicoes.stream().limit(5).forEach(m -> sb.append("   ").append(m).append("\n"));
        }

        sb.append("--------------------------------------------------------------------\n");
        sb.append("3. ÚLTIMOS ALERTAS EMITIDOS (até 5 mais recentes):\n");
        if (alertas.isEmpty()) {
            sb.append("   Nenhum alerta emitido até o momento.\n");
        } else {
            alertas.stream().limit(5).forEach(a -> sb.append("   ").append(a).append("\n"));
        }
        sb.append("====================================================================\n");
        return sb.toString();
    }
}
