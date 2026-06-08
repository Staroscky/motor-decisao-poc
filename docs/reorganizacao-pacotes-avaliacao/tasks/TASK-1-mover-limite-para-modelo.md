# TASK-1 — Mover `Limite` de `contexto/` para `modelo/`

**Arquivo alvo:** `avaliacao/modelo/Limite.java` (novo — criado por move)
**Referência SPEC:** Seção 4.1, RF-01, RF-05
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

`Limite` é um `record` puro de domínio (`record Limite(int qtdDiasAlt) {}`). Está em `avaliacao.contexto` sem justificativa de design — o mesmo pacote que contém o contexto de execução (`AvaliacaoContexto`) e os agregados de resultado mutáveis. A regra definida na SPEC é que tipos de domínio imutáveis ficam em `avaliacao.modelo`.

## O que fazer

1. Criar `src/main/java/com/staroscky/motordecisao/avaliacao/modelo/Limite.java` com package `avaliacao.modelo` e conteúdo idêntico ao atual.
2. Deletar `src/main/java/com/staroscky/motordecisao/avaliacao/contexto/Limite.java`.
3. Atualizar o import em `avaliacao/contexto/ResultadoViabilidade.java`:
   - De: `import com.staroscky.motordecisao.avaliacao.contexto.Limite;`
   - Para: `import com.staroscky.motordecisao.avaliacao.modelo.Limite;`
4. Atualizar o import em `avaliacao/response/ResultadoViabilidadeDto.java`:
   - De: `import com.staroscky.motordecisao.avaliacao.contexto.Limite;`
   - Para: `import com.staroscky.motordecisao.avaliacao.modelo.Limite;`
5. Rodar `mvn compile` para confirmar sem erros.

## Notas de implementação

- O conteúdo do arquivo não muda — apenas a linha `package` na linha 1.
- Grep para confirmar que não há outros importadores de `avaliacao.contexto.Limite`:
  `grep -r "avaliacao.contexto.Limite" src/`

## Critério de aceite

- [ ] `src/main/java/com/staroscky/motordecisao/avaliacao/modelo/Limite.java` existe com package `avaliacao.modelo`
- [ ] `src/main/java/com/staroscky/motordecisao/avaliacao/contexto/Limite.java` não existe
- [ ] `ResultadoViabilidade.java` importa `avaliacao.modelo.Limite`
- [ ] `ResultadoViabilidadeDto.java` importa `avaliacao.modelo.Limite`
- [ ] `mvn compile` passa sem erros
