# SPEC — Agendamento: Restrições, Avisos e Pipelines por Instrumento

## 1. Contexto da solicitação

### 1.1 História ou tarefa do usuário

- **Solicitante:** Desenvolvedor do projeto `motordecisao`
- **Tipo:** evolução de feature existente
- **História:** Como sistema consumer, quero que o bloco `agendamento` no response de `POST /v1/avaliacao` me diga se o agendamento é possível, qual a próxima data válida quando a data selecionada não é viável, e quais datas problemáticas existem no intervalo — para que eu possa apresentar todas essas informações ao usuário de forma precisa, sem misturar motivos de bloqueio com avisos informativos.
- **Valor esperado:** Frontend pode tratar restrições e avisos com comportamentos distintos — bloqueio de ação vs. exibição de alerta — sem depender de heurística sobre o conteúdo das mensagens.

### 1.2 Problema observado

O bloco `agendamento` em `ResultadoInstrumento` usa hoje a mesma classe `ResultadoViabilidade` que `dataSelecionada`. Isso acarreta quatro problemas:

1. **Sem distinção entre bloqueante e informativo:** `ResultadoViabilidade` só tem `restricoes` — não há como modelar um aviso que não bloqueia o agendamento.
2. **Early exit ausente no pipeline de agendamento:** `PipelineAgendamento` itera por todos os validadores sem interromper ao encontrar uma restrição — poderia chamar I/O desnecessariamente.
3. **Early exit ausente no pipeline de data selecionada:** `PipelineDataSelecionada` usa `stream().filter().forEach()` — não interrompe quando `resultado.isValido()` torna-se `false` após a primeira restrição.
4. **Pipeline plano sem ordenação por instrumento:** `PipelineAgendamento` itera sobre todos os instrumentos com os mesmos validadores, sem suporte a regras específicas por instrumento ou ordenação explícita.

Adicionalmente, `ProximaDataValidador` verifica `dataSelecionada.isValido()` sem antes checar se o agendamento é sequer permitido — pode tentar buscar próxima data para um instrumento bloqueado por restrição.

### 1.3 Objetivo da entrega

A entrega está completa quando:
- `agendamento` em cada instrumento responde as três perguntas de forma independente: `podeAgendar`, `proximaDataDisponivel` e `avisos`
- `podeAgendar: false` nunca coexiste com `proximaDataDisponivel` presente
- `dataSelecionada.valido: true` e `agendamento.podeAgendar: false` coexistem corretamente (ex.: QR Code COB)
- O pipeline não chama validadores de I/O quando já há restrição bloqueante
- TED e TEF têm seus próprios pipelines com validadores específicos
- `PipelineDataSelecionada` interrompe a avaliação de um instrumento ao encontrar a primeira restrição

---

## 2. Objetivo técnico

Separar o modelo interno de agendamento (`ResultadoAgendamento`) do modelo de data selecionada (`ResultadoViabilidade`), introduzir `Aviso` como conceito distinto de `Restricao`, evoluir o response com `AvaliacaoAgendamento`, fragmentar `PipelineAgendamento` em pipelines por instrumento com early exit correto e nova interface `ValidadorAgendamento`, e corrigir o early exit em `PipelineDataSelecionada` com a mesma lógica de interrupção por restrição.

---

## 3. Estado atual

| Arquivo | Conteúdo atual relevante |
|---|---|
| `avaliacao/contexto/ResultadoInstrumento.java` | `ResultadoViabilidade dataSelecionada` + `ResultadoViabilidade agendamento` — mesmo modelo para ambos os blocos |
| `avaliacao/contexto/ResultadoViabilidade.java` | `boolean valido`, `List<Restricao> restricoes`, `int ordemMelhor`, `Limite limite` — `valido` é campo mutado por `adicionarRestricao()` |
| `avaliacao/pipeline/PipelineAgendamento.java` | Itera sobre `contexto.getInstrumentos()`, usa `Validador` (com `Instrumento` como param), sem early exit |
| `avaliacao/validador/agendamento/PermiteAgendamentoValidador.java` | Suporta `PIX + QRCODE`; adiciona `RestricaoQrcode` em `ResultadoViabilidade`; resultado vai para o bloco `agendamento` |
| `avaliacao/validador/agendamento/ProximaDataValidador.java` | `suporta()` retorna `!dataSelecionada.isValido()` — não verifica se o agendamento é permitido antes |
| `avaliacao/contexto/Restricao.java` | `sealed interface` em `avaliacao.contexto`, com `RestricaoGenerica` e `RestricaoQrcode` no mesmo pacote |
| `avaliacao/response/InstrumentoPix.java` | `ResultadoViabilidadeDto agendamento` — mesmo DTO que `dataSelecionada` |
| `avaliacao/mapper/AvaliacaoResponseMapper.java` | `toDto(ResultadoViabilidade)` usado para ambos os blocos |
| `avaliacao/pipeline/PipelineDataSelecionada.java` | Em `avaliacao.pipeline`; usa `stream().filter().forEach()` — sem early exit quando `resultado.isValido()` torna-se `false` |
| Testes | `PermiteAgendamentoValidadorTest` usa `ResultadoViabilidade` e `Validador` |

