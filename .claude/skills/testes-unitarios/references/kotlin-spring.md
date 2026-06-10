# Convenções da stack (Kotlin / Spring Boot)

> Pressuposto de stack: JUnit 5 + Mockito (com `mockito-kotlin`) para unitário; ferramentas de stub HTTP (ex.: WireMock) e de asserção de payload (ex.: JSONAssert) para integração. Ajuste se o projeto usar outro conjunto.

## Estrutura e nomenclatura de arquivo

- Teste fica em `src/test/kotlin`, espelhando o pacote da classe sob teste.
- Nome da classe: `<ClasseSobTeste>Test`.
- Nomes de teste em linguagem natural com backticks:

```kotlin
@Test
fun `retorna erro quando saldo é insuficiente`() { ... }
```

## JUnit 5

- `@Test` para casos simples; `@ParameterizedTest` com `@MethodSource`/`@CsvSource` para variações.
- `@Nested` para agrupar cenários de um mesmo comportamento.
- `@BeforeEach` só para setup genuinamente comum; prefira builders/fixtures locais a setup compartilhado que esconde o Arrange.
- Asserções: uma forma só no projeto (AssertJ ou kotlin.test). Para múltiplas asserções do mesmo comportamento, use `assertAll`.

## Mockito (com JUnit 5)

- Habilite a extensão: `@ExtendWith(MockitoExtension::class)`.
- Declare mocks com `@Mock` e injete no SUT com `@InjectMocks`, ou crie via `mock()` no Arrange.
- Prefira o wrapper `mockito-kotlin`: `whenever(colaborador.metodo()).thenReturn(...)` evita o conflito com `when` (palavra reservada do Kotlin) e lida melhor com generics e nulabilidade.
- Classes Kotlin são `final` por padrão. O Mockito 5 já usa o inline mock maker por padrão e mocka finais; em versões anteriores, habilite `mockito-inline`.
- `verify(colaborador).metodo()` apenas quando a interação é o comportamento sob teste; caso contrário, asserte o resultado.
- Use `any()`/`eq()`/`argumentCaptor` do `mockito-kotlin` para argumentos. O `MockitoExtension` é estrito: acusa stub declarado e não usado, então não crie stub a mais.
- Para funções suspensas, combine o mock com `runTest`/`runBlocking` no Act.

## Fixtures e dados de teste

- Builders ou funções fábrica para montar objetos de domínio válidos, com sobrescrita só do campo relevante ao caso:

```kotlin
fun umPedido(valor: BigDecimal = BigDecimal.TEN) = Pedido(valor = valor, /* defaults válidos */)
```

- Evite literais mágicos espalhados; nomeie o que importa para o caso.

## Sealed classes e exaustividade

Para hierarquias seladas (resultado, decisão, estado), teste cada variante relevante e use `when` exaustivo na asserção para que uma variante nova force revisão do teste:

```kotlin
when (val r = resultado) {
    is Sucesso -> assertThat(r.valor).isEqualTo(...)
    is Falha   -> fail("esperava Sucesso")
}
```

## Lógica com ramos e early-exit

Quando há fluxo que pode interromper cedo (validação que barra, fallback), teste: o ramo que interrompe interrompe de fato; os passos seguintes não executam após a interrupção; o resultado agregado reflete a primeira condição encontrada. Cada componente isolado também tem seu próprio teste unitário.

## Fronteira unitário × integração

- **Unitário**: lógica de domínio, serviços, mapeamentos, regras — sem Spring context, sem rede.
- **Integração**: comportamento real contra dependências externas com stubs HTTP e validação de payload; configuração de clients, decoders e filtros.
- Não suba `@SpringBootTest` para testar regra de domínio pura — é lento e frágil para esse fim.

## Concorrência

- Teste a lógica de composição/agregação de forma determinística, sem depender de timing real.
- Para chamadas assíncronas (futures, suspensão), complete-as de forma controlada no Arrange em vez de confiar em corrida real de threads.
