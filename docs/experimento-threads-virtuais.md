# Experimento: threads virtuais no Java 21

## Pergunta

O laboratório roda em Java 21. Ligar `spring.threads.virtual.enabled` melhora a vazão ou a latência deste sistema?

A resposta não pode ser "sim, porque é Java 21". Precisa de medição, como o resto do projeto.

## O que muda no código

Uma linha de configuração por serviço:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

Isso faz o Tomcat atender cada requisição em uma thread virtual, em vez de uma thread de plataforma do pool. As threads do consumidor Kafka não mudam: elas vêm do `ConcurrentMessageListenerContainer`, que não é afetado por essa propriedade.

Para o experimento, a chave também pode ser ligada e desligada sem recompilar:

```powershell
$env:VT_ENABLED = "false"
docker compose -f docker-compose.yml -f docker-compose.observabilidade.yml up -d
```

## Método

| Item | Valor |
|---|---|
| carga | 400 publicações por segundo, por 10 segundos |
| aquecimento | dois pulsos descartados antes de medir |
| repetições | 3 por rodada, mediana |
| rodadas | 2 com threads virtuais, 2 sem, em ordem alternada |
| latência | `http_server_requests_seconds` do `spi-simulator`, no endpoint `/spi/mensagens` |
| vazão | requisições processadas por segundo, no mesmo endpoint |

A ordem alternada existe para descartar viés: a segunda medição sempre parece melhor, porque o sistema já está quente.

## Resultados

| Configuração | Vazão mediana | Latência mediana | Threads de JVM |
|---|---|---|---|
| threads virtuais, 1ª rodada | 181,7 por segundo | 2,75 ms | 20 |
| threads virtuais, 2ª rodada | 179,0 por segundo | 2,79 ms | 20 |
| threads de plataforma, 1ª rodada | 217,8 por segundo | 2,30 ms | 24 |
| threads de plataforma, 2ª rodada | 207,7 por segundo | 2,89 ms | 24 |

A latência fica na mesma faixa nas duas configurações. A vazão sem threads virtuais ficou cerca de 15 por cento maior nas duas rodadas, mas a variação entre repetições é grande o suficiente para eu não afirmar que essa diferença seja do recurso e não do gerador de carga.

O único número que se moveu com certeza é o de threads da JVM: caiu de 24 para 20.

## Por que o ganho não aparece

A lei de Little resolve a dúvida:

```
concorrência média = vazão × tempo de resposta
concorrência média = 200 por segundo × 0,0028 segundos = 0,56
```

Menos de **uma** requisição em andamento, em média. O sistema nunca teve várias requisições esperando ao mesmo tempo, então não havia escassez de thread para resolver.

Thread virtual ganha quando existe espera bloqueante em massa: mil requisições paradas esperando banco, disco ou outro serviço. Aqui, cada requisição leva menos de três milissegundos e o Tomcat de plataforma, com 200 threads, nunca chegou perto do limite.

## Conclusão

Não ligar. A configuração fica desligada, e o motivo está escrito.

O ganho apareceria em outro cenário:

| Cenário | Thread virtual ajudaria |
|---|---|
| requisição esperando banco por 200 ms | sim, e muito |
| mil conexões simultâneas e lentas | sim |
| este laboratório, requisição de 3 ms | não |
| consumidor Kafka com uma thread por partição | não, o recurso não alcança |

## O que este experimento ensina

O recurso existe, é bom, e não serve para este caso. Saber dizer isso com número é diferente de não conhecer o recurso.

É a mesma lógica do retorno do SPI: ficou de fora, com motivo escrito.
