# ADR-0004: O E2EID é a chave de correlação

- Status: aceito
- Data: 2026-11-17

## Contexto

Uma transação atravessa dois serviços e dois tópicos. Sem um identificador comum, não existe como correlacionar o cálculo com o envio, nem garantir que mensagens da mesma transação cheguem na ordem.

O identificador canônico da transação no Pix é o E2EID (End-to-End ID, identificador único ponta a ponta). O Manual de Operações do CGIBS é explícito: o único identificador considerado no contexto do Split Payment é o E2EID.

A API da Plataforma Pública também expõe um `nsuId`, mas ele é o número sequencial da mensagem na plataforma, usado para paginação. Não identifica a transação.

## Decisão

O E2EID é a chave da mensagem em todos os pontos onde há chave.

- No tópico `segregacao.calculada.v1`, a chave da mensagem é o E2EID.
- O mesmo E2EID é o campo de correlação nos dois eventos.
- Toda transação de uma mesma compra cai na mesma partição e é consumida em ordem.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| NSU | não identifica a transação no Pix, é sequência de mensagem |
| txid | identifica a cobrança, não a transação, e não existe em todo arranjo |
| Gerar um identificador próprio | cria um segundo vocabulário para o mesmo conceito |

## Consequências

Positivas:

- Ordem garantida por transação, sem particionamento manual.
- Correlação direta entre cálculo e envio.
- O vocabulário é o mesmo que o mercado usa para Pix.

Negativas:

- O serviço depende de o sistema interno enviar o E2EID preenchido.
- Uma chave malformada concentra carga em uma partição.
- Guardar a deduplicação por E2EID ainda não está implementado. Hoje a chave garante ordem, não unicidade. A deduplicação é requisito RS-4, ainda pendente na matriz de rastreabilidade.
