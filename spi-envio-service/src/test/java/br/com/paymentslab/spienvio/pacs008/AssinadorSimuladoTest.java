package br.com.paymentslab.spienvio.pacs008;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

class AssinadorSimuladoTest {

    private static final String NAMESPACE = "https://www.bcb.gov.br/pi/pacs.008/1.15";

    @Test
    void deve_recusar_documento_sem_elemento_sgntr() throws ParserConfigurationException {
        Document documento = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();
        documento.appendChild(documento.createElementNS(NAMESPACE, "Envelope"));

        Assinador assinador = new AssinadorSimulado();

        assertThrows(IllegalStateException.class, () -> assinador.assinar(documento));
    }
}
