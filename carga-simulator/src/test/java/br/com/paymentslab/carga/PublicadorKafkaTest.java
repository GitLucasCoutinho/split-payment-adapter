package br.com.paymentslab.carga;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class PublicadorKafkaTest {

    @Test
    @SuppressWarnings("unchecked")
    void deve_publicar_no_topico_com_o_end_to_end_id_como_chave() {
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        PublicadorKafka publicador = new PublicadorKafka(template, "pagamento.liquidado.v1");

        Map<String, Object> pagamento = Map.of("endToEndId", "E1");
        publicador.publicar("E1", pagamento);

        verify(template).send(eq("pagamento.liquidado.v1"), eq("E1"), any(Object.class));
    }
}
