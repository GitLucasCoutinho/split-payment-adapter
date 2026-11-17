# ADR-0010: O simulador valida, não é eco

- Status: aceito
- Data: 2026-11-17

## Contexto

O destino real é o SPI, que exige conexão e certificado que o projeto não tem. Precisamos de um dublê.

O caminho mais barato seria um endpoint que recebe o XML e devolve `200` sempre. Esse caminho não prova nada: um mapper errado passaria despercebido.

## Decisão

O simulador valida a mensagem contra o XSD antes de aceitar.

| Situação | Resposta |
|---|---|
| mensagem válida | `200` com protocolo |
| mensagem fora do contrato | `400` com o motivo |
| E2EID repetido | `409` |
| mensagem aceita | log com E2EID e protocolo |

Ele usa o mesmo codec do produtor, por meio do módulo de contrato.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Endpoint que sempre aceita | não detecta erro de mapeamento |
| WireMock com resposta fixa | valida nada, só simula latência |
| Não ter simulador | a demo perde o fim do fluxo |

## Consequências

Positivas:

- Erro no mapper aparece imediatamente, no lugar certo.
- A duplicidade é recusada, como no SPI real.
- O projeto demonstra o contrato dos dois lados.

Negativas:

- O simulador deixa de ser um dublê trivial.
- Guarda estado em memória, então reinício limpa os protocolos.
