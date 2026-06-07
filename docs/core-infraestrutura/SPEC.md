# SPEC — Core Infraestrutura

## 1. Contexto da solicitação

### 1.1 História ou tarefa do usuário

- **Solicitante:** Desenvolvedor do projeto `motordecisao`
- **Tipo:** feature (infraestrutura transversal)
- **História:** Como desenvolvedor, quero que o serviço tenha uma camada `core` com cliente HTTP declarativo, logging estruturado de HTTP, virtual threads e propagação automática de `x-correlationId` e `x-flowId`, para que os slices de negócio possam ser construídos sobre uma base operacional sólida e sem duplicação de configuração.
- **Valor esperado:** Todos os slices de negócio herdam rastreabilidade, logging e escalabilidade sem precisar lidar com infraestrutura.

### 1.2 Problema observado

O projeto é um scaffolding Spring Boot 3.5 sem nenhuma infraestrutura transversal. Construir features de negócio agora tornaria cada slice responsável por:
- Configurar seu próprio cliente HTTP;
- Propagar manualmente cabeçalhos de rastreabilidade;
- Lidar com perda de MDC em threads assíncronas.

A ausência de uma base comum gera inconsistência nos logs, rastreabilidade quebrada e acoplamento entre regras de negócio e configuração operacional.

### 1.3 Objetivo da entrega

A entrega está completa quando:
- Qualquer requisição com `x-correlationId` e `x-flowId` tem esses valores nos logs (MDC) e na resposta;
- Chamadas de saída via Feign carregam os mesmos headers;
- Logbook registra entrada e saída automaticamente, sem configuração por slice;
- Virtual threads estão ativas no servidor e no executor assíncrono;
- MDC está disponível em threads assíncronas filhas.

---

## 2. Objetivo técnico

Criar o pacote `com.staroscky.motordecisao.core` com componentes transversais que serão compartilhados por todos os slices verticais. A entrega cobre seis responsabilidades distintas que se orquestram de forma transparente no pipeline de execução de cada requisição.

---

## 3. Estado atual

O projeto possui apenas:

| Arquivo | Conteúdo atual |
|---|---|
| `src/main/java/.../MotordecisaoApplication.java` | `@SpringBootApplication` vazio, sem qualquer configuração adicional |
| `src/main/resources/application.yaml` | Apenas `spring.application.name: motordecisao` |
| `pom.xml` | `spring-boot-starter-web` e `spring-boot-starter-test`. Sem Spring Cloud, sem Logbook |

Não existe nenhum filtro, interceptor, cliente HTTP, decorador de thread ou configuração de logging além do padrão do Spring Boot.

---

## 4. Escopo da solução

### 4.1 O que muda

| Área | Estado atual | Estado esperado | Impacto |
|---|---|---|---|
| `pom.xml` | Apenas `spring-boot-starter-web` | + Spring Cloud BOM 2025.0.2, `spring-cloud-starter-openfeign`, `logbook-spring-boot-starter:4.0.4`, `logbook-openfeign:4.0.4` | Alto |
| `application.yaml` | `spring.application.name` apenas | + `spring.threads.virtual.enabled: true` + configurações do Logbook | Médio |
| `MotordecisaoApplication.java` | Sem `@EnableFeignClients` | Sem alteração — `@EnableFeignClients` vai em `FeignConfig.java` do `core` | — |
| `core/config/FeignConfig.java` | Não existe | Cria: `@EnableFeignClients` + beans do logger Feign (`FeignLogbookLogger`) | Alto |
| `core/config/AsyncConfig.java` | Não existe | Cria: `@EnableAsync` + `AsyncConfigurer` com `SimpleAsyncTaskExecutorBuilder` + `MdcTaskDecorator` | Alto |
| `core/filter/CorrelationHeaderFilter.java` | Não existe | Cria: `OncePerRequestFilter` — lê headers, grava MDC, repassa na resposta | Alto |
| `core/feign/CorrelationFeignInterceptor.java` | Não existe | Cria: `RequestInterceptor` — lê MDC e injeta nos headers de saída do Feign | Alto |
| `core/mdc/MdcTaskDecorator.java` | Não existe | Cria: `TaskDecorator` — copia snapshot MDC para threads filhas | Alto |

