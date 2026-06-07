# SPEC — BFF de Avaliação de Instrumentos de Pagamento

## 1. Contexto da solicitação

### 1.1 História ou tarefa do usuário

- **Solicitante:** Desenvolvedor do projeto `motordecisao`
- **Tipo:** feature (primeiro slice de negócio)
- **História:** Como sistema consumer, quero chamar `POST /v1/avaliacao` com um `checkinId`, uma data e uma conta de origem, e receber a lista de instrumentos de pagamento disponíveis com a viabilidade para data selecionada e para agendamento, para que eu possa apresentar as opções ao usuário antes de executar a transação.
- **Valor esperado:** Centraliza a lógica de elegibilidade de instrumentos (PIX, TED, TEF), eliminando replicação nos sistemas consumer e garantindo consistência nas regras de avaliação.

### 1.2 Problema observado

Não há nenhum slice de negócio no projeto — apenas a infraestrutura transversal do `core/` está em vigor. Os sistemas consumer que precisam decidir qual instrumento de pagamento usar para uma transação não têm onde buscar essa avaliação de forma centralizada. O resultado é lógica de elegibilidade duplicada e inconsistente em cada sistema.

### 1.3 Objetivo da entrega

A entrega está completa quando:
- `POST /v1/avaliacao` responde com a lista de instrumentos disponíveis para qualquer tipo de checkin válido
- Cada instrumento carrega dois blocos de viabilidade: `dataSelecionada` e `agendamento`
- Restrições específicas por instrumento e contexto são retornadas de forma estruturada
- A derivação de instrumentos segue a tabela definida no PRD (qrcode/chaves_pix → PIX; agconta+Itaú → PIX+TEF; agconta+outro → PIX+TED)

---

## 2. Objetivo técnico

Criar o slice vertical `avaliacao/` em `com.staroscky.motordecisao.avaliacao`, implementando o endpoint BFF com dois pipelines sequenciais de avaliação, validadores autocontidos com `suporta()`, contexto compartilhado por pipeline, resolver de instrumentos e mapper de resposta. O slice deve integrar com a infraestrutura `core/` existente (Feign para o upstream de ISPB, Logbook + MDC para observabilidade).

---

## 3. Estado atual

| Arquivo | Conteúdo atual |
|---|---|
| `src/main/java/.../MotordecisaoApplication.java` | `@SpringBootApplication` sem slices de negócio |
| `src/main/java/.../core/config/FeignConfig.java` | `@EnableFeignClients(basePackages = "com.staroscky.motordecisao")` — qualquer `@FeignClient` em qualquer subpacote é detectado automaticamente |
| `src/main/java/.../core/filter/CorrelationHeaderFilter.java` | Propaga `x-correlationId` e `x-flowId` para o MDC — todos os requests ao BFF já carregam rastreabilidade |
| `src/main/java/.../core/feign/CorrelationFeignInterceptor.java` | Injeta `x-correlationId` e `x-flowId` do MDC em toda chamada Feign de saída — `InstituicaoClient` receberá esses headers automaticamente |
| `pom.xml` | Spring Boot 3.5.14, Spring Cloud 2025.0.2, OpenFeign, Jackson (via `spring-boot-starter-web`), Logbook 4.0.4 — sem dependências novas necessárias para este slice |

Não existe nenhum pacote de negócio. O `avaliacao/` será o primeiro slice vertical do projeto.

---

## 4. Escopo da solução

### 4.1 O que muda

| Área | Estado atual | Estado esperado | Impacto |
|---|---|---|---|
| `avaliacao/` (pacote inteiro) | Não existe | Criar com todos os componentes do slice | Alto |
| `avaliacao/AvaliacaoController.java` | Não existe | Criar: `@RestController` com `POST /v1/avaliacao` | Alto |
| `avaliacao/AvaliacaoService.java` | Não existe | Criar: orquestra parse → ISPB → resolve → contexto → pipelines → mapper | Alto |
| `avaliacao/AvaliacaoOrquestrador.java` | Não existe | Criar: executa os dois pipelines em sequência | Médio |
| `avaliacao/request/` | Não existe | Criar: `AvaliacaoRequest`, `QrcodeAvaliacaoRequest`, `ChavePixAvaliacaoRequest`, `AgcontaAvaliacaoRequest`, `DadosOrigem`, `DadosQrcode` | Alto |
| `avaliacao/response/` | Não existe | Criar: `AvaliacaoResponse`, `InstrumentoAvaliado`, `InstrumentoPix`, `InstrumentoTef`, `InstrumentoTed`, `ResultadoViabilidadeDto` | Alto |
| `avaliacao/contexto/` | Não existe | Criar: `AvaliacaoContexto`, `ResultadoInstrumento`, `ResultadoViabilidade`, `Restricao`, `RestricaoGenerica`, `RestricaoQrcode`, `Limite` | Alto |
| `avaliacao/pipeline/` | Não existe | Criar: `PipelineDataSelecionada`, `PipelineAgendamento` | Alto |
| `avaliacao/validador/` | Não existe | Criar: interface `Validador` + 5 validadores iniciais | Alto |
| `avaliacao/resolver/` | Não existe | Criar: `TipoCheckin`, `Instrumento`, `CheckinIdParser`, `InstrumentoResolver` | Alto |
| `avaliacao/upstream/InstituicaoClient.java` | Não existe | Criar: `@FeignClient` para busca do ISPB da instituição de destino | Alto |
| `avaliacao/mapper/AvaliacaoResponseMapper.java` | Não existe | Criar: converte `AvaliacaoContexto` → `AvaliacaoResponse` | Médio |

