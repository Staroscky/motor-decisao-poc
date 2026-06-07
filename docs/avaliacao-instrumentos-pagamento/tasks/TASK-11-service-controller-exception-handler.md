# TASK-11 — AvaliacaoService, AvaliacaoController e tratamento de erros

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/` (novos)
**Referência SPEC:** Seções 8.1, 9, RF-03
**Depende de:** TASK-2, TASK-4, TASK-8, TASK-9, TASK-10
**Bloqueada por:** nenhuma

---

## Contexto

É a última peça do slice: o controller expõe o endpoint HTTP, o service orquestra o fluxo completo (parse → ISPB → resolve → contexto → pipelines → mapper), e o exception handler converte `CheckinIdInvalidoException` em HTTP 400. Com esta task o endpoint está funcional end-to-end.

## O que fazer

**`AvaliacaoService.java`**
```java
@Service
public class AvaliacaoService {

    private final InstituicaoClient instituicaoClient;
    private final AvaliacaoOrquestrador orquestrador;
    private final AvaliacaoResponseMapper mapper;

    public AvaliacaoService(InstituicaoClient instituicaoClient,
                             AvaliacaoOrquestrador orquestrador,
                             AvaliacaoResponseMapper mapper) {
        this.instituicaoClient = instituicaoClient;
        this.orquestrador = orquestrador;
        this.mapper = mapper;
    }

    public AvaliacaoResponse avaliar(AvaliacaoRequest request) {
        TipoCheckin tipo        = CheckinIdParser.parse(request.checkinId());
        String ispb             = instituicaoClient.buscarIspb(request.checkinId());
        Set<Instrumento> instrs = InstrumentoResolver.resolver(tipo, ispb);

        AvaliacaoContexto contexto = new AvaliacaoContexto(request, tipo, ispb, instrs);
        orquestrador.executar(contexto);

        return mapper.toResponse(contexto);
    }
}
```

**`AvaliacaoController.java`**
```java
@RestController
@RequestMapping("/v1/avaliacao")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @PostMapping
    public AvaliacaoResponse avaliar(@RequestBody AvaliacaoRequest request) {
        return service.avaliar(request);
    }
}
```

**`AvaliacaoExceptionHandler.java`** (no pacote `avaliacao/` ou em um pacote `core/exception/` global)
```java
@RestControllerAdvice
public class AvaliacaoExceptionHandler {

    @ExceptionHandler(CheckinIdInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleCheckinIdInvalido(CheckinIdInvalidoException ex) {
        return Map.of("erro", ex.getMessage());
    }
}
```

## Notas de implementação

- O `@RestControllerAdvice` pode ficar no pacote `avaliacao/` se a intenção for escopo restrito, ou em um futuro `core/exception/` se virar handler global para múltiplos slices — para esta entrega, colocar em `avaliacao/` e mover quando necessário
- O `@RequestBody AvaliacaoRequest` desencadeia a desserialização polimórfica via Jackson DEDUCTION — nenhuma anotação adicional é necessária no controller
- O `AvaliacaoService` não captura falha do `InstituicaoClient` — deixar propagar como HTTP 500 nesta entrega

## Critério de aceite

- [ ] `POST /v1/avaliacao` com payload válido retorna HTTP 200 com `AvaliacaoResponse`
- [ ] `POST /v1/avaliacao` com `checkinId` de tipo desconhecido retorna HTTP 400
- [ ] `POST /v1/avaliacao` com `checkinId` sem dois segmentos retorna HTTP 400
- [ ] Aplicação sobe sem erros com `mvn spring-boot:run`
- [ ] Build sem erros: `mvn verify`