### 4.2 O que não muda

- A classe `MotordecisaoApplication.java` não recebe alterações de lógica ou anotações novas.
- Nenhum slice de negócio existente é afetado (não há nenhum).
- Nenhuma regra de domínio é introduzida.
- Não há mudança no modelo de deployment ou em variáveis de ambiente.
- Autenticação, circuit breaker, retry e rate limiting ficam fora desta entrega.

### 4.3 Restrições e pressupostos

- Java 21 — virtual threads são nativas via `Thread.ofVirtual()`.
- Spring Boot 3.5.x — servlet stack (Tomcat), não reativo.
- Spring Cloud 2025.0.2 (Northfields) — versão compatível com Spring Boot 3.5.x.
- Logbook 4.0.4 — versão Jakarta EE compatível com Spring Boot 3.x.
- MDC do Logback é baseado em `ThreadLocal` — não propagado automaticamente para virtual threads.
- `SimpleAsyncTaskExecutor` com virtual threads habilitados pelo Spring Boot é o executor padrão para `@Async`; o `TaskDecorator` é aplicado via `SimpleAsyncTaskExecutorBuilder`.
- O `CorrelationHeaderFilter` deve ter prioridade máxima para garantir que o MDC esteja preenchido antes do Logbook registrar a requisição.
- Se `x-correlationId` ou `x-flowId` estiverem ausentes na requisição, nenhum valor é gerado automaticamente nesta entrega (geração automática fica fora do escopo).

---

## 5. Requisitos funcionais

| ID | Requisito funcional | Prioridade | Origem |
|---|---|---|---|
| RF-01 | O sistema deve habilitar virtual threads no Tomcat via `spring.threads.virtual.enabled: true` | must | PRD |
| RF-02 | O sistema deve registrar `x-correlationId` e `x-flowId` no MDC ao receber uma requisição HTTP com esses headers | must | PRD |
| RF-03 | O sistema deve repassar `x-correlationId` e `x-flowId` como headers na resposta HTTP | must | PRD |
| RF-04 | O sistema deve injetar `x-correlationId` e `x-flowId` do MDC nos headers de toda chamada de saída via Feign | must | PRD |
| RF-05 | O sistema deve logar automaticamente requisições de entrada e respostas de saída via Logbook, incluindo os campos MDC de correlação | must | PRD |
| RF-06 | O sistema deve logar chamadas Feign de saída via `FeignLogbookLogger` com nível `FULL` | must | PRD |
| RF-07 | O sistema deve propagar o snapshot do MDC para qualquer thread assíncrona filha via `MdcTaskDecorator` | must | PRD |
| RF-08 | As configurações de infraestrutura devem estar isoladas no pacote `core`, sem dependência de pacotes de negócio | must | Arquitetura |

---

## 6. Cenários e fluxos esperados

### 6.1 Cenários principais

- **Requisição com headers de correlação:** `POST /decisao` com `x-correlationId: abc-123` e `x-flowId: flow-456` → MDC populado → Logbook loga entrada e saída com MDC → resposta contém os mesmos headers.
- **Feign chamando serviço externo:** handler chama `ExemploFeignClient.consultar()` → `CorrelationFeignInterceptor` injeta `x-correlationId` e `x-flowId` nos headers → `FeignLogbookLogger` loga a chamada de saída.
- **Processamento assíncrono com `@Async`:** handler delega para método `@Async` → `MdcTaskDecorator` copia o snapshot MDC para a nova virtual thread → logs do método assíncrono carregam os mesmos campos de correlação.

### 6.2 Edge cases e falhas esperadas

