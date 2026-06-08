# SPEC — Reorganização dos Pacotes de `avaliacao/`

## 1. Contexto da solicitação

### 1.1 História ou tarefa do usuário

- **Solicitante:** Desenvolvedor do projeto `motordecisao`
- **Tipo:** refactor guiado por comportamento de manutenção
- **História:** Como desenvolvedor que mantém o motor de decisão, quero que os subpacotes de `avaliacao/` sigam regras claras e consistentes, para que ao adicionar um novo tipo de domínio ou um novo validator eu saiba exatamente onde criá-lo, sem precisar analisar precedentes conflitantes.
- **Valor esperado:** Fim das decisões de "onde coloco isso?" — a estrutura de pacotes responde sozinha.

### 1.2 Problema observado

A evolução orgânica de `avaliacao/` criou duas inconsistências estruturais:

**Inconsistência 1 — `contexto/` vs `modelo/`:**

`modelo/` foi criado para conter tipos de domínio imutáveis (`Aviso`, `Restricao*`). Mas `Limite`, que tem a mesma natureza (um `record` puro com dado de negócio), ficou em `contexto/` — pacote cujo propósito principal é o contexto de execução (`AvaliacaoContexto`) e os agregados de resultado mutáveis (`ResultadoInstrumento`, `ResultadoViabilidade`, `ResultadoAgendamento`). Não existe regra documentada para separar os dois pacotes.

**Inconsistência 2 — validators em dois lugares:**

A interface `Validador` e os validators de viabilidade (`data/`, `instituicao/`) vivem em `validador/`. A interface `ValidadorAgendamento` e os 10 validators de agendamento por instrumento vivem em `pipeline/agendamento/pix|ted|tef/`. A decisão de co-localizar validators com pipelines foi documentada em `agendamento-restricoes-avisos/SPEC.md §7.1` como "Coesão do pipeline", mas gerou a inconsistência que o usuário agora observa: o pacote `validador/` não é a fonte única de verdade para validators do sistema.

### 1.3 Objetivo da entrega

A entrega está completa quando:
- `modelo/` contém todos os tipos de domínio imutáveis, incluindo `Limite`
- `validador/` contém todas as interfaces de validator e todas as implementações de validator, incluindo os 10 validators de agendamento por instrumento
- `pipeline/` contém apenas classes de orquestração de pipeline (zero validators)
- As regras de pacote estão implícitas na estrutura — adição de validator sempre vai para `validador/`, adição de tipo de domínio sempre vai para `modelo/`
- Build compila e todos os testes passam sem regressão comportamental

---

## 2. Objetivo técnico

Aplicar três mudanças de pacote pontuais que alinham a estrutura existente às regras implícitas que o projeto já usa parcialmente: `modelo/` para domínio imutável, `contexto/` para contexto de execução, `validador/` para validators. Nenhum comportamento muda — apenas declarações de pacote e imports.

---

## 3. Estado atual

### Estrutura relevante

```
avaliacao/
├── contexto/
│   ├── AvaliacaoContexto.java          ← contexto de execução (correto)
│   ├── ResultadoInstrumento.java       ← agregado mutável (correto)
│   ├── ResultadoViabilidade.java       ← agregado mutável (correto)
│   ├── ResultadoAgendamento.java       ← agregado mutável (correto)
│   └── Limite.java                     ← record puro: está ERRADO aqui, deveria ser modelo/
│
├── modelo/
│   ├── Aviso.java                      ← record puro (correto)
│   └── restricao/
│       ├── Restricao.java              ← sealed interface (correto)
│       ├── RestricaoGenerica.java      ← record (correto)
│       └── RestricaoQrcode.java        ← record (correto)
│
├── pipeline/
│   ├── data/
│   │   └── PipelineDataSelecionada.java    ← pipeline (correto)
│   └── agendamento/
│       ├── PipelineAgendamento.java           ← pipeline (correto)
│       ├── PipelineAgendamentoInstrumento.java ← interface de pipeline (correto)
│       ├── ValidadorAgendamento.java           ← interface de VALIDATOR aqui: ERRADO
│       ├── pix/
│       │   ├── PipelineAgendamentoPix.java    ← pipeline (correto)
│       │   ├── AgendamentoPixQrcodeValidador.java ← validator aqui: ERRADO
│       │   ├── AgendamentoPixValidador.java       ← validator aqui: ERRADO
│       │   ├── ProximaDataPixValidador.java       ← validator aqui: ERRADO
│       │   └── AvisoIntervaloPixValidador.java    ← validator aqui: ERRADO
│       ├── ted/
│       │   ├── PipelineAgendamentoTed.java    ← pipeline (correto)
│       │   ├── AgendamentoTedValidador.java       ← validator aqui: ERRADO
│       │   ├── ProximaDataTedValidador.java       ← validator aqui: ERRADO
│       │   └── AvisoIntervaloTedValidador.java    ← validator aqui: ERRADO
│       └── tef/
│           ├── PipelineAgendamentoTef.java    ← pipeline (correto)
│           ├── AgendamentoTefValidador.java       ← validator aqui: ERRADO
│           ├── ProximaDataTefValidador.java       ← validator aqui: ERRADO
│           └── AvisoIntervaloTefValidador.java    ← validator aqui: ERRADO
│
└── validador/
    ├── Validador.java                  ← interface de validator (correto)
    ├── data/
    │   ├── DataValidaValidador.java        ← validator (correto)
    │   ├── HorarioPermitidoValidador.java  ← validator (correto)
    │   └── LimiteDiarioValidador.java      ← validator (correto)
    └── instituicao/
        ├── StatusInstituicaoValidador.java ← validator (correto)
        └── LimiteInstituicaoValidador.java ← validator (correto)
```