---

## 4. Escopo da solução

### 4.1 O que muda

| Área | Estado atual | Estado esperado | Impacto |
|---|---|---|---|
| `contexto/ResultadoAgendamento.java` | Não existe | Criar: modelo interno de agendamento com `isPodeAgendar()` derivado | Alto |
| `modelo/Aviso.java` | Não existe | Criar: `record` com `codigo`, `data`, `descricao` | Alto |
| `modelo/restricao/Restricao.java` | Em `contexto/Restricao.java` | Mover para `modelo/restricao/` | Alto (todos os importadores mudam) |
| `modelo/restricao/RestricaoGenerica.java` | Em `contexto/` | Mover para `modelo/restricao/` | Alto |
| `modelo/restricao/RestricaoQrcode.java` | Em `contexto/` | Mover para `modelo/restricao/` | Alto |
| `contexto/ResultadoViabilidade.java` | Importa `contexto.Restricao` | Atualizar import para `modelo.restricao.Restricao` | Baixo |
| `contexto/ResultadoInstrumento.java` | `ResultadoViabilidade agendamento` | Substituir por `ResultadoAgendamento agendamento` | Alto |
| `response/AvaliacaoAgendamento.java` | Não existe | Criar: `record` com `podeAgendar`, `restricoes`, `proximaDataDisponivel?`, `avisos?` | Alto |
| `response/InstrumentoPix.java` | `ResultadoViabilidadeDto agendamento` | Substituir por `AvaliacaoAgendamento agendamento` | Médio |
| `response/InstrumentoTed.java` | `ResultadoViabilidadeDto agendamento` | Substituir por `AvaliacaoAgendamento agendamento` | Médio |
| `response/InstrumentoTef.java` | `ResultadoViabilidadeDto agendamento` | Substituir por `AvaliacaoAgendamento agendamento` | Médio |
| `response/ResultadoViabilidadeDto.java` | Importa `contexto.Restricao` | Atualizar import para `modelo.restricao.Restricao` | Baixo |
| `pipeline/agendamento/ValidadorAgendamento.java` | Não existe | Criar: interface com `suporta(AvaliacaoContexto)` e `validar(AvaliacaoContexto, ResultadoAgendamento)` | Alto |
| `pipeline/agendamento/PipelineAgendamentoInstrumento.java` | Não existe | Criar: interface base com `executar(AvaliacaoContexto)` | Médio |
| `pipeline/agendamento/PipelineAgendamento.java` | Em `pipeline/` — itera sobre instrumentos | Mover para `pipeline/agendamento/`; tornar orquestrador que despacha por instrumento | Alto |
| `pipeline/agendamento/pix/` | Não existe | Criar: pipeline PIX + 4 validadores | Alto |
| `pipeline/agendamento/ted/` | Não existe | Criar: pipeline TED + 3 validadores | Alto |
| `pipeline/agendamento/tef/` | Não existe | Criar: pipeline TEF + 3 validadores | Alto |
| `mapper/AvaliacaoResponseMapper.java` | `toDto(ResultadoViabilidade)` genérico | Adicionar `toAvaliacaoAgendamento(ResultadoAgendamento)` específico | Médio |
| `pipeline/data/PipelineDataSelecionada.java` | Em `pipeline/PipelineDataSelecionada.java`; sem early exit | Mover para `pipeline/data/` e substituir `stream().forEach()` por loop com `if (!resultado.isValido()) break` | Médio |
| `AvaliacaoOrquestrador.java` | Importa `pipeline.PipelineAgendamento` e `pipeline.PipelineDataSelecionada` | Atualizar imports para novos pacotes | Baixo |

### 4.2 O que não muda

- `ResultadoViabilidade` — continua sendo o modelo de `dataSelecionada`; recebe apenas atualização de import
- `Validador` (interface) — continua sendo usada pelos validadores de `dataSelecionada`
- Validadores em `validador/data/` e `validador/instituicao/` — sem alteração
- `AvaliacaoContexto`, `AvaliacaoOrquestrador`, `AvaliacaoService`, `AvaliacaoController` — sem alteração de comportamento
- `CheckinIdParser`, `InstrumentoResolver` — sem alteração

### 4.3 Restrições e pressupostos

