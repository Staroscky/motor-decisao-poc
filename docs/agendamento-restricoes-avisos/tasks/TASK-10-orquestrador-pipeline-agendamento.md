# TASK-10 — Evoluir PipelineAgendamento para orquestrador por instrumento

**Arquivo alvo:** `avaliacao/pipeline/agendamento/PipelineAgendamento.java` (criar) + `avaliacao/pipeline/PipelineAgendamento.java` (deletar)
**Referência SPEC:** Seção 8.10
**Depende de:** TASK-7, TASK-8, TASK-9
**Bloqueada por:** nenhuma

---

## Contexto

`PipelineAgendamento` hoje é um pipeline plano que itera sobre todos os instrumentos com os mesmos validadores. Após as tasks anteriores, ele precisa se tornar um orquestrador simples: para cada instrumento, despacha para o pipeline específico. A lógica de early exit e validação está encapsulada em cada pipeline por instrumento.

## O que fazer

1. Criar `avaliacao/pipeline/agendamento/PipelineAgendamento.java`:

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix.PipelineAgendamentoPix;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted.PipelineAgendamentoTed;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef.PipelineAgendamentoTef;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PipelineAgendamento {

    private final Map<Instrumento, PipelineAgendamentoInstrumento> pipelines;

    public PipelineAgendamento(
        PipelineAgendamentoPix pix,
        PipelineAgendamentoTed ted,
        PipelineAgendamentoTef tef
    ) {
        this.pipelines = Map.of(Instrumento.PIX, pix, Instrumento.TED, ted, Instrumento.TEF, tef);
    }

    public void executar(AvaliacaoContexto contexto) {
        contexto.getInstrumentos().forEach(instrumento ->
            pipelines.get(instrumento).executar(contexto));
    }
}
```

2. Deletar `avaliacao/pipeline/PipelineAgendamento.java` (versão antiga com `Validador`).

## Notas de implementação

- `Map.of()` retorna mapa imutável — adequado aqui pois o mapeamento é fixo e definido no construtor.
- Se um novo `Instrumento` for adicionado ao enum sem ser mapeado aqui, `pipelines.get(instrumento)` retornará `null` e a chamada a `.executar()` lançará `NullPointerException`. O mapa deve ser exaustivo para todos os valores do enum `Instrumento` (hoje: PIX, TED, TEF).
- A ordem de execução por instrumento segue a ordem de `contexto.getInstrumentos()` (um `EnumSet`, que itera em ordem de declaração do enum).

## Critério de aceite

- [ ] `PipelineAgendamento` está em `avaliacao.pipeline.agendamento` com o `Map<Instrumento, PipelineAgendamentoInstrumento>`
- [ ] Arquivo antigo em `avaliacao.pipeline` foi deletado
- [ ] `Map` cobre todos os três instrumentos (PIX, TED, TEF)
- [ ] Build compila sem erros (importo de `AvaliacaoOrquestrador` ainda quebrado — será corrigido na TASK-12)
