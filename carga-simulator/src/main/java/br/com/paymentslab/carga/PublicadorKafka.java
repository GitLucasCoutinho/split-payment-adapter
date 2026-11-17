package br.com.paymentslab.carga;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saida: publica o pagamento no topico de liquidacao.
 *
 * <p>A chave e o EndToEndId, a mesma que o split-service usa para ordenar e
 * correlacionar.
 */
@Component
public class PublicadorKafka implements PublicadorPagamento {

    private final KafkaTemplate<String, Object> template;
    private final String topico;

    public PublicadorKafka(
            KafkaTemplate<String, Object> template,
            @Value("${carga.topico}") String topico) {
        this.template = template;
        this.topico = topico;
    }

    @Override
    public void publicar(String chave, Map<String, Object> pagamento) {
        template.send(topico, chave, pagamento);
    }
}