- **Requisição sem headers de correlação:** `CorrelationHeaderFilter` não grava nada no MDC (não lança exceção, não gera ID). Os campos de correlação simplesmente não aparecem nos logs.
- **Header presente mas vazio:** headers com string vazia não são gravados no MDC. Validação: `!StringUtils.hasText(valor)` → ignora.
- **Feign sem MDC populado:** `CorrelationFeignInterceptor` verifica `MDC.get(...)` antes de adicionar o header; se `null`, não adiciona o header na requisição de saída.
- **Thread assíncrona sem MDC pai:** `MdcTaskDecorator` verifica se `MDC.getCopyOfContextMap()` é `null` antes de chamar `MDC.setContextMap(...)`.
- **Logbook em ambiente de testes:** Logbook está ativo por padrão em todos os profiles. Para desabilitar em testes, usar `logbook.filter.enabled: false` no `application.yaml` de teste.

---

## 7. Alternativas consideradas

### 7.1 Alternativa escolhida

**`OncePerRequestFilter` para correlação + `RequestInterceptor` do Feign para propagação de saída.**

O filtro centraliza a captura dos headers e a gravação no MDC em um único ponto, independente do handler. O interceptor Feign lê do MDC, eliminando a necessidade de passar contexto explicitamente entre camadas.

### 7.2 Alternativas descartadas

| Alternativa | Vantagens | Desvantagens | Motivo da não escolha |
|---|---|---|---|
| Geração automática de `x-correlationId` quando ausente | Garante rastreabilidade mesmo quando o chamador não envia | Gera IDs sem significado externo; dificulta correlação fim-a-fim em sistemas distribuídos | Fora do escopo desta entrega; pode ser adicionado depois com critério |
| `HandlerInterceptor` ao invés de `OncePerRequestFilter` | Integra com MVC | Não intercepta erros de servlet, não cobre chamadas fora do contexto MVC | `OncePerRequestFilter` cobre mais casos no pipeline servlet |
| `ThreadPoolTaskExecutor` com virtual threads manuais | Controle granular do pool | Configuração mais verbosa; `SimpleAsyncTaskExecutorBuilder` já é idiomático no Spring Boot 3.5 com virtual threads | `SimpleAsyncTaskExecutorBuilder` é a abordagem recomendada pelo Spring Boot |
| Não usar `logbook-openfeign`, logar Feign via `feign.Logger` customizado | Menos dependência | Duplicação de lógica de logging; perde integração com o formato e filtros do Logbook | `logbook-openfeign` centraliza o logging no Logbook |
| OpenTelemetry para propagação de contexto | Padrão de mercado, extensível | Overhead de setup; não é requisito do PRD nesta fase | Fora do escopo; o MDC atende a necessidade atual |

---

## 8. Design da solução

### 8.1 Visão geral da abordagem

Seis componentes novos compõem o `core`. A ordem de aplicação em runtime é:

```
Requisição HTTP → [CorrelationHeaderFilter] → [LogbookFilter] → Handler → [CorrelationFeignInterceptor + FeignLogbookLogger] → Serviço externo
                                                                   ↓
                                              [AsyncConfig + MdcTaskDecorator] → Virtual thread filha (@Async)
```

Os componentes são todos beans Spring e não têm dependência entre si, exceto onde explicitado.

---

### 8.2 `core/filter/CorrelationHeaderFilter.java`

- Estende `OncePerRequestFilter`
- Anotado com `@Component` e `@Order(Ordered.HIGHEST_PRECEDENCE)`
- Constantes: `X_CORRELATION_ID = "x-correlationId"`, `X_FLOW_ID = "x-flowId"`
- Lê o header da requisição; se presente e não vazio, grava no MDC e adiciona no header da resposta
- No bloco `finally`, remove as chaves do MDC (`MDC.remove(...)`) para evitar vazamento entre requisições

```java
// Esboço da implementação
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationHeaderFilter extends OncePerRequestFilter {

    private static final String X_CORRELATION_ID = "x-correlationId";
    private static final String X_FLOW_ID        = "x-flowId";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            setIfPresent(req, res, X_CORRELATION_ID, "correlationId");
            setIfPresent(req, res, X_FLOW_ID,        "flowId");
            chain.doFilter(req, res);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("flowId");
        }
    }

    private void setIfPresent(HttpServletRequest req, HttpServletResponse res, String header, String mdcKey) {
        String value = req.getHeader(header);
        if (StringUtils.hasText(value)) {
            MDC.put(mdcKey, value);
            res.addHeader(header, value);
        }
    }
}
```