### Importadores de `Limite` (afetados pelo move)

| Arquivo | Importa |
|---|---|
| `avaliacao/response/ResultadoViabilidadeDto.java` | `avaliacao.contexto.Limite` |
| `avaliacao/contexto/ResultadoViabilidade.java` | implícito — campo `Limite` |

### Importadores de `ValidadorAgendamento` (afetados pelo move)

Grep `import.*pipeline.agendamento.ValidadorAgendamento` → 14 ocorrências:
- `pipeline/agendamento/pix/PipelineAgendamentoPix.java`
- `pipeline/agendamento/ted/PipelineAgendamentoTed.java`
- `pipeline/agendamento/tef/PipelineAgendamentoTef.java`
- 10 validators (`AgendamentoPix*`, `ProximaDataPix*`, `AvisoIntervaloPix*`, `ted`, `tef`)
- `test/.../PipelineAgendamentoPixTest.java`

---

## 4. Escopo da solução

### 4.1 O que muda

| Arquivo | Estado atual | Estado esperado | Impacto |
|---|---|---|---|
| `contexto/Limite.java` | `package avaliacao.contexto` | Mover para `modelo/` → `package avaliacao.modelo` | Médio (importadores) |
| `response/ResultadoViabilidadeDto.java` | importa `avaliacao.contexto.Limite` | importa `avaliacao.modelo.Limite` | Baixo |
| `contexto/ResultadoViabilidade.java` | campo `Limite` importado de `contexto` | atualizar import para `avaliacao.modelo.Limite` | Baixo |
| `pipeline/agendamento/ValidadorAgendamento.java` | `package avaliacao.pipeline.agendamento` | Mover para `validador/agendamento/` → `package avaliacao.validador.agendamento` | Alto (14 importadores) |
| `pipeline/agendamento/pix/AgendamentoPixQrcodeValidador.java` | `package avaliacao.pipeline.agendamento.pix` | Mover para `validador/agendamento/pix/` | Alto (package + import) |
| `pipeline/agendamento/pix/AgendamentoPixValidador.java` | idem | Mover para `validador/agendamento/pix/` | Alto |
| `pipeline/agendamento/pix/ProximaDataPixValidador.java` | idem | Mover para `validador/agendamento/pix/` | Alto |
| `pipeline/agendamento/pix/AvisoIntervaloPixValidador.java` | idem | Mover para `validador/agendamento/pix/` | Alto |
| `pipeline/agendamento/ted/AgendamentoTedValidador.java` | `package avaliacao.pipeline.agendamento.ted` | Mover para `validador/agendamento/ted/` | Alto |
| `pipeline/agendamento/ted/ProximaDataTedValidador.java` | idem | Mover para `validador/agendamento/ted/` | Alto |
| `pipeline/agendamento/ted/AvisoIntervaloTedValidador.java` | idem | Mover para `validador/agendamento/ted/` | Alto |
| `pipeline/agendamento/tef/AgendamentoTefValidador.java` | `package avaliacao.pipeline.agendamento.tef` | Mover para `validador/agendamento/tef/` | Alto |
| `pipeline/agendamento/tef/ProximaDataTefValidador.java` | idem | Mover para `validador/agendamento/tef/` | Alto |
| `pipeline/agendamento/tef/AvisoIntervaloTefValidador.java` | idem | Mover para `validador/agendamento/tef/` | Alto |
| `pipeline/agendamento/pix/PipelineAgendamentoPix.java` | importa `ValidadorAgendamento` do pacote atual | atualizar import para `avaliacao.validador.agendamento.ValidadorAgendamento` | Baixo |
| `pipeline/agendamento/ted/PipelineAgendamentoTed.java` | importa `ValidadorAgendamento` do pacote atual | atualizar import para `avaliacao.validador.agendamento.ValidadorAgendamento` | Baixo |
| `pipeline/agendamento/tef/PipelineAgendamentoTef.java` | importa `ValidadorAgendamento` do pacote atual | atualizar import para `avaliacao.validador.agendamento.ValidadorAgendamento` | Baixo |
| `test/.../PipelineAgendamentoPixTest.java` | importa `ValidadorAgendamento` e `AgendamentoPix*` de `pipeline.agendamento.*` | atualizar imports | Baixo |

