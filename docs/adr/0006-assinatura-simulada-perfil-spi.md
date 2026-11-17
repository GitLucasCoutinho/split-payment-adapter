# ADR-0006: Assinatura simulada com o perfil do SPI

- Status: aceito
- Data: 2026-11-17

## Contexto

O projeto é acadêmico e não tem certificado digital do Banco Central. Mas o schema do SPI exige assinatura dentro do elemento `Sgntr`, com um perfil mais restrito que o padrão W3C.

Sem assinatura, a mensagem não valida, e sem validação o projeto perde a credibilidade que quer demonstrar.

## Decisão

Tratar a assinatura como porta, com um adaptador simulado como padrão.

- `Assinador` é a porta. `AssinadorSimulado` monta uma assinatura estruturalmente válida, com valores fixos.
- O valor da assinatura é literalmente `assinatura-simulada` em base64, para que ninguém confunda com assinatura real.
- Quem tiver certificado implementa `Assinador` e troca o bean, sem tocar no codec.

## O perfil de assinatura do SPI

Descoberto por validação, não por leitura de blog:

| Restrição | Exigência |
|---|---|
| Canonicalização | apenas `xml-exc-c14n#` |
| Método de assinatura | apenas `rsa-sha256` |
| Método de resumo | apenas `sha256` |
| Referências | mínimo de 3 no `SignedInfo` |
| `KeyInfo` | obrigatório, com atributo `Id` do tipo ID |
| `X509Data` | exige `X509IssuerSerial`, não o certificado |

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Enviar sem assinatura | o schema recusa, e o simulador recusaria |
| Desligar a validação no teste | esconde o problema em vez de resolvê-lo |
| Gerar certificado autoassinado | a estrutura não bateria com o perfil, e daria falsa sensação de validade |

## Consequências

Positivas:

- A mensagem valida contra o XSD oficial.
- O caminho de produção fica isolado em um adaptador.
- O projeto documenta um perfil de assinatura que quase ninguém conhece.

Negativas:

- A assinatura não tem valor criptográfico.
- Um leitor desatento pode achar que está assinado de verdade. Mitigação: o valor é explícito e este ADR está no repositório.