---

### 8.3 `core/feign/CorrelationFeignInterceptor.java`

- Implementa `feign.RequestInterceptor`
- Anotado com `@Component` — Spring Cloud OpenFeign detecta automaticamente todos os `RequestInterceptor` beans e os aplica globalmente
- Lê `correlationId` e `flowId` do MDC; se presentes, injeta nos headers da requisição de saída

```java
@Component
public class CorrelationFeignInterceptor implements RequestInterceptor {

    private static final String X_CORRELATION_ID = "x-correlationId";
    private static final String X_FLOW_ID        = "x-flowId";

    @Override
    public void apply(RequestTemplate template) {
        addIfPresent(template, X_CORRELATION_ID, MDC.get("correlationId"));
        addIfPresent(template, X_FLOW_ID,        MDC.get("flowId"));
    }

    private void addIfPresent(RequestTemplate template, String header, String value) {
        if (StringUtils.hasText(value)) {
            template.header(header, value);
        }
    }
}
```

---

### 8.4 `core/mdc/MdcTaskDecorator.java`

- Implementa `org.springframework.core.task.TaskDecorator`
- Captura o snapshot do MDC no momento da decoração (no thread pai), restaura no thread filho antes da execução, e limpa no `finally`
- Não é anotado com `@Component`; é instanciado explicitamente no `AsyncConfig` via builder

```java
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (mdcSnapshot != null) {
                    MDC.setContextMap(mdcSnapshot);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```

---

### 8.5 `core/config/AsyncConfig.java`

- Anotado com `@Configuration` e `@EnableAsync`
- Implementa `AsyncConfigurer` para sobrescrever o executor padrão do `@Async`
- Usa `SimpleAsyncTaskExecutorBuilder` (auto-configurado pelo Spring Boot com virtual threads) e aplica o `MdcTaskDecorator`
- `SimpleAsyncTaskExecutorBuilder` já configura virtual threads automaticamente quando `spring.threads.virtual.enabled: true`

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final SimpleAsyncTaskExecutorBuilder executorBuilder;

    public AsyncConfig(SimpleAsyncTaskExecutorBuilder executorBuilder) {
        this.executorBuilder = executorBuilder;
    }

    @Override
    public Executor getAsyncExecutor() {
        return executorBuilder
            .taskDecorator(new MdcTaskDecorator())
            .build();
    }
}
```

---

### 8.6 `core/config/FeignConfig.java`

- Anotado com `@Configuration`
- Contém `@EnableFeignClients(basePackages = "com.staroscky.motordecisao")` para habilitar a varredura de interfaces `@FeignClient` em todo o projeto
- Define dois beans globais para Feign: `feign.Logger` (usando `FeignLogbookLogger`) e `feign.Logger.Level.FULL`
- `FeignLogbookLogger` recebe o bean `Logbook` auto-configurado pelo `logbook-spring-boot-starter`

```java
@Configuration
@EnableFeignClients(basePackages = "com.staroscky.motordecisao")
public class FeignConfig {