### 4.2 O que não muda

- Nenhum arquivo do pacote `core/` é modificado
- `pom.xml` não recebe novas dependências
- `application.yaml` não recebe configurações adicionais para este slice
- `MotordecisaoApplication.java` não é alterado
- A implementação interna de cada regra de negócio dos validadores (horários, limites, status de instituição) está fora do escopo — os validadores serão criados com a estrutura correta e lógica de placeholder

### 4.3 Restrições e pressupostos

- Java 21 — `sealed interface` e `record` são utilizados conforme o design do PRD
- Spring Boot 3.5 + Jackson 2.17 — `@JsonTypeInfo(use = Id.DEDUCTION)` está disponível e funciona com `sealed interface`
- `@EnableFeignClients` já aponta para `"com.staroscky.motordecisao"` — `InstituicaoClient` em `avaliacao/upstream/` será detectado sem configuração adicional
- `CorrelationFeignInterceptor` propaga os headers de correlação para todas as chamadas Feign, incluindo `InstituicaoClient`
- A URL base do `InstituicaoClient` é definida via `application.yaml` — o contrato exato do upstream (path, formato de resposta) é um **pressuposto externo** a ser confirmado
- DEDUCTION para `InstrumentoAvaliado` na resposta: `InstrumentoPix` e `InstrumentoTef` possuem exatamente os mesmos campos — DEDUCTION não consegue distingui-los na deserialização. Como o servidor apenas serializa a resposta (nunca a deserializa), isso não causa problema de runtime. Ver seção 15 para detalhes.
- Sem Lombok — classes e records com Java puro

---

## 5. Requisitos funcionais

| ID | Requisito funcional | Prioridade | Origem |
|---|---|---|---|
| RF-01 | O sistema deve aceitar `POST /v1/avaliacao` com request polimórfico (qrcode, chaves_pix, agconta) via Jackson DEDUCTION | must | PRD |
| RF-02 | O sistema deve extrair o `TipoCheckin` do `checkinId` pelo segundo segmento (`checkin:<tipo>:...`) | must | PRD |
| RF-03 | O sistema deve retornar HTTP 400 para `checkinId` com tipo desconhecido ou formato inválido | must | PRD |
| RF-04 | O sistema deve buscar o ISPB da instituição de destino via `InstituicaoClient` a partir do `checkinId` | must | PRD |
| RF-05 | O sistema deve derivar os instrumentos disponíveis via `InstrumentoResolver` usando `TipoCheckin` e ISPB | must | PRD |
| RF-06 | O sistema deve avaliar a viabilidade de cada instrumento para a data selecionada via `PipelineDataSelecionada` | must | PRD |
| RF-07 | O sistema deve avaliar a viabilidade de cada instrumento para agendamento via `PipelineAgendamento`, com acesso ao resultado do pipeline anterior | must | PRD |
| RF-08 | Cada validador deve implementar `suporta(contexto, instrumento)` — o pipeline não deve conter lógica de filtragem por instrumento ou tipo | must | PRD |
| RF-09 | O sistema deve retornar resposta polimórfica por instrumento (`InstrumentoPix`, `InstrumentoTef`, `InstrumentoTed`) | must | PRD |
| RF-10 | `InstrumentoTed` deve incluir a lista de `finalidades` disponíveis | must | PRD |
| RF-11 | Restrições devem ser polimórficas — `RestricaoGenerica` (só `motivo`) e `RestricaoQrcode` (`motivo` + `contexto`) | must | PRD |
| RF-12 | `adicionarRestricao()` em `ResultadoViabilidade` deve automaticamente marcar `valido = false` | must | PRD |
| RF-13 | `AvaliacaoContexto.isOnline()` deve retornar `true` quando `data` for igual à data atual | must | PRD |
| RF-14 | `ProximaDataValidador` deve executar somente quando `dataSelecionada.isValido() == false` para aquele instrumento | must | PRD |

---

## 6. Cenários e fluxos esperados

### 6.1 Cenários principais

