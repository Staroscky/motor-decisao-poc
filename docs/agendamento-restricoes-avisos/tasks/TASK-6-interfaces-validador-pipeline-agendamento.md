# TASK-6 — Criar interfaces ValidadorAgendamento e PipelineAgendamentoInstrumento

**Arquivo alvo:** `avaliacao/pipeline/agendamento/ValidadorAgendamento.java` (criar) + `avaliacao/pipeline/agendamento/PipelineAgendamentoInstrumento.java` (criar)
**Referência SPEC:** Seção 8.5, RF-10
**Depende de:** TASK-3
**Bloqueada por:** nenhuma

---

## Contexto

Os pipelines de agendamento por instrumento precisam de duas interfaces:
- `ValidadorAgendamento` — contrato dos validadores internos de cada pipeline, sem o parâmetro `Instrumento` (cada pipeline já conhece o seu)
- `PipelineAgendamentoInstrumento` — contrato dos pipelines por instrumento, usado pelo orquestrador

## O que fazer

1. Criar `avaliacao/pipeline/agendamento/ValidadorAgendamento.java`:

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;

public interface ValidadorAgendamento {

    boolean suporta(AvaliacaoContexto contexto);

    void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado);
}
```

2. Criar `avaliacao/pipeline/agendamento/PipelineAgendamentoInstrumento.java`:

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;

public interface PipelineAgendamentoInstrumento {

    void executar(AvaliacaoContexto contexto);
}
```

## Notas de implementação

- `ValidadorAgendamento` não recebe `Instrumento` como parâmetro — essa é a diferença intencional em relação a `Validador`. Cada pipeline por instrumento já está co-localizado com o instrumento que gerencia.
- O diretório `avaliacao/pipeline/agendamento/` ainda não existe — criar junto com os arquivos.

## Critério de aceite

- [ ] `ValidadorAgendamento` existe com os dois métodos corretos
- [ ] `PipelineAgendamentoInstrumento` existe com `executar(AvaliacaoContexto)`
- [ ] Build compila sem erros
