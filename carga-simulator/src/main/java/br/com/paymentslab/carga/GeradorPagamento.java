package br.com.paymentslab.carga;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gera pagamentos liquidados com identificador unico.
 *
 * <p>O formato do EndToEndId e definido pelo schema do SPI:
 * {@code E + 8 do ISPB + AAAAMMDDHHmm + 11 alfanumericos}, sempre 32 caracteres.
 * Errar isso faz a mensagem ser recusada na validacao, e nao no envio.
 */
public final class GeradorPagamento {

    /** Mesmo padrao declarado no XSD, em EndToEndIdType. */
    static final String PADRAO_END_TO_END_ID =
            "[E][0-9A-Z]{8}[0-9]{4}[0-1][0-9][0-3][0-9][0-2][0-9][0-5][0-9][a-zA-Z0-9]{11}";

    private static final String ISPB_RECEBEDOR = "99999010";
    private static final String ISPB_PAGADOR = "00038166";
    private static final String FORMATO_DATA_HORA = "yyyyMMddHHmm";
    private static final int VALOR_MINIMO_CENTAVOS = 100;
    private static final int VALOR_MAXIMO_CENTAVOS = 1_000_000;

    private final AtomicLong sequencia = new AtomicLong();
    private final LocalDateTime referencia;

    public GeradorPagamento() {
        this(LocalDateTime.now());
    }

    public GeradorPagamento(LocalDateTime referencia) {
        this.referencia = referencia;
    }

    /** Proximo identificador, unico dentro desta instancia. */
    public String proximoEndToEndId() {
        return prefixo() + String.format("%011d", sequencia.incrementAndGet());
    }

    /** Corpo do pagamento no formato que o split-service consome. */
    public Map<String, Object> proximoPagamento() {
        long numero = sequencia.incrementAndGet();
        long valor = VALOR_MINIMO_CENTAVOS + (numero * 37) % VALOR_MAXIMO_CENTAVOS;

        Map<String, Object> pagador = new LinkedHashMap<>();
        pagador.put("ispb", ISPB_PAGADOR);
        pagador.put("nome", "Pagador de Carga");
        pagador.put("documento", "70000000000");
        pagador.put("agencia", "3000");
        pagador.put("conta", "500000");
        pagador.put("tipoConta", "CACC");

        Map<String, Object> recebedor = new LinkedHashMap<>();
        recebedor.put("ispb", ISPB_RECEBEDOR);
        recebedor.put("nome", "Recebedor de Carga");
        recebedor.put("documento", "80000000000");
        recebedor.put("agencia", "4000");
        recebedor.put("conta", "600000");
        recebedor.put("tipoConta", "SVGS");

        Map<String, Object> pagamento = new LinkedHashMap<>();
        pagamento.put("endToEndId", prefixo() + String.format("%011d", numero));
        pagamento.put("valor", Map.of("centavos", valor));
        pagamento.put("pagador", pagador);
        pagamento.put("recebedor", recebedor);
        pagamento.put("finalidade", "IPAY");
        pagamento.put("mensagem", "Carga " + numero);
        pagamento.put("liquidadoEm", referencia.toString() + "Z");
        return pagamento;
    }

    private String prefixo() {
        return "E" + ISPB_RECEBEDOR + referencia.format(
                DateTimeFormatter.ofPattern(FORMATO_DATA_HORA));
    }
}