- **QR Code (qualquer ISPB):** `checkinId: checkin:qrcode:1:uuid` → instrumentos: `[PIX]` → `InstrumentoPix` na resposta
- **Chave Pix (qualquer ISPB):** `checkinId: checkin:chaves_pix:1:uuid` → instrumentos: `[PIX]` → `InstrumentoPix` na resposta
- **Conta Itaú:** `checkinId: checkin:agconta:1:uuid`, ISPB `60701190` → instrumentos: `[PIX, TEF]` → `[InstrumentoPix, InstrumentoTef]`
- **Conta outro banco:** `checkinId: checkin:agconta:1:uuid`, ISPB diferente → instrumentos: `[PIX, TED]` → `[InstrumentoPix, InstrumentoTed]`
- **QR Code COB sem agendamento:** `PermiteAgendamentoValidador` suporta `PIX + QRCODE`; valida o tipo do QR Code e adiciona restrição `QR_CODE_NAO_PERMITE` com `contexto.tipo = "COB"` se não permitido
- **Data selecionada indisponível:** algum validador do `PipelineDataSelecionada` adiciona restrição → `dataSelecionada.valido = false` → `PipelineAgendamento` executa `ProximaDataValidador` para aquele instrumento (pois `suporta()` checa `!dataSelecionada.isValido()`)
- **Data futura:** `isOnline() = false` → `HorarioPermitidoValidador` não executa (seu `suporta()` retorna `false`)

### 6.2 Edge cases e falhas esperadas

- **`checkinId` inválido (menos de 2 segmentos):** `CheckinIdParser` lança `CheckinIdInvalidoException` → controller deve mapear para HTTP 400
- **`checkinId` com tipo desconhecido** (ex: `checkin:boleto:1:uuid`): `switch` do parser cai no `default` → `CheckinIdInvalidoException` → HTTP 400
- **`InstituicaoClient` falha:** comportamento de fallback não definido neste escopo — deve ser tratado como exceção não mapeada (HTTP 500) até que retry/circuit breaker sejam definidos
- **`data` no passado:** `isOnline()` retorna `false` (data anterior à hoje) — lógica de negócio de cada validador define o que fazer; o contexto expõe `isOnline()` como helper
- **Instrumento sem restrições:** `restricoes` é lista vazia, `valido = true` — resposta retorna `"restricoes": []`
- **`QrcodeAvaliacaoRequest` sem campo `qrcode` no JSON:** Jackson DEDUCTION infere `ChavePixAvaliacaoRequest` (mesmos campos) — o client deve garantir que o campo `qrcode` esteja presente para QR Codes

---

## 7. Alternativas consideradas

### 7.1 Alternativa escolhida

**Dois pipelines sequenciais nomeados + validadores com `suporta()`**

O `PipelineDataSelecionada` executa primeiro e acumula resultados no `AvaliacaoContexto`. O `PipelineAgendamento` executa depois, com acesso ao estado já produzido pelo pipeline anterior via contexto — sem acoplamento direto entre eles. Cada validador declara autonomamente em quais condições deve executar.

### 7.2 Alternativas descartadas

| Alternativa | Vantagens | Desvantagens | Motivo da não escolha |
|---|---|---|---|
| Campo `tipoCheckin` explícito no request | Discriminador óbvio no payload | O tipo já está no `checkinId`; campo duplicado gera inconsistência se divergirem | DEDUCTION elimina redundância; tipo é inferível sem ambiguidade pelo segundo segmento do ID |
| Pipeline único com `if (instrumento == PIX && isOnline())` | Menos classes | Condicionais espalhadas no pipeline; cada novo validador exige alterar o pipeline | `suporta()` torna cada validador autocontido e testável isoladamente sem tocar no pipeline |
| Parâmetro direto entre pipelines (sem contexto) | Menos acoplamento ao contexto | Pipeline de agendamento precisaria receber o resultado do pipeline de data como parâmetro — acoplamento explícito de assinatura | Handoff via contexto elimina esse acoplamento; pipelines não se conhecem |
| `@JsonTypeInfo(use = NAME, property = "tipo")` para `InstrumentoAvaliado` | Discriminador explícito, sem ambiguidade | Requer campo `tipo` como discriminador Jackson (sobrepõe o campo de negócio) | O servidor nunca deserializa sua própria resposta; DEDUCTION funciona corretamente na serialização. Caso clientes precisem deserializar, avaliar `NAME` com `property = "tipo"` |

---

## 8. Design da solução

### 8.1 Visão geral da abordagem

O fluxo dentro do service é linear e sem paralelismo nesta entrega:

```
AvaliacaoController
  → AvaliacaoService
      → CheckinIdParser           (parse do checkinId → TipoCheckin)
      → InstituicaoClient         (Feign upstream → ISPB)
      → InstrumentoResolver       (TipoCheckin + ISPB → Set<Instrumento>)
      → new AvaliacaoContexto     (agrega request + tipo + ispb + instrumentos)
      → AvaliacaoOrquestrador
          → PipelineDataSelecionada  (valida data selecionada por instrumento)
          → PipelineAgendamento      (valida agendamento, lê resultado do pipeline anterior)
      → AvaliacaoResponseMapper   (AvaliacaoContexto → AvaliacaoResponse)
  ← AvaliacaoResponse (HTTP 200)
```

### 8.2 `avaliacao/request/` — Request polimórfico

