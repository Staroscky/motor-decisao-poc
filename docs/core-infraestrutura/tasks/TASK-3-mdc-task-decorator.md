# TASK-3 — Criar MdcTaskDecorator

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/core/mdc/MdcTaskDecorator.java` (novo)
**Referência SPEC:** Seções 5 (RF-07), 8.4
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

O MDC do Logback é baseado em `ThreadLocal` e não é herdado automaticamente por virtual threads filhas. Esta classe captura um snapshot do MDC no thread pai e o restaura no thread filho antes da execução, garantindo que campos como `correlationId` e `flowId` estejam disponíveis em métodos `@Async`.

Não deve ser anotada com `@Component` — é instanciada explicitamente no `AsyncConfig` (TASK-6).

## O que fazer

Criar a classe no pacote `com.staroscky.motordecisao.core.mdc`:

```java
package com.staroscky.motordecisao.core.mdc;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

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

## Notas de implementação

- O snapshot é capturado no momento da decoração (thread pai), não dentro do `Runnable` (thread filho).
- `MDC.clear()` no `finally` remove todos os campos do MDC da thread filha após a execução — necessário para evitar vazamento entre tasks que reusem threads.
- Se no futuro outros frameworks gravarem campos no MDC que precisem persistir entre tasks, trocar `MDC.clear()` por remoção seletiva das chaves conhecidas.
- `MDC.getCopyOfContextMap()` retorna `null` quando o MDC está vazio — o `if (mdcSnapshot != null)` é obrigatório.

## Critério de aceite

- [ ] Classe compila sem erros
- [ ] Teste unitário: criar thread com MDC populado → decorar Runnable → dentro do Runnable, `MDC.get("correlationId")` retorna o valor esperado
- [ ] Teste unitário: após execução do Runnable decorado, `MDC.get("correlationId")` retorna `null` (MDC limpo)
- [ ] Teste unitário: Runnable decorado com MDC vazio (`null`) não lança exceção
- [ ] Build e testes relevantes passam sem erros
