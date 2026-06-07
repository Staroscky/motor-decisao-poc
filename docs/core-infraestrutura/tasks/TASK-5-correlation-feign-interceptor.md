# TASK-5 — Criar CorrelationFeignInterceptor

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/core/feign/CorrelationFeignInterceptor.java` (novo)
**Referência SPEC:** Seções 5 (RF-04), 6.2, 8.3
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

Sem este interceptor, chamadas de saída via Feign não carregam `x-correlationId` e `x-flowId`, quebrando a rastreabilidade fim-a-fim entre serviços. O interceptor lê os valores do MDC (populado pelo `CorrelationHeaderFilter`) e os injeta nos headers de toda chamada Feign de forma automática e transparente para os slices.

## O que fazer

Criar a classe no pacote `com.staroscky.motordecisao.core.feign`:

```java
package com.staroscky.motordecisao.core.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

## Notas de implementação

- Declarar como `@Component` é suficiente — Spring Cloud OpenFeign detecta automaticamente todos os beans `RequestInterceptor` e os aplica a todos os `@FeignClient` do contexto.
- O interceptor só funciona se o MDC estiver populado no momento da chamada. Em chamadas fora do contexto de uma requisição HTTP (ex: jobs `@Scheduled`), os campos estarão ausentes — comportamento esperado.
- `MDC.get(...)` retorna `null` se a chave não existir. `StringUtils.hasText(null)` retorna `false`, então nenhum header vazio é injetado.
- Este componente não depende do `CorrelationHeaderFilter` diretamente — apenas do MDC estar populado, o que pode ocorrer por qualquer caminho.

## Critério de aceite

- [ ] Classe compila sem erros
- [ ] Teste unitário: MDC com `correlationId=abc` e `flowId=xyz` → `RequestTemplate` recebe headers `x-correlationId: abc` e `x-flowId: xyz`
- [ ] Teste unitário: MDC vazio → nenhum header de correlação é adicionado ao `RequestTemplate`
- [ ] Teste unitário: MDC com apenas `correlationId` (sem `flowId`) → apenas `x-correlationId` é injetado
- [ ] Build e testes relevantes passam sem erros
