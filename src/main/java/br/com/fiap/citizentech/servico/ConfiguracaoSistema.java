package br.com.fiap.citizentech.servico;

import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.util.HashMap;
import java.util.Map;

/**
 * Padrao Criacional: Singleton.
 * Garante uma unica instancia centralizada dos limiares operacionais em todo o sistema.
 */
public class ConfiguracaoSistema {
    private static volatile ConfiguracaoSistema instancia;

    private final Map<String, double[]> limiaresPorTipo;

    private ConfiguracaoSistema() {
        this.limiaresPorTipo = new HashMap<>();
        // Formato: [ATENCAO, ALERTA, CRITICO]
        limiaresPorTipo.put("PLUVIOMETRO", new double[]{25.0, 50.0, 75.0});   // mm/h
        limiaresPorTipo.put("NIVEL_RIO", new double[]{2.20, 3.10, 4.00});     // metros
        limiaresPorTipo.put("UMIDADE_SOLO", new double[]{65.0, 80.0, 90.0});  // %
    }

    public static ConfiguracaoSistema getInstance() {
        if (instancia == null) {
            synchronized (ConfiguracaoSistema.class) {
                if (instancia == null) {
                    instancia = new ConfiguracaoSistema();
                }
            }
        }
        return instancia;
    }

    public double getLimiarAtencao(String tipoSensor) {
        return obterArrayLimiares(tipoSensor)[0];
    }

    public double getLimiarAlerta(String tipoSensor) {
        return obterArrayLimiares(tipoSensor)[1];
    }

    public double getLimiarCritico(String tipoSensor) {
        return obterArrayLimiares(tipoSensor)[2];
    }

    public void atualizarLimiares(String tipoSensor, double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        if (tipoSensor == null || !limiaresPorTipo.containsKey(tipoSensor.toUpperCase())) {
            throw new DadosInvalidosException("Tipo de sensor desconhecido na configuração: " + tipoSensor);
        }
        if (atencao <= 0 || !(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(
                "Limiares inválidos: devem ser positivos e obedecer Atenção < Alerta < Crítico."
            );
        }
        limiaresPorTipo.put(tipoSensor.toUpperCase(), new double[]{atencao, alerta, critico});
    }

    private double[] obterArrayLimiares(String tipoSensor) {
        if (tipoSensor == null) {
            return new double[]{25.0, 50.0, 75.0};
        }
        return limiaresPorTipo.getOrDefault(tipoSensor.toUpperCase(), new double[]{25.0, 50.0, 75.0});
    }
}
