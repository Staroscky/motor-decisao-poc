# TASK-5 — Response polimórfico

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/response/` (novo)
**Referência SPEC:** Seção 8.7
**Depende de:** TASK-3 (usa `Restricao` e `Limite` do contexto)
**Bloqueada por:** nenhuma

---

## Contexto

Define o contrato de saída do endpoint. A resposta é polimórfica por instrumento: `InstrumentoTed` tem o campo extra `finalidades`; `InstrumentoPix` e `InstrumentoTef` têm os mesmos campos. O mapper (TASK-10) preenche esses tipos a partir do `AvaliacaoContexto`.

## O que fazer

Criar os seguintes arquivos em `com.staroscky.motordecisao.avaliacao.response`:

**`ResultadoViabilidadeDto.java`** (DTO imutável — distinto da classe mutável `ResultadoViabilidade` do contexto)
```java
public record ResultadoViabilidadeDto(
    boolean valido,
    List<Restricao> restricoes,
    int ordemMelhor,
    Limite limite
) {}
```

**`InstrumentoAvaliado.java`**
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(InstrumentoPix.class),
    @JsonSubTypes.Type(InstrumentoTef.class),
    @JsonSubTypes.Type(InstrumentoTed.class)
})
public sealed interface InstrumentoAvaliado
    permits InstrumentoPix, InstrumentoTef, InstrumentoTed {}
```

**`InstrumentoPix.java`**
```java
public record InstrumentoPix(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento
) implements InstrumentoAvaliado {}
```

**`InstrumentoTef.java`**
```java
public record InstrumentoTef(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento
) implements InstrumentoAvaliado {}
```

**`InstrumentoTed.java`**
```java
public record InstrumentoTed(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento,
    List<FinalidadeTed> finalidades
) implements InstrumentoAvaliado {
    public record FinalidadeTed(String id, String descricao) {}
}
```

**`AvaliacaoResponse.java`**
```java
public record AvaliacaoResponse(List<InstrumentoAvaliado> instrumentos) {}
```

## Notas de implementação

- `Restricao` e `Limite` importados de `com.staroscky.motordecisao.avaliacao.contexto` — eles são objetos de domínio imutáveis compartilhados entre model interno e DTO de resposta
- DEDUCTION para `InstrumentoAvaliado` não distingue `InstrumentoPix` de `InstrumentoTef` na desserialização (campos idênticos) — mas o servidor nunca deserializa a própria resposta, então não há problema de runtime
- Os valores de `tipo` em runtime serão `"PIX"`, `"TED"`, `"TEF"` — preenchidos pelo mapper via `Instrumento.name()`

## Critério de aceite

- [ ] `InstrumentoTed` serializa com campo `finalidades` no JSON
- [ ] `InstrumentoPix` e `InstrumentoTef` serializam sem `finalidades`
- [ ] `ResultadoViabilidadeDto` serializa com `valido`, `restricoes`, `ordemMelhor`, `limite`
- [ ] `RestricaoQrcode` serializa com campo `contexto` incluindo `tipo`
- [ ] Build sem erros: `mvn verify`
