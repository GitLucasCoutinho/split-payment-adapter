# ADR-0009: O evento carrega o estado necessário

- Status: aceito
- Data: 2026-11-17

## Contexto

O `spi-envio-service` precisa dos dados do pagamento para montar a mensagem: nome, documento, conta e ISPB do pagador e do recebedor. Esses dados nascem no evento de entrada do `split-service`.

Existem duas formas de obter isso: consultar o serviço de origem, ou receber tudo no evento.

## Decisão

O evento `segregacao.calculada.v1` carrega o estado completo necessário para o próximo serviço.

Isso é transferência de estado por evento. O consumidor não faz chamada síncrona para montar a mensagem.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Consultar o `split-service` por HTTP | cria acoplamento síncrono e um ponto de falha a mais |
| Consumir também o tópico de pagamento | duplica estado e exige junção entre tópicos |
| Guardar em banco compartilhado | viola o artigo 11 da constitution |

## Consequências

Positivas:

- O `spi-envio-service` funciona mesmo se o `split-service` estiver fora.
- Não existe chamada síncrona entre serviços.
- O evento é autossuficiente e testável isoladamente.

Negativas:

- O evento cresce e carrega dado que talvez nem todo consumidor use.
- Dado do pagamento fica duplicado no tópico.
- Mudança no cadastro do participante não se propaga retroativamente.
