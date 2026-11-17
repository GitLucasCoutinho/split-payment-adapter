package br.com.paymentslab.spienvio.pacs008;

import org.w3c.dom.Document;

/**
 * Porta de assinatura digital.
 *
 * O servico nao sabe como a assinatura e produzida. O adaptador simulado gera uma
 * assinatura estruturalmente valida, suficiente para o projeto academico. Quem
 * tiver certificado e chave pode plugar um adaptador real, sem tocar no codec.
 */
public interface Assinador {

    /**
     * Aplica a assinatura no documento, preenchendo o elemento Sgntr.
     */
    void assinar(Document documento);
}