### 4.2 O que não muda

- Comportamento de nenhum validator, pipeline, mapper ou controller
- Conteúdo de nenhum arquivo — apenas package declaration e imports
- Pacotes `request/`, `response/`, `upstream/`, `resolver/`, `mapper/` — sem alteração de organização
- `Instrumento` e `TipoCheckin` permanecem em `resolver/` — coesos com a lógica de parsing
- `validador/data/` e `validador/instituicao/` — sem alteração
- `pipeline/agendamento/pix|ted|tef/` mantém as classes de Pipeline* (apenas validators saem)

### 4.3 Restrições e pressupostos

- Esta reorganização reverte a decisão de co-localização de `agendamento-restricoes-avisos/SPEC.md §7.1`. O trade-off aceito é: perder co-localização de validators com seu pipeline em troca de ter `validador/` como fonte única de verdade para todos os validators.
- Os arquivos de validators não precisam de nenhuma alteração de conteúdo — apenas package declaration na linha 1 e imports internos.
- A reorganização pressupõe que a feature `agendamento-restricoes-avisos` já está implementada (os arquivos dos validators de agendamento já existem no repositório).
- Sem Lombok — nenhuma anotação adicional necessária.
- O compilador Java garante que nenhuma referência pendente existe após a reorganização — build é o gate de validação.

---

## 5. Requisitos funcionais

| ID | Requisito funcional | Prioridade | Origem |
|---|---|---|---|
| RF-01 | `Limite.java` deve ter package `avaliacao.modelo` após a mudança | must | PRD §Inclui |
| RF-02 | `ValidadorAgendamento.java` deve ter package `avaliacao.validador.agendamento` após a mudança | must | PRD §Inclui |
| RF-03 | Os 10 validators de agendamento (pix/ted/tef) devem ter packages `avaliacao.validador.agendamento.<instrumento>` | must | PRD §Inclui |
| RF-04 | `PipelineAgendamentoPix/Ted/Tef.java` devem importar `ValidadorAgendamento` do novo pacote `avaliacao.validador.agendamento` | must | dependência de RF-02 |
| RF-05 | `ResultadoViabilidade.java` e `ResultadoViabilidadeDto.java` devem importar `Limite` do novo pacote `avaliacao.modelo` | must | dependência de RF-01 |
| RF-06 | Nenhum arquivo em `pipeline/` deve declarar package de validator | must | PRD §CriteriosSucesso |
| RF-07 | Build Maven compila sem erros após a reorganização | must | PRD §CriteriosSucesso |
| RF-08 | Todos os testes existentes passam sem regressão | must | PRD §CriteriosSucesso |

---

## 6. Cenários e fluxos esperados

### 6.1 Cenário de adição após a reorganização

**Adicionar `AgendamentoPixChaveValidador` (futuro validator PIX para chave):**
1. Criar `avaliacao/validador/agendamento/pix/AgendamentoPixChaveValidador.java` — sem dúvida de onde vai
2. Registrar na lista do `PipelineAgendamentoPix`
3. Pronto — nenhuma decisão de pacote a tomar

**Antes da reorganização:** poderia ir em `validador/agendamento/pix/` ou em `pipeline/agendamento/pix/` — a co-localização do SPEC anterior criava precedente de fazer o segundo.

### 6.2 Edge cases