- **Invariante de design:** o bloco `agendamento` nunca usa `dataSelecionada.isValido()` para decidir se o agendamento é possível. A única exceção é `ProximaData*Validador`, que lê esse estado apenas para decidir se a sugestão de próxima data é necessária.
- `Restricao` permanece como `sealed interface` — subtypes existentes são `RestricaoGenerica` e `RestricaoQrcode`. Novos casos requerem novo subtipo.
- `isPodeAgendar()` em `ResultadoAgendamento` é sempre derivado — nunca existe como campo diretamente mutável.
- Os validadores `ProximaData*` e `AvisoIntervalo*` têm o corpo de lógica de negócio marcado como `TODO` — a estrutura (interface, `suporta()`, esqueleto de `validar()`) deve ser implementada, mas as chamadas upstream são diferidas.
- A ordem de registro dos validadores em cada pipeline importa para o early exit.

---

## 5. Requisitos funcionais

| ID | Requisito funcional | Prioridade | Origem |
|---|---|---|---|
| RF-01 | `ResultadoAgendamento` deve expor `isPodeAgendar()` derivado de `restricoes.isEmpty()`, nunca como campo direto | must | PRD §3.1 |
| RF-02 | `ResultadoAgendamento` deve suportar `adicionarRestricao(Restricao)` e `adicionarAviso(Aviso)` independentemente | must | PRD §3.1 |
| RF-03 | O pipeline de agendamento deve interromper ao encontrar a primeira restrição bloqueante, sem executar validadores subsequentes | must | PRD §6 |
| RF-04 | Avisos não devem interromper o pipeline | must | PRD §2.1 |
| RF-05 | `ProximaData*Validador.suporta()` deve verificar `isPodeAgendar()` antes de verificar `dataSelecionada.isValido()` | must | PRD §7.2 |
| RF-06 | `AvisoIntervalo*Validador.suporta()` deve verificar se `proximaDataDisponivel` foi populada | must | PRD §7.3 |
| RF-07 | `AvaliacaoAgendamento` deve omitir `proximaDataDisponivel` quando `null` (`NON_NULL`) | must | PRD §4.1 |
| RF-08 | `AvaliacaoAgendamento` deve omitir `avisos` quando lista vazia (`NON_EMPTY`) | must | PRD §4.1 |
| RF-09 | `AvaliacaoAgendamento.restricoes` deve ser sempre presente, mesmo quando vazia | must | PRD §4.1 |
| RF-10 | Cada instrumento deve ter seu próprio pipeline de agendamento com validadores registrados explicitamente | must | PRD §8 |
| RF-11 | `Restricao`, `RestricaoGenerica` e `RestricaoQrcode` devem residir em `avaliacao.modelo.restricao` | should | PRD §9 |
| RF-12 | `Aviso` deve ser `record` com `codigo: String`, `data: LocalDate`, `descricao: String` | must | PRD §3.2 |
| RF-13 | `PipelineDataSelecionada` deve interromper ao encontrar a primeira restrição bloqueante (`!resultado.isValido()`), sem executar validadores subsequentes | must | Correção — mesmo princípio de RF-03 |

---

## 6. Cenários e fluxos esperados

### 6.1 Cenários principais

**Cenário 1 — Agendamento permitido, data selecionada válida:**
PIX via chave; data selecionada válida. Nenhum validador de agendamento adiciona restrição. `ProximaDataPixValidador.suporta()` retorna `false` porque `dataSelecionada.isValido()` é `true`. Response: `{ podeAgendar: true, restricoes: [] }`.

**Cenário 2 — Agendamento bloqueado por restrição de QRCODE:**
PIX via QR Code COB. `AgendamentoPixQrcodeValidador.suporta()` retorna `true` (tipo QRCODE). `validar()` adiciona `RestricaoQrcode("QR_CODE_NAO_PERMITE", { tipo: "COB" })`. Loop encerra. `dataSelecionada` pode ser `valido: true`. Response: `{ podeAgendar: false, restricoes: [{ motivo: "QR_CODE_NAO_PERMITE", contexto: { tipo: "COB" } }] }`.

**Cenário 3 — Agendamento permitido, data selecionada inválida, intervalo limpo:**
PIX via chave; data selecionada é feriado. `AgendamentoPixQrcodeValidador.suporta()` retorna `false`. `AgendamentoPixValidador` não adiciona restrição. `ProximaDataPixValidador.suporta()` retorna `true` (podeAgendar=true, dataSelecionada inválida). Popula `proximaDataDisponivel`. `AvisoIntervaloPixValidador.suporta()` retorna `true`. `verificarIntervalo()` não encontra problemas. Response: `{ podeAgendar: true, restricoes: [], proximaDataDisponivel: "2026-06-09" }`.

**Cenário 4 — Agendamento permitido, data inválida, intervalo com problemas:**
Igual ao Cenário 3, mas o intervalo contém feriado e fim de semana. Response: `{ podeAgendar: true, restricoes: [], proximaDataDisponivel: "2026-06-12", avisos: [{ codigo: "FERIADO_NACIONAL", data: "2026-06-10", descricao: "Corpus Christi" }, { codigo: "FIM_DE_SEMANA", data: "2026-06-11", descricao: "Domingo" }] }`.

