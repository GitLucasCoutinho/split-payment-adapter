package br.com.paymentslab.spienvio.comum.correlacao;

import java.util.UUID;
import org.slf4j.MDC;

/**
 * Identificador de correlacao da transacao.
 *
 * <p>Vem do cabecalho Kafka quando quem publicou informou. Se nao vier, e gerado
 * aqui. Fica no MDC, entao aparece no log e e propagado na chamada HTTP: a mesma
 * transacao pode ser seguida do inicio ao fim.
 */
public final class CorrelationId {

    /** Nome do cabecalho usado no Kafka e na chamada HTTP. */
    public static final String CABECALHO = "X-Correlation-Id";

    /** Chave usada no MDC do log. */
    public static final String CHAVE_MDC = "correlationId";

    private CorrelationId() {
    }

    /** Identificador atual da thread, ou nulo quando nao ha escopo. */
    public static String atual() {
        return MDC.get(CHAVE_MDC);
    }

    /**
     * Devolve o identificador recebido, ou gera um novo quando ele nao existe.
     */
    public static String garantir(String recebido) {
        if (recebido != null && !recebido.isBlank()) {
            return recebido;
        }
        return UUID.randomUUID().toString();
    }

    /** Abre o escopo de correlacao na thread atual. */
    public static void definir(String valor) {
        if (valor != null && !valor.isBlank()) {
            MDC.put(CHAVE_MDC, valor);
        }
    }

    /** Fecha o escopo de correlacao na thread atual. */
    public static void limpar() {
        MDC.remove(CHAVE_MDC);
    }
}
