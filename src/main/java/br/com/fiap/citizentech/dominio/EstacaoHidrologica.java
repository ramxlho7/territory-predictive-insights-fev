package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EstacaoHidrologica extends ComponenteTerritorial {
    private final DadosTerritorio dadosTerritorio;
    private final List<Sensor> sensores;

    public EstacaoHidrologica(String id, String nome, DadosTerritorio dadosTerritorio)
            throws DadosInvalidosException {
        super(id, nome, dadosTerritorio.getRegiao(), "OPERACIONAL");
        this.dadosTerritorio = dadosTerritorio;
        this.sensores = new ArrayList<>();
    }

    public void adicionarSensor(Sensor sensor) throws DadosInvalidosException {
        if (sensor == null) {
            throw new DadosInvalidosException("Não é possível vincular um sensor nulo à estação.");
        }
        this.sensores.add(sensor);
    }

    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }

    public DadosTerritorio getDadosTerritorio() {
        return dadosTerritorio;
    }

    @Override
    public String verificarStatus() {
        if (sensores.isEmpty()) {
            this.status = "SEM_SENSORES";
            return this.status;
        }
        long ativos = sensores.stream().filter(Sensor::verificarFuncionamento).count();
        if (ativos == 0) {
            this.status = "INOPERANTE (0/" + sensores.size() + " sensores ativos)";
        } else if (ativos < sensores.size()) {
            this.status = "DEGRADADO (" + ativos + "/" + sensores.size() + " sensores ativos)";
        } else {
            this.status = "OPERACIONAL (" + ativos + "/" + sensores.size() + " sensores ativos)";
        }
        return this.status;
    }
}
