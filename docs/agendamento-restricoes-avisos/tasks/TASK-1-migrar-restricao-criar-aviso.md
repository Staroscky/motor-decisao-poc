# TASK-1 — Migrar Restricao* para modelo/restricao/ e criar Aviso

**Arquivo alvo:** `avaliacao/modelo/restricao/` (criar) + `avaliacao/modelo/Aviso.java` (criar) + `avaliacao/contexto/Restricao*.java` (deletar)
**Referência SPEC:** Seções 8.3, RF-11, RF-12
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

`Restricao`, `RestricaoGenerica` e `RestricaoQrcode` vivem hoje em `avaliacao.contexto`, junto de classes de estado interno como `ResultadoViabilidade`. A SPEC separa esses tipos para `avaliacao.modelo.restricao` — pacote de domínio, sem estado interno. `Aviso` é um tipo novo que também pertence ao mesmo pacote de modelo.

Esta task não muda comportamento — é uma migração de pacote + criação de tipo.

## O que fazer

1. Criar `avaliacao/modelo/restricao/Restricao.java` — copiar conteúdo de `avaliacao/contexto/Restricao.java`, ajustando o `package` para `com.staroscky.motordecisao.avaliacao.modelo.restricao`.

2. Criar `avaliacao/modelo/restricao/RestricaoGenerica.java` — copiar de `avaliacao/contexto/RestricaoGenerica.java`, mesmo ajuste de pacote.

3. Criar `avaliacao/modelo/restricao/RestricaoQrcode.java` — copiar de `avaliacao/contexto/RestricaoQrcode.java`, mesmo ajuste de pacote.

4. Criar `avaliacao/modelo/Aviso.java`:
```java
package com.staroscky.motordecisao.avaliacao.modelo;

import java.time.LocalDate;

public record Aviso(String codigo, LocalDate data, String descricao) {}
```

5. Atualizar `avaliacao/contexto/ResultadoViabilidade.java` — trocar import de `avaliacao.contexto.Restricao` por `avaliacao.modelo.restricao.Restricao`.

6. Atualizar `avaliacao/response/ResultadoViabilidadeDto.java` — mesmo ajuste de import.

7. Deletar os três arquivos originais em `avaliacao/contexto/`:
   - `Restricao.java`
   - `RestricaoGenerica.java`
   - `RestricaoQrcode.java`

## Notas de implementação

- A `@JsonTypeInfo` e `@JsonSubTypes` de `Restricao` devem ser preservadas integralmente — o contrato de serialização não muda.
- Verificar se há outros arquivos que importam `avaliacao.contexto.Restricao*` além dos dois listados. Compilação irá falhar e apontar os que faltaram.
- O diretório `avaliacao/modelo/` não existe ainda — criar junto com o subdiretório `restricao/`.

## Critério de aceite

- [ ] `avaliacao.modelo.restricao.Restricao`, `RestricaoGenerica` e `RestricaoQrcode` existem com o conteúdo correto
- [ ] `avaliacao.modelo.Aviso` existe com os três campos
- [ ] Nenhuma referência a `avaliacao.contexto.Restricao*` permanece no projeto
- [ ] Build compila sem erros
