package br.com.paymentslab.spienvio.dominio.porta.saida;

import java.time.Instant;

/**
 * Recibo devolvido pelo destino do envio.
 */
public record Protocolo(String numero, Instant recebidoEm) {
}
