package br.com.paymentslab.carga;

import java.util.Map;

/**
 * Porta de saida da carga: quem publica o pagamento.
 *
 * <p>Existe para o executor poder ser testado sem Kafka.
 */
public interface PublicadorPagamento {

    void publicar(String chave, Map<String, Object> pagamento);
}
