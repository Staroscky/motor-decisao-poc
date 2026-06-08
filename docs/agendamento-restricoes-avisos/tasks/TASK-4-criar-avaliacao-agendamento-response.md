# TASK-4 — Criar AvaliacaoAgendamento e atualizar records de response

**Arquivo alvo:** `avaliacao/response/AvaliacaoAgendamento.java` (criar) + `InstrumentoPix/Ted/Tef.java` (modificar)
**Referência SPEC:** Seções 8.4, RF-07, RF-08, RF-09
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

O response do bloco `agendamento` hoje usa `ResultadoViabilidadeDto` — o mesmo DTO de `dataSelecionada`. `AvaliacaoAgendamento` é o record específico para o bloco de agendamento, com serialização condicional de `proximaDataDisponivel` e `avisos`. Os três records de instrumento precisam trocar o tipo do campo `agendamento`.

## O que fazer

1. Criar `avaliacao/response/AvaliacaoAgendamento.java`:

```java
package com.staroscky.motordecisao.avaliacao.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.Restricao;

import java.time.LocalDate;
import java.util.List;

public record AvaliacaoAgendamento(
    boolean podeAgendar,
    List<Restricao> restricoes,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    LocalDate proximaDataDisponivel,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<Aviso> avisos
) {}
```

2. Atualizar `avaliacao/response/InstrumentoPix.java`:
```java
public record InstrumentoPix(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    AvaliacaoAgendamento agendamento
) implements InstrumentoAvaliado {}
```

3. Aplicar a mesma troca em `InstrumentoTed.java` e `InstrumentoTef.java`.

## Notas de implementação

- `restricoes` **não** recebe `@JsonInclude` — deve ser serializado mesmo quando `[]`. Isso é deliberado: o frontend itera sem null-check.
- `proximaDataDisponivel` usa `NON_NULL` — ausente quando `null`.
- `avisos` usa `NON_EMPTY` — ausente quando lista vazia. `List.copyOf(Collections.emptyList())` retorna lista vazia, não nula — `NON_EMPTY` cobre esse caso corretamente no Jackson.
- `InstrumentoTed` tem o campo `finalidades` adicional — apenas o campo `agendamento` muda de tipo; não alterar o resto do record.

## Critério de aceite

- [ ] `AvaliacaoAgendamento` compilado com as anotações `@JsonInclude` corretas
- [ ] `InstrumentoPix`, `InstrumentoTed` e `InstrumentoTef` usam `AvaliacaoAgendamento` no campo `agendamento`
- [ ] Build compila sem erros (o mapper ainda compila com erro neste ponto — será corrigido na TASK-11)
