package br.com.paymentslab.spienvio.pacs008;

import br.gov.bcb.pi.pacs008.ObjectFactory;
import br.gov.bcb.pi.pacs008.SPIEnvelopeMessage;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Le, assina, escreve e valida a mensagem pacs.008 do SPI.
 *
 * O schema de assinatura entra junto com o do pacs.008 porque o elemento Sgntr
 * usa xs:any com validacao estrita. Sem o schema do XML Digital Signature na
 * fabrica, a assinatura seria recusada na validacao.
 *
 * O XML final nao carrega nenhum dado de trace. O schema e fechado e qualquer
 * elemento fora do contrato seria rejeitado.
 */
public final class Pacs008Codec {

    private static final String CAMINHO_XSD = "/xsd/pacs.008.spi.1.15.xsd";
    private static final String CAMINHO_XSD_ASSINATURA = "/xsd/xmldsig.spi.1.0.xsd";

    private final JAXBContext contexto;
    private final Schema schema;

    public Pacs008Codec() {
        try {
            this.contexto = JAXBContext.newInstance(SPIEnvelopeMessage.class);
            this.schema = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(new javax.xml.transform.Source[]{
                            fonte(CAMINHO_XSD),
                            fonte(CAMINHO_XSD_ASSINATURA)
                    });
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException("Falha ao inicializar o codec do pacs.008", e);
        }
    }

    private static javax.xml.transform.Source fonte(String caminho) {
        URL url = Pacs008Codec.class.getResource(caminho);
        if (url == null) {
            throw new IllegalStateException("XSD nao encontrado no classpath: " + caminho);
        }
        return new StreamSource(url.toString());
    }

    /**
     * Le um XML sem aplicar o schema. Aceita o exemplo publicado, que ainda nao
     * tem assinatura.
     */
    public SPIEnvelopeMessage ler(InputStream entrada) {
        try {
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            JAXBElement<SPIEnvelopeMessage> elemento =
                    unmarshaller.unmarshal(new StreamSource(entrada), SPIEnvelopeMessage.class);
            return elemento.getValue();
        } catch (JAXBException e) {
            throw new IllegalStateException("Falha ao ler o pacs.008", e);
        }
    }

    /**
     * Escreve, assina e valida. E o caminho de producao: a mensagem so sai se
     * estiver conforme o XSD oficial.
     */
    public String escrever(SPIEnvelopeMessage envelope, Assinador assinador) {
        String semAssinatura = escreverSemValidar(envelope);
        Document documento = lerDocumento(semAssinatura);
        assinador.assinar(documento);
        String assinado = escreverDocumento(documento);
        validar(assinado);
        return assinado;
    }

    /**
     * Escreve o XML sem assinar e sem validar. Usado para comparar com o
     * exemplo, que e um molde sem assinatura.
     */
    public String escreverSemValidar(SPIEnvelopeMessage envelope) {
        try {
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            StringWriter saida = new StringWriter();
            marshaller.marshal(new ObjectFactory().createEnvelope(envelope), saida);
            return saida.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException("Falha ao escrever o pacs.008", e);
        }
    }

    /**
     * Valida um XML contra o XSD oficial.
     */
    public void validar(String xml) {
        try {
            Validator validador = schema.newValidator();
            validador.validate(new StreamSource(new StringReader(xml)));
        } catch (SAXException | IOException e) {
            throw new IllegalStateException("pacs.008 invalido: " + e.getMessage(), e);
        }
    }

    private Document lerDocumento(String xml) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setNamespaceAware(true);
            return fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Falha ao interpretar o XML gerado", e);
        }
    }

    private String escreverDocumento(Document documento) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            StringWriter saida = new StringWriter();
            transformer.transform(new DOMSource(documento), new StreamResult(saida));
            return saida.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException("Falha ao serializar o XML assinado", e);
        }
    }
}
