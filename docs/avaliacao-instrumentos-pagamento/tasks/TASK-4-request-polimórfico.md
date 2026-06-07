# TASK-4 — Request polimórfico

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/request/` (novo)
**Referência SPEC:** Seção 8.2
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

Define o contrato de entrada do endpoint `POST /v1/avaliacao`. O request é polimórfico: Jackson infere o subtipo pelo campo `qrcode` (presente somente em `QrcodeAvaliacaoRequest`). Os outros dois subtipos têm os mesmos campos — a distinção entre eles é feita pelo `CheckinIdParser` via `checkinId`, não pelo Jackson.

## O que fazer

Criar os seguintes arquivos em `com.staroscky.motordecisao.avaliacao.request`:

**`DadosOrigem.java`**
```java
public record DadosOrigem(String agencia, String conta) {}
```

**`DadosQrcode.java`**
```java
public record DadosQrcode(String emv, String tipo) {}
```

**`AvaliacaoRequest.java`**
```java
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
```

**`QrcodeAvaliacaoRequest.java`**
```java
public record QrcodeAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem,
    DadosQrcode qrcode
) implements AvaliacaoRequest {}
```

**`ChavePixAvaliacaoRequest.java`**
```java
public record ChavePixAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem
) implements AvaliacaoRequest {}
```

**`AgcontaAvaliacaoRequest.java`**
```java
public record AgcontaAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem
) implements AvaliacaoRequest {}
```

## Notas de implementação

- A anotação `@JsonTypeInfo(use = DEDUCTION)` na interface instrui o Jackson a inferir o subtipo pelos campos do JSON; o campo `qrcode` é o único diferenciador estrutural
- `ChavePixAvaliacaoRequest` e `AgcontaAvaliacaoRequest` têm campos idênticos — o Jackson desserializará ambos para o mesmo subtipo quando o JSON não tiver `qrcode`; o `AvaliacaoService` não depende do subtipo Java para esses dois, apenas do `TipoCheckin` derivado pelo `CheckinIdParser`
- `LocalDate` em records com Spring Boot 3.x é desserializado corretamente via Jackson JSR-310 module (já incluído no `spring-boot-starter-web`)

## Critério de aceite

- [ ] JSON com campo `qrcode` é desserializado como `QrcodeAvaliacaoRequest`
- [ ] JSON sem campo `qrcode` é desserializado como `ChavePixAvaliacaoRequest` (ou `AgcontaAvaliacaoRequest` — são estruturalmente idênticos ao Jackson)
- [ ] `LocalDate` é desserializado corretamente a partir de `"2026-06-04"`
- [ ] Build sem erros: `mvn verify`