```java
// Jackson infere o subtipo pelos campos presentes no JSON.
// QrcodeAvaliacaoRequest é identificado pela presença do campo `qrcode`.
// ChavePixAvaliacaoRequest e AgcontaAvaliacaoRequest são diferenciados
// pelo segundo segmento do checkinId — não por campos Jackson.

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(QrcodeAvaliacaoRequest.class),
    @JsonSubTypes.Type(ChavePixAvaliacaoRequest.class),
    @JsonSubTypes.Type(AgcontaAvaliacaoRequest.class)
})
public sealed interface AvaliacaoRequest
    permits QrcodeAvaliacaoRequest, ChavePixAvaliacaoRequest, AgcontaAvaliacaoRequest {

    String checkinId();
    LocalDate data();
    DadosOrigem origem();
}

public record QrcodeAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem,
    DadosQrcode qrcode
) implements AvaliacaoRequest {}

public record ChavePixAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem
) implements AvaliacaoRequest {}

public record AgcontaAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem
) implements AvaliacaoRequest {}

public record DadosOrigem(String agencia, String conta) {}

public record DadosQrcode(String emv, String tipo) {}
```

> **Nota de implementação:** `ChavePixAvaliacaoRequest` e `AgcontaAvaliacaoRequest` têm exatamente os mesmos campos. Jackson DEDUCTION não consegue distingui-los no momento da desserialização. O tipo real é sempre inferido pelo `CheckinIdParser` lendo o segundo segmento do `checkinId`. O subtipo Jackson desserializado para ambos será o mesmo — o service não depende do subtipo Java para esses dois, apenas do `TipoCheckin` derivado pelo parser.

### 8.3 `avaliacao/resolver/` — Enums, Parser e Resolver

```java
public enum TipoCheckin { QRCODE, CHAVE_PIX, AGCONTA }

public enum Instrumento { PIX, TED, TEF }

public final class CheckinIdParser {
    // Formato esperado: checkin:<tipo>:<versao>:<uuid>
    public static TipoCheckin parse(String checkinId) {
        String[] partes = checkinId.split(":");
        if (partes.length < 2) throw new CheckinIdInvalidoException(checkinId);
        return switch (partes[1]) {
            case "qrcode"     -> TipoCheckin.QRCODE;
            case "chaves_pix" -> TipoCheckin.CHAVE_PIX;
            case "agconta"    -> TipoCheckin.AGCONTA;
            default           -> throw new CheckinIdInvalidoException(checkinId);
        };
    }
}

public final class InstrumentoResolver {
    private static final String ISPB_ITAU = "60701190";

    public static Set<Instrumento> resolver(TipoCheckin tipo, String ispb) {
        return switch (tipo) {
            case QRCODE, CHAVE_PIX -> Set.of(Instrumento.PIX);
            case AGCONTA -> ispb.equals(ISPB_ITAU)
                ? Set.of(Instrumento.PIX, Instrumento.TEF)
                : Set.of(Instrumento.PIX, Instrumento.TED);
        };
    }
}
```

`CheckinIdInvalidoException` deve ser uma `RuntimeException` mapeada para HTTP 400 no controller ou via `@ControllerAdvice`.

### 8.4 `avaliacao/contexto/` — Contexto e modelo interno

```java
public class AvaliacaoContexto {
    private final AvaliacaoRequest request;
    private final TipoCheckin tipoCheckin;
    private final String ispb;
    private final Set<Instrumento> instrumentos;
    private final Map<Instrumento, ResultadoInstrumento> resultados =
        new EnumMap<>(Instrumento.class);

    // construtor público com todos os campos finais

    public ResultadoInstrumento getResultado(Instrumento instrumento) {
        return resultados.computeIfAbsent(instrumento, k -> new ResultadoInstrumento());
    }

    public boolean isOnline() {
        return !request.data().isAfter(LocalDate.now());
    }

    // getters para todos os campos
}

public class ResultadoInstrumento {
    private final ResultadoViabilidade dataSelecionada = new ResultadoViabilidade();
    private final ResultadoViabilidade agendamento     = new ResultadoViabilidade();
    // getters
}

public class ResultadoViabilidade {
    private boolean valido = true;
    private int ordemMelhor;
    private Limite limite;
    private final List<Restricao> restricoes = new ArrayList<>();

    public void adicionarRestricao(Restricao restricao) {
        this.restricoes.add(restricao);
        this.valido = false;
    }
    // getters e setters para ordemMelhor, limite
}

public record Limite(int qtdDiasAlt) {}
```

**Tipos de restrição:**

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(RestricaoGenerica.class),
    @JsonSubTypes.Type(RestricaoQrcode.class)
})
public sealed interface Restricao permits RestricaoGenerica, RestricaoQrcode {}

public record RestricaoGenerica(String motivo) implements Restricao {}

