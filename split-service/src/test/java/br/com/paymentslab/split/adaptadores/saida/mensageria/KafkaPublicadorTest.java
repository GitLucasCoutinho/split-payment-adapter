package br.com.paymentslab.split.adaptadores.saida.mensageria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaPublicadorTest {

    @Test
    @SuppressWarnings("unchecked")
    void deve_enviar_o_evento_com_a_chave_e_o_cabecalho_de_correlacao() {
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        KafkaPublicador publicador = new KafkaPublicador(template);

        publicador.publicar("segregacao.calculada.v1", "E1", "evento");

        ArgumentCaptor<ProducerRecord<String, Object>> capturado =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(template).send(capturado.capture());

        ProducerRecord<String, Object> registro = capturado.getValue();
        assertEquals("segregacao.calculada.v1", registro.topic());
        assertEquals("E1", registro.key());
        assertEquals("evento", registro.value());

        var cabecalho = registro.headers().lastHeader(KafkaPublicador.CABECALHO_CORRELACAO);
        assertNotNull(cabecalho, "o consumidor depende deste cabecalho");
        assertEquals("E1", new String(cabecalho.value(), StandardCharsets.UTF_8));
    }
}
