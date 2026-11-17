package br.com.paymentslab.spienvio.pacs008;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Assinador simulado.
 *
 * Monta uma assinatura com a estrutura exigida pelo perfil de assinatura do SPI,
 * mas com valores fixos. Serve para o projeto academico: a mensagem passa na
 * validacao contra o XSD oficial e o fluxo de ponta a ponta fecha.
 *
 * O perfil do SPI impoe tres restricoes que o padrao W3C nao impoe, e todas
 * apareceram na validacao:
 *
 * 1. canonicalizacao exclusiva (xml-exc-c14n), nao o C14N classico;
 * 2. metodo de assinatura rsa-sha256 e resumo sha256;
 * 3. no minimo tres referencias dentro do SignedInfo.
 *
 * NAO tem valor criptografico. Em producao, troque por um adaptador que assine
 * com a chave privada do certificado do PSP.
 */
public final class AssinadorSimulado implements Assinador {

    private static final String NS_DS = "http://www.w3.org/2000/09/xmldsig#";
    private static final int QUANTIDADE_MINIMA_REFERENCIAS = 3;
    private static final String VALOR_FIXO = Base64.getEncoder()
            .encodeToString("assinatura-simulada".getBytes(StandardCharsets.UTF_8));

    @Override
    public void assinar(Document documento) {
        Element sgntr = localizarSgntr(documento);
        if (sgntr == null) {
            throw new IllegalStateException("Elemento Sgntr nao encontrado no envelope");
        }
        while (sgntr.hasChildNodes()) {
            sgntr.removeChild(sgntr.getFirstChild());
        }
        sgntr.appendChild(construirAssinatura(documento));
    }

    private Element localizarSgntr(Document documento) {
        NodeList elementos = documento.getElementsByTagNameNS("*", "Sgntr");
        if (elementos.getLength() == 0) {
            return null;
        }
        return (Element) elementos.item(0);
    }

    private Element construirAssinatura(Document documento) {
        Element assinatura = documento.createElementNS(NS_DS, "ds:Signature");

        Element info = documento.createElementNS(NS_DS, "ds:SignedInfo");

        Element canonicalizacao = documento.createElementNS(NS_DS, "ds:CanonicalizationMethod");
        canonicalizacao.setAttribute("Algorithm", "http://www.w3.org/2001/10/xml-exc-c14n#");
        info.appendChild(canonicalizacao);

        Element metodo = documento.createElementNS(NS_DS, "ds:SignatureMethod");
        metodo.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        info.appendChild(metodo);

        for (int indice = 1; indice <= QUANTIDADE_MINIMA_REFERENCIAS; indice++) {
            info.appendChild(criarReferencia(documento, resumoFixo(indice)));
        }

        assinatura.appendChild(info);

        Element valorAssinatura = documento.createElementNS(NS_DS, "ds:SignatureValue");
        valorAssinatura.setTextContent(VALOR_FIXO);
        assinatura.appendChild(valorAssinatura);

        assinatura.appendChild(criarKeyInfo(documento));

        return assinatura;
    }

    /**
     * O perfil do SPI exige KeyInfo com X509Data e um atributo Id obrigatorio.
     */
    private Element criarKeyInfo(Document documento) {
        Element keyInfo = documento.createElementNS(NS_DS, "ds:KeyInfo");
        keyInfo.setAttribute("Id", "KeyInfo-simulado");

        Element dados = documento.createElementNS(NS_DS, "ds:X509Data");

        Element emissorSerial = documento.createElementNS(NS_DS, "ds:X509IssuerSerial");
        Element emissor = documento.createElementNS(NS_DS, "ds:X509IssuerName");
        emissor.setTextContent("CN=Certificado Simulado, OU=Academico, O=Lab, C=BR");
        emissorSerial.appendChild(emissor);
        Element serial = documento.createElementNS(NS_DS, "ds:X509SerialNumber");
        serial.setTextContent("1");
        emissorSerial.appendChild(serial);
        dados.appendChild(emissorSerial);

        keyInfo.appendChild(dados);

        return keyInfo;
    }

    private static String resumoFixo(int indice) {
        return Base64.getEncoder()
                .encodeToString(("assinatura-simulada-" + indice).getBytes(StandardCharsets.UTF_8));
    }

    private Element criarReferencia(Document documento, String resumo) {
        Element referencia = documento.createElementNS(NS_DS, "ds:Reference");
        referencia.setAttribute("URI", "");

        Element transformacoes = documento.createElementNS(NS_DS, "ds:Transforms");
        Element envelopada = documento.createElementNS(NS_DS, "ds:Transform");
        envelopada.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#enveloped-signature");
        transformacoes.appendChild(envelopada);
        Element exclusiva = documento.createElementNS(NS_DS, "ds:Transform");
        exclusiva.setAttribute("Algorithm", "http://www.w3.org/2001/10/xml-exc-c14n#");
        transformacoes.appendChild(exclusiva);
        referencia.appendChild(transformacoes);

        Element metodoResumo = documento.createElementNS(NS_DS, "ds:DigestMethod");
        metodoResumo.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#sha256");
        referencia.appendChild(metodoResumo);

        Element valorResumo = documento.createElementNS(NS_DS, "ds:DigestValue");
        valorResumo.setTextContent(resumo);
        referencia.appendChild(valorResumo);

        return referencia;
    }
}
