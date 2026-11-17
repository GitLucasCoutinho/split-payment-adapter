package br.com.paymentslab.spienvio.adaptadores.saida.pacs008;

import br.com.paymentslab.spienvio.entrada.evento.ParticipanteRecebido;
import br.com.paymentslab.spienvio.entrada.evento.SegregacaoCalculadaRecebida;
import br.gov.bcb.pi.pacs008.AccountIdentification4Choice;
import br.gov.bcb.pi.pacs008.ActiveCurrencyAndAmount;
import br.gov.bcb.pi.pacs008.ActiveCurrencyCode;
import br.gov.bcb.pi.pacs008.BranchAndFinancialInstitutionIdentification5;
import br.gov.bcb.pi.pacs008.BranchAndFinancialInstitutionIdentification6;
import br.gov.bcb.pi.pacs008.CashAccount38CdtrAcct;
import br.gov.bcb.pi.pacs008.CashAccount38DbtrAcct;
import br.gov.bcb.pi.pacs008.CashAccountType2Choice;
import br.gov.bcb.pi.pacs008.ChargeBearerType1Code;
import br.gov.bcb.pi.pacs008.ClearingSystemMemberIdentification2;
import br.gov.bcb.pi.pacs008.CreditTransferMandateData1;
import br.gov.bcb.pi.pacs008.CreditTransferTransaction43;
import br.gov.bcb.pi.pacs008.ExternalCashAccountType1Code;
import br.gov.bcb.pi.pacs008.ExternalPurpose1Code;
import br.gov.bcb.pi.pacs008.FIToFICustomerCreditTransferV09;
import br.gov.bcb.pi.pacs008.FinancialInstitutionIdentification18;
import br.gov.bcb.pi.pacs008.FinancialInstitutionIdentification8;
import br.gov.bcb.pi.pacs008.GenericAccountIdentification1;
import br.gov.bcb.pi.pacs008.GenericFinancialIdentification1;
import br.gov.bcb.pi.pacs008.GenericPersonIdentification1;
import br.gov.bcb.pi.pacs008.GroupHeader93;
import br.gov.bcb.pi.pacs008.IdPrivateIdentification;
import br.gov.bcb.pi.pacs008.LocalInstrument2Choice;
import br.gov.bcb.pi.pacs008.MandateTypeInformation2;
import br.gov.bcb.pi.pacs008.NmIdPrivateIdentification;
import br.gov.bcb.pi.pacs008.Party9Choice;
import br.gov.bcb.pi.pacs008.PaymentIdentification13;
import br.gov.bcb.pi.pacs008.PaymentTypeInformation28;
import br.gov.bcb.pi.pacs008.PersonIdentification13;
import br.gov.bcb.pi.pacs008.PrivateIdentification;
import br.gov.bcb.pi.pacs008.Priority2Code;
import br.gov.bcb.pi.pacs008.PrtryLclInstrmCode;
import br.gov.bcb.pi.pacs008.PrtrySvcLvlCode;
import br.gov.bcb.pi.pacs008.Purpose2Choice;
import br.gov.bcb.pi.pacs008.RemittanceInformation16;
import br.gov.bcb.pi.pacs008.ServiceLevel8Choice;
import br.gov.bcb.pi.pacs008.SettlementInstruction7;
import br.gov.bcb.pi.pacs008.SettlementMethod1Code;
import br.gov.bcb.pi.pacs008.SPIEnvelopeMessage;
import br.gov.bcb.pi.pacs008.SPIHead00100101;
import br.gov.bcb.pi.pacs008.SPIPacs00800109;
import br.gov.bcb.pi.pacs008.SignatureEnvelope;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.stereotype.Component;

/**
 * Converte o evento de segregacao na mensagem pacs.008 do SPI.
 *
 * O valor transportado na mensagem e o valor bruto do pagamento. A segregacao
 * tributaria nao viaja no pacs.008: ela e comunicada a Plataforma Publica do
 * Split Payment por fluxo proprio. O que este servico faz e carregar o pagamento
 * e, no mesmo ciclo, informar o tributo.
 */
@Component
public class Pacs008Mapper {

    private static final String DEFINICAO_MENSAGEM = "pacs.008.spi.1.15";

