# TASK-10 — AvaliacaoResponseMapper

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/mapper/AvaliacaoResponseMapper.java` (novo)
**Referência SPEC:** Seções 8.1, 10
**Depende de:** TASK-3 (modelo interno), TASK-5 (response DTOs)
**Bloqueada por:** nenhuma

---

## Contexto

O mapper converte o `AvaliacaoContexto` (com todos os `ResultadoViabilidade` acumulados pelos pipelines) em `AvaliacaoResponse`. É o único ponto que conhece ambos os modelos — interno e de resposta. A ordem dos instrumentos na lista deve ser determinística.

## O que fazer

**`mapper/AvaliacaoResponseMapper.java`**
```java
@Component
public class AvaliacaoResponseMapper {

    public AvaliacaoResponse toResponse(AvaliacaoContexto contexto) {
        List<InstrumentoAvaliado> instrumentos = contexto.getInstrumentos().stream()
            .sorted(Comparator.comparingInt(Instrumento::ordinal))
            .map(instrumento -> toInstrumentoAvaliado(instrumento, contexto))
            .toList();
        return new AvaliacaoResponse(instrumentos);
    }

    private InstrumentoAvaliado toInstrumentoAvaliado(Instrumento instrumento,
                                                       AvaliacaoContexto contexto) {
        ResultadoInstrumento resultado = contexto.getResultado(instrumento);
        ResultadoViabilidadeDto dataSelecionada = toDto(resultado.getDataSelecionada());
        ResultadoViabilidadeDto agendamento     = toDto(resultado.getAgendamento());

        return switch (instrumento) {
            case PIX -> new InstrumentoPix("PIX", dataSelecionada, agendamento);
            case TEF -> new InstrumentoTef("TEF", dataSelecionada, agendamento);
            case TED -> new InstrumentoTed("TED", dataSelecionada, agendamento,
                                           finalidadesTed());
        };
    }

    private ResultadoViabilidadeDto toDto(ResultadoViabilidade rv) {
        return new ResultadoViabilidadeDto(
            rv.isValido(),
            rv.getRestricoes(),
            rv.getOrdemMelhor(),
            rv.getLimite()
        );
    }

    private List<InstrumentoTed.FinalidadeTed> finalidadesTed() {
        // TODO: buscar finalidades de configuração ou upstream
        return List.of(
            new InstrumentoTed.FinalidadeTed("01", "Crédito em Conta"),
            new InstrumentoTed.FinalidadeTed("03", "DOC/TED")
        );
    }
}
```

## Notas de implementação

- A ordenação por `Instrumento::ordinal` garante que `PIX` sempre aparece antes de `TED` e `TEF` na resposta, independente da ordem do `Set<Instrumento>` retornado por `InstrumentoResolver`
- `rv.getRestricoes()` retorna a lista interna de `ResultadoViabilidade` — se a lista for mutável, considerar `List.copyOf()` no DTO para evitar mutação acidental do contexto após o mapper rodar
- `finalidadesTed()` retorna placeholder hardcoded — a origem real das finalidades (configuração, upstream, enum) deve ser definida em sprint posterior

## Critério de aceite

- [ ] `toResponse()` retorna `AvaliacaoResponse` com instrumentos na ordem: PIX antes de TED/TEF
- [ ] `InstrumentoTed` na resposta inclui as finalidades
- [ ] `ResultadoViabilidadeDto` reflete corretamente `valido`, `restricoes`, `ordemMelhor`, `limite` do modelo interno
- [ ] Build sem erros: `mvn verify`
