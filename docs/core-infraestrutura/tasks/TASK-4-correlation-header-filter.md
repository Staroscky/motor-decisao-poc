# TASK-4 — Criar CorrelationHeaderFilter

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/core/filter/CorrelationHeaderFilter.java` (novo)
**Referência SPEC:** Seções 5 (RF-02, RF-03), 6.2, 8.2
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

Sem este filtro, `x-correlationId` e `x-flowId` não chegam ao MDC e não aparecem nos logs. Este filtro centraliza a captura desses headers em um único ponto do pipeline, antes do Logbook registrar a requisição, garantindo que os campos de correlação estejam disponíveis nos logs de entrada.

## O que fazer

Criar a classe no pacote `com.staroscky.motordecisao.core.filter`:

```java
package com.staroscky.motordecisao.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationHeaderFilter extends OncePerRequestFilter {

    private static final String X_CORRELATION_ID = "x-correlationId";
    private static final String X_FLOW_ID        = "x-flowId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            setIfPresent(request, response, X_CORRELATION_ID, "correlationId");
            setIfPresent(request, response, X_FLOW_ID,        "flowId");
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("flowId");
        }
    }

    private void setIfPresent(HttpServletRequest request, HttpServletResponse response,
                               String header, String mdcKey) {
        String value = request.getHeader(header);
        if (StringUtils.hasText(value)) {
            MDC.put(mdcKey, value);
            response.addHeader(header, value);
        }
    }
}
```

## Notas de implementação

- `@Order(Ordered.HIGHEST_PRECEDENCE)` garante que este filtro execute antes do `LogbookFilter`, que tem prioridade menor. Isso é fundamental para que os campos MDC estejam populados quando o Logbook registrar a requisição.
- `MDC.remove(...)` seletivo no `finally` é preferível a `MDC.clear()` aqui, pois outros filtros ou frameworks podem ter gravado campos no MDC antes deste ponto.
- `StringUtils.hasText(value)` rejeita `null`, string vazia e strings com apenas espaço — não grava MDC com valor inválido.
- Se `x-correlationId` estiver ausente na requisição, o campo simplesmente não é adicionado ao MDC e nem à resposta. Nenhuma exceção é lançada.

## Critério de aceite

- [ ] Classe compila sem erros
- [ ] Teste unitário: requisição com `x-correlationId: abc` e `x-flowId: xyz` → MDC contém `correlationId=abc` e `flowId=xyz` durante `doFilter`
- [ ] Teste unitário: resposta contém os headers `x-correlationId` e `x-flowId` após o filtro
- [ ] Teste unitário: após o filtro, MDC não contém `correlationId` nem `flowId` (limpeza no `finally`)
- [ ] Teste unitário: requisição sem headers de correlação → nenhum campo gravado no MDC, nenhuma exceção
- [ ] Teste unitário: requisição com header vazio (`x-correlationId: ""`) → campo não gravado no MDC
- [ ] Build e testes relevantes passam sem erros