    private final DatatypeFactory fabricaDeDatas;

    public Pacs008Mapper() {
        try {
            this.fabricaDeDatas = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Falha ao criar a fabrica de datas XML", e);
        }
    }

    public SPIEnvelopeMessage mapear(SegregacaoCalculadaRecebida evento, String idMensagem) {
        SPIEnvelopeMessage envelope = new SPIEnvelopeMessage();
        envelope.setAppHdr(cabecalho(evento, idMensagem));
        envelope.setDocument(documento(evento, idMensagem));
        return envelope;
    }

    private SPIHead00100101 cabecalho(SegregacaoCalculadaRecebida evento, String idMensagem) {
        SPIHead00100101 cabecalho = new SPIHead00100101();
        cabecalho.setFr(parte(evento.pagador().ispb()));
        cabecalho.setTo(parte(evento.recebedor().ispb()));
        cabecalho.setBizMsgIdr(idMensagem);
        cabecalho.setMsgDefIdr(DEFINICAO_MENSAGEM);
        cabecalho.setCreDt(data(evento.liquidadoEm()));
        cabecalho.setSgntr(new SignatureEnvelope());
        return cabecalho;
    }

    private Party9Choice parte(String ispb) {
        GenericFinancialIdentification1 identificacao = new GenericFinancialIdentification1();
        identificacao.setId(ispb);

        FinancialInstitutionIdentification8 instituicao = new FinancialInstitutionIdentification8();
        instituicao.setOthr(identificacao);

        BranchAndFinancialInstitutionIdentification5 agencia = new BranchAndFinancialInstitutionIdentification5();
        agencia.setFinInstnId(instituicao);

        Party9Choice parte = new Party9Choice();
        parte.setFIId(agencia);
        return parte;
    }

    private SPIPacs00800109 documento(SegregacaoCalculadaRecebida evento, String idMensagem) {
        FIToFICustomerCreditTransferV09 transferencia = new FIToFICustomerCreditTransferV09();
        transferencia.setGrpHdr(grupo(evento, idMensagem));
        transferencia.getCdtTrfTxInf().add(transacao(evento));

        SPIPacs00800109 documento = new SPIPacs00800109();
        documento.setFIToFICstmrCdtTrf(transferencia);
        return documento;
    }

    private GroupHeader93 grupo(SegregacaoCalculadaRecebida evento, String idMensagem) {
        GroupHeader93 grupo = new GroupHeader93();
        grupo.setMsgId(idMensagem);
        grupo.setCreDtTm(data(evento.liquidadoEm()));
        grupo.setNbOfTxs(BigInteger.ONE);

        SettlementInstruction7 liquidacao = new SettlementInstruction7();
        liquidacao.setSttlmMtd(SettlementMethod1Code.CLRG);
        grupo.setSttlmInf(liquidacao);

        ServiceLevel8Choice nivel = new ServiceLevel8Choice();
        nivel.setPrtry(PrtrySvcLvlCode.PAGPRI);

        PaymentTypeInformation28 tipo = new PaymentTypeInformation28();
        tipo.setInstrPrty(Priority2Code.HIGH);
        tipo.setSvcLvl(nivel);
        grupo.setPmtTpInf(tipo);

        return grupo;
    }

    private CreditTransferTransaction43 transacao(SegregacaoCalculadaRecebida evento) {
        CreditTransferTransaction43 transacao = new CreditTransferTransaction43();

        PaymentIdentification13 identificacao = new PaymentIdentification13();
        identificacao.setEndToEndId(evento.endToEndId());
        transacao.setPmtId(identificacao);

        ActiveCurrencyAndAmount valor = new ActiveCurrencyAndAmount();
        valor.setValue(BigDecimal.valueOf(evento.valorBrutoCentavos(), 2));
        valor.setCcy(ActiveCurrencyCode.BRL);
        transacao.setIntrBkSttlmAmt(valor);

        transacao.setAccptncDtTm(data(evento.liquidadoEm()));
        transacao.setChrgBr(ChargeBearerType1Code.SLEV);
        transacao.setMndtRltdInf(mandato());
        transacao.setDbtr(devedor(evento.pagador()));
        transacao.setDbtrAcct(contaDevedor(evento.pagador()));
        transacao.setDbtrAgt(agente(evento.pagador().ispb()));
        transacao.setCdtrAgt(agente(evento.recebedor().ispb()));
        transacao.setCdtr(credor(evento.recebedor()));
        transacao.setCdtrAcct(contaCredor(evento.recebedor()));

        Purpose2Choice finalidade = new Purpose2Choice();
        finalidade.setCd(ExternalPurpose1Code.IPAY);
        transacao.setPurp(finalidade);

        if (evento.mensagem() != null && !evento.mensagem().isBlank()) {
            RemittanceInformation16 informacao = new RemittanceInformation16();
            informacao.setUstrd(evento.mensagem());
            transacao.setRmtInf(informacao);
        }

        return transacao;
    }

