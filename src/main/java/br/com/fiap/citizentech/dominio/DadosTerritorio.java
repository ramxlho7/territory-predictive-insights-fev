package br.com.fiap.citizentech.dominio;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;
import br.com.fiap.citizentech.servico.ConfiguracaoSistema;

public class DadosTerritorio {
    private String regiao;
    private int populacaoExposta;
    private double areaVulneravelKm2;
    private double limiarAtencaoChuva;
    private double limiarAlertaChuva;
    private double limiarCriticoChuva;

    public DadosTerritorio(String regiao, int populacaoExposta, double areaVulneravelKm2,
                           double limiarAtencaoChuva, double limiarAlertaChuva, double limiarCriticoChuva)
            throws DadosInvalidosException {
        setRegiao(regiao);
        setPopulacaoExposta(populacaoExposta);
        setAreaVulneravelKm2(areaVulneravelKm2);
        atualizarLimiaresChuva(limiarAtencaoChuva, limiarAlertaChuva, limiarCriticoChuva);
    }

    public void atualizarLimiaresChuva(double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        if (atencao <= 0 || alerta <= 0 || critico <= 0) {
            throw new DadosInvalidosException("Os limiares de chuva devem ser valores estritamente positivos (mm/h).");
        }
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(
                String.format("Hierarquia de limiares inválida (%.1f / %.1f / %.1f): deve obedecer Atenção < Alerta < Crítico.",
                    atencao, alerta, critico)
            );
        }
        this.limiarAtencaoChuva = atencao;
        this.limiarAlertaChuva = alerta;
        this.limiarCriticoChuva = critico;

        // Sincroniza com o Singleton ConfiguracaoSistema
        ConfiguracaoSistema.getInstance().atualizarLimiares("PLUVIOMETRO", atencao, alerta, critico);
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A região do território não pode ser vazia.");
        }
        this.regiao = regiao.trim();
    }

    public int getPopulacaoExposta() {
        return populacaoExposta;
    }

    public void setPopulacaoExposta(int populacaoExposta) throws DadosInvalidosException {
        if (populacaoExposta < 0) {
            throw new DadosInvalidosException("A população exposta não pode ser negativa: " + populacaoExposta);
        }
        this.populacaoExposta = populacaoExposta;
    }

    public double getAreaVulneravelKm2() {
        return areaVulneravelKm2;
    }

    public void setAreaVulneravelKm2(double areaVulneravelKm2) throws DadosInvalidosException {
        if (areaVulneravelKm2 <= 0) {
            throw new DadosInvalidosException("A área vulnerável (km²) deve ser maior que zero: " + areaVulneravelKm2);
        }
        this.areaVulneravelKm2 = areaVulneravelKm2;
    }

    public double getLimiarAtencaoChuva() {
        return limiarAtencaoChuva;
    }

    public double getLimiarAlertaChuva() {
        return limiarAlertaChuva;
    }

    public double getLimiarCriticoChuva() {
        return limiarCriticoChuva;
    }
}
