# Spec: spi-envio-service

| Campo | Valor |
|---|---|
| Serviço | `spi-envio-service` |
| Porta | 8082 |
| Entrada | `segregacao.calculada.v1` |
| Saída | HTTP POST para o simulador do SPI |
| Fonte normativa | Manual de Integração v1.1.1 e `pacs.008.spi.1.15.xsd` |

## 1. Escopo

Consumir a segregação calculada, montar a mensagem pacs.008, assinar, validar contra o XSD e enviar ao destino configurado.

Não calcula tributo e não decide alíquota.

## 2. Requisitos funcionais

**RP-1, mapeamento**
QUANDO a segregação chega, O SISTEMA DEVE montar um `Envelope` com cabeçalho e documento, preenchendo os elementos obrigatórios do schema.

**RP-2, identificador**
O SISTEMA DEVE gerar o identificador da mensagem no padrão `M` mais oito caracteres alfanuméricos maiúsculos mais 23 alfanuméricos, derivado do E2EID (End-to-End ID), para que o reenvio produza o mesmo identificador.

**RP-3, valor**
O SISTEMA DEVE transportar no pacs.008 o valor bruto do pagamento, com duas casas decimais e moeda `BRL`.

**RP-4, assinatura**
O SISTEMA DEVE preencher o elemento `Sgntr` com uma assinatura digital válida segundo o perfil do SPI antes de validar a mensagem.

**RP-5, validação**
O SISTEMA DEVE validar a mensagem contra o XSD oficial e NÃO DEVE enviar mensagem que falhe na validação.

**RP-6, envio**
O SISTEMA DEVE enviar a mensagem assinada pela porta `EnvioSpi` e registrar o protocolo devolvido.

**RP-7, trace**
O SISTEMA NÃO DEVE incluir nenhum dado de trace no XML. O schema é fechado e elemento extra é recusado na validação.

## 3. Requisitos não funcionais

| ID | Requisito | SLI | Alvo |
|---|---|---|---|
| NFP-1 | Latência de montagem e assinatura | p99 de `pacs008_montagem_segundos` | menor que 200 ms |
| NFP-2 | Mensagens inválidas enviadas | contador | zero |
| NFP-3 | Rastreabilidade | mensagens com trace completo | 100% |

## 4. Critérios de aceite

| # | Dado | Quando | Então |
|---|---|---|---|
| CA-1 | uma segregação válida | é processada | o XML gerado valida contra o XSD |
| CA-2 | o mesmo E2EID duas vezes | é processado | o identificador da mensagem é o mesmo |
| CA-3 | qualquer mensagem | é inspecionada | não contém campo de trace |
| CA-4 | o envio falha | ocorre erro | a mensagem não é considerada enviada |

## 5. Testes

| Requisito | Teste | Nível |
|---|---|---|
| RP-1 a RP-3 | `deve_gerar_mensagem_valida_contra_o_xsd` | integração |
| RP-4, RP-5 | `deve_assinarE_validar...` no módulo de contrato | integração |
| RP-7 | inspeção do XML gerado no teste acima | integração |