- **Referência cíclica:** `pipeline/agendamento/pix/PipelineAgendamentoPix` importará validators de `validador/agendamento/pix/`. É referência direta — sem ciclo.
- **Validator que suporta múltiplos instrumentos (futuro):** validator vai para `validador/agendamento/` (sem subpacote) e é referenciado por múltiplos pipelines — a estrutura suporta sem inconsistência.
- **`PipelineDataSelecionada` já está correto:** usa `validador/data|instituicao/` — nenhuma alteração necessária.

---

## 7. Alternativas consideradas

### 7.1 Alternativa escolhida

Mover todos os validators e suas interfaces para `validador/`, tornando-o a fonte única de verdade. Pipelines ficam em `pipeline/` apenas com classes de orquestração.

**Justificativa:**
- Regra única e sem exceção: "validator vai em `validador/`"
- Reverte decisão documentada de `agendamento-restricoes-avisos/SPEC.md §7.1` que se mostrou inadequada em retrospecto
- Custo de migração é baixo: apenas package declarations e imports, zero mudança de lógica

### 7.2 Alternativas descartadas

| Alternativa | Vantagens | Desvantagens | Motivo da não escolha |
|---|---|---|---|
| Manter co-localização de validators com pipeline (status quo) | Validators e pipeline específicos de um instrumento ficam juntos na mesma pasta | `validador/` não é fonte única de verdade; desenvolvedores não sabem onde buscar validators | Perpetua a confusão que motivou este spec |
| Mover `pipeline/` para dentro de `validador/` por instrumento (pasta `pix/` com pipeline + validators) | Co-localização total por instrumento | Destrói a separação de concerns entre "orquestração" e "validação"; `pipeline/` desaparece como conceito | Troca clareza de propósito por proximidade física |
| Mesclar `contexto/` e `modelo/` em `dominio/` | Um único pacote de domínio | Rename massivo; `AvaliacaoContexto` e `Resultado*` têm semântica diferente de value objects | Custo alto para um ganho menor do que a separação clara entre mutable (contexto) e immutable (modelo) |
| Mover `Instrumento` e `TipoCheckin` para `modelo/` junto com `Limite` | Enums de domínio ficam em `modelo/` | Impacto de import em todo o codebase; `resolver/` já é um pacote coeso | Escopo maior do que o problema observado; risco de regressão sem benefício claro agora |

---

## 8. Design da solução

### 8.1 Visão geral

A mudança é exclusivamente de pacote — três grupos de movimentos:

1. **`Limite`: `contexto/` → `modelo/`** — atualizar 2 importadores
2. **`ValidadorAgendamento`: `pipeline/agendamento/` → `validador/agendamento/`** — atualizar 14 importadores
3. **10 validators de agendamento: `pipeline/agendamento/pix|ted|tef/` → `validador/agendamento/pix|ted|tef/`** — atualizar package declaration em cada um + atualizar import nos 3 Pipeline*

### 8.2 Estrutura alvo

```
avaliacao/
├── contexto/
│   ├── AvaliacaoContexto.java          ← contexto de execução e resultado de runtime
│   ├── ResultadoInstrumento.java
│   ├── ResultadoViabilidade.java
│   └── ResultadoAgendamento.java
│
├── modelo/
│   ├── Aviso.java
│   ├── Limite.java                     ← MOVIDO de contexto/
│   └── restricao/
│       ├── Restricao.java
│       ├── RestricaoGenerica.java
│       └── RestricaoQrcode.java
│
├── pipeline/
│   ├── data/
│   │   └── PipelineDataSelecionada.java
│   └── agendamento/
│       ├── PipelineAgendamento.java
│       ├── PipelineAgendamentoInstrumento.java
│       ├── pix/
│       │   └── PipelineAgendamentoPix.java   ← apenas o pipeline; validators saem
│       ├── ted/
│       │   └── PipelineAgendamentoTed.java
│       └── tef/
│           └── PipelineAgendamentoTef.java
│
└── validador/
    ├── Validador.java
    ├── data/
    │   ├── DataValidaValidador.java
    │   ├── HorarioPermitidoValidador.java
    │   └── LimiteDiarioValidador.java
    ├── instituicao/
    │   ├── StatusInstituicaoValidador.java
    │   └── LimiteInstituicaoValidador.java
    └── agendamento/                          ← NOVO subpacote
        ├── ValidadorAgendamento.java         ← MOVIDO de pipeline/agendamento/
        ├── pix/                              ← MOVIDO de pipeline/agendamento/pix/
        │   ├── AgendamentoPixQrcodeValidador.java
        │   ├── AgendamentoPixValidador.java
        │   ├── ProximaDataPixValidador.java
        │   └── AvisoIntervaloPixValidador.java
        ├── ted/
        │   ├── AgendamentoTedValidador.java
        │   ├── ProximaDataTedValidador.java
        │   └── AvisoIntervaloTedValidador.java
        └── tef/
            ├── AgendamentoTefValidador.java
            ├── ProximaDataTefValidador.java
            └── AvisoIntervaloTefValidador.java
```