    @Bean
    public feign.Logger feignLogger(Logbook logbook) {
        return new FeignLogbookLogger(logbook);
    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}
```

---

### 8.7 `application.yaml` — alterações

```yaml
spring:
  application:
    name: motordecisao
  threads:
    virtual:
      enabled: true

logbook:
  filter:
    enabled: true
  format:
    style: json
  obfuscate:
    headers:
      - Authorization
      - X-Auth-Token
```

---

### 8.8 `pom.xml` — alterações

Adicionar dentro de `<dependencyManagement>`:
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>2025.0.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Adicionar dentro de `<dependencies>`:
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>logbook-spring-boot-starter</artifactId>
  <version>4.0.4</version>
</dependency>
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>logbook-openfeign</artifactId>
  <version>4.0.4</version>
</dependency>
```

---

### 8.9 Contratos, dados e interfaces

**Headers de correlação esperados na requisição de entrada:**

| Header | MDC key | Tipo | Obrigatório |
|---|---|---|---|
| `x-correlationId` | `correlationId` | string livre | não |
| `x-flowId` | `flowId` | string livre | não |

**Headers repassados na resposta de saída:**
- Mesmos headers recebidos na entrada (não há modificação de valor).

**Headers injetados pelo Feign em chamadas de saída:**
- Mesmos que os lidos do MDC (`correlationId` → `x-correlationId`, `flowId` → `x-flowId`).

---

## 9. Fluxos técnicos

```text
Requisição de entrada:

 Cliente externo
      │
      │  POST /decisao
      │  x-correlationId: abc-123
      │  x-flowId: flow-456
      ▼
 CorrelationHeaderFilter (Ordered.HIGHEST_PRECEDENCE)
   ├─ MDC.put("correlationId", "abc-123")
   ├─ MDC.put("flowId", "flow-456")
   └─ response.addHeader(x-correlationId, x-flowId)
      │
      ▼
 LogbookFilter (Logbook)
   └─ loga requisição com campos MDC visíveis no appender
      │
      ▼
 Handler / Service
      │
      ├─ caminho síncrono ─────────────────────────────────►
      │                                                    │
      └─ caminho @Async                                    │
           │                                              │
           ▼                                              │
      MdcTaskDecorator                                    │
        ├─ captura snapshot MDC                           │
        └─ nova virtual thread com MDC restaurado         │
                                                          │
      ◄─────────────────────────────────────────────────────
      │
      ▼
 LogbookFilter
   └─ loga resposta
      │
      ▼
 Resposta ao cliente (com x-correlationId e x-flowId)
```

```text
Chamada de saída via Feign:

 Handler
   └─ ExemploFeignClient.consultar(...)
         │
         ▼
   CorrelationFeignInterceptor (RequestInterceptor)
     ├─ MDC.get("correlationId") → injeta x-correlationId
     └─ MDC.get("flowId")        → injeta x-flowId
         │
         ▼
   FeignLogbookLogger (feign.Logger, Level.FULL)
     └─ loga request/response de saída via Logbook
         │
         ▼
   Serviço externo
```

---

## 10. Arquivos afetados

| Arquivo | Tipo | Mudança |
|---|---|---|
| `pom.xml` | Modificar | Adicionar `dependencyManagement` para Spring Cloud BOM + 3 dependências |
| `src/main/resources/application.yaml` | Modificar | Adicionar `spring.threads.virtual.enabled` e config do Logbook |
| `src/main/java/.../core/filter/CorrelationHeaderFilter.java` | Criar | `OncePerRequestFilter` com leitura de headers e gravação no MDC |
| `src/main/java/.../core/feign/CorrelationFeignInterceptor.java` | Criar | `RequestInterceptor` que propaga MDC para headers Feign |
| `src/main/java/.../core/mdc/MdcTaskDecorator.java` | Criar | `TaskDecorator` que propaga MDC para virtual threads filhas |
| `src/main/java/.../core/config/AsyncConfig.java` | Criar | `@EnableAsync` + `AsyncConfigurer` com `SimpleAsyncTaskExecutorBuilder` |
| `src/main/java/.../core/config/FeignConfig.java` | Criar | `@EnableFeignClients` + beans `FeignLogbookLogger` e `Logger.Level.FULL` |

---

## 11. Requisitos não funcionais

| Categoria | Requisito não funcional | Meta ou critério |
|---|---|---|
| Performance | Virtual threads não devem bloquear threads de plataforma em I/O | Validado por design: virtual threads são gerenciadas pela JVM sem blocking de thread de plataforma |
| Observabilidade | Todo log de requisição deve incluir `correlationId` e `flowId` quando presentes | Validar no log de saída que os campos MDC aparecem em requests com esses headers |
| Observabilidade | Chamadas Feign de saída devem ser logadas com body e headers | Logbook com `Logger.Level.FULL` |
| Segurança | Headers `Authorization` e `X-Auth-Token` não devem aparecer nos logs do Logbook | Configurado via `logbook.obfuscate.headers` |
| Confiabilidade | MDC não deve vazar entre requisições (ThreadLocal não reciclado) | `OncePerRequestFilter` limpa MDC no `finally`; `MdcTaskDecorator` limpa no `finally` |
| Compatibilidade | Componentes do `core` não devem introduzir dependência em nenhum slice de negócio | Verificado por estrutura de pacotes — `core` não importa pacotes fora de si |

---

## 12. Estratégia de rollout ou migração

Não aplicável — o projeto está em estado inicial sem código de produção ou usuários ativos. Os componentes serão adicionados diretamente na branch principal.

---

## 13. Estratégia de validação

- **Testes de integração (`@SpringBootTest`):**
  - Requisição com `x-correlationId` → verificar que o MDC contém o valor durante a execução do handler (via `MDC.get(...)` em um bean de teste)
  - Verificar que a resposta contém os headers de correlação
- **Testes unitários:**
  - `CorrelationHeaderFilter`: mock de `HttpServletRequest`/`HttpServletResponse`, verificar MDC populado e limpeza no `finally`
  - `CorrelationFeignInterceptor`: mock de `RequestTemplate` com MDC pré-populado, verificar headers injetados
  - `MdcTaskDecorator`: criar thread filha, verificar que snapshot MDC está disponível dentro da execução decorada
- **Verificação manual / sinais operacionais:**
  - `curl -H "x-correlationId: test-123" -H "x-flowId: flow-001" http://localhost:8080/actuator/health` → verificar log JSON com campos `correlationId` e `flowId`
  - Confirmar que `spring.threads.virtual.enabled` está ativo no log de startup (procurar por `TomcatVirtualThreadsWebServerFactoryCustomizer`)

---

## 14. Critérios de aceite

- [ ] `mvn verify` passa sem erros após as alterações no `pom.xml`
- [ ] Aplicação sobe sem erros com `spring.threads.virtual.enabled: true`
- [ ] Requisição com `x-correlationId: X` e `x-flowId: Y` gera log com `"correlationId":"X"` e `"flowId":"Y"` no MDC
- [ ] Resposta HTTP contém `x-correlationId` e `x-flowId` iguais aos recebidos
- [ ] Requisição sem headers de correlação não gera erro — campos simplesmente ausentes dos logs
- [ ] Chamada Feign de saída carrega `x-correlationId` e `x-flowId` nos headers quando MDC está populado
- [ ] Logs de chamadas Feign de saída aparecem via Logbook
- [ ] Thread assíncrona (`@Async`) preserva os campos de correlação no MDC
- [ ] Headers `Authorization` não aparecem nos logs do Logbook
- [ ] Nenhum slice de negócio foi criado ou alterado
- [ ] Build e testes relevantes passam sem regressão

---

## 15. Riscos e observações

- **`FeignLogbookLogger` é marcado como `@API(status = EXPERIMENTAL)`** no código do Logbook — acompanhar changelog em upgrades de versão.
- **Logbook loga bodies por padrão** — para endpoints com payload grande ou sensível, configurar `logbook.write.max-body-size` ou estratégias de obfuscação específicas conforme os slices evoluírem.
- **`MDC.clear()` no `finally` do `MdcTaskDecorator`** remove todos os campos, incluindo eventuais campos adicionados por frameworks de terceiros dentro da mesma thread. Se no futuro algum framework depender de MDC persistente entre tasks, rever para `MDC.remove()` seletivo.
- **Spring Cloud 2025.0.2 ainda não tem GA longo** — validar no upgrade do Spring Boot se a versão do BOM evoluiu.

---

## 16. Questões em aberto

- Geração automática de `x-correlationId` quando ausente na requisição de entrada (UUID): definir como feature separada quando o primeiro serviço consumer for integrado.
- Formato do log Logbook (JSON puro vs. Logstash ECS): avaliar quando a stack de observabilidade for definida. A configuração atual usa `style: json` como ponto de partida.
