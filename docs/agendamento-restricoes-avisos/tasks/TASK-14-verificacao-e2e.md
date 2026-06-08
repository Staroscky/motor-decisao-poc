# TASK-14 — Verificação e2e

**Arquivo alvo:** nenhum (verificação manual ou de integração)
**Referência SPEC:** Seção 14 (Critérios de aceite)
**Depende de:** TASK-12, TASK-13
**Bloqueada por:** nenhuma

---

## Contexto

Verificação end-to-end do comportamento de `POST /v1/avaliacao` após todas as tasks implementadas. Foca nos cenários que envolvem o bloco `agendamento` — especialmente a coexistência de `dataSelecionada.valido: true` com `agendamento.podeAgendar: false`, e a ausência/presença condicional de `proximaDataDisponivel` e `avisos`.

## O que fazer

Subir a aplicação localmente e executar as seguintes chamadas:

### Cenário 1 — QRCODE COB (restrição de agendamento)

```
POST /v1/avaliacao
{
  "checkinId": "checkin:qrcode:1:<uuid>",
  "data": "<hoje>",
  "origem": { "agencia": "0001", "conta": "12345-6" },
  "qrcode": { "emv": "...", "tipo": "COB" }
}
```

Verificar no response de PIX:
- `dataSelecionada.valido: true` (data de hoje é válida)
- `agendamento.podeAgendar: false`
- `agendamento.restricoes` contém `{ "motivo": "QR_CODE_NAO_PERMITE", "contexto": { "tipo": "COB" } }`
- `agendamento.proximaDataDisponivel` **ausente** no JSON
- `agendamento.avisos` **ausente** no JSON

### Cenário 2 — QRCODE COB com data futura

```
POST /v1/avaliacao
{
  "checkinId": "checkin:qrcode:1:<uuid>",
  "data": "<data futura válida>",
  "origem": { "agencia": "0001", "conta": "12345-6" },
  "qrcode": { "emv": "...", "tipo": "COB" }
}
```

Verificar:
- `agendamento.podeAgendar: false` independente de `dataSelecionada.valido`
- `agendamento.restricoes` preenchida

### Cenário 3 — Chave PIX, agendamento permitido, data válida

```
POST /v1/avaliacao
{
  "checkinId": "checkin:chaves_pix:1:<uuid>",
  "data": "<hoje>",
  "origem": { "agencia": "0001", "conta": "12345-6" }
}
```

Verificar:
- `agendamento.podeAgendar: true`
- `agendamento.restricoes: []`
- `agendamento.proximaDataDisponivel` **ausente** no JSON (dataSelecionada é válida)
- `agendamento.avisos` **ausente** no JSON

### Verificações transversais

- O campo `restricoes` está sempre presente no JSON de `agendamento`, mesmo quando `[]`
- O campo `dataSelecionada` do response continua com a estrutura anterior (`valido`, `restricoes`, etc.) — sem regressão
- Build passa com `mvn test` antes de subir o servidor

## Notas de implementação

- Os stubs `TODO` em `ProximaData*Validador` e `AvisoIntervalo*Validador` fazem com que `proximaDataDisponivel` nunca seja populado nesta fase. Isso é esperado — o comportamento correto é ausência desses campos no JSON quando os stubs não retornam nada.
- Se o upstream de `checkinId` não estiver disponível localmente, usar um `@MockBean` ou WireMock no teste de integração.

## Critério de aceite

- [ ] QRCODE COB retorna `agendamento.podeAgendar: false` com `dataSelecionada.valido: true` coexistindo
- [ ] `restricoes` presente como `[]` em respostas sem bloqueio
- [ ] `proximaDataDisponivel` e `avisos` ausentes do JSON quando não aplicável
- [ ] Nenhum campo de `dataSelecionada` foi alterado (sem regressão no schema)
- [ ] `mvn test` passa sem falhas
