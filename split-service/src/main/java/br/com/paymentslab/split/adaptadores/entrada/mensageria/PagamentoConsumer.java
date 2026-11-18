package br.com.paymentslab.split.adaptadores.entrada.mensageria;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Segregacao;
import br.com.paymentslab.split.dominio.porta.entrada.ProcessarPagamento;
import br.com.paymentslab.split.entrada.evento.MapeadorPagamento;
import br.com.paymentslab.split.entrada.evento.PagamentoRecebido;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: consome o pagamento liquidado do Kafka.
 *
 * <p>O evento e convertido em dominio antes de qualquer regra. Evento invalido
 * e recusado aqui, com metrica e motivo, e nao dentro do desserializador.
 */
@Component
public class PagamentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagamentoConsumer.class);

    private final ProcessarPagamento processarPagamento;
    private final MapeadorPagamento mapeador = new MapeadorPagamento();
    private final MeterRegistry metricas;

    public PagamentoConsumer(ProcessarPagamento processarPagamento, MeterRegistry metricas) {
        this.processarPagamento = processarPagamento;
        this.metricas = metricas;
    }

    @KafkaListener(topics = "${split.kafka.topico-pagamento}")
    public void consumir(PagamentoRecebido recebido) {
        PagamentoLiquidado pagamento;
        try {
            pagamento = mapeador.paraDominio(recebido);
        } catch (IllegalArgumentException erro) {
            metricas.counter("split.pagamentos", "resultado", "recusado").increment();
            log.warn("Pagamento recusado: {}", erro.getMessage());
            throw erro;
        }

        log.info("Pagamento recebido: endToEndId={} valor={}",
                pagamento.endToEndId(), pagamento.valor().emReais());

        Segregacao segregacao;
        try {
            segregacao = Timer.builder("split.calculo")
                    .description("Tempo de calculo da segregacao")
                    .register(metricas)
                    .record(() -> processarPagamento.processar(pagamento));
            metricas.counter("split.pagamentos", "resultado", "calculado").increment();
        } catch (RuntimeException erro) {
            metricas.counter("split.pagamentos", "resultado", "falha").increment();
            throw erro;
        }

        log.info("Segregacao calculada: endToEndId={} ibs={} cbs={} liquido={}",
                segregacao.endToEndId(),
                segregacao.valorIbs().emReais(),
                segregacao.valorCbs().emReais(),
                segregacao.valorLiquido().emReais());
    }
}
