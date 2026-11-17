# Contexto regulatório do Split Payment

Notas de estudo que embasam o projeto. Tudo aqui tem fonte oficial e data de consulta. Regra do repositório: nenhuma afirmação regulatória sem link.

## O que é

Split Payment é a retenção automática de IBS e CBS no momento da liquidação financeira. A parcela do tributo é separada na hora do pagamento e recolhida ao Fisco. O estabelecimento recebe apenas o líquido.

Hoje o lojista recebe o valor cheio e apura o tributo depois. Com o split, o dinheiro já sai certo e o crédito do comprador depende da extinção do débito.

## Siglas

- **IBS**: Imposto sobre Bens e Serviços, de competência estadual e municipal.
- **CBS**: Contribuição sobre Bens e Serviços, de competência federal.
- **PP**: Plataforma Pública do Split Payment.
- **PSP**: Prestador de Serviços de Pagamento.
- **RFB**: Receita Federal do Brasil.
- **CGIBS**: Comitê Gestor do IBS.
- **ANS**: Acordo de Nível de Serviço.
- **MOC**: Mecanismo de Ocorrências.

## Base legal

| Norma | Papel |
|---|---|
| LC 214/2025 | Institui e regulamenta o IBS e a CBS, incluindo o split payment |
| LC 227/2026 | Altera a LC 214/2025 |
| Decreto 12.955/2026 | Regulamento da CBS |

## Cronograma

O calendário está em movimento. Confirmar antes de citar em conversa.

| Ano | O que acontece |
|---|---|
| 2026 | transição e testes, com alíquotas reduzidas |
| 2027 | CBS em vigor, split em adesão facultativa, adoção faseada a partir de março |
| 2028 | split obrigatório nas operações entre empresas |

O Comitê Gestor já declarou publicamente que o sistema de arrecadação não ficaria pronto em janeiro de 2027.

## A Plataforma Pública

A PP é o ponto único de comunicação entre os PSPs e os entes governamentais. E o manual é explícito: **a PP não implementa regra de negócio**. Ela recebe eventos, aplica validação sintática e semântica, registra para rastreabilidade e auditoria, e roteia para o destinatário.

Consequência prática: o cálculo do tributo é responsabilidade do PSP. A PP não valida o mérito do valor.

Documentos que compõem a especificação:

| Documento | Versão | Finalidade |
|---|---|---|
| Manual de Integração e OpenAPI | 1.1.1 | contrato técnico, endpoints, dicionário de campos |
| Manual de Operações | 1.0.1 | fluxos, regras de cálculo, experiência do usuário |
| Manual de Tempos | 1.0.1 | prazos, janelas, ANS e marcos temporais |
| Manual de Redes | 1.0.1 | topologia, capacidade e resiliência |
| Manual de Segurança | 1.0.0 | autenticação, criptografia, auditoria |
| Manual de Habilitação | 1.0.0 | checklist de entrada de novos PSPs |
| Plano de Implantação | não consultado | cronograma e penalidades por etapa |

## Arranjos cobertos

| Arranjo | Cálculo de proporcionalidade | Janela de retorno | Segregação |
|---|---|---|---|
| Pix Dinâmico | sim | sim | 3 passos |
| Pix Automático | sim | sim | 3 passos |
| Pix Estático | não | não | 3 passos |
| Boleto | sim | sim | 3 passos |
| TED | não | não | 3 passos |
| TEF | não | não | 3 passos |

Este projeto implementa **Pix Dinâmico**, que é o arranjo que reúne cálculo, janela de retorno e segregação completa.

## O ponto que todo adquirente vai perguntar

O OpenAPI oficial v1.1.1 **não cobre cartão**. Cobre boleto, Pix nas três modalidades, TED e TEF.

Para adquirência de cartão, o caminho é outro: ou uma versão futura da PP, ou o **RAD** (Recolhimento pelo Adquirente), que é mecanismo distinto e com regulamentação própria.

Essa é a primeira pergunta que alguém de adquirência faz ao ver o projeto. Melhor ter a resposta pronta do que ser pego.

## Fontes

Consultadas em 17 de novembro de 2026.

- [Página oficial do Split Payment no CGIBS](https://www.cgibs.gov.br/split-payment), com os manuais e o OpenAPI v1.1.1. Confirmada em 17 de novembro de 2026: lista os manuais de Integração 1.1.1, Operações 1.0.1, Tempos 1.0.1, Redes 1.0.1, Segurança 1.0.0 e Habilitação 1.0.0
- [Split payment e o cronograma de obrigatoriedade](https://v360.io/blog/split-payment-reforma-tributaria/)
- [Split payment padrão e simplificado](https://v360.io/blog/split-payment-padrao-simplificado/)
- [Sistema de arrecadação não fica pronto para janeiro de 2027](https://netcpa.com.br/colunas/sistema-de-arrecadacao-da-reforma-tributaria-split-payment-nao-fica-pronto-para-janeiro-de-2027/28259)
- [Impacto no caixa e na operação das empresas](https://revistapegn.globo.com/gestao/reforma-tributaria/noticia/2026/09/reforma-tributaria-entenda-o-que-muda-no-caixa-e-na-operacao-das-empresas-com-o-split-payment.ghtml)

## Duas fontes oficiais, não uma

O projeto usa dois repositórios oficiais diferentes, e vale não confundir:

| O que veio | De onde |
|---|---|
| Manuais regulatórios e OpenAPI da Plataforma Pública | [CGIBS](https://www.cgibs.gov.br/split-payment) |
| Schemas XML do SPI, incluindo o `pacs.008` | [Banco Central, comunicação de dados](https://www.bcb.gov.br/estabilidadefinanceira/comunicacaodados), pacote 5.12.1 |

O primeiro embasa o contexto e a regra de cálculo. O segundo define a mensagem que o código realmente gera e valida. A origem dos schemas está registrada no [README do serviço de envio](../../spi-envio-service/README.md).
