package br.com.paymentslab.split.dominio.porta.saida;

/**
 * Porta de saida para publicar eventos no barramento.
 */
public interface PublicadorEvento {

    void publicar(String topico, String chave, Object evento);
}