public record RestricaoQrcode(
    String motivo,
    ContextoQrcode contexto
) implements Restricao {
    public record ContextoQrcode(String tipo) {}
}
```

### 8.5 `avaliacao/validador/` — Interface e validadores

```java
public interface Validador {
    boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento);
    void validar(AvaliacaoContexto contexto, Instrumento instrumento, ResultadoViabilidade resultado);
}
```

**Validadores do `PipelineDataSelecionada`** (registrados na ordem declarada no construtor):

| Classe | `suporta()` | Comportamento |
|---|---|---|
| `StatusInstituicaoValidador` | sempre | Verifica status operacional da instituição destino |
| `LimiteInstituicaoValidador` | sempre | Verifica limites da instituição para o instrumento |
| `HorarioPermitidoValidador` | `isOnline()` | Verifica janela de horário para data atual |
| `LimiteDiarioValidador` | sempre | Verifica limite diário por instrumento |
| `DataValidaValidador` | sempre | Verifica se a data é válida para o instrumento |

**Validadores do `PipelineAgendamento`:**

| Classe | `suporta()` | Comportamento |
|---|---|---|
| `PermiteAgendamentoValidador` | `PIX + QRCODE` | Valida se o tipo de QR Code permite agendamento |
| `ProximaDataValidador` | `!dataSelecionada.isValido()` | Busca próxima data disponível quando a selecionada é inviável |

Cada validador é anotado com `@Component`. Os pipelines recebem os validadores por injeção de construtor na ordem desejada, não por auto-wiring de lista.

### 8.6 `avaliacao/pipeline/` — Dois pipelines sequenciais

```java
@Component
public class PipelineDataSelecionada {
    private final List<Validador> validadores;

    public PipelineDataSelecionada(
        StatusInstituicaoValidador statusInstituicao,
        LimiteInstituicaoValidador limiteInstituicao,
        HorarioPermitidoValidador horario,
        LimiteDiarioValidador limiteDiario,
        DataValidaValidador dataValida
    ) {
        this.validadores = List.of(statusInstituicao, limiteInstituicao,
                                   horario, limiteDiario, dataValida);
    }

    public void executar(AvaliacaoContexto contexto) {
        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado =
                contexto.getResultado(instrumento).getDataSelecionada();
            validadores.stream()
                .filter(v -> v.suporta(contexto, instrumento))
                .forEach(v -> v.validar(contexto, instrumento, resultado));
        }
    }
}

@Component
public class PipelineAgendamento {
    private final List<Validador> validadores;

    public PipelineAgendamento(
        PermiteAgendamentoValidador permiteAgendamento,
        ProximaDataValidador proximaData
    ) {
        this.validadores = List.of(permiteAgendamento, proximaData);
    }

    public void executar(AvaliacaoContexto contexto) {
        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado =
                contexto.getResultado(instrumento).getAgendamento();
            validadores.stream()
                .filter(v -> v.suporta(contexto, instrumento))
                .forEach(v -> v.validar(contexto, instrumento, resultado));
        }
    }
}
```

### 8.7 `avaliacao/response/` — Response polimórfico

```java
public record AvaliacaoResponse(List<InstrumentoAvaliado> instrumentos) {}

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(InstrumentoPix.class),
    @JsonSubTypes.Type(InstrumentoTef.class),
    @JsonSubTypes.Type(InstrumentoTed.class)
})
public sealed interface InstrumentoAvaliado
    permits InstrumentoPix, InstrumentoTef, InstrumentoTed {}

public record InstrumentoPix(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento
) implements InstrumentoAvaliado {}

public record InstrumentoTef(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento
) implements InstrumentoAvaliado {}

public record InstrumentoTed(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento,
    List<FinalidadeTed> finalidades
) implements InstrumentoAvaliado {
    public record FinalidadeTed(String id, String descricao) {}
}

public record ResultadoViabilidadeDto(
    boolean valido,
    List<Restricao> restricoes,
    int ordemMelhor,
    Limite limite
) {}
```

`Restricao` e `Limite` são compartilhados entre o modelo interno (`contexto/`) e o DTO de resposta — são objetos de domínio imutáveis.

### 8.8 `avaliacao/upstream/InstituicaoClient.java`

```java
@FeignClient(name = "instituicao-client", url = "${avaliacao.upstream.instituicao.url}")
public interface InstituicaoClient {
    String buscarIspb(String checkinId);
}
```

O path exato, o método HTTP e o formato de resposta do upstream devem ser definidos junto com a equipe responsável pela integração. Esta SPEC define apenas o contrato interno esperado: dado um `checkinId`, retorna o ISPB como string.

### 8.9 Contratos e payloads

**Request — `POST /v1/avaliacao`:**

```json
// QR Code
{ "checkinId": "checkin:qrcode:1:<uuid>", "data": "2026-06-04",
  "origem": { "agencia": "0001", "conta": "12345-6" },
  "qrcode": { "emv": "00020126...", "tipo": "COB" } }

// Chave Pix
{ "checkinId": "checkin:chaves_pix:1:<uuid>", "data": "2026-06-10",
  "origem": { "agencia": "0001", "conta": "12345-6" } }

// Conta
{ "checkinId": "checkin:agconta:1:<uuid>", "data": "2026-06-10",
  "origem": { "agencia": "0001", "conta": "12345-6" } }
