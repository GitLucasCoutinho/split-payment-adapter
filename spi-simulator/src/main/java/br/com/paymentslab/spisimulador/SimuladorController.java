package br.com.paymentslab.spisimulador;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulador do SPI.
 *
 * Recebe o pacs.008 assinado, valida contra o XSD e devolve um protocolo. Nao e
 * um eco: se a mensagem estiver fora do contrato, recusa e explica o motivo.
 * Mensagem repetida para o mesmo EndToEndId tambem e recusada, porque o SPI real
 * nao aceita a mesma transferencia duas vezes.
 */
@RestController
@RequestMapping("/spi")
public class SimuladorController {

    private static final Logger log = LoggerFactory.getLogger(SimuladorController.class);

    private final ValidadorPacs008 validador;
    private final MeterRegistry metricas;
    private final Set<String> endToEndIdsProcessados = ConcurrentHashMap.newKeySet();
    private final AtomicLong sequencia = new AtomicLong();

    public SimuladorController(ValidadorPacs008 validador, MeterRegistry metricas) {
        this.validador = validador;
        this.metricas = metricas;
    }

    @PostMapping(value = "/mensagens", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Object> receber(@RequestBody String xml) {
        try {
            Timer.builder("simulador.validacao")
                    .description("Tempo de validacao do pacs.008 contra o schema")
                    .register(metricas)
                    .record(() -> validador.validar(xml));
        } catch (IllegalStateException erro) {
            metricas.counter("simulador.mensagens", "resultado", "recusada").increment();
            log.warn("Mensagem recusada na validacao: {}", erro.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "mensagem fora do contrato",
                    "detalhe", erro.getMessage()));
        }

        String endToEndId = validador.endToEndId(xml);
        if (!endToEndIdsProcessados.add(endToEndId)) {
            metricas.counter("simulador.mensagens", "resultado", "duplicada").increment();
            log.warn("Mensagem duplicada recusada: {}", endToEndId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "erro", "mensagem ja processada",
                    "endToEndId", endToEndId));
        }

        String protocolo = "SIM-%s-%06d".formatted(LocalDate.now(), sequencia.incrementAndGet());
        Instant recebidoEm = Instant.now();
        metricas.counter("simulador.mensagens", "resultado", "aceita").increment();
        log.info("pacs.008 aceito: endToEndId={} protocolo={} tamanho={}",
                endToEndId, protocolo, xml.length());

        return ResponseEntity.ok(new Recibo(protocolo, recebidoEm, endToEndId));
    }

    /**
     * Recibo devolvido ao remetente.
     */
    public record Recibo(String protocolo, Instant recebidoEm, String endToEndId) {
    }
}
