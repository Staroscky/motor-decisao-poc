    # Project Context

## Resumo do produto

`motordecisao` é uma API REST responsável por executar lógica de decisão orientada a regras de negócio.
O serviço recebe requisições de sistemas upstream, avalia critérios configuráveis e retorna decisões estruturadas.
É construído como um microsserviço autônomo, projetado para ser chamado via HTTP por outros serviços.

## Objetivo principal

Centralizar e padronizar a execução de regras de decisão, evitando que cada sistema consumer precise reimplementar lógica de negócio própria.

## Stack e runtime

- Linguagem: Java 21
- Framework principal: Spring Boot 3.5
- UI: Nenhuma (API REST pura)
- Backend/API: Spring Web MVC (servlet stack)
- Banco de dados: não definido ainda
- Ferramentas de build/test: Maven, Spring Boot Test

## Arquitetura e convenções

- Arquitetura de referência: **Vertical Slice** — cada feature ou domínio fica isolado em seu próprio pacote com handler, serviço, modelo e repositório
- Pacote `core/` — configurações e componentes transversais reutilizáveis entre todos os slices (logging, observabilidade, HTTP client, propagação de contexto)
- Pacote raiz: `com.staroscky.motordecisao`
- Configurações externalizada via `application.yaml`
- Sem uso de Lombok por ora (não declarado no `pom.xml`)

## Domínio e regras importantes

- O termo "decisão" representa o resultado de uma avaliação de regras sobre um input estruturado
- Rastreabilidade de chamadas entre serviços é requisito operacional desde o início
- Cada requisição deve ser identificável por `x-correlationId` e `x-flowId`
- **Domínio de pagamentos:** o motor avalia viabilidade de instrumentos de pagamento (PIX, TED, TEF) para transações financeiras
- **checkinId:** identificador de transação com formato `checkin:<tipo>:<versao>:<uuid>` onde `<tipo>` pode ser `qrcode`, `chaves_pix` ou `agconta` — o tipo é inferido do próprio ID, sem discriminador explícito
- **ISPB:** código de identificação de instituição financeira; determina quais instrumentos estão disponíveis para contas `agconta` (Itaú `60701190` → PIX+TEF; outros → PIX+TED)
- **BFF pattern:** o serviço funciona como BFF (Backend for Frontend), orquestrando consultas a upstreams e retornando uma resposta agregada otimizada para o cliente

## Integrações e dependências externas

- Chamadas HTTP de saída via OpenFeign
- `InstituicaoClient`: cliente Feign para busca do ISPB da instituição de destino a partir do `checkinId`
- Logging estruturado de requisições/respostas via Logbook (Zalando)
- Sem autenticação definida ainda
- Sem APM ou tracing distribuído definido ainda (Logbook + MDC cobrem a necessidade inicial)

## Riscos e cuidados recorrentes

- Virtual threads (Java 21) exigem cuidado com propagação de contexto: `ThreadLocal` não é herdado por virtual threads filhas sem decoração explícita
- MDC do Logback é baseado em `ThreadLocal` — sem o task decorator adequado, logs em virtual threads perdem os campos de correlação
- Logbook e OpenFeign precisam ser integrados para que chamadas de saída também sejam logadas de forma consistente
