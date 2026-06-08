# TASK-13 — Testes unitários

**Arquivo alvo:** `src/test/java/.../avaliacao/` (criar e modificar)
**Referência SPEC:** Seção 13
**Depende de:** TASK-7, TASK-8, TASK-9, TASK-10, TASK-11
**Bloqueada por:** nenhuma

---

## Contexto

Esta task cria os testes que cobrem os comportamentos especificados: early exit, `isPodeAgendar()` derivado, `suporta()` correto dos validadores de agendamento, e serialização do mapper nos quatro cenários. O teste do validador antigo (`PermiteAgendamentoValidadorTest`) foi atualizado na TASK-7 — não duplicar aqui.

## O que fazer

### 1. `ResultadoAgendamentoTest`

Criar `src/test/java/.../avaliacao/ResultadoAgendamentoTest.java`:

- `isPodeAgendar_verdadeiroQuandoSemRestricoes()`
- `isPodeAgendar_falsoAposAdicionarRestricao()`
- `adicionarAviso_naoAfetaIsPodeAgendar()`
- `temProximaData_falsoQuandoNull()` e `verdadeiroAposSet()`

### 2. `PipelineAgendamentoPixTest`

Criar `src/test/java/.../avaliacao/PipelineAgendamentoPixTest.java`:

- `earlyExit_interrompeAposRestricaoSemExecutarProximaData()` — verifica que validadores seguintes não executam quando restrição é adicionada
- `aviso_naoInterrompePipeline()` — adicionar aviso não causa break

Use mocks para os validadores ou implemente validadores de teste que adicionam restrição/aviso em `validar()`.

### 3. `PipelineDataSelecionadaTest`

Criar (ou atualizar se já existir) `src/test/java/.../avaliacao/PipelineDataSelecionadaTest.java`:

- `earlyExit_interrompeAposRestricaoNoDataSelecionada()` — o segundo validador não deve executar após o primeiro adicionar restrição

### 4. `AvaliacaoResponseMapperTest`

Criar `src/test/java/.../avaliacao/AvaliacaoResponseMapperTest.java` cobrindo os quatro cenários de serialização do bloco `agendamento`:

- **Cenário 1:** `podeAgendar: true`, `restricoes: []`, sem `proximaDataDisponivel`, sem `avisos`
- **Cenário 2:** `podeAgendar: false`, `restricoes` com uma entrada, sem `proximaDataDisponivel`, sem `avisos`
- **Cenário 3:** `podeAgendar: true`, `restricoes: []`, `proximaDataDisponivel` presente, sem `avisos`
- **Cenário 4:** `podeAgendar: true`, `restricoes: []`, `proximaDataDisponivel` presente, `avisos` com entradas

Verificar tanto o mapeamento do objeto quanto a serialização JSON (usando `ObjectMapper`) para confirmar que `proximaDataDisponivel` e `avisos` estão ausentes do JSON quando não aplicável.

## Notas de implementação

- Para testar early exit do pipeline sem mocks pesados, criar classes anônimas ou inner classes de validador dentro do teste que controlam se foram chamadas via contador ou flag.
- `List.copyOf(Collections.emptyList())` resulta em lista não-nula mas vazia — confirmar que `@JsonInclude(NON_EMPTY)` exclui do JSON corretamente usando `ObjectMapper` diretamente no teste do mapper.
- Não criar testes de pipeline para TED e TEF além do smoke test — a lógica de loop é idêntica ao PIX.

## Critério de aceite

- [ ] `ResultadoAgendamentoTest` cobre `isPodeAgendar()`, `temProximaData()` e independência de avisos
- [ ] `PipelineAgendamentoPixTest` verifica early exit e não-interrupção por aviso
- [ ] `PipelineDataSelecionadaTest` verifica early exit no pipeline de data selecionada
- [ ] `AvaliacaoResponseMapperTest` verifica os quatro cenários de JSON do bloco `agendamento`
- [ ] Todos os testes passam com `mvn test`
