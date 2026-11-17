package br.com.paymentslab.spienvio.comum.correlacao;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

/**
 * Abre o escopo de correlacao a cada mensagem consumida.
 *
 * <p>Os tipos genericos precisam ser exatamente {@code Object, Object}. O Spring
 * Boot procura um bean {@code RecordInterceptor<Object, Object>} para registrar
 * na fabrica de containers; um bean com outros genericos e ignorado em silencio.
 *
 * <p>Se o cabecalho nao vier, um identificador e gerado. Isso garante que todo
 * log tenha referencia, mesmo quando o produtor nao colabora.
 */
@Component
public class CorrelationIdRecordInterceptor implements RecordInterceptor<Object, Object> {

    @Override
    public ConsumerRecord<Object, Object> intercept(
            ConsumerRecord<Object, Object> registro,
            Consumer<Object, Object> consumidor) {

        CorrelationId.definir(CorrelationId.garantir(lerCabecalho(registro)));
        return registro;
    }

    @Override
    public void afterRecord(
            ConsumerRecord<Object, Object> registro,
            Consumer<Object, Object> consumidor) {

        CorrelationId.limpar();
    }

    private String lerCabecalho(ConsumerRecord<Object, Object> registro) {
        Header cabecalho = registro.headers().lastHeader(CorrelationId.CABECALHO);
        if (cabecalho == null || cabecalho.value() == null) {
            return null;
        }
        return new String(cabecalho.value(), StandardCharsets.UTF_8);
    }
}
