package br.com.paymentslab.spienvio.adaptadores.entrada.mensageria;

/**
 * Gera o identificador da mensagem no padrao exigido pelo schema.
 *
 * O schema exige o formato M + 8 caracteres alfanumericos maiusculos + 23
 * caracteres alfanumericos. O identificador e derivado do EndToEndId para ser
 * deterministico: reenviar o mesmo pagamento gera o mesmo identificador.
 */
final class IdentificadorMensagem {

    private static final int TAMANHO_SUFIXO = 23;

    private IdentificadorMensagem() {
    }

    static String gerar(String endToEndId, String ispb) {
        String sufixo = endToEndId.length() >= TAMANHO_SUFIXO
                ? endToEndId.substring(endToEndId.length() - TAMANHO_SUFIXO)
                : String.format("%23s", endToEndId).replace(' ', '0');
        return "M" + ispb + sufixo;
    }
}
