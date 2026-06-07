# PRD — Core Infraestrutura

## Resumo

Estabelecer a camada de infraestrutura transversal do serviço `motordecisao` antes da construção das features de negócio. Isso inclui cliente HTTP declarativo, logging estruturado de HTTP, execução com virtual threads e propagação de contexto de rastreabilidade entre threads e serviços.

## Problema

O projeto está no estado de scaffolding inicial — sem cliente HTTP, sem logging de requisições/respostas, sem suporte a virtual threads e sem mecanismo de propagação de `x-correlationId` e `x-flowId`. Construir features de negócio sobre essa base tornaria cada slice responsável por replicar configurações operacionais, aumentando o risco de inconsistência nos logs, perda de rastreabilidade e acoplamento entre preocupações transversais e regras de negócio.

## Objetivo

Entregar um pacote `core` que forneça, de forma transparente para os slices de negócio:

- Cliente HTTP declarativo e configurado para chamadas de saída
- Logging automático e estruturado de todas as requisições e respostas HTTP (entrada e saída)
- Execução do servidor com virtual threads habilitadas
- Propagação automática de `x-correlationId` e `x-flowId` como headers de saída e como campos MDC nos logs
- Garantia de que o MDC seja propagado corretamente em virtual threads filhas

## Escopo

### Inclui

- Adição das dependências: Spring Cloud OpenFeign, Logbook (Spring Boot Starter), e suporte a virtual threads via configuração Spring Boot
- Configuração do `@EnableFeignClients` no pacote `core`
- Configuração do Logbook com nível e formato adequados para produção (sem logar bodies sensíveis por padrão)
- Habilitação de virtual threads via `spring.threads.virtual.enabled=true`
- `OncePerRequestFilter` que lê `x-correlationId` e `x-flowId` dos headers de entrada, grava no MDC e repassa nos headers das respostas e chamadas de saída (via `RequestInterceptor` do Feign)
- `TaskDecorator` para `ThreadPoolTaskExecutor` (ou equivalente) que propaga o MDC de threads pai para virtual threads filhas

### Não inclui

- Implementação de qualquer regra de negócio ou slice de domínio
- Autenticação ou autorização
- Tracing distribuído (ex.: OpenTelemetry, Zipkin, Jaeger)
- Configuração de banco de dados
- Rate limiting, circuit breaker ou retry policy (fora do escopo desta entrega)
- Geração automática de `x-correlationId` quando ausente no header de entrada (pode ser adicionado depois)

## Fluxo esperado

1. Uma requisição chega ao `motordecisao` com os headers `x-correlationId` e `x-flowId`.
2. O filtro captura esses valores, grava no MDC e os repassa no header da resposta.
3. O Logbook registra automaticamente a requisição de entrada e a resposta de saída com os campos de correlação visíveis no log.
4. Se o handler aciona um Feign client para chamar outro serviço, o interceptor de saída injeta os mesmos `x-correlationId` e `x-flowId` no header da chamada.
5. Todo o processamento ocorre em virtual threads, e o MDC está disponível corretamente em cada thread filha durante o ciclo de vida da requisição.

## Critérios de sucesso

- Requisição recebida com `x-correlationId` aparece nos logs com esse valor no MDC
- Requisição recebida com `x-flowId` aparece nos logs com esse valor no MDC
- Chamada de saída via Feign carrega `x-correlationId` e `x-flowId` nos headers
- Logbook registra entrada e saída sem configuração adicional por slice
- Virtual threads estão ativas e confirmadas via log de startup ou teste
- Nenhum slice de negócio precisa lidar diretamente com propagação de contexto

## Restrições ou observações

- Usar Java 21 — virtual threads são nativas, sem bibliotecas externas
- A integração Logbook + Feign pode exigir um `FeignLoggerFactory` customizado ou uso do `LogbookFeignLogger` provido pelo Logbook
- MDC do Logback é `ThreadLocal`-based: o `TaskDecorator` é obrigatório para garantir propagação correta
- Manter configurações no pacote `core` para não poluir os slices de negócio
