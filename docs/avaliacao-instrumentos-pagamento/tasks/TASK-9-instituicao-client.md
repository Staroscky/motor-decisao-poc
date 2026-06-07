# TASK-9 — InstituicaoClient e configuração

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/upstream/InstituicaoClient.java` (novo) e `src/main/resources/application.yaml` (modificar)
**Referência SPEC:** Seção 8.8, 10
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

`InstituicaoClient` é o cliente Feign responsável por buscar o ISPB da instituição de destino a partir do `checkinId`. O `@EnableFeignClients` em `FeignConfig` já aponta para `"com.staroscky.motordecisao"`, então qualquer `@FeignClient` em qualquer subpacote é detectado automaticamente. O `CorrelationFeignInterceptor` injeta os headers de correlação em todas as chamadas sem configuração adicional.

## O que fazer

**`upstream/InstituicaoClient.java`**

O contrato exato do upstream (path, método, corpo) deve ser confirmado com a equipe responsável. Estrutura inicial:

```java
@FeignClient(name = "instituicao-client", url = "${avaliacao.upstream.instituicao.url}")
public interface InstituicaoClient {

    @GetMapping("/ispb")                   // ajustar path conforme contrato real
    String buscarIspb(@RequestParam("checkinId") String checkinId);
}
```

**`application.yaml`** — adicionar a propriedade:
```yaml
avaliacao:
  upstream:
    instituicao:
      url: http://localhost:8081  # placeholder — substituir pela URL real em cada ambiente
```

## Notas de implementação

- `@FeignClient(url = "${...}")` usa a propriedade do `application.yaml` — em testes, sobrescrever via `@TestPropertySource` ou `application-test.yaml`
- O `CorrelationFeignInterceptor` já em vigor em `core/feign/` injeta `x-correlationId` e `x-flowId` automaticamente em todas as chamadas Feign, incluindo esta
- `FeignLogbookLogger` já configurado em `core/config/FeignConfig` loga request e response desta chamada via Logbook
- Qualquer falha do upstream gera exceção Feign não tratada (HTTP 500) nesta entrega — retry e circuit breaker são fora de escopo

## Critério de aceite

- [ ] `InstituicaoClient` é anotado com `@FeignClient` e compila sem erros
- [ ] A propriedade `avaliacao.upstream.instituicao.url` está definida em `application.yaml`
- [ ] Aplicação sobe sem erros com `mvn spring-boot:run`
- [ ] Build sem erros: `mvn verify`
