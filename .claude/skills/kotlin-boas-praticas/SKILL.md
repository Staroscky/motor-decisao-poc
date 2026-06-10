---
name: kotlin-boas-praticas
description: Boas práticas e idiomas de Kotlin do projeto (Kotlin/Spring Boot). Use sempre que for escrever ou revisar código Kotlin, criar uma classe ou função, modelar dados de domínio, decidir entre data/sealed/value class, lidar com nulabilidade, escolher imutabilidade, usar scope functions, ou avaliar legibilidade e idiomático de um trecho. Aplique também ao decidir como modelar erros e estados e em código de hot path/alta RPS, mesmo quando o pedido não citar "Kotlin" explicitamente.
---

# Kotlin Boas Práticas

Use esta Skill para escrever Kotlin idiomático, seguro contra nulos e fácil de manter, alinhado ao estilo do projeto.

## Arquivos da Skill

- Leia [references/idiomas.md](references/idiomas.md) para nulabilidade, imutabilidade, expressões, scope functions e coleções.
- Leia [references/modelagem.md](references/modelagem.md) para data class, sealed class/interface, value class, enums e modelagem de erro e estado.

## Regras obrigatórias

1. Nulabilidade explícita: evitar `!!`; usar `?.`, `?:`, `requireNotNull`/`checkNotNull` com mensagem, e tipos não-nulos por padrão.
2. Imutabilidade primeiro: `val` sobre `var`, coleções somente-leitura, `data class` com `val`.
3. Modelar hierarquias fechadas com `sealed class`/`sealed interface` e consumir com `when` exaustivo (sem `else` que mascare casos novos).
4. Usar `value class` (inline) para identificadores e wrappers de valor, em vez de tipos primitivos crus.
5. Preferir expressões a statements: `if`, `when` e `try` como expressão quando produzem valor.
6. Scope functions com intenção clara (`let`/`run`/`apply`/`also`/`with`), sem aninhar a ponto de prejudicar a leitura.
7. Em hot path/alta RPS, preferir passagem única e estruturas eficientes; evitar regex e alocações desnecessárias onde uma máquina de estados resolve.

## Execução resumida

1. Definir os tipos primeiro: o que é dado, o que é estado fechado, o que é identificador.
2. Escolher data/sealed/value class conforme `references/modelagem.md`.
3. Garantir nulabilidade e imutabilidade corretas desde a assinatura.
4. Escrever a lógica com expressões e scope functions idiomáticas.
5. Revisar legibilidade e, em caminho quente, custo de alocação/iteração.

## Comportamentos de qualidade

- Não usar `!!` para silenciar o compilador; tratar o nulo de verdade.
- Não usar `lateinit` para contornar injeção; preferir injeção por construtor.
- Não criar funções de extensão genéricas demais ou em escopo amplo demais.
- Não abusar de scope functions a ponto de esconder o fluxo.
- Não modelar estados mutuamente exclusivos com flags booleanas soltas; usar `sealed`.
- Não otimizar prematuramente fora do hot path; clareza vence micro-otimização no caminho frio.
- Sempre deixar o tipo contar a história: estado impossível não deve ser representável.
