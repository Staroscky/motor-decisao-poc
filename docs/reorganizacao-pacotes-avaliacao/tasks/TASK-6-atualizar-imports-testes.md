# TASK-6 — Atualizar imports nos testes e compilar

**Arquivo alvo:** `src/test/java/.../avaliacao/PipelineAgendamentoPixTest.java`
**Referência SPEC:** Seção 10 (arquivos afetados — testes), RF-07, RF-08
**Depende de:** TASK-2, TASK-3 (ValidadorAgendamento e validators PIX movidos)
**Bloqueada por:** nenhuma

---

## Contexto

`PipelineAgendamentoPixTest.java` importa `ValidadorAgendamento` e possivelmente os validators PIX do antigo pacote `pipeline.agendamento.*`. Com os moves das tasks anteriores, esses imports precisam ser atualizados para compilar.

## O que fazer

1. Abrir `src/test/java/com/staroscky/motordecisao/avaliacao/PipelineAgendamentoPixTest.java`.
2. Localizar todos os imports que referenciam `pipeline.agendamento.*` (ValidadorAgendamento e/ou validators pix).
3. Atualizar para os novos pacotes:
   - `avaliacao.pipeline.agendamento.ValidadorAgendamento` → `avaliacao.validador.agendamento.ValidadorAgendamento`
   - `avaliacao.pipeline.agendamento.pix.*` → `avaliacao.validador.agendamento.pix.*`
4. Rodar `mvn compile` — deve passar sem erros.

## Notas de implementação

- Verificar se há outros arquivos de teste com imports afetados:
  `grep -r "pipeline.agendamento" src/test/`
- Após `mvn compile` passar limpo, rodar `mvn test` para confirmar que não há regressão.

## Critério de aceite

- [ ] `PipelineAgendamentoPixTest.java` importa `ValidadorAgendamento` e validators PIX dos novos pacotes
- [ ] `grep -r "pipeline.agendamento" src/` retorna apenas os arquivos de Pipeline (sem validators e sem testes)
- [ ] `mvn compile` passa sem erros