### 8.3 Regra de pacote documentada

| Pacote | Responsabilidade | Critério de inclusão |
|---|---|---|
| `modelo/` | Tipos de domínio imutáveis | records, sealed interfaces e enums que são dados puros sem comportamento de execução |
| `contexto/` | Runtime de execução | O objeto de contexto (`AvaliacaoContexto`) e os agregados de resultado mutáveis que acumulam estado durante o pipeline |
| `validador/` | Toda lógica de validação | qualquer interface `Validador*` e qualquer classe que implementa validação — sem exceção |
| `pipeline/` | Orquestração de pipeline | apenas classes que controlam a sequência e delegam para validators — nunca validators |
| `request/` | DTOs de entrada HTTP | tipos que chegam via request body |
| `response/` | DTOs de saída HTTP | tipos retornados no response body |
| `upstream/` | Clientes e DTOs externos | Feign clients e seus DTOs |
| `resolver/` | Parsing e resolução do checkinId | CheckinId → Instrumento/TipoCheckin + enums de domínio coesos com esse parsing |
| `mapper/` | Mapeamento de contexto → response | classes que transformam agregados internos em DTOs |

### 8.4 Contratos e interfaces — sem alteração

As assinaturas de `Validador` e `ValidadorAgendamento` não mudam:

```java
// avaliacao.validador.Validador (não muda, apenas referência)
public interface Validador {
    boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento);
    void validar(AvaliacaoContexto contexto, Instrumento instrumento, ResultadoViabilidade resultado);
}

// avaliacao.validador.agendamento.ValidadorAgendamento (package muda, conteúdo não)
public interface ValidadorAgendamento {
    boolean suporta(AvaliacaoContexto contexto);
    void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado);
}
```

---

## 9. Fluxos técnicos

Fluxo de execução — sem alteração:

```text
POST /v1/avaliacao
    └── AvaliacaoController → AvaliacaoService → AvaliacaoOrquestrador
            ├── PipelineDataSelecionada
            │       └── Validador (validador.data/, validador.instituicao/) — sem mudança
            └── PipelineAgendamento
                    ├── PipelineAgendamentoPix
                    │       └── ValidadorAgendamento (agora em validador.agendamento.pix/)
                    ├── PipelineAgendamentoTed
                    │       └── ValidadorAgendamento (agora em validador.agendamento.ted/)
                    └── PipelineAgendamentoTef
                            └── ValidadorAgendamento (agora em validador.agendamento.tef/)
```

---

## 10. Arquivos afetados

### Mover (alteração de package declaration + imports internos)

| Arquivo | De | Para |
|---|---|---|
| `Limite.java` | `avaliacao/contexto/` | `avaliacao/modelo/` |
| `ValidadorAgendamento.java` | `avaliacao/pipeline/agendamento/` | `avaliacao/validador/agendamento/` |
| `AgendamentoPixQrcodeValidador.java` | `avaliacao/pipeline/agendamento/pix/` | `avaliacao/validador/agendamento/pix/` |
| `AgendamentoPixValidador.java` | `avaliacao/pipeline/agendamento/pix/` | `avaliacao/validador/agendamento/pix/` |
| `ProximaDataPixValidador.java` | `avaliacao/pipeline/agendamento/pix/` | `avaliacao/validador/agendamento/pix/` |
| `AvisoIntervaloPixValidador.java` | `avaliacao/pipeline/agendamento/pix/` | `avaliacao/validador/agendamento/pix/` |
| `AgendamentoTedValidador.java` | `avaliacao/pipeline/agendamento/ted/` | `avaliacao/validador/agendamento/ted/` |
| `ProximaDataTedValidador.java` | `avaliacao/pipeline/agendamento/ted/` | `avaliacao/validador/agendamento/ted/` |
| `AvisoIntervaloTedValidador.java` | `avaliacao/pipeline/agendamento/ted/` | `avaliacao/validador/agendamento/ted/` |
| `AgendamentoTefValidador.java` | `avaliacao/pipeline/agendamento/tef/` | `avaliacao/validador/agendamento/tef/` |
| `ProximaDataTefValidador.java` | `avaliacao/pipeline/agendamento/tef/` | `avaliacao/validador/agendamento/tef/` |
| `AvisoIntervaloTefValidador.java` | `avaliacao/pipeline/agendamento/tef/` | `avaliacao/validador/agendamento/tef/` |

