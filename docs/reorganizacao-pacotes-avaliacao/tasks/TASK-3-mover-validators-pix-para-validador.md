# TASK-3 — Mover validators PIX para `validador/agendamento/pix/`

**Arquivo alvo:** `avaliacao/validador/agendamento/pix/` (4 arquivos)
**Referência SPEC:** Seção 4.1, RF-03, RF-06
**Depende de:** TASK-2 (ValidadorAgendamento já deve estar em `validador.agendamento`)
**Bloqueada por:** nenhuma (pode ser feita junto com TASK-4 e TASK-5)

---

## Contexto

Os 4 validators de agendamento do PIX estão em `avaliacao.pipeline.agendamento.pix`. A SPEC determina que validators ficam em `avaliacao.validador`. Após esta task, `pipeline/agendamento/pix/` conterá apenas `PipelineAgendamentoPix.java`.

## O que fazer

Para cada um dos 4 validators abaixo, fazer:
1. Criar arquivo em `avaliacao/validador/agendamento/pix/` com o mesmo nome
2. Alterar a linha de package de `avaliacao.pipeline.agendamento.pix` para `avaliacao.validador.agendamento.pix`
3. Atualizar o import de `ValidadorAgendamento`:
   - De: `import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;`
   - Para: `import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;`
4. Deletar o arquivo original em `pipeline/agendamento/pix/`

Arquivos a mover:
- `AgendamentoPixQrcodeValidador.java`
- `AgendamentoPixValidador.java`
- `ProximaDataPixValidador.java`
- `AvisoIntervaloPixValidador.java`

Atualizar imports em `PipelineAgendamentoPix.java` para referenciar os validators no novo pacote:
- De: `import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix.*`
- Para: `import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.*` (ou imports individuais)

## Notas de implementação

- O import de `PipelineAgendamentoPix` pode usar wildcard `validador.agendamento.pix.*` se preferir.
- Verificar se há outros importadores dos validators PIX além do Pipeline:
  `grep -r "pipeline.agendamento.pix.Agendamento\|pipeline.agendamento.pix.Proxima\|pipeline.agendamento.pix.Aviso" src/`
- O conteúdo lógico dos validators não muda — apenas package e imports.

## Critério de aceite

- [ ] `avaliacao/validador/agendamento/pix/` contém os 4 validators
- [ ] `avaliacao/pipeline/agendamento/pix/` contém apenas `PipelineAgendamentoPix.java`
- [ ] Os 4 validators têm package `avaliacao.validador.agendamento.pix`
- [ ] `PipelineAgendamentoPix.java` importa validators de `avaliacao.validador.agendamento.pix`
