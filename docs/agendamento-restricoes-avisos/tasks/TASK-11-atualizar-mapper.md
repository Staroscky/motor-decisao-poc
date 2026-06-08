# TASK-11 — Atualizar AvaliacaoResponseMapper

**Arquivo alvo:** `avaliacao/mapper/AvaliacaoResponseMapper.java` (modificar)
**Referência SPEC:** Seção 8.11
**Depende de:** TASK-3, TASK-4, TASK-5
**Bloqueada por:** nenhuma

---

## Contexto

`AvaliacaoResponseMapper` hoje usa um único método `toDto(ResultadoViabilidade)` tanto para `dataSelecionada` quanto para `agendamento`. Com `ResultadoInstrumento.getAgendamento()` retornando `ResultadoAgendamento`, o mapper precisa de um método dedicado para mapear esse tipo para `AvaliacaoAgendamento`.

## O que fazer

Atualizar `avaliacao/mapper/AvaliacaoResponseMapper.java`:

1. Adicionar método privado `toAvaliacaoAgendamento(ResultadoAgendamento)`:

```java
private AvaliacaoAgendamento toAvaliacaoAgendamento(ResultadoAgendamento ag) {
    return new AvaliacaoAgendamento(
        ag.isPodeAgendar(),
        List.copyOf(ag.getRestricoes()),
        ag.getProximaDataDisponivel(),
        List.copyOf(ag.getAvisos())
    );
}
```

2. Atualizar `toInstrumentoAvaliado()` para usar o novo método no campo `agendamento`:

```java
private InstrumentoAvaliado toInstrumentoAvaliado(Instrumento instrumento,
                                                   AvaliacaoContexto contexto) {
    ResultadoInstrumento resultado = contexto.getResultado(instrumento);
    ResultadoViabilidadeDto dataSelecionada = toDto(resultado.getDataSelecionada());
    AvaliacaoAgendamento agendamento        = toAvaliacaoAgendamento(resultado.getAgendamento());

    return switch (instrumento) {
        case PIX -> new InstrumentoPix("PIX", dataSelecionada, agendamento);
        case TEF -> new InstrumentoTef("TEF", dataSelecionada, agendamento);
        case TED -> new InstrumentoTed("TED", dataSelecionada, agendamento, finalidadesTed());
    };
}
```

3. Adicionar os imports necessários: `AvaliacaoAgendamento`, `ResultadoAgendamento`.

## Notas de implementação

- `List.copyOf(ag.getAvisos())` retorna uma lista imutável vazia quando `avisos` é vazio — não é `null`. A anotação `@JsonInclude(NON_EMPTY)` em `AvaliacaoAgendamento` trata corretamente esse caso no Jackson.
- `ag.getProximaDataDisponivel()` pode ser `null` — `@JsonInclude(NON_NULL)` garante que não aparece no JSON quando ausente.
- O método `toDto(ResultadoViabilidade)` permanece inalterado — continua sendo usado para `dataSelecionada`.

## Critério de aceite

- [ ] `toAvaliacaoAgendamento()` existe e mapeia todos os campos de `ResultadoAgendamento`
- [ ] O switch no `toInstrumentoAvaliado()` usa `AvaliacaoAgendamento` para o campo `agendamento`
- [ ] Build compila sem erros
