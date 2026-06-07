# TASK-2 — CheckinIdParser e InstrumentoResolver

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/resolver/` (novo)
**Referência SPEC:** Seção 8.3
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

`CheckinIdParser` converte o `checkinId` bruto em `TipoCheckin`. `InstrumentoResolver` deriva os instrumentos disponíveis a partir do `TipoCheckin` e do ISPB. Ambos são chamados pelo `AvaliacaoService` no início de cada avaliação.

## O que fazer

Criar os dois arquivos no pacote `com.staroscky.motordecisao.avaliacao.resolver`:

**`CheckinIdParser.java`**
```java
public final class CheckinIdParser {

    private CheckinIdParser() {}

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
```

**`InstrumentoResolver.java`**
```java
public final class InstrumentoResolver {

    private static final String ISPB_ITAU = "60701190";

    private InstrumentoResolver() {}

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

## Notas de implementação

- `split(":")` em `"checkin:qrcode:1:uuid"` produz `["checkin", "qrcode", "1", "uuid"]` — o índice `[1]` é o tipo
- `split` com menos de 2 segmentos ocorre em strings como `"checkin"` ou `""` — verificar `partes.length < 2`
- `Set.of()` retorna conjunto imutável sem ordem garantida — o `AvaliacaoContexto` deve iterar em ordem determinística (ver TASK-3)

## Critério de aceite

- [ ] `CheckinIdParser.parse("checkin:qrcode:1:uuid")` retorna `QRCODE`
- [ ] `CheckinIdParser.parse("checkin:chaves_pix:1:uuid")` retorna `CHAVE_PIX`
- [ ] `CheckinIdParser.parse("checkin:agconta:1:uuid")` retorna `AGCONTA`
- [ ] `CheckinIdParser.parse("invalido")` lança `CheckinIdInvalidoException`
- [ ] `CheckinIdParser.parse("checkin:boleto:1:uuid")` lança `CheckinIdInvalidoException`
- [ ] `InstrumentoResolver.resolver(QRCODE, qualquer)` retorna `{PIX}`
- [ ] `InstrumentoResolver.resolver(CHAVE_PIX, qualquer)` retorna `{PIX}`
- [ ] `InstrumentoResolver.resolver(AGCONTA, "60701190")` retorna `{PIX, TEF}`
- [ ] `InstrumentoResolver.resolver(AGCONTA, "outro")` retorna `{PIX, TED}`
- [ ] Build sem erros: `mvn verify`