### 6.2 Edge cases e falhas esperadas

- **Múltiplas restrições:** pipeline interrompe na primeira restrição — `ResultadoAgendamento` conterá exatamente uma restrição (a primeira encontrada); validadores subsequentes não executam.
- **TEF sem upstream:** `AgendamentoTefValidador` pode verificar regra local; `ProximaDataTefValidador` é stub com `TODO`.
- **TED com restrição:** early exit antes de `ProximaDataTedValidador` — `proximaDataDisponivel` ausente no response.
- **`AvisoIntervalo*` com `proximaDataDisponivel` igual a `data` da request:** borda do intervalo vazia — `avisos` ausente no response.
- **Instrumento sem validador de agendamento que suporta o contexto:** todos os `suporta()` retornam `false`; resultado permanece `podeAgendar: true`, sem avisos nem próxima data.

---

## 7. Alternativas consideradas

### 7.1 Alternativa escolhida

Criar `ResultadoAgendamento` como classe distinta de `ResultadoViabilidade`, com `isPodeAgendar()` derivado, e separar os pipelines por instrumento com nova interface `ValidadorAgendamento` (sem `Instrumento` como parâmetro).

A justificativa é:
- **Consistência de modelo:** `valido` em `ResultadoViabilidade` é um campo mutado por efeito colateral de `adicionarRestricao()`. Em `ResultadoAgendamento`, `isPodeAgendar()` é uma consulta pura à lista de restrições — elimina a possibilidade de estado inconsistente.
- **Early exit legível:** o loop no pipeline é `if (!resultado.isPodeAgendar()) break` — semântica clara, sem flag auxiliar.
- **Coesão do pipeline:** validadores de agendamento co-localizados com seus pipelines, em vez de espalhados em `validador/agendamento/`.

### 7.2 Alternativas descartadas

| Alternativa | Vantagens | Desvantagens | Motivo da não escolha |
|---|---|---|---|
| Adicionar `List<Aviso>` em `ResultadoViabilidade` | Menor criação de classes | Modelo híbrido com semântica mista; `valido` e `isPodeAgendar()` seriam campos separados, risco de inconsistência | Violaria a distinção clara entre os dois blocos |
| Reutilizar `Validador` com `Instrumento` para agendamento | Sem nova interface | `suporta(contexto, instrumento)` seria ignorada no instrumento — cada pipeline já sabe o instrumento; parâmetro redundante | Interface carregada com parâmetro sem uso nos validadores de agendamento |
| Pipeline único com discriminação interna por instrumento | Menos classes | Ordem de validadores não é explícita por instrumento; dificulta early exit eficaz | Contraria o princípio de que a ordem importa e deve ser óbvia pela construção do pipeline |

---

## 8. Design da solução

### 8.1 Visão geral

A mudança acontece em quatro camadas, na ordem de dependência:

1. **Modelo:** criar `Aviso`, mover `Restricao*` para `modelo/restricao/`, criar `ResultadoAgendamento`
2. **Response:** criar `AvaliacaoAgendamento`, atualizar `InstrumentoPix/Ted/Tef`
3. **Pipeline:** criar `ValidadorAgendamento`, `PipelineAgendamentoInstrumento`, três pipelines por instrumento com seus validadores
4. **Integração:** atualizar `ResultadoInstrumento`, `AvaliacaoResponseMapper`, `AvaliacaoOrquestrador`, mover `PipelineDataSelecionada`

### 8.2 Modelo interno — `ResultadoAgendamento`

Nova classe em `avaliacao.contexto`. Substitui `ResultadoViabilidade` no campo `agendamento` de `ResultadoInstrumento`.

Campos: `List<Restricao> restricoes` (imutável via `new ArrayList<>()`), `List<Aviso> avisos`, `LocalDate proximaDataDisponivel`.
Métodos: `adicionarRestricao(Restricao)`, `adicionarAviso(Aviso)`, `isPodeAgendar()` (retorna `restricoes.isEmpty()`), `temProximaData()` (retorna `proximaDataDisponivel != null`).

`isPodeAgendar()` nunca é um campo. Isso impede estado inconsistente entre `podeAgendar` e a lista de restrições.

### 8.3 Modelo de domínio — `Aviso` e migração de `Restricao*`

`Aviso` é um `record` simples em `avaliacao.modelo`:

```java
package com.staroscky.motordecisao.avaliacao.modelo;

import java.time.LocalDate;

public record Aviso(String codigo, LocalDate data, String descricao) {}
```

`Restricao`, `RestricaoGenerica` e `RestricaoQrcode` são movidos de `avaliacao.contexto` para `avaliacao.modelo.restricao`. O conteúdo dos records não muda — apenas o pacote. Após a migração, os arquivos originais em `avaliacao.contexto` são removidos.

Arquivos que importam `avaliacao.contexto.Restricao*` e precisam atualizar o import:
- `ResultadoViabilidade.java`
- `ResultadoViabilidadeDto.java`
- (validadores antigos serão deletados)

