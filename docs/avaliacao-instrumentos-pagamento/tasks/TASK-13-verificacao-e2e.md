# TASK-13 — Verificação e2e

**Arquivo alvo:** `src/test/java/com/staroscky/motordecisao/avaliacao/AvaliacaoIntegrationTest.java` (novo)
**Referência SPEC:** Seções 13, 14
**Depende de:** TASK-1, TASK-2, TASK-3, TASK-4, TASK-5, TASK-6, TASK-7, TASK-8, TASK-9, TASK-10, TASK-11, TASK-12
**Bloqueada por:** nenhuma

---

## Contexto

Testa o fluxo completo do endpoint via `@SpringBootTest` com WireMock para o `InstituicaoClient`. Verifica que o request chega ao controller, atravessa o service e os pipelines, e a resposta JSON está correta — incluindo polimorfismo e restrições.

## O que fazer

Adicionar dependência do WireMock ao `pom.xml` (escopo test):
```xml
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-spring-boot</artifactId>
    <version>3.6.0</version>
    <scope>test</scope>
</dependency>
```

**`AvaliacaoIntegrationTest.java`** — cobrir os seguintes cenários com `MockMvc` ou `TestRestTemplate`:

1. **QR Code → PIX**
   - Request: `checkinId: "checkin:qrcode:1:uuid"`, com campo `qrcode: { emv: "...", tipo: "COB" }`
   - WireMock retorna ISPB qualquer (ex.: `"00000000"`)
   - Assert: HTTP 200, `instrumentos[0].tipo == "PIX"`, `agendamento.valido == false`, restrição `QR_CODE_NAO_PERMITE` com `contexto.tipo == "COB"`

2. **Conta Itaú → PIX + TEF**
   - Request: `checkinId: "checkin:agconta:1:uuid"`
   - WireMock retorna ISPB `"60701190"`
   - Assert: HTTP 200, dois instrumentos — `PIX` e `TEF` — nessa ordem

3. **Conta outro banco → PIX + TED**
   - Request: `checkinId: "checkin:agconta:1:uuid"`
   - WireMock retorna ISPB `"12345678"`
   - Assert: HTTP 200, dois instrumentos — `PIX` e `TED`; `TED` inclui campo `finalidades`

4. **Chave Pix → PIX**
   - Request: `checkinId: "checkin:chaves_pix:1:uuid"` (sem campo `qrcode`)
   - Assert: HTTP 200, `instrumentos[0].tipo == "PIX"`

5. **checkinId inválido → HTTP 400**
   - Request: `checkinId: "checkin:boleto:1:uuid"`
   - Assert: HTTP 400

6. **Desserialização polimórfica do request**
   - Request QR Code: campo `qrcode` presente → desserializado e processado corretamente
   - Request sem `qrcode`: sem erros de desserialização

## Notas de implementação

- Configurar WireMock para interceptar chamadas ao `InstituicaoClient` antes de cada teste via `@WireMockTest` ou configuração manual
- Usar `application-test.yaml` ou `@TestPropertySource` para apontar `avaliacao.upstream.instituicao.url` para o WireMock
- Os validadores com lógica TODO não adicionam restrições — `dataSelecionada.valido` será `true` para todos, exceto onde `PermiteAgendamentoValidador` atua
- Verificar no log de teste que `x-correlationId` está presente na chamada ao WireMock (via `CorrelationFeignInterceptor`) quando o header é enviado na requisição de entrada

## Critério de aceite

- [ ] Cenário 1 (QR Code PIX): HTTP 200 com `instrumentos[0].tipo == "PIX"` e restrição de agendamento `QR_CODE_NAO_PERMITE`
- [ ] Cenário 2 (Conta Itaú): HTTP 200 com PIX e TEF na resposta, nessa ordem
- [ ] Cenário 3 (Conta outro banco): HTTP 200 com PIX e TED; TED contém `finalidades`
- [ ] Cenário 4 (Chave Pix): HTTP 200 com único instrumento PIX
- [ ] Cenário 5 (checkinId inválido): HTTP 400
- [ ] `mvn verify` passa sem erros
- [ ] Nenhum arquivo do pacote `core/` foi modificado durante toda a entrega