    private CreditTransferMandateData1 mandato() {
        LocalInstrument2Choice instrumento = new LocalInstrument2Choice();
        instrumento.setPrtry(PrtryLclInstrmCode.MANU);

        MandateTypeInformation2 tipo = new MandateTypeInformation2();
        tipo.setLclInstrm(instrumento);

        CreditTransferMandateData1 mandato = new CreditTransferMandateData1();
        mandato.setTp(tipo);
        return mandato;
    }

    private NmIdPrivateIdentification devedor(ParticipanteRecebido participante) {
        NmIdPrivateIdentification devedor = new NmIdPrivateIdentification();
        devedor.setNm(participante.nome());
        devedor.setId(identificacao(participante.documento()));
        return devedor;
    }

    private IdPrivateIdentification credor(ParticipanteRecebido participante) {
        IdPrivateIdentification credor = new IdPrivateIdentification();
        credor.setId(identificacao(participante.documento()));
        return credor;
    }

    private PrivateIdentification identificacao(String documento) {
        GenericPersonIdentification1 generica = new GenericPersonIdentification1();
        generica.setId(documento);

        PersonIdentification13 pessoa = new PersonIdentification13();
        pessoa.setOthr(generica);

        PrivateIdentification privada = new PrivateIdentification();
        privada.setPrvtId(pessoa);
        return privada;
    }

    private CashAccount38DbtrAcct contaDevedor(ParticipanteRecebido participante) {
        CashAccount38DbtrAcct conta = new CashAccount38DbtrAcct();
        conta.setId(identificacaoConta(participante));
        conta.setTp(tipoConta(participante.tipoConta()));
        return conta;
    }

    private CashAccount38CdtrAcct contaCredor(ParticipanteRecebido participante) {
        CashAccount38CdtrAcct conta = new CashAccount38CdtrAcct();
        conta.setId(identificacaoConta(participante));
        conta.setTp(tipoConta(participante.tipoConta()));
        return conta;
    }

    private AccountIdentification4Choice identificacaoConta(ParticipanteRecebido participante) {
        GenericAccountIdentification1 generica = new GenericAccountIdentification1();
        generica.setId(participante.conta());
        if (participante.agencia() != null && !participante.agencia().isBlank()) {
            generica.setIssr(new BigInteger(participante.agencia()));
        }

        AccountIdentification4Choice conta = new AccountIdentification4Choice();
        conta.setOthr(generica);
        return conta;
    }

    private CashAccountType2Choice tipoConta(String tipo) {
        CashAccountType2Choice tipoConta = new CashAccountType2Choice();
        tipoConta.setCd(ExternalCashAccountType1Code.fromValue(tipo));
        return tipoConta;
    }

    private BranchAndFinancialInstitutionIdentification6 agente(String ispb) {
        ClearingSystemMemberIdentification2 membro = new ClearingSystemMemberIdentification2();
        membro.setMmbId(ispb);

        FinancialInstitutionIdentification18 instituicao = new FinancialInstitutionIdentification18();
        instituicao.setClrSysMmbId(membro);

        BranchAndFinancialInstitutionIdentification6 agente = new BranchAndFinancialInstitutionIdentification6();
        agente.setFinInstnId(instituicao);
        return agente;
    }

    private XMLGregorianCalendar data(Instant instante) {
        return fabricaDeDatas.newXMLGregorianCalendar(
                GregorianCalendar.from(instante.atZone(ZoneOffset.UTC)));
    }
}