### Modificar somente imports (conteúdo não muda)

| Arquivo | Import que muda |
|---|---|
| `contexto/ResultadoViabilidade.java` | `avaliacao.contexto.Limite` → `avaliacao.modelo.Limite` |
| `response/ResultadoViabilidadeDto.java` | `avaliacao.contexto.Limite` → `avaliacao.modelo.Limite` |
| `pipeline/agendamento/pix/PipelineAgendamentoPix.java` | `pipeline.agendamento.ValidadorAgendamento` → `validador.agendamento.ValidadorAgendamento` + imports de validators pix |
| `pipeline/agendamento/ted/PipelineAgendamentoTed.java` | idem para ted |
| `pipeline/agendamento/tef/PipelineAgendamentoTef.java` | idem para tef |
| `test/.../PipelineAgendamentoPixTest.java` | imports de `ValidadorAgendamento` e validators pix |

---

## 11. Requisitos não funcionais

| Categoria | Requisito | Meta ou critério |
|---|---|---|
| Confiabilidade | Nenhum comportamento funcional pode mudar | Garantido pela natureza da mudança (apenas package/imports) + suite de testes |
| Compatibilidade | API pública (`POST /v1/avaliacao`) não muda em nada | Mudança é interna; request/response DTOs não são afetados |
| Manutenibilidade | Regra de pacote deve ser óbvia sem documentação extra | Estrutura final deve ser autoexplicativa pela posição dos arquivos |
| Observabilidade | Sem alteração de logging | MDC e Logbook não dependem de nomes de pacote |

---

## 12. Estratégia de rollout ou migração

Não há impacto externo — mudança é estritamente interna. Deploy direto sem coordenação com consumers.

A única dependência é que a feature `agendamento-restricoes-avisos` deve estar completa (validators de agendamento criados) antes desta reorganização ser aplicada — caso contrário, os arquivos a mover ainda não existem.

---

## 13. Estratégia de validação

- **Compilação Maven:** `mvn compile` após cada TASK deve passar sem erros — é o gate primário. Erros de compilação indicam import pendente.
- **Testes unitários:** `mvn test` ao final deve passar sem regressão — garante que nenhum comportamento mudou.
- **Inspeção manual da estrutura final:** verificar com `find` ou pelo IDE que `pipeline/agendamento/pix|ted|tef/` contém apenas `Pipeline*.java` e que `validador/agendamento/pix|ted|tef/` contém apenas validators.
- **Sem testes e2e necessários:** mudança não afeta comportamento observável pelo endpoint.

---

## 14. Critérios de aceite

- [ ] `avaliacao/modelo/Limite.java` existe; `avaliacao/contexto/Limite.java` não existe
- [ ] `avaliacao/validador/agendamento/ValidadorAgendamento.java` existe; `avaliacao/pipeline/agendamento/ValidadorAgendamento.java` não existe
- [ ] `avaliacao/validador/agendamento/pix/` contém os 4 validators PIX; `pipeline/agendamento/pix/` contém apenas `PipelineAgendamentoPix.java`
- [ ] `avaliacao/validador/agendamento/ted/` contém os 3 validators TED; `pipeline/agendamento/ted/` contém apenas `PipelineAgendamentoTed.java`
- [ ] `avaliacao/validador/agendamento/tef/` contém os 3 validators TEF; `pipeline/agendamento/tef/` contém apenas `PipelineAgendamentoTef.java`
- [ ] `mvn compile` passa sem erros
- [ ] `mvn test` passa sem regressão
- [ ] Nenhum arquivo em `pipeline/` importa ou declara package de validator

---

## 15. Riscos e observações

- **Risco mínimo:** mudança de pacote puro, sem lógica de negócio — o compilador detecta qualquer import pendente na hora.
- **Dependência de ordem:** esta reorganização deve ser aplicada *após* a conclusão de `agendamento-restricoes-avisos`. Aplicar antes significa que os arquivos dos validators ainda não existem.
- **IDE pode ajudar ou atrapalhar:** IDEs como IntelliJ fazem "Move class" que atualiza imports automaticamente. Se feito manualmente, cada arquivo movido exige atualizar package declaration E atualizar imports em todos os arquivos que o importam.

---

## 16. Questões em aberto

- Nenhuma. O escopo está bem delimitado e não há decisões pendentes.
