# TASK-6 — Criar AsyncConfig

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/core/config/AsyncConfig.java` (novo)
**Referência SPEC:** Seções 5 (RF-01, RF-07), 8.5
**Depende de:** TASK-1, TASK-3
**Bloqueada por:** TASK-3

---

## Contexto

Com virtual threads habilitadas, o `@Async` do Spring usa `SimpleAsyncTaskExecutor` configurado automaticamente via `SimpleAsyncTaskExecutorBuilder`. Esta configuração sobrescreve o executor padrão para aplicar o `MdcTaskDecorator`, garantindo que o MDC seja propagado para as threads virtuais filhas criadas por métodos `@Async`.

## O que fazer

Criar a classe no pacote `com.staroscky.motordecisao.core.config`:

```java
package com.staroscky.motordecisao.core.config;

import com.staroscky.motordecisao.core.mdc.MdcTaskDecorator;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

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

## Notas de implementação

- `SimpleAsyncTaskExecutorBuilder` é um bean auto-configurado pelo Spring Boot. Quando `spring.threads.virtual.enabled: true`, o builder já configura virtual threads automaticamente — não é preciso chamar `.virtualThreads(true)` manualmente.
- `AsyncConfigurer.getAsyncExecutor()` sobrescreve o executor padrão para todos os métodos `@Async` da aplicação.
- O `MdcTaskDecorator` é instanciado diretamente (não injetado como bean) — é uma escolha intencional para manter o decorator sem estado de Spring e evitar dependência circular.
- Se no futuro forem necessários executores diferenciados por domínio (ex: pool separado para I/O intensivo), criar beans `Executor` nomeados e referenciar via `@Async("nomeDoExecutor")`.

## Critério de aceite

- [ ] Classe compila sem erros
- [ ] Aplicação sobe sem erros com `@EnableAsync` ativo
- [ ] Teste de integração (`@SpringBootTest`): método `@Async` com MDC populado → campos de correlação disponíveis dentro da execução assíncrona
- [ ] Build e testes relevantes passam sem erros
