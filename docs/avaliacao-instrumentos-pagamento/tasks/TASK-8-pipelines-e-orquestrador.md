# TASK-8 — Pipelines e Orquestrador

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/pipeline/` e `avaliacao/` (novos)
**Referência SPEC:** Seções 8.6, 8.1
**Depende de:** TASK-6, TASK-7
**Bloqueada por:** nenhuma

---

## Contexto

Os pipelines encapsulam a lógica de iteração por instrumento e delegação aos validadores. O orquestrador garante a sequência correta: `PipelineDataSelecionada` sempre antes de `PipelineAgendamento`. Nenhum dos dois pipelines conhece o outro.

## O que fazer

**`pipeline/PipelineDataSelecionada.java`**
```java
@Component
public class PipelineDataSelecionada {

    private final List<Validador> validadores;

    public PipelineDataSelecionada(
        StatusInstituicaoValidador statusInstituicao,
        LimiteInstituicaoValidador limiteInstituicao,
        HorarioPermitidoValidador horario,
        LimiteDiarioValidador limiteDiario,
        DataValidaValidador dataValida
    ) {
        this.validadores = List.of(
            statusInstituicao, limiteInstituicao, horario, limiteDiario, dataValida
        );
    }

    public void executar(AvaliacaoContexto contexto) {
        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado =
                contexto.getResultado(instrumento).getDataSelecionada();
            validadores.stream()
                .filter(v -> v.suporta(contexto, instrumento))
                .forEach(v -> v.validar(contexto, instrumento, resultado));
        }
    }
}
```

**`pipeline/PipelineAgendamento.java`**
```java
@Component
public class PipelineAgendamento {

    private final List<Validador> validadores;

    public PipelineAgendamento(
        PermiteAgendamentoValidador permiteAgendamento,
        ProximaDataValidador proximaData
    ) {
        this.validadores = List.of(permiteAgendamento, proximaData);
    }

    public void executar(AvaliacaoContexto contexto) {
        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado =
                contexto.getResultado(instrumento).getAgendamento();
            validadores.stream()
                .filter(v -> v.suporta(contexto, instrumento))
                .forEach(v -> v.validar(contexto, instrumento, resultado));
        }
    }
}
```

**`AvaliacaoOrquestrador.java`** (pacote raiz `avaliacao/`)
```java
@Component
public class AvaliacaoOrquestrador {

    private final PipelineDataSelecionada pipelineDataSelecionada;
    private final PipelineAgendamento pipelineAgendamento;

    public AvaliacaoOrquestrador(PipelineDataSelecionada pipelineDataSelecionada,
                                  PipelineAgendamento pipelineAgendamento) {
        this.pipelineDataSelecionada = pipelineDataSelecionada;
        this.pipelineAgendamento = pipelineAgendamento;
    }

    public void executar(AvaliacaoContexto contexto) {
        pipelineDataSelecionada.executar(contexto);
        pipelineAgendamento.executar(contexto);
    }
}
```

## Notas de implementação

- Os validadores são injetados por construtor explícito nos pipelines — **não usar `List<Validador>` autowired**, pois o Spring coletaria todos os beans `Validador` sem garantia de ordem
- A ordem dos validadores no `List.of(...)` determina a ordem de execução — mudar a ordem exige alterar o construtor do pipeline
- `for (Instrumento instrumento : contexto.getInstrumentos())` itera pelo `EnumMap` — ordem determinística por `Instrumento.ordinal()`

## Critério de aceite

- [ ] `PipelineDataSelecionada` injeta os 5 validadores na ordem: status, limite-inst, horário, limite-diário, dataValida
- [ ] `PipelineAgendamento` injeta os 2 validadores na ordem: permiteAgendamento, proximaData
- [ ] `AvaliacaoOrquestrador.executar()` chama `pipelineDataSelecionada` antes de `pipelineAgendamento`
- [ ] Build sem erros: `mvn verify`