### 8.4 Response — `AvaliacaoAgendamento`

Novo record em `avaliacao.response`:

```java
package com.staroscky.motordecisao.avaliacao.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.Restricao;

import java.time.LocalDate;
import java.util.List;

public record AvaliacaoAgendamento(
    boolean podeAgendar,
    List<Restricao> restricoes,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    LocalDate proximaDataDisponivel,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<Aviso> avisos
) {}
```

`InstrumentoPix`, `InstrumentoTed` e `InstrumentoTef` substituem o campo `agendamento` de `ResultadoViabilidadeDto` por `AvaliacaoAgendamento`.

### 8.5 Pipeline — `ValidadorAgendamento` e `PipelineAgendamentoInstrumento`

```java
// avaliacao/pipeline/agendamento/ValidadorAgendamento.java
public interface ValidadorAgendamento {
    boolean suporta(AvaliacaoContexto contexto);
    void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado);
}

// avaliacao/pipeline/agendamento/PipelineAgendamentoInstrumento.java
public interface PipelineAgendamentoInstrumento {
    void executar(AvaliacaoContexto contexto);
}
```

`ValidadorAgendamento` não recebe `Instrumento` — cada pipeline já conhece o seu.

### 8.6 Pipeline PIX — ordem explícita e early exit

```java
// avaliacao/pipeline/agendamento/pix/PipelineAgendamentoPix.java
@Component
public class PipelineAgendamentoPix implements PipelineAgendamentoInstrumento {

    private final List<ValidadorAgendamento> validadores;

    public PipelineAgendamentoPix(
        AgendamentoPixQrcodeValidador agendamentoQrcode,
        AgendamentoPixValidador agendamento,
        ProximaDataPixValidador proximaData,
        AvisoIntervaloPixValidador avisoIntervalo
    ) {
        this.validadores = List.of(agendamentoQrcode, agendamento, proximaData, avisoIntervalo);
    }

    @Override
    public void executar(AvaliacaoContexto contexto) {
        ResultadoAgendamento resultado =
            contexto.getResultado(Instrumento.PIX).getAgendamento();

        for (ValidadorAgendamento validador : validadores) {
            if (!validador.suporta(contexto)) continue;
            validador.validar(contexto, resultado);
            if (!resultado.isPodeAgendar()) break;
        }
    }
}
```

Pipelines TED e TEF seguem a mesma estrutura, com três validadores cada (`Agendamento*`, `ProximaData*`, `AvisoIntervalo*` — sem o validador QRCODE que é específico de PIX).

### 8.7 Validadores PIX

**`AgendamentoPixQrcodeValidador`** (substitui `PermiteAgendamentoValidador`):
- `suporta()`: `contexto.getTipoCheckin() == TipoCheckin.QRCODE`
- `validar()`: se `!tipoPermiteAgendamento(tipo)`, adiciona `RestricaoQrcode("QR_CODE_NAO_PERMITE", ...)` em `resultado`
- Diferença em relação ao validador atual: recebe `ResultadoAgendamento` em vez de `ResultadoViabilidade`; não recebe `Instrumento` (PIX está implícito no pipeline)

**`AgendamentoPixValidador`** (novo — cobre chave e agconta):
- `suporta()`: `contexto.getTipoCheckin() != TipoCheckin.QRCODE` (aplica para chave e agconta)
- `validar()`: verifica regras gerais de agendamento PIX (elegibilidade do instrumento) — corpo é `TODO`

**`ProximaDataPixValidador`** (substitui `ProximaDataValidador`):
- `suporta()`: `resultado.isPodeAgendar() && !contexto.getResultado(Instrumento.PIX).getDataSelecionada().isValido()`
- A verificação de `isPodeAgendar()` vem **antes** de `dataSelecionada.isValido()` — respeita a invariante
- `validar()`: busca próxima data disponível (TODO upstream) e chama `resultado.setProximaDataDisponivel(proxima)`

**`AvisoIntervaloPixValidador`** (novo):
- `suporta()`: `contexto.getResultado(Instrumento.PIX).getAgendamento().temProximaData()`
- `validar()`: verifica datas entre `request.data()` e `resultado.getProximaDataDisponivel()`, adiciona avisos via `resultado.adicionarAviso(...)`
- Nunca adiciona restrições — apenas avisos

### 8.8 Early exit em `PipelineDataSelecionada`

O loop atual usa `stream().filter().forEach()`, que não permite interrupção. A correção substitui por um for loop com break explícito, espelhando o padrão dos pipelines de agendamento:

```java
// pipeline/data/PipelineDataSelecionada.java
public void executar(AvaliacaoContexto contexto) {
    for (Instrumento instrumento : contexto.getInstrumentos()) {
        ResultadoViabilidade resultado =
            contexto.getResultado(instrumento).getDataSelecionada();

        for (Validador validador : validadores) {
            if (!validador.suporta(contexto, instrumento)) continue;
            validador.validar(contexto, instrumento, resultado);
            if (!resultado.isValido()) break;
        }
    }
}
```

