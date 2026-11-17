# Glossário

Termos do domínio. Toda sigla que aparece no README e na spec está definida aqui, e é definida na primeira menção em cada documento.

## Regulatório

| Termo | Definição |
|---|---|
| IBS | Imposto sobre Bens e Serviços. Competência compartilhada entre estados e municípios |
| CBS | Contribuição sobre Bens e Serviços. Competência federal |
| Reforma Tributária do Consumo | Substituição de PIS, Cofins, ICMS e ISS por CBS e IBS |
| Split Payment | Retenção automática de IBS e CBS na liquidação financeira |
| RAD | Recolhimento pelo Adquirente. Mecanismo de recolhimento pelo adquirente, distinto do split |
| ANS | Acordo de Nível de Serviço. Prazo máximo e meta por indicador |
| CGIBS | Comitê Gestor do IBS |
| RFB | Receita Federal do Brasil |

## Plataforma

| Termo | Definição |
|---|---|
| PP | Plataforma Pública do Split Payment. Ponto único entre PSPs e entes governamentais |
| PSP | Prestador de Serviços de Pagamento |
| PSP Recebedor Direto | PSP responsável pelo cumprimento dos prazos e pelos informes |
| Arranjo | Modalidade de pagamento: Pix Dinâmico, Pix Estático, Pix Automático, Boleto, TED, TEF |
| Retorno Super Inteligente | Mecanismo de devolução de eventos por streaming com token |
| Consulta Retroativa | Recuperação de mensagens por intervalo de NSU |
| MOC | Mecanismo de Ocorrências. Canal de notificação, solicitação de estorno e consulta |
| idInfSegr | Identificador da remessa de segregação, devolvido no passo 1 |
| streamId | Identificador da stream de retorno |
| Token de continuação | Posição de leitura, com validade de 2 minutos |

## Domínio do projeto

| Termo | Definição |
|---|---|
| Transação | Pagamento liquidado que precisa ser informado à PP |
| CBS/IBS Informado | Valor do tributo declarado pelo PSP |
| CBS/IBS Segregado | Valor efetivamente retido, após cálculo de proporcionalidade |
| Segregação | Ato de separar a parcela do tributo do valor do estabelecimento |
| Remessa de segregação | Envelope de três passos que agrupa os lotes de transação |
| Lote | Conjunto de transações enviado dentro de uma remessa |
| NSU | Número sequencial único. Na PP identifica a **mensagem**, não a transação, e serve para paginação |
| E2EID | End-to-End ID. Identificador único ponta a ponta da transação Pix. É a chave de idempotência |
| txid | Identificador da cobrança Pix (o QR Code), definido pelo PSP recebedor |
| Estabelecimento | O cliente PJ que recebe o pagamento |

## Arquitetura do código

| Termo | Definição |
|---|---|
| Hexagonal | Estilo em que o domínio define portas e o mundo externo implementa adaptadores |
| Porta | Interface que expressa uma necessidade ou uma oferta do domínio |
| Adaptador | Implementação concreta de uma porta, como HTTP, Kafka ou banco |
| SLI | Service Level Indicator. O número medido |
| SLO | Service Level Objective. A meta do indicador |
| DTO | Data Transfer Object. Objeto de transporte na borda |
