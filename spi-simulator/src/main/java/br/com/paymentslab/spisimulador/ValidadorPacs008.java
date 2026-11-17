package br.com.paymentslab.spisimulador;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Valida o pacs.008 contra os schemas oficiais e extrai o EndToEndId.
 *
 * <p>O simulador faz o papel do SPI, que e um sistema externo. Por isso ele tem
 * os proprios schemas e a propria leitura, sem depender do codigo de quem envia.
 * Se o emissor gerar um XML torto, a recusa acontece aqui.
 */
public final class ValidadorPacs008 {

    private static final String CAMINHO_XSD = "/xsd/pacs.008.spi.1.15.xsd";
    private static final String CAMINHO_XSD_ASSINATURA = "/xsd/xmldsig.spi.1.0.xsd";

    private final Schema schema;

    public ValidadorPacs008() {
        try {
            this.schema = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(new Source[]{fonte(CAMINHO_XSD), fonte(CAMINHO_XSD_ASSINATURA)});
        } catch (SAXException e) {
            throw new IllegalStateException("Falha ao carregar os schemas do pacs.008", e);
        }
    }

    /** Recusa a mensagem quando ela nao esta conforme o contrato. */
    public void validar(String xml) {
        try {
            Validator validador = schema.newValidator();
            validador.validate(new StreamSource(new StringReader(xml)));
        } catch (SAXException | IOException e) {
            throw new IllegalStateException("pacs.008 invalido: " + e.getMessage(), e);
        }
    }

    /**
     * Extrai o identificador ponta a ponta da transacao.
     *
     * <p>A busca usa local-name porque o XML tem namespace padrao, e o
     * simulador nao gera classes a partir do schema.
     */
    public String endToEndId(String xml) {
        String expressao = "//*[local-name()='CdtTrfTxInf']"
                + "/*[local-name()='PmtId']/*[local-name()='EndToEndId']";
        try {
            DocumentBuilder construtor = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Sem isto, um XML quebrado imprime "Fatal Error" no console do build.
            construtor.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException erro) {
                }

                @Override
                public void error(SAXParseException erro) {
                }

                @Override
                public void fatalError(SAXParseException erro) throws SAXException {
                    throw erro;
                }
            });
            Document documento = construtor.parse(new InputSource(new StringReader(xml)));
            XPath xpath = XPathFactory.newInstance().newXPath();
            String valor = xpath.evaluate(expressao, documento);
            if (valor == null || valor.isBlank()) {
                throw new IllegalStateException("pacs.008 sem EndToEndId");
            }
            return valor;
        } catch (ParserConfigurationException | SAXException | IOException
                 | XPathExpressionException e) {
            throw new IllegalStateException("Falha ao extrair o EndToEndId", e);
        }
    }

    private static Source fonte(String caminho) {
        URL url = ValidadorPacs008.class.getResource(caminho);
        if (url == null) {
            throw new IllegalStateException("XSD nao encontrado no classpath: " + caminho);
        }
        return new StreamSource(url.toString());
    }
}
