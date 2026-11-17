package br.com.paymentslab.spienvio.adaptadores.entrada.mensageria;

import br.com.paymentslab.spienvio.adaptadores.saida.pacs008.Pacs008Mapper;
import br.com.paymentslab.spienvio.dominio.porta.saida.EnvioSpi;
import br.com.paymentslab.spienvio.dominio.porta.saida.Protocolo;
import br.com.paymentslab.spienvio.entrada.evento.SegregacaoCalculadaRecebida;
import br.com.paymentslab.spienvio.pacs008.Assinador;
import br.com.paymentslab.spienvio.pacs008.Pacs008Codec;
import br.gov.bcb.pi.pacs008.SPIEnvelopeMessage;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: consome a segregacao calculada, monta o pacs.008,
 * assina, valida e envia.
 *
 * <p>As metricas ficam no adaptador. O caso de uso e o mapeador nao conhecem
 * biblioteca de observabilidade.
 */
@Component
public class SegregacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(SegregacaoConsumer.class);

    private final Pacs008Mapper mapper;
    private final Pacs008Codec codec;
    private final Assinador assinador;
    private final EnvioSpi envioSpi;
    private final MeterRegistry metricas;

    public SegregacaoConsumer(
            Pacs008Mapper mapper,
            Pacs008Codec codec,
            Assinador assinador,
            EnvioSpi envioSpi,
            MeterRegistry metricas) {
        this.mapper = mapper;
        this.codec = codec;
        this.assinador = assinador;
        this.envioSpi = envioSpi;
        this.metricas = metricas;
    }

    @KafkaListener(topics = "${split.kafka.topico-segregacao}")
    public void consumir(SegregacaoCalculadaRecebida evento) {
        Timer envio = Timer.builder("pacs008.envio")
                .description("Tempo entre receber a segregacao e receber o protocolo do SPI")
                .register(metricas);
        try {
            envio.record(() -> enviar(evento));
            metricas.counter("pacs008.envios", "resultado", "enviado").increment();
        } catch (RuntimeException erro) {
            metricas.counter("pacs008.envios", "resultado", "falha").increment();
            throw erro;
        }
    }

    private void enviar(SegregacaoCalculadaRecebida evento) {
        String idMensagem = IdentificadorMensagem.gerar(evento.endToEndId(), evento.pagador().ispb());

        SPIEnvelopeMessage envelope = mapper.mapear(evento, idMensagem);
        String xml = codec.escrever(envelope, assinador);

        Protocolo protocolo = envioSpi.enviar(xml);

        log.info("pacs.008 enviado: endToEndId={} protocolo={} tamanho={}",
                evento.endToEndId(), protocolo.numero(), xml.length());
    }
}