A condição de parada é `!resultado.isValido()` — equivalente ao `!resultado.isPodeAgendar()` dos pipelines de agendamento, mas usando a semântica de `ResultadoViabilidade`.

### 8.10 Orquestrador — `PipelineAgendamento`

`PipelineAgendamento` sai de `avaliacao.pipeline` e vai para `avaliacao.pipeline.agendamento`. Torna-se orquestrador que despacha por instrumento:

```java
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

### 8.11 Mapper — `AvaliacaoResponseMapper`

Adicionar método `toAvaliacaoAgendamento(ResultadoAgendamento)` e utilizá-lo nos três `switch` cases:

```java
private AvaliacaoAgendamento toAvaliacaoAgendamento(ResultadoAgendamento ag) {
    return new AvaliacaoAgendamento(
        ag.isPodeAgendar(),
        List.copyOf(ag.getRestricoes()),
        ag.getProximaDataDisponivel(),
        List.copyOf(ag.getAvisos())
    );
}
```

`List.copyOf()` em `avisos` retorna lista vazia quando não há avisos — `@JsonInclude(NON_EMPTY)` cuida da omissão no JSON.

### 8.12 Contratos e payloads

Quatro cenários de response para o bloco `agendamento`:

```json
// 1. Pode agendar, data selecionada válida
{ "podeAgendar": true, "restricoes": [] }

// 2. Não pode agendar (ex.: QR Code COB)
{ "podeAgendar": false, "restricoes": [{ "motivo": "QR_CODE_NAO_PERMITE", "contexto": { "tipo": "COB" } }] }

// 3. Pode agendar, data inválida, intervalo limpo
{ "podeAgendar": true, "restricoes": [], "proximaDataDisponivel": "2026-06-09" }

// 4. Pode agendar, data inválida, intervalo com problemas
{
  "podeAgendar": true, "restricoes": [], "proximaDataDisponivel": "2026-06-12",
  "avisos": [
    { "codigo": "FERIADO_NACIONAL", "data": "2026-06-10", "descricao": "Corpus Christi" },
    { "codigo": "FIM_DE_SEMANA", "data": "2026-06-11", "descricao": "Domingo" }
  ]
}
```

---

## 9. Fluxos técnicos

```text
POST /v1/avaliacao
    └── AvaliacaoOrquestrador.executar()
            ├── PipelineDataSelecionada.executar()   [early exit: if (!resultado.isValido()) break]
            └── PipelineAgendamento.executar()        [orquestrador]
                    ├── PipelineAgendamentoPix.executar()
                    │       ├── AgendamentoPixQrcodeValidador  [suporta QRCODE → bloqueante]
                    │       │       └── se restrição → BREAK
                    │       ├── AgendamentoPixValidador        [suporta chave/agconta → bloqueante]
                    │       │       └── se restrição → BREAK
                    │       ├── ProximaDataPixValidador        [suporta se podeAgendar && dataSelecionada inválida → I/O]
                    │       └── AvisoIntervaloPixValidador     [suporta se proximaData populada → sem I/O]
                    ├── PipelineAgendamentoTed.executar()
                    │       ├── AgendamentoTedValidador
                    │       ├── ProximaDataTedValidador
                    │       └── AvisoIntervaloTedValidador
                    └── PipelineAgendamentoTef.executar()
                            ├── AgendamentoTefValidador
                            ├── ProximaDataTefValidador
                            └── AvisoIntervaloTefValidador
```

```text
Early exit — restrição bloqueante (ex.: QRCODE COB):
    AgendamentoPixQrcodeValidador.suporta() → true
    AgendamentoPixQrcodeValidador.validar() → adicionarRestricao(RestricaoQrcode)
    resultado.isPodeAgendar() → false → BREAK
    [AgendamentoPixValidador, ProximaDataPixValidador, AvisoIntervaloPixValidador não executam]
