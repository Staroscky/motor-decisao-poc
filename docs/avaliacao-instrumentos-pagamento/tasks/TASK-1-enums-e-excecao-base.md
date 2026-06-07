# TASK-1 — Enums e exceção base

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/resolver/` (novo)
**Referência SPEC:** Seções 8.3, 4.1
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

Todos os outros componentes do slice dependem dos enums `TipoCheckin` e `Instrumento`, e da exceção `CheckinIdInvalidoException`. Eles precisam existir antes de qualquer outra classe do pacote `avaliacao/`.

## O que fazer

Criar os três arquivos no pacote `com.staroscky.motordecisao.avaliacao.resolver`:

**`TipoCheckin.java`**
```java
public enum TipoCheckin { QRCODE, CHAVE_PIX, AGCONTA }
```

**`Instrumento.java`**
```java
public enum Instrumento { PIX, TED, TEF }
```

**`CheckinIdInvalidoException.java`**
```java
public class CheckinIdInvalidoException extends RuntimeException {
    public CheckinIdInvalidoException(String checkinId) {
        super("checkinId inválido: " + checkinId);
    }
}
```

## Notas de implementação

- Os enums ficam no pacote `resolver/` por coesão — são usados pelo `CheckinIdParser` e pelo `InstrumentoResolver`
- A ordem de declaração de `Instrumento` importa: o mapper usará `Instrumento.ordinal()` para ordenar os instrumentos na resposta (PIX=0, TED=1, TEF=2)
- `CheckinIdInvalidoException` não carrega stack trace gerado (pode usar `super(msg, null, true, false)` se performance de criação for relevante em testes de carga, mas não é obrigatório agora)

## Critério de aceite

- [ ] `TipoCheckin` possui os três valores: `QRCODE`, `CHAVE_PIX`, `AGCONTA`
- [ ] `Instrumento` possui os três valores: `PIX`, `TED`, `TEF`
- [ ] `CheckinIdInvalidoException` estende `RuntimeException` e inclui o `checkinId` na mensagem
- [ ] Build sem erros: `mvn verify`
