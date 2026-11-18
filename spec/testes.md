# Pirâmide de testes

A estratégia é simples: **o teste mais barato possível para provar cada coisa**. Unidade onde a regra é pura, contrato onde o formato é imposto, integração onde o framework entra, arquitetura onde a regra é de dependência.

## Os números

Total: **72 testes** em 4 módulos.

| Camada | Quantidade | Fatia | Roda em |
|---|---|---|---|
| Unidade | 54 | 75% | milissegundos |
| Contrato (XSD oficial) | 11 | 15% | segundos |
| Integração | 5 | 7% | segundos |
| Arquitetura | 2 | 3% | milissegundos |
| Ponta a ponta | 0 | 0% | manual, no Docker |

```
        ▲  ponta a ponta (manual, no Docker)
       ╱ ╲
      ╱   ╲      integração        5
     ╱─────╲     contrato         11
    ╱───────╲    arquitetura       2
   ╱─────────╲   unidade          53
  ╱───────────╲
```

A base é larga e o topo é fino. É a forma desejada: feedback rápido na maior parte do tempo, e o caro só onde não há alternativa.

## Por camada

### Unidade, 53 testes

Prova a regra de negócio sem framework, sem banco, sem rede.

| Onde | O que protege |
|---|---|
| `ValorMonetario` | aritmética em centavos, truncamento, soma |
| `Segregacao` | invariante de fechamento: bruto = líquido + IBS + CBS |
| `CalculoSegregacaoPadrao` | alíquota de 2026, truncamento para baixo |
| `MapeadorPagamento` | conversão do evento para domínio e recusa |
| `PagamentoConsumer` | contagem de calculado, recusado e falha |
| `GeradorPagamento` | EndToEndId conforme o padrão do schema |
| `ExecutorCarga` | taxa, duração, limite e interrupção |
| `CorrelationId*` | propagação do identificador, sem vazar escopo |

### Contrato, 11 testes

Valida a mensagem contra o **XSD oficial do Banco Central**. É a camada que pega o erro que ninguém pega: campo fora do formato, elemento opcional inventado, assinatura sem o perfil exigido.

| Onde | O que protege |
|---|---|
| `Pacs008CodecTest` | leitura, escrita, assinatura e validação |
| `Pacs008MapperTest` | o mapper produz mensagem conforme |
| `ValidadorPacs008Test` | o simulador recusa o que está fora |

Essa camada não existiria num projeto comum. Ela existe porque o contrato é externo e caro: uma mensagem torta é recusada pelo Banco Central, não por nós.

### Integração, 5 testes

Prova que o framework e o wiring funcionam.

| Onde | O que protege |
|---|---|
| `ApplicationContextTest` | o contexto sobe com as configurações atuais |
| `SimuladorControllerTest` | o endpoint valida, recusa e protocola via `MockMvc` |
| `EnviadorHttpTest` | o envio HTTP funciona, com `MockRestServiceServer` |

O `ApplicationContextTest` existe por um motivo concreto: um padrão de logback inválido derrubou a aplicação em produção de teste, e nenhum teste pegou. Agora pega.

### Arquitetura, 2 testes

Prova que a regra de dependência não foi violada.

| Regra | O que garante |
|---|---|
| `o_nucleo_nao_conhece_framework` | domínio e aplicação não importam Spring, Kafka, Micrometer, Jackson |
| `o_nucleo_nao_depende_dos_adaptadores` | a dependência aponta para dentro |

Antes isso era revisão manual, que depende de disciplina. Agora o build quebra se alguém importar Spring no domínio.

### Ponta a ponta, zero

Não há teste automatizado de ponta a ponta. A validação é manual, com o ambiente no Docker:

1. publicar um pagamento
2. conferir o cálculo no log
3. conferir o `pacs.008` aceito no simulador
4. conferir a métrica no Prometheus
5. conferir o log no Kibana

Isso está no `docs/demonstracao.md`. Automatizar exigiria Testcontainers subindo Kafka e os serviços, o que encarece o build e o torna dependente de Docker.

## O que falta, e por quê

| Lacuna | Por que não está feito |
|---|---|
| ponta a ponta automatizado | exigiria Testcontainers. O retorno não paga o custo num projeto de referência |
| teste de contrato entre serviços | o contrato é o XSD, já coberto. Não há consumidor externo para pactuar |
| teste de carga automatizado | o `carga-simulator` existe, mas roda sob demanda, não no build |
| teste da mensagem morta | validado manualmente. Um teste de integração com Kafka embarcado é o próximo passo |

## Como rodar

```bash
mvn test        # 72 testes, sem Docker e sem broker
mvn verify      # o mesmo, mais o gate de cobertura
```

O gate exige 80% de linha e 70% de branch por módulo, e 90% de linha no pacote de domínio do `split-service`. Hoje todos passam.

## A regra que eu levo

> "Teste de unidade prova a regra. Teste de contrato prova o formato. Teste de integração prova o wiring. Teste de arquitetura prova a intenção. O que não se prova barato, se prova à mão, e se documenta."
