# TASK-3 — Modelo interno do contexto

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/contexto/` (novo)
**Referência SPEC:** Seção 8.4
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

`AvaliacaoContexto` é o estado compartilhado entre os dois pipelines. Ele acumula os resultados por instrumento à medida que os validadores executam. Os demais componentes do pacote (`ResultadoInstrumento`, `ResultadoViabilidade`, `Restricao`, `Limite`) compõem esse modelo interno — são distintos dos tipos de resposta HTTP.

## O que fazer

Criar os seguintes arquivos em `com.staroscky.motordecisao.avaliacao.contexto`:

**`Limite.java`**
```java
public record Limite(int qtdDiasAlt) {}
```

**`Restricao.java`** (sealed interface com DEDUCTION — usada também na resposta HTTP)
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(RestricaoGenerica.class),
    @JsonSubTypes.Type(RestricaoQrcode.class)
})
public sealed interface Restricao permits RestricaoGenerica, RestricaoQrcode {}
```

**`RestricaoGenerica.java`**
```java
public record RestricaoGenerica(String motivo) implements Restricao {}
```

**`RestricaoQrcode.java`**
```java
public record RestricaoQrcode(String motivo, ContextoQrcode contexto) implements Restricao {
    public record ContextoQrcode(String tipo) {}
}
```

**`ResultadoViabilidade.java`**
```java
public class ResultadoViabilidade {
    private boolean valido = true;
    private int ordemMelhor;
    private Limite limite;
    private final List<Restricao> restricoes = new ArrayList<>();

    public void adicionarRestricao(Restricao restricao) {
        this.restricoes.add(restricao);
        this.valido = false;
    }

    // getters e setters para valido, ordemMelhor, limite, restricoes
}
```

**`ResultadoInstrumento.java`**
```java
public class ResultadoInstrumento {
    private final ResultadoViabilidade dataSelecionada = new ResultadoViabilidade();
    private final ResultadoViabilidade agendamento     = new ResultadoViabilidade();
    // getters
}
```

**`AvaliacaoContexto.java`**
```java
public class AvaliacaoContexto {
    private final AvaliacaoRequest request;
    private final TipoCheckin tipoCheckin;
    private final String ispb;
    private final Set<Instrumento> instrumentos;
    private final Map<Instrumento, ResultadoInstrumento> resultados =
        new EnumMap<>(Instrumento.class);

    public AvaliacaoContexto(AvaliacaoRequest request, TipoCheckin tipoCheckin,
                              String ispb, Set<Instrumento> instrumentos) { ... }

    public ResultadoInstrumento getResultado(Instrumento instrumento) {
        return resultados.computeIfAbsent(instrumento, k -> new ResultadoInstrumento());
    }

    public boolean isOnline() {
        return !request.data().isAfter(LocalDate.now());
    }

    public Set<Instrumento> getInstrumentos() { return instrumentos; }
    // demais getters
}
```

## Notas de implementação

- `EnumMap` em `AvaliacaoContexto.resultados` garante iteração na ordem de declaração do enum `Instrumento` (PIX, TED, TEF) — isso resolve a ordem dos instrumentos na resposta
- `ResultadoViabilidade.valido` começa como `true`; `adicionarRestricao()` é o único ponto que seta `false` — nunca setar `valido = false` diretamente fora desse método
- `Restricao` vive em `contexto/` mas é compartilhada com `response/` (ver TASK-5) — é uma classe de domínio imutável, não um objeto de persistência
- `AvaliacaoRequest` é do pacote `request/` (TASK-4) — a compilação da TASK-3 depende que TASK-4 compile junto ou que `AvaliacaoRequest` seja criado antes; ao criar os arquivos, criar `AvaliacaoRequest` primeiro ou deixar o import pendente até TASK-4

## Critério de aceite

- [ ] `adicionarRestricao()` em `ResultadoViabilidade` seta `valido = false`
- [ ] `AvaliacaoContexto.isOnline()` retorna `true` para a data atual e `false` para datas futuras
- [ ] `AvaliacaoContexto.getResultado()` cria `ResultadoInstrumento` lazily via `computeIfAbsent`
- [ ] `EnumMap` usado em `resultados` — ordem de iteração é determinística
- [ ] Build sem erros: `mvn verify`
