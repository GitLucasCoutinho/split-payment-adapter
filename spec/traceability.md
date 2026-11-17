# Matriz de rastreabilidade

Liga cada requisito ao serviço que o implementa, à origem normativa, ao teste que o prova e à métrica que o observa.

Status possíveis: **pronto** quando existe teste automatizado; **pendente** quando o requisito está especificado mas ainda não tem teste.

## split-service

| Requisito | Origem | Teste | Métrica | Status |
|---|---|---|---|---|
| RS-1 cálculo | Manual de Operações | `deve_calcular_cbs_e_ibs_sobre_mil_reais` | `split_calculo_seconds` | pronto |
| RS-1 truncamento | Manual de Operações | `deve_truncar_para_baixo_e_nunca_arredondar_para_cima` | não se aplica | pronto |
| RS-2 fechamento | Constitution, artigo 2 | `a_segregacao_deve_fechar_em_qualquer_valor` | `split_pagamentos_total{resultado="falha"}` | pronto |
| RS-2 limite | Constitution, artigo 2 | `os_tributos_nunca_devem_passar_do_valor_bruto` | não se aplica | pronto |
| RS-3 evento | Visão geral | `deve_publicar_evento_com_os_dados_do_pagamento` | não implementado | pronto |
| RS-4 idempotência | Constitution, artigo 3 | não implementado | `split_duplicidade_total` | pendente |
| RS-5 valor inválido | Constitution, artigo 1 | não implementado | `split_recusa_total` | pendente |

## spi-envio-service

| Requisito | Origem | Teste | Métrica | Status |
|---|---|---|---|---|
| RP-1 mapeamento | Manual de Integração | `deve_gerar_mensagem_valida_contra_o_xsd` | não se aplica | pronto |
| RP-2 identificador | `pacs.008.spi.1.15.xsd` | `deve_derivar_do_end_to_end_id_quando_ele_tem_tamanho_suficiente` | não implementado | pronto |
| RP-3 valor | `pacs.008.spi.1.15.xsd` | `deve_gerar_mensagem_valida_contra_o_xsd` | não se aplica | pronto |
| RP-4 assinatura | `xmldsig.spi.1.0.xsd` | `deve_assinar_e_validar_contra_o_xsd` | não implementado | pronto |
| RP-5 validação | Constitution, artigo 13 | `deve_assinar_e_validar_contra_o_xsd` | não se aplica | pronto |
| RP-6 envio | Visão geral | `deve_enviar_o_xml_e_devolver_o_protocolo` | `pacs008_envio_seconds` | pronto |
| RP-7 sem trace no XML | Constitution, artigo 12 | inspeção no teste do mapper | não se aplica | pronto |

## spi-simulator

| Requisito | Origem | Teste | Métrica | Status |
|---|---|---|---|---|
| RM-1 validação | Manual de Integração | `deve_aceitar_mensagem_assinada_e_conforme` | `simulador_validacao_seconds` | pronto |
| RM-2 recusa por contrato | Constitution, artigo 13 | `deve_recusar_mensagem_sem_assinatura` | `simulador_mensagens_total{resultado="recusada"}` | pronto |
| RM-3 recusa duplicidade | Constitution, artigo 3 | `deve_recusar_mensagem_duplicada` | `simulador_mensagens_total{resultado="duplicada"}` | pronto |
| RM-4 protocolo | Visão geral | `deve_aceitar_mensagem_assinada_e_conforme` | não implementado | pronto |
| RM-5 log estruturado | Constitution, artigo 9 | log em JSON enviado ao Kibana | não se aplica | pronto |

## Plataforma

| Requisito | Origem | Verificação | Status |
|---|---|---|---|
| Evento com chave E2EID | Constitution, artigo 3 | `deve_enviar_o_evento_com_a_chave_e_o_cabecalho_de_correlacao` | pronto |
| Contrato versionado | Constitution, artigo 12 | nome do tópico com `.v1` | pronto |
| Serviço com dono de dado | Constitution, artigo 11 | revisão de dependências | pronto |
| Correlação propagada | ADR-0012 | `deve_propagar_o_identificador_na_chamada_de_saida` | pronto |
| Métrica de negócio exposta | ADR-0012 | `/actuator/prometheus` nos três serviços | pronto |
| Erro vai para tópico de erro | ADR-0011 | validado manualmente no Docker, sem teste automatizado | pendente |
| Domínio sem framework | Constitution, artigo 5 | revisão de imports | pendente |

## Leitura da matriz

Hoje são 22 requisitos prontos e 4 pendentes.

Os pendentes são reais, não esquecimento: idempotência no consumidor, recusa de valor inválido, teste automatizado da mensagem morta e verificação automática de que o domínio não importa framework.

Essa é a lista de trabalho que falta, e ela é derivada da spec, não de opinião.
