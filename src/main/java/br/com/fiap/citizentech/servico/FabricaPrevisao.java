package br.com.fiap.citizentech.servico;

import br.com.fiap.citizentech.dominio.EstrategiaPrevisao;
import br.com.fiap.citizentech.dominio.PrevisaoComparacao;
import br.com.fiap.citizentech.dominio.PrevisaoMediaMovel;
import br.com.fiap.citizentech.dominio.TipoPrevisao;
import br.com.fiap.citizentech.excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Padrao Criacional: Factory (com registro polimorfico via EnumMap, evitando cadeias de if/instanceof).
 */
public class FabricaPrevisao {
    private static final Map<TipoPrevisao, Supplier<EstrategiaPrevisao>> REGISTRO =
            new EnumMap<>(TipoPrevisao.class);

    static {
        REGISTRO.put(TipoPrevisao.MEDIA_MOVEL, () -> new PrevisaoMediaMovel(5));
        REGISTRO.put(TipoPrevisao.COMPARACAO_PERIODOS, PrevisaoComparacao::new);
    }

    private FabricaPrevisao() {
    }

    public static EstrategiaPrevisao criar(TipoPrevisao tipo) throws DadosInvalidosException {
        if (tipo == null || !REGISTRO.containsKey(tipo)) {
            throw new DadosInvalidosException("Tipo de estratégia de previsão não suportado: " + tipo);
        }
        return REGISTRO.get(tipo).get();
    }
}
