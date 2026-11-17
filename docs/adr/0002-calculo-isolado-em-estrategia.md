# ADR-0002: O cálculo de proporcionalidade fica atrás de uma estratégia

- Status: aceito
- Data: 2026-11-17

## Contexto

O Manual de Operações define que o CBS/IBS Segregado é calculado por proporcionalidade, com truncamento para baixo e uma hierarquia de valores de referência. A fórmula exata depende de detalhes que ainda não estão fechados na leitura, e a regulamentação está em movimento.

Se essa regra ficar espalhada no caso de uso, cada ajuste vira cirurgia em código de orquestração.

## Decisão

Tratar o cálculo como uma estratégia substituível.

- Existe uma porta `CalculoSegregacao` no domínio.
- A implementação inicial documenta a premissa usada, com o link para a seção do manual.
- O caso de uso depende da porta, nunca da implementação.
- Trocar a regra significa trocar a implementação e o teste, sem tocar em orquestração.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Calcular direto no caso de uso | acopla regra fiscal a fluxo e dificulta teste isolado |
| Deixar como TODO e não implementar | o requisito R1 fica sem código, e sem código não há comparação de valor |
| Usar biblioteca de terceiro | não existe para esta regra específica e adicionaria dependência sem ganho |

## Consequências

Positivas:

- A regra é testável isoladamente, com casos de truncamento.
- A incerteza fica explícita e localizada, não escondida.
- A troca de regra é um commit pequeno.

Negativas:

- Uma indireção a mais no caminho.
- Risco de a premissa envelhecer sem ninguém revisar. Mitigação: o teste carrega o caso do manual e o README aponta a dúvida aberta.
