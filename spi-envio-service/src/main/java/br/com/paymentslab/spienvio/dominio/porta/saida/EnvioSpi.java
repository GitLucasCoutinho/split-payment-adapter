package br.com.paymentslab.spienvio.dominio.porta.saida;

/**
 * Porta de envio ao SPI (Sistema de Pagamentos Instantaneos).
 *
 * O servico nao sabe se o envio e HTTP, arquivo ou mensageria. Quem decide e o
 * adaptador configurado.
 */
public interface EnvioSpi {

    Protocolo enviar(String xmlAssinado);
}