```

---

## 10. Arquivos afetados

| Arquivo | Tipo | Mudança |
|---|---|---|
| `avaliacao/modelo/Aviso.java` | Criar | Record com `codigo`, `data`, `descricao` |
| `avaliacao/modelo/restricao/Restricao.java` | Criar (mover) | Sealed interface movida de `contexto/` |
| `avaliacao/modelo/restricao/RestricaoGenerica.java` | Criar (mover) | Record movido de `contexto/` |
| `avaliacao/modelo/restricao/RestricaoQrcode.java` | Criar (mover) | Record movido de `contexto/` |
| `avaliacao/contexto/Restricao.java` | Deletar | Substituído por `modelo/restricao/Restricao.java` |
| `avaliacao/contexto/RestricaoGenerica.java` | Deletar | Substituído por `modelo/restricao/RestricaoGenerica.java` |
| `avaliacao/contexto/RestricaoQrcode.java` | Deletar | Substituído por `modelo/restricao/RestricaoQrcode.java` |
| `avaliacao/contexto/ResultadoAgendamento.java` | Criar | Modelo interno de agendamento com `isPodeAgendar()` derivado |
| `avaliacao/contexto/ResultadoViabilidade.java` | Modificar | Atualizar import de `Restricao` para `modelo.restricao` |
| `avaliacao/contexto/ResultadoInstrumento.java` | Modificar | `ResultadoViabilidade agendamento` → `ResultadoAgendamento agendamento` |
| `avaliacao/response/AvaliacaoAgendamento.java` | Criar | Record response com serialização condicional |
| `avaliacao/response/InstrumentoPix.java` | Modificar | Campo `agendamento` de `ResultadoViabilidadeDto` para `AvaliacaoAgendamento` |
| `avaliacao/response/InstrumentoTed.java` | Modificar | Mesmo que PIX |
| `avaliacao/response/InstrumentoTef.java` | Modificar | Mesmo que PIX |
| `avaliacao/response/ResultadoViabilidadeDto.java` | Modificar | Atualizar import de `Restricao` para `modelo.restricao` |
| `avaliacao/pipeline/agendamento/ValidadorAgendamento.java` | Criar | Interface de validadores de agendamento |
| `avaliacao/pipeline/agendamento/PipelineAgendamentoInstrumento.java` | Criar | Interface dos pipelines por instrumento |
| `avaliacao/pipeline/agendamento/PipelineAgendamento.java` | Criar (mover+evoluir) | Orquestrador que despacha por instrumento |
| `avaliacao/pipeline/PipelineAgendamento.java` | Deletar | Substituído pela versão em `pipeline/agendamento/` |
| `avaliacao/pipeline/agendamento/pix/PipelineAgendamentoPix.java` | Criar | Pipeline PIX com early exit |
| `avaliacao/pipeline/agendamento/pix/AgendamentoPixQrcodeValidador.java` | Criar | Substitui `PermiteAgendamentoValidador` |
| `avaliacao/pipeline/agendamento/pix/AgendamentoPixValidador.java` | Criar | Regra geral PIX para chave e agconta |
| `avaliacao/pipeline/agendamento/pix/ProximaDataPixValidador.java` | Criar | Substitui `ProximaDataValidador`; `suporta()` corrigido |
| `avaliacao/pipeline/agendamento/pix/AvisoIntervaloPixValidador.java` | Criar | Novo; verifica intervalo, adiciona avisos |
| `avaliacao/pipeline/agendamento/ted/PipelineAgendamentoTed.java` | Criar | Pipeline TED |
| `avaliacao/pipeline/agendamento/ted/AgendamentoTedValidador.java` | Criar | Regra geral TED |
| `avaliacao/pipeline/agendamento/ted/ProximaDataTedValidador.java` | Criar | ProximaData para TED |
| `avaliacao/pipeline/agendamento/ted/AvisoIntervaloTedValidador.java` | Criar | AvisoIntervalo para TED |
| `avaliacao/pipeline/agendamento/tef/PipelineAgendamentoTef.java` | Criar | Pipeline TEF |
| `avaliacao/pipeline/agendamento/tef/AgendamentoTefValidador.java` | Criar | Regra geral TEF |
| `avaliacao/pipeline/agendamento/tef/ProximaDataTefValidador.java` | Criar | ProximaData para TEF |
| `avaliacao/pipeline/agendamento/tef/AvisoIntervaloTefValidador.java` | Criar | AvisoIntervalo para TEF |
| `avaliacao/pipeline/data/PipelineDataSelecionada.java` | Criar (mover + modificar) | Movido de `pipeline/`; loop substituído por `for` com `if (!resultado.isValido()) break` |
| `avaliacao/pipeline/PipelineDataSelecionada.java` | Deletar | Substituído pela versão em `pipeline/data/` |
| `avaliacao/mapper/AvaliacaoResponseMapper.java` | Modificar | Adicionar `toAvaliacaoAgendamento(ResultadoAgendamento)` |
| `avaliacao/AvaliacaoOrquestrador.java` | Modificar | Atualizar imports para novos pacotes |
| `avaliacao/validador/agendamento/PermiteAgendamentoValidador.java` | Deletar | Substituído por `AgendamentoPixQrcodeValidador` |
| `avaliacao/validador/agendamento/ProximaDataValidador.java` | Deletar | Substituído pelos `ProximaData*Validador` por instrumento |
| Testes de `PermiteAgendamentoValidador` | Modificar | Atualizar para `AgendamentoPixQrcodeValidador` e `ResultadoAgendamento` |

---

## 11. Requisitos não funcionais

| Categoria | Requisito | Meta ou critério |
|---|---|---|
| Confiabilidade | `isPodeAgendar()` nunca pode ter estado inconsistente com `restricoes` | Garantido pela ausência de setter — apenas `adicionarRestricao()` altera a lista |
| Confiabilidade | Pipeline não deve lançar exceção por instrumento ausente em `pipelines` map | `PipelineAgendamento` deve cobrir todos os valores do enum `Instrumento` |
| Performance | Early exit evita I/O desnecessário quando restrição simples bloqueia | Validadores sem I/O devem ser os primeiros na lista de cada pipeline |
| Compatibilidade | O campo `agendamento` no response muda de schema — breaking change para consumers | Verificar com equipe de frontend antes de deploys em ambientes compartilhados |
| Observabilidade | Sem alteração de logging nesta entrega | MDC já propagado pelo `core/` cobre rastreabilidade das chamadas |

---

## 12. Estratégia de rollout ou migração

Breaking change no campo `agendamento` do response: `ResultadoViabilidadeDto` → `AvaliacaoAgendamento`. O schema muda de `{ valido, restricoes, ordemMelhor, limite }` para `{ podeAgendar, restricoes, proximaDataDisponivel?, avisos? }`.

- Deploy deve ser coordenado com o(s) consumer(s) do endpoint
- Não há compatibilidade retroativa possível no mesmo endpoint sem versionamento
- `ordemMelhor` e `limite` deixam de existir no bloco `agendamento` — verificar se algum consumer os utiliza

---

## 13. Estratégia de validação

- **Testes unitários:** um teste por validador (`suporta()` com contextos válido e inválido; `validar()` para cada caminho de negócio). `ResultadoAgendamentoTest` verifica que `isPodeAgendar()` é derivado corretamente.
- **Testes de pipeline:** verificar que o early exit acontece na ordem correta; verificar que avisos não interrompem.
- **Testes de mapper:** verificar serialização de `AvaliacaoAgendamento` nos quatro cenários — em especial a ausência de `proximaDataDisponivel` e `avisos` quando não aplicável.
- **Testes e2e:** chamar `POST /v1/avaliacao` com QRCODE COB e verificar response completo com `podeAgendar: false`.

---

## 14. Critérios de aceite

- [ ] `ResultadoAgendamento.isPodeAgendar()` retorna `true` quando `restricoes` está vazio e `false` quando não está
- [ ] `dataSelecionada.valido: true` e `agendamento.podeAgendar: false` coexistem corretamente no response de QRCODE COB
- [ ] `proximaDataDisponivel` ausente no JSON quando `dataSelecionada` é válida
- [ ] `avisos` ausente no JSON quando o intervalo está limpo
- [ ] `restricoes` presente no JSON mesmo quando vazio (`[]`)
- [ ] `PipelineDataSelecionada` interrompe por instrumento ao encontrar a primeira restrição (`!resultado.isValido()`)
- [ ] Pipeline para PIX interrompe após restrição sem executar `ProximaDataPixValidador`
- [ ] `ProximaDataPixValidador.suporta()` retorna `false` quando `podeAgendar: false`, independente de `dataSelecionada`
- [ ] `AvisoIntervaloPixValidador.suporta()` retorna `false` quando `proximaDataDisponivel` não foi populada
- [ ] Build compila sem erros após deleção das classes em `contexto/Restricao*`
- [ ] `PermiteAgendamentoValidadorTest` atualizado e passando com a nova implementação
- [ ] Todos os testes existentes passam sem regressão

---

## 15. Riscos e observações

- **Breaking change no response:** o campo `agendamento` muda de schema. Coordenar com consumers antes do deploy.
- **Stub de I/O:** `ProximaData*Validador` e `AvisoIntervalo*Validador` terão corpo `TODO` para a lógica de negócio — em produção, sem a implementação real, `proximaDataDisponivel` nunca será populada e `avisos` nunca aparecerá. O comportamento é correto mas incompleto.
- **Deleção de arquivos:** `contexto/Restricao*`, `validador/agendamento/Permite*` e `ProximaData*` devem ser deletados para evitar ambiguidade de pacotes. Compilação garante que não há referências pendentes.
- **`Instrumento` não coberto:** se um novo `Instrumento` for adicionado ao enum, `PipelineAgendamento` lançará `NullPointerException` ao tentar `pipelines.get(instrumento)`. Considerar uso de `EnumMap` com validação no construtor.

## 16. Questões em aberto

- Qual a regra de negócio de `AgendamentoPixValidador` para chave e agconta? (regras de horário, limite de antecedência etc.) — diferida para implementação posterior.
- Qual o upstream responsável por retornar a próxima data disponível? — diferido.
- Quais são os códigos de aviso oficiais além de `FERIADO_NACIONAL` e `FIM_DE_SEMANA`? — diferido.
- `limite` e `ordemMelhor` eram campos de `ResultadoViabilidadeDto agendamento` — algum consumer os usa? Confirmar antes do deploy.
