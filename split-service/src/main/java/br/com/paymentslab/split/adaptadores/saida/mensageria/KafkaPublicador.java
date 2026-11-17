package br.com.paymentslab.split.adaptadores.saida.mensageria;

import br.com.paymentslab.split.dominio.porta.saida.PublicadorEvento;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saida: publica eventos no Kafka.
 *
 * <p>A chave e o E2EID, e ele tambem vai no cabecalho de correlacao. Assim o
 * consumidor consegue amarrar log, evento e chamada HTTP ao mesmo identificador,
 * sem precisar deduzir nada.
 */
@Component
public class KafkaPublicador implements PublicadorEvento {

    /** Combinado com o servico consumidor. */
    public static final String CABECALHO_CORRELACAO = "X-Correlation-Id";

    private final KafkaTemplate<String, Object> template;

    public KafkaPublicador(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    @Override
    public void publicar(String topico, String chave, Object evento) {
        ProducerRecord<String, Object> registro = new ProducerRecord<>(topico, chave, evento);
        if (chave != null) {
            registro.headers().add(CABECALHO_CORRELACAO, chave.getBytes(StandardCharsets.UTF_8));
        }
        template.send(registro);
    }
}
