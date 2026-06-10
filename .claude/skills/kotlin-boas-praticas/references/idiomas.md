# Idiomas de Kotlin

## Nulabilidade

- Tipos não-nulos por padrão; só marque `?` quando o nulo é um estado real e significativo.
- Nunca use `!!` para calar o compilador. Se sabe que não é nulo, prove: `requireNotNull(x) { "x é obrigatório" }`.
- Acesso seguro e default: `valor?.trim() ?: ""`.
- `let` para executar só quando não-nulo: `usuario?.let { enviar(it) }`.
- Não devolva `null` para representar erro quando há um tipo melhor (ver `modelagem.md`).

## Imutabilidade

- `val` por padrão; `var` só quando a mutação é essencial e local.
- Exponha coleções como `List`/`Map`/`Set` (somente-leitura), não `MutableList` etc.
- `data class` com `val`; para "mudar", use `copy(...)`.
- Prefira funções puras: receber, transformar, devolver — sem efeito colateral escondido.

## Expressões sobre statements

`if`, `when` e `try` produzem valor — use isso:

```kotlin
val faixa = when {
    valor < 100  -> Faixa.BAIXA
    valor < 1000 -> Faixa.MEDIA
    else         -> Faixa.ALTA
}
```

Evite atribuir dentro de ramos quando a expressão direta é mais clara.

## Scope functions

Escolha por intenção, não por hábito:

- `let` — transformar/usar um valor, sobretudo após `?.`.
- `run` — executar um bloco e devolver o resultado; bom para configurar e calcular.
- `apply` — configurar um objeto e devolvê-lo (`this`).
- `also` — efeito colateral sobre o valor (log, validação) devolvendo o próprio valor.
- `with` — agrupar chamadas sobre um receiver.

Não aninhe scope functions a ponto de `it`/`this` ficarem ambíguos. Se confundiu, extraia uma função nomeada.

## Coleções e funções de ordem superior

- Prefira `map`/`filter`/`associate`/`groupBy` a loops manuais quando expressam a intenção.
- Cuidado com cadeias longas que criam muitas coleções intermediárias em caminho quente; use `asSequence()` quando a cadeia é longa e a fonte é grande.
- `firstOrNull`/`singleOrNull` em vez de `first`/`single` quando ausência é possível.

## Hot path / alta RPS

- Em iteração de listas grandes e alto throughput, conte alocações: evite boxing, cadeias intermediárias e regex em laço quente.
- Uma máquina de estados de passagem única costuma vencer regex/split repetido (ex.: parsing de strings em laço quente).
- Otimize com medição, e só onde o caminho é realmente quente — no caminho frio, clareza ganha.

## Funções e extensões

- Funções pequenas, com nome que diz a intenção.
- Extensões só quando aumentam legibilidade no domínio certo; evite extensões genéricas em tipos amplos (`Any`, `String`) que poluem o autocomplete global.
- Argumentos nomeados e defaults em vez de múltiplas sobrecargas.

## Kotlin + Spring

- Injeção por construtor; evite `lateinit var` para dependências.
- `@ConfigurationProperties` com `data class` para configuração tipada.
- Beans e componentes como `class` final (padrão Kotlin); abra só o necessário.
