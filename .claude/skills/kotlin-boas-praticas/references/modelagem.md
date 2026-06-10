# Modelagem em Kotlin

## Escolha do tipo

| Precisa de... | Use |
|---|---|
| Agrupar dados com igualdade por valor | `data class` |
| Conjunto fechado de variantes com dados próprios | `sealed class` / `sealed interface` |
| Conjunto fixo de constantes sem dados ricos | `enum class` |
| Envolver um valor único com significado de domínio | `value class` (inline) |
| Hierarquia aberta para extensão por terceiros | `interface` / `abstract class` |

## data class

- Para modelos de domínio e DTOs: campos `val`, igualdade e `copy` gratuitos.
- Não coloque lógica pesada na `data class`; ela representa dado.
- Evite `data class` com muitos campos opcionais nuláveis — sinal de que há variantes que deveriam ser `sealed`.

## sealed class / sealed interface

- Para estados mutuamente exclusivos: resultado, decisão, retorno polimórfico.
- Consuma com `when` exaustivo **sem `else`**, para que adicionar uma variante force a revisão de todos os pontos de uso:

```kotlin
sealed interface Resultado
data class Sucesso(val valor: String) : Resultado
data class Falha(val motivo: String) : Resultado
object Vazio : Resultado

val mensagem = when (resultado) {
    is Sucesso -> resultado.valor
    is Falha   -> resultado.motivo
    Vazio      -> "sem resultado"
}
```

- Bom para: representar sucesso/falha tipado, modelar resultados de cache ou de validação, request/response polimórfico.

## value class

- Para identificadores e wrappers: em vez de `String`/`Long` crus circulando pelo código.
- Ganha segurança de tipo (não trocar um id por outro) sem custo de alocação no caminho comum.

```kotlin
@JvmInline
value class UserId(val valor: String)
```

## enum class

- Para conjuntos fixos e simples (status, tipo, categoria). Se cada constante precisa de comportamento/dado rico e divergente, considere `sealed`.

## Modelagem de erro e estado

- Erros **esperados** (regra de negócio, indisponibilidade) modelados no tipo de retorno (`sealed`), não como exceção.
- Exceções para o **excepcional** e irrecuperável no nível local.
- Não use `null` como "deu errado": diz pouco. Um `sealed` diz o porquê.
- Torne estados impossíveis irrepresentáveis: se dois campos nunca coexistem, são duas variantes `sealed`, não dois nuláveis na mesma classe.

## Desserialização polimórfica (Jackson)

- Hierarquias `sealed` com `@JsonTypeInfo` (ou dedução por campos) para request/response polimórfico mantêm o domínio fechado e exaustivo no consumo.
- Mantenha o DTO externo separado do modelo de domínio: a forma do JSON externo não deve ditar o desenho do domínio.
