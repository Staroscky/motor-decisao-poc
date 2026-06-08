# TASK-2 — Mover `ValidadorAgendamento` para `validador/agendamento/`

**Arquivo alvo:** `avaliacao/validador/agendamento/ValidadorAgendamento.java` (novo — criado por move)
**Referência SPEC:** Seção 4.1, RF-02, RF-04
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

A interface `ValidadorAgendamento` define o contrato de todos os validators de agendamento por instrumento. Está hoje em `avaliacao.pipeline.agendamento` — pacote de orquestração de pipeline. A regra da SPEC é que interfaces de validator pertencem a `avaliacao.validador`. Esta task move apenas a interface; os validators que a implementam são movidos nas TASK-3, 4 e 5.

## O que fazer

1. Criar `src/main/java/com/staroscky/motordecisao/avaliacao/validador/agendamento/ValidadorAgendamento.java` com package `avaliacao.validador.agendamento` e conteúdo idêntico ao atual.
2. Deletar `src/main/java/com/staroscky/motordecisao/avaliacao/pipeline/agendamento/ValidadorAgendamento.java`.
3. Atualizar o import nos três Pipeline de instrumento:
   - `pipeline/agendamento/pix/PipelineAgendamentoPix.java`
   - `pipeline/agendamento/ted/PipelineAgendamentoTed.java`
   - `pipeline/agendamento/tef/PipelineAgendamentoTef.java`
   - Em cada um: de `import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;`
   - Para: `import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;`
4. **Não atualizar** os 10 validators nesta task — eles têm o import quebrado temporariamente até TASK-3/4/5.
5. Rodar `mvn compile` para confirmar que a interface e os Pipeline* compilam; warnings de validators são esperados até as tasks seguintes.

## Notas de implementação

- Os 10 validators de agendamento também importam `ValidadorAgendamento`. Seus imports ficam momentaneamente quebrados até serem movidos nas TASK-3/4/5. Se o build de compile intermediário for executado somente após TASK-2 e antes de TASK-3, haverá erros nesses arquivos — isso é esperado. O gate de compile definitivo é executado na TASK-6.
- Alternativamente, executar TASK-2, TASK-3, TASK-4 e TASK-5 em sequência sem rodar compile entre elas.

## Critério de aceite

- [ ] `avaliacao/validador/agendamento/ValidadorAgendamento.java` existe com package `avaliacao.validador.agendamento`
- [ ] `avaliacao/pipeline/agendamento/ValidadorAgendamento.java` não existe
- [ ] `PipelineAgendamentoPix.java`, `PipelineAgendamentoTed.java`, `PipelineAgendamentoTef.java` importam `avaliacao.validador.agendamento.ValidadorAgendamento`
