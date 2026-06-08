# TASK-12 — Atualizar AvaliacaoOrquestrador

**Arquivo alvo:** `avaliacao/AvaliacaoOrquestrador.java` (modificar)
**Referência SPEC:** Seção 4.1
**Depende de:** TASK-2, TASK-10
**Bloqueada por:** nenhuma

---

## Contexto

`AvaliacaoOrquestrador` importa `PipelineDataSelecionada` de `avaliacao.pipeline` e `PipelineAgendamento` também de `avaliacao.pipeline`. Após TASK-2 e TASK-10, ambas as classes foram movidas para subpacotes (`pipeline.data` e `pipeline.agendamento`). Esta task atualiza apenas os imports — sem alteração de comportamento.

## O que fazer

Atualizar `avaliacao/AvaliacaoOrquestrador.java` — substituir os dois imports:

```java
// antes
import com.staroscky.motordecisao.avaliacao.pipeline.PipelineAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.PipelineDataSelecionada;

// depois
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.data.PipelineDataSelecionada;
```

O restante do arquivo (`executar()`, construtores, campos) permanece igual.

## Notas de implementação

- Esta é a última peça para o build compilar integralmente após todas as mudanças de pacote.
- Verificar se algum outro arquivo ainda importa os pacotes antigos após esta task.

## Critério de aceite

- [ ] `AvaliacaoOrquestrador` compila com os imports atualizados
- [ ] Build compila completamente sem erros
- [ ] Nenhuma referência a `avaliacao.pipeline.PipelineAgendamento` (pacote antigo) permanece no projeto
- [ ] Nenhuma referência a `avaliacao.pipeline.PipelineDataSelecionada` (pacote antigo) permanece no projeto