```

**Response — HTTP 200:**

```json
{
  "instrumentos": [
    { "tipo": "PIX",
      "dataSelecionada": { "valido": true, "restricoes": [], "ordemMelhor": 1, "limite": { "qtdDiasAlt": 2 } },
      "agendamento":     { "valido": false, "restricoes": [{ "motivo": "QR_CODE_NAO_PERMITE", "contexto": { "tipo": "COB" } }], "ordemMelhor": 1, "limite": { "qtdDiasAlt": 2 } }
    },
    { "tipo": "TED",
      "dataSelecionada": { "valido": true, "restricoes": [], "ordemMelhor": 2, "limite": { "qtdDiasAlt": 1 } },
      "agendamento":     { "valido": true, "restricoes": [], "ordemMelhor": 2, "limite": { "qtdDiasAlt": 1 } },
      "finalidades": [{ "id": "01", "descricao": "Crédito em Conta" }, { "id": "03", "descricao": "DOC/TED" }]
    }
  ]
}
```

**Response — HTTP 400 (checkinId inválido):**
Estrutura de erro padrão Spring Boot (a ser definida em task separada ou futuro `@ControllerAdvice` global).

---

## 9. Fluxos técnicos

```text
POST /v1/avaliacao
        │
        ├─ Jackson DEDUCTION ──► sealed subtype do request
        │                        (QrcodeAvaliacaoRequest se tiver campo `qrcode`;
        │                         ChavePixAvaliacaoRequest ou AgcontaAvaliacaoRequest caso contrário)
        │
        ├─ CheckinIdParser ──► TipoCheckin  (CheckinIdInvalidoException → 400)
        │
        ├─ InstituicaoClient (Feign + CorrelationFeignInterceptor) ──► ISPB
        │
        ├─ InstrumentoResolver ──► Set<Instrumento>
        │
        ├─ new AvaliacaoContexto(request, tipo, ispb, instrumentos)
        │
        ├─ PipelineDataSelecionada
        │     └─ para cada instrumento:
        │           validadores.filter(suporta).forEach(validar)
        │           ↳ acumula ResultadoViabilidade.dataSelecionada no contexto
        │
        ├─ PipelineAgendamento
        │     └─ para cada instrumento:
        │           validadores.filter(suporta).forEach(validar)
        │           ↳ ProximaDataValidador.suporta() lê dataSelecionada.isValido() do contexto
        │           ↳ acumula ResultadoViabilidade.agendamento no contexto
        │
        └─ AvaliacaoResponseMapper ──► AvaliacaoResponse (HTTP 200)
```

```text
Fluxo de falha — checkinId inválido:

POST /v1/avaliacao { "checkinId": "checkin:boleto:1:uuid", ... }
        │
        ├─ CheckinIdParser.parse("checkin:boleto:1:uuid")
        │     └─ switch("boleto") → default → throw CheckinIdInvalidoException
        │
        └─ @ExceptionHandler / @ControllerAdvice ──► HTTP 400
