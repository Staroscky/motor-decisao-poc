# TASK-5 — Mover validators TEF para `validador/agendamento/tef/`

**Arquivo alvo:** `avaliacao/validador/agendamento/tef/` (3 arquivos)
**Referência SPEC:** Seção 4.1, RF-03, RF-06
**Depende de:** TASK-2 (ValidadorAgendamento já deve estar em `validador.agendamento`)
**Bloqueada por:** nenhuma (pode ser feita junto com TASK-3 e TASK-4)

---

## Contexto

Os 3 validators de agendamento do TEF estão em `avaliacao.pipeline.agendamento.tef`. Mesmo padrão de PIX e TED. Após esta task, `pipeline/agendamento/tef/` conterá apenas `PipelineAgendamentoTef.java`.

## O que fazer

Para cada um dos 3 validators abaixo, fazer:
1. Criar arquivo em `avaliacao/validador/agendamento/tef/` com o mesmo nome
2. Alterar a linha de package de `avaliacao.pipeline.agendamento.tef` para `avaliacao.validador.agendamento.tef`
3. Atualizar o import de `ValidadorAgendamento`:
   - De: `import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;`
   - Para: `import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;`
4. Deletar o arquivo original em `pipeline/agendamento/tef/`

Arquivos a mover:
- `AgendamentoTefValidador.java`
- `ProximaDataTefValidador.java`
- `AvisoIntervaloTefValidador.java`

Atualizar imports em `PipelineAgendamentoTef.java`:
- De: `import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef.*`
- Para: `import com.staroscky.motordecisao.avaliacao.validador.agendamento.tef.*`

## Notas de implementação

- Verificar outros importadores: `grep -r "pipeline.agendamento.tef" src/`
- Conteúdo lógico dos validators não muda.

## Critério de aceite

- [ ] `avaliacao/validador/agendamento/tef/` contém os 3 validators
- [ ] `avaliacao/pipeline/agendamento/tef/` contém apenas `PipelineAgendamentoTef.java`
- [ ] Os 3 validators têm package `avaliacao.validador.agendamento.tef`
- [ ] `PipelineAgendamentoTef.java` importa validators de `avaliacao.validador.agendamento.tef`
