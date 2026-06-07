# TASK-8 — Verificação E2E do core

**Arquivo alvo:** `src/test/java/com/staroscky/motordecisao/core/` (testes novos)
**Referência SPEC:** Seções 13, 14
**Depende de:** TASK-1, TASK-2, TASK-3, TASK-4, TASK-5, TASK-6, TASK-7
**Bloqueada por:** todas as tasks anteriores

---

## Contexto

Verificação integrada de todos os componentes do `core` operando em conjunto. O objetivo é confirmar que o pipeline completo (filtro → MDC → Logbook → Feign → async) funciona conforme especificado antes de liberar a base para desenvolvimento dos slices de negócio.

## O que fazer

### 1. Teste de integração: propagação de headers de correlação

Criar `src/test/java/com/staroscky/motordecisao/core/CorrelationHeaderFilterIT.java`:

- Usar `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`
- Criar um endpoint mínimo de teste (ex: `@RestController` apenas no escopo de teste via `@TestConfiguration`) ou usar `/actuator/health` se o Actuator estiver disponível
- Enviar requisição com `x-correlationId: test-correlation` e `x-flowId: test-flow`
- Verificar que a resposta contém os mesmos headers
- Verificar que o MDC continha os valores durante o processamento (via bean de teste que captura MDC)

### 2. Teste de integração: limpeza do MDC

- Enviar duas requisições em sequência: primeira com `x-correlationId: req1`, segunda sem o header
- Verificar que na segunda requisição o MDC não contém `correlationId` (sem vazamento da primeira)

### 3. Teste unitário: `MdcTaskDecorator` com MDC vazio

Criar `src/test/java/com/staroscky/motordecisao/core/mdc/MdcTaskDecoratorTest.java`:

- Cenário 1: MDC populado → Runnable decorado enxerga os campos → após execução, MDC está limpo
- Cenário 2: MDC vazio (null) → Runnable decorado executa sem exceção

### 4. Teste unitário: `CorrelationFeignInterceptor`

Criar `src/test/java/com/staroscky/motordecisao/core/feign/CorrelationFeignInterceptorTest.java`:

- Cenário 1: MDC com ambos os campos → `RequestTemplate` recebe os dois headers
- Cenário 2: MDC vazio → nenhum header adicionado

### 5. Verificação manual de startup

Subir a aplicação localmente e verificar:
- [ ] Log de startup contém `TomcatVirtualThreadsWebServerFactoryCustomizer` (virtual threads no Tomcat)
- [ ] `curl -H "x-correlationId: abc-123" -H "x-flowId: flow-001" http://localhost:8080/actuator/health` → log JSON contém `"correlationId":"abc-123"` e `"flowId":"flow-001"` se o Logbook estiver logando no nível correto
- [ ] Resposta do curl contém os headers `x-correlationId` e `x-flowId`

## Notas de implementação

- Para verificar MDC dentro de um handler em teste de integração, uma abordagem é criar um `@Bean` de escopo de teste que registra o MDC atual durante a requisição (ex: via `HandlerInterceptor` de teste).
- Se o `actuator` não estiver no classpath, criar um `@RestController` mínimo apenas no contexto de teste com `@TestConfiguration`.
- A verificação do Logbook nos logs pode ser feita capturando o `Appender` do Logback em teste ou verificando a saída de `MockMvc`.

## Critério de aceite

- [ ] `mvn verify` passa com todos os testes novos
- [ ] Requisição com headers de correlação → resposta contém os mesmos headers
- [ ] MDC não vaza entre requisições sequenciais
- [ ] `MdcTaskDecorator` propaga MDC para thread filha e limpa após execução
- [ ] `CorrelationFeignInterceptor` injeta headers corretamente a partir do MDC
- [ ] Aplicação sobe sem erros com todas as configurações do `core` ativas
- [ ] Log de startup confirma virtual threads ativas