```

---

## 10. Arquivos afetados

| Arquivo | Tipo | Mudança |
|---|---|---|
| `src/main/java/.../avaliacao/AvaliacaoController.java` | Criar | `@RestController POST /v1/avaliacao` |
| `src/main/java/.../avaliacao/AvaliacaoService.java` | Criar | Orquestra parse → ISPB → resolver → contexto → pipelines → mapper |
| `src/main/java/.../avaliacao/AvaliacaoOrquestrador.java` | Criar | Executa `PipelineDataSelecionada` depois `PipelineAgendamento` |
| `src/main/java/.../avaliacao/request/AvaliacaoRequest.java` | Criar | `sealed interface` com DEDUCTION |
| `src/main/java/.../avaliacao/request/QrcodeAvaliacaoRequest.java` | Criar | `record` com campo `qrcode` |
| `src/main/java/.../avaliacao/request/ChavePixAvaliacaoRequest.java` | Criar | `record` sem campo extra |
| `src/main/java/.../avaliacao/request/AgcontaAvaliacaoRequest.java` | Criar | `record` sem campo extra |
| `src/main/java/.../avaliacao/request/DadosOrigem.java` | Criar | `record(agencia, conta)` |
| `src/main/java/.../avaliacao/request/DadosQrcode.java` | Criar | `record(emv, tipo)` |
| `src/main/java/.../avaliacao/response/AvaliacaoResponse.java` | Criar | `record(List<InstrumentoAvaliado>)` |
| `src/main/java/.../avaliacao/response/InstrumentoAvaliado.java` | Criar | `sealed interface` com DEDUCTION |
| `src/main/java/.../avaliacao/response/InstrumentoPix.java` | Criar | `record(tipo, dataSelecionada, agendamento)` |
| `src/main/java/.../avaliacao/response/InstrumentoTef.java` | Criar | `record(tipo, dataSelecionada, agendamento)` |
| `src/main/java/.../avaliacao/response/InstrumentoTed.java` | Criar | `record(tipo, dataSelecionada, agendamento, finalidades)` |
| `src/main/java/.../avaliacao/response/ResultadoViabilidadeDto.java` | Criar | `record(valido, restricoes, ordemMelhor, limite)` |
| `src/main/java/.../avaliacao/contexto/AvaliacaoContexto.java` | Criar | Estado mutável compartilhado pelos pipelines |
| `src/main/java/.../avaliacao/contexto/ResultadoInstrumento.java` | Criar | Agrega `dataSelecionada` e `agendamento` |
| `src/main/java/.../avaliacao/contexto/ResultadoViabilidade.java` | Criar | Modelo interno com `adicionarRestricao()` |
| `src/main/java/.../avaliacao/contexto/Restricao.java` | Criar | `sealed interface` com DEDUCTION |
| `src/main/java/.../avaliacao/contexto/RestricaoGenerica.java` | Criar | `record(motivo)` |
| `src/main/java/.../avaliacao/contexto/RestricaoQrcode.java` | Criar | `record(motivo, contexto)` com inner `record ContextoQrcode` |
| `src/main/java/.../avaliacao/contexto/Limite.java` | Criar | `record(qtdDiasAlt)` |
| `src/main/java/.../avaliacao/pipeline/PipelineDataSelecionada.java` | Criar | Executa validadores para `dataSelecionada` |
| `src/main/java/.../avaliacao/pipeline/PipelineAgendamento.java` | Criar | Executa validadores para `agendamento` |
| `src/main/java/.../avaliacao/validador/Validador.java` | Criar | Interface com `suporta()` e `validar()` |
| `src/main/java/.../avaliacao/validador/data/HorarioPermitidoValidador.java` | Criar | Suporta `isOnline()` |
| `src/main/java/.../avaliacao/validador/data/LimiteDiarioValidador.java` | Criar | Suporta sempre |
| `src/main/java/.../avaliacao/validador/data/DataValidaValidador.java` | Criar | Suporta sempre |
| `src/main/java/.../avaliacao/validador/instituicao/StatusInstituicaoValidador.java` | Criar | Suporta sempre |
| `src/main/java/.../avaliacao/validador/instituicao/LimiteInstituicaoValidador.java` | Criar | Suporta sempre |
| `src/main/java/.../avaliacao/validador/agendamento/PermiteAgendamentoValidador.java` | Criar | Suporta `PIX + QRCODE` |
| `src/main/java/.../avaliacao/validador/agendamento/ProximaDataValidador.java` | Criar | Suporta quando `!dataSelecionada.isValido()` |
| `src/main/java/.../avaliacao/resolver/TipoCheckin.java` | Criar | Enum: `QRCODE, CHAVE_PIX, AGCONTA` |
| `src/main/java/.../avaliacao/resolver/Instrumento.java` | Criar | Enum: `PIX, TED, TEF` |
| `src/main/java/.../avaliacao/resolver/CheckinIdParser.java` | Criar | `static parse(checkinId)` |
| `src/main/java/.../avaliacao/resolver/CheckinIdInvalidoException.java` | Criar | `RuntimeException` |
| `src/main/java/.../avaliacao/resolver/InstrumentoResolver.java` | Criar | `static resolver(tipo, ispb)` |
| `src/main/java/.../avaliacao/upstream/InstituicaoClient.java` | Criar | `@FeignClient` para busca do ISPB |
| `src/main/java/.../avaliacao/mapper/AvaliacaoResponseMapper.java` | Criar | Converte `AvaliacaoContexto` → `AvaliacaoResponse` |
| `src/main/resources/application.yaml` | Modificar | Adicionar `avaliacao.upstream.instituicao.url` para configurar o `InstituicaoClient` |

---

## 11. Requisitos não funcionais

| Categoria | Requisito não funcional | Meta ou critério |
|---|---|---|
| Performance | Execução dos pipelines deve ser O(validadores × instrumentos) — sem paralelismo ou I/O nos validadores locais | Validado por design: `stream().filter().forEach()` síncrono |
| Observabilidade | Toda requisição ao endpoint deve ser logada pelo Logbook com campos de correlação | Garantido pela infraestrutura `core/` existente sem configuração adicional |
| Observabilidade | Chamada ao `InstituicaoClient` deve ser logada pelo `FeignLogbookLogger` com headers de correlação | Garantido pelo `CorrelationFeignInterceptor` + `FeignConfig.feignLogger()` existentes |
| Confiabilidade | `CheckinIdInvalidoException` deve retornar HTTP 400 sem expor stack trace | Tratar via `@ExceptionHandler` ou `@ControllerAdvice` |
| Segurança | Nenhum dado sensível de `origem` ou `qrcode.emv` deve aparecer nos logs | Configurar `logbook.obfuscate` para os campos sensíveis ou usar estratégia de body masking |
| Compatibilidade | O slice não deve importar classes de outros slices de negócio | Apenas `core/` é dependência transversal permitida |

---

## 12. Estratégia de rollout ou migração

Primeira entrega de negócio — sem rollout gradual ou flag de feature. O slice pode ser desenvolvido em branch e integrado diretamente. Não há dado persistido nem migração de banco envolvida.

O `InstituicaoClient` depende de um upstream externo. Enquanto o upstream não estiver disponível, o `url` pode apontar para um WireMock ou stub de desenvolvimento.

---

## 13. Estratégia de validação

- **Testes unitários:**
  - `CheckinIdParser`: todos os tipos válidos, formato inválido (< 2 segmentos), tipo desconhecido
  - `InstrumentoResolver`: todas as combinações de `TipoCheckin` × ISPB
  - Cada `Validador`: `suporta()` retorna `true`/`false` nas condições certas; `validar()` adiciona restrição correta
  - `ResultadoViabilidade`: `adicionarRestricao()` seta `valido = false`
  - `AvaliacaoContexto.isOnline()`: data atual → `true`, data futura → `false`
- **Testes de integração (`@SpringBootTest` com WireMock para `InstituicaoClient`):**
  - Fluxo completo para `qrcode` → `InstrumentoPix` retornado
  - Fluxo `agconta` + ISPB Itaú → `[InstrumentoPix, InstrumentoTef]`
  - Fluxo `agconta` + ISPB outro → `[InstrumentoPix, InstrumentoTed]`
  - Serialização/desserialização do request polimórfico com e sem campo `qrcode`
  - `CheckinIdInvalidoException` resulta em HTTP 400
- **Testes de serialização Jackson:**
  - `QrcodeAvaliacaoRequest` serializa e deserializa com DEDUCTION
  - `InstrumentoTed` na resposta inclui `finalidades`
  - `RestricaoQrcode` inclui campo `contexto` no JSON
- **Sinais operacionais:**
  - Log de entrada do Logbook contém `checkinId` (se configurado para logar body)
  - Log da chamada Feign ao `InstituicaoClient` contém `x-correlationId`

---

## 14. Critérios de aceite

- [ ] `POST /v1/avaliacao` com payload `qrcode` retorna `instrumentos: [{ tipo: "PIX", ... }]`
- [ ] `POST /v1/avaliacao` com `agconta` + ISPB `60701190` retorna `[PIX, TEF]`
- [ ] `POST /v1/avaliacao` com `agconta` + ISPB diferente retorna `[PIX, TED]`
- [ ] `InstrumentoTed` inclui `finalidades` no JSON de resposta
- [ ] QR Code que não permite agendamento retorna restrição `QR_CODE_NAO_PERMITE` com `contexto.tipo` no bloco `agendamento`
- [ ] `checkinId` inválido retorna HTTP 400
- [ ] `ProximaDataValidador` executa somente quando `dataSelecionada.isValido() == false`
- [ ] `HorarioPermitidoValidador` não executa para datas futuras (`isOnline() = false`)
- [ ] Nenhum arquivo do pacote `core/` foi modificado
- [ ] Testes unitários de `CheckinIdParser` e `InstrumentoResolver` passam
- [ ] Build sem erros: `mvn verify`

---

## 15. Riscos e observações

- **Jackson DEDUCTION em `InstrumentoAvaliado`:** `InstrumentoPix` e `InstrumentoTef` têm os mesmos campos — Jackson não consegue distingui-los na deserialização. O servidor nunca deserializa a resposta, então não há problema de runtime. Se clientes Java precisarem deserializar, `@JsonTypeInfo(use = NAME, property = "tipo")` é a alternativa segura.
- **Jackson DEDUCTION em `AvaliacaoRequest`:** `ChavePixAvaliacaoRequest` e `AgcontaAvaliacaoRequest` têm os mesmos campos — Jackson também não os distingue. O tipo real vem do `checkinId`. Nenhuma lógica deve depender do subtipo Java para esses dois; toda diferenciação usa `TipoCheckin` derivado pelo parser.
- **Contrato do `InstituicaoClient`:** o upstream (path, autenticação, formato de resposta) não está definido. Toda a lógica downstream depende do ISPB — falha no upstream bloqueia a avaliação completa.
- **Lógica interna dos validadores:** `StatusInstituicaoValidador`, `LimiteInstituicaoValidador`, `LimiteDiarioValidador`, `DataValidaValidador` e `ProximaDataValidador` podem depender de chamadas adicionais a upstreams ou regras de domínio não mapeadas neste escopo. A estrutura do validador acomoda qualquer lógica interna sem alterar o pipeline.
- **Ordenação de `Set<Instrumento>`:** `Set.of()` não garante ordem. O `AvaliacaoResponseMapper` deve definir uma ordem fixa para os instrumentos na lista de resposta (por exemplo, via `EnumSet` ou ordenação explícita por `Instrumento.ordinal()`).

---

## 16. Questões em aberto

- Contrato exato do `InstituicaoClient`: path, método HTTP, corpo de resposta, autenticação — depende da equipe responsável pelo upstream
- Valores reais de `ordemMelhor` e `limite.qtdDiasAlt` por instrumento — definidos pelas regras de negócio de cada validador, fora do escopo desta SPEC
- Lista de `finalidades` do TED (`id`, `descricao`) — origem dos dados (hardcoded, upstream ou configuração)
- Tratamento de erro global (`@ControllerAdvice`) — pode ser task separada ou incluída nesta entrega
- Mascaramento de campos sensíveis no Logbook (`qrcode.emv`, `origem.conta`) — avaliar junto com a política de logs do projeto
