# TASK-4 — Mover validators TED para `validador/agendamento/ted/`

**Arquivo alvo:** `avaliacao/validador/agendamento/ted/` (3 arquivos)
**Referência SPEC:** Seção 4.1, RF-03, RF-06
**Depende de:** TASK-2 (ValidadorAgendamento já deve estar em `validador.agendamento`)
**Bloqueada por:** nenhuma (pode ser feita junto com TASK-3 e TASK-5)

---

## Contexto

Os 3 validators de agendamento do TED estão em `avaliacao.pipeline.agendamento.ted`. Mesma situação que PIX — validators devem estar em `avaliacao.validador`. Após esta task, `pipeline/agendamento/ted/` conterá apenas `PipelineAgendamentoTed.java`.

## O que fazer

Para cada um dos 3 validators abaixo, fazer:
1. Criar arquivo em `avaliacao/validador/agendamento/ted/` com o mesmo nome
2. Alterar a linha de package de `avaliacao.pipeline.agendamento.ted` para `avaliacao.validador.agendamento.ted`
3. Atualizar o import de `ValidadorAgendamento`:
   - De: `import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;`
   - Para: `import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;`
4. Deletar o arquivo original em `pipeline/agendamento/ted/`

Arquivos a mover:
- `AgendamentoTedValidador.java`
- `ProximaDataTedValidador.java`
- `AvisoIntervaloTedValidador.java`

Atualizar imports em `PipelineAgendamentoTed.java`:
- De: `import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted.*`
- Para: `import com.staroscky.motordecisao.avaliacao.validador.agendamento.ted.*`

## Notas de implementação

- Verificar outros importadores: `grep -r "pipeline.agendamento.ted" src/`
- Conteúdo lógico dos validators não muda.

## Critério de aceite

- [ ] `avaliacao/validador/agendamento/ted/` contém os 3 validators
- [ ] `avaliacao/pipeline/agendamento/ted/` contém apenas `PipelineAgendamentoTed.java`
- [ ] Os 3 validators têm package `avaliacao.validador.agendamento.ted`
- [ ] `PipelineAgendamentoTed.java` importa validators de `avaliacao.validador.agendamento.ted`
