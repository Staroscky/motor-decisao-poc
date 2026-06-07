# TASK-12 — Testes unitários

**Arquivo alvo:** `src/test/java/com/staroscky/motordecisao/avaliacao/` (novo)
**Referência SPEC:** Seção 13
**Depende de:** TASK-2, TASK-6, TASK-7
**Bloqueada por:** nenhuma

---

## Contexto

Testes unitários das peças com lógica de decisão: `CheckinIdParser`, `InstrumentoResolver`, o `suporta()` de cada validador e `ResultadoViabilidade`. São testes rápidos sem Spring context — usam apenas JUnit 5 e Mockito quando necessário.

## O que fazer

**`CheckinIdParserTest.java`**
- Todos os tipos válidos: `qrcode`, `chaves_pix`, `agconta`
- `checkinId` com menos de 2 segmentos → `CheckinIdInvalidoException`
- Tipo desconhecido (ex: `boleto`) → `CheckinIdInvalidoException`
- Formato com versão e UUID completo: `"checkin:qrcode:1:550e8400-e29b-41d4-a716"` → `QRCODE`

**`InstrumentoResolverTest.java`**
- `QRCODE` + qualquer ISPB → `{PIX}`
- `CHAVE_PIX` + qualquer ISPB → `{PIX}`
- `AGCONTA` + `"60701190"` → `{PIX, TEF}`
- `AGCONTA` + `"outro"` → `{PIX, TED}`
- `AGCONTA` + `""` → `{PIX, TED}` (ISPB vazio não é o de Itaú)

**`HorarioPermitidoValidadorTest.java`**
- `suporta()` com data atual (`LocalDate.now()`) → `true`
- `suporta()` com data futura (`LocalDate.now().plusDays(1)`) → `false`

**`PermiteAgendamentoValidadorTest.java`**
- `suporta()` com `PIX` + `QRCODE` → `true`
- `suporta()` com `TED` + `QRCODE` → `false`
- `suporta()` com `PIX` + `AGCONTA` → `false`
- `validar()` com tipo que não permite agendamento → `resultado.isValido() == false` + restrição `QR_CODE_NAO_PERMITE`

**`ProximaDataValidadorTest.java`**
- `suporta()` quando `dataSelecionada.isValido() == false` → `true`
- `suporta()` quando `dataSelecionada.isValido() == true` → `false`

**`ResultadoViabilidadeTest.java`**
- `adicionarRestricao()` seta `valido = false`
- `adicionarRestricao()` adiciona a restrição à lista
- Múltiplas restrições são acumuladas

## Notas de implementação

- Para os validadores, criar `AvaliacaoContexto` e `ResultadoViabilidade` reais (sem mock) — são objetos simples sem dependências externas
- `PermiteAgendamentoValidadorTest` precisa de um `QrcodeAvaliacaoRequest` com `DadosQrcode` — construir diretamente o record
- Não usar `@SpringBootTest` ou `@ExtendWith(SpringExtension.class)` nesta task — testes puramente unitários com `@ExtendWith(MockitoExtension.class)` onde necessário

## Critério de aceite

- [ ] Todos os casos de `CheckinIdParserTest` passam
- [ ] Todos os casos de `InstrumentoResolverTest` passam
- [ ] `suporta()` de `HorarioPermitidoValidador` retorna correto para data atual e futura
- [ ] `suporta()` de `PermiteAgendamentoValidador` retorna correto para todas as combinações
- [ ] `suporta()` de `ProximaDataValidador` lê o estado de `dataSelecionada` corretamente
- [ ] `ResultadoViabilidade.adicionarRestricao()` inverte `valido` para `false`
- [ ] `mvn test` passa sem erros
