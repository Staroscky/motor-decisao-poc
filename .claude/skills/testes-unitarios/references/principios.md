# Princípios de testes unitários

## FIRST

- **Fast** — milissegundos por teste. Sem rede, banco, disco ou `sleep`.
- **Isolated** — cada teste é independente; nenhuma ordem ou estado compartilhado.
- **Repeatable** — mesmo resultado em qualquer máquina, qualquer hora. Sem dependência de relógio, fuso ou aleatoriedade não controlada.
- **Self-validating** — passa ou falha sozinho, sem inspeção manual de log.
- **Timely** — escrito junto com o código, não muito depois.

## Estrutura AAA / given–when–then

Toda a unidade do teste em três blocos visíveis:

```
// Arrange  (given)  — monta entrada e colaboradores
// Act      (when)   — executa a única ação sob teste
// Assert   (then)   — verifica o comportamento esperado
```

Uma única ação no bloco Act. Se precisa de duas ações para o caso fazer sentido, provavelmente são dois testes.

## Nomenclatura

O nome descreve **comportamento e condição**, não o método:

- Ruim: `testCalcular()`, `deveFuncionar()`
- Bom: `deve rejeitar transferência quando saldo é insuficiente`
- Bom: `retorna erro quando o serviço externo está indisponível`

O nome deve permitir entender o que quebrou sem abrir o corpo do teste.

## O que testar

- Caminho feliz com entradas válidas representativas.
- Edge cases: limites, vazio, nulo, coleção de um elemento, valores extremos.
- Comportamentos de erro: exceções esperadas, fallbacks, ramos de erro e aviso.
- Regras de domínio e invariantes.
- Lógica de decisão e ramificação real (cada ramo relevante).

## O que NÃO testar

- Getters/setters e `data class` geradas pelo compilador.
- Código do framework (Spring, Jackson) — confie que ele funciona; teste a sua configuração só na camada de integração.
- Detalhes internos privados que não afetam o contrato observável.
- Combinações exaustivas sem valor: prefira poucos casos representativos.

## Comportamento, não implementação

Teste **o que** a unidade entrega, não **como**. Sinais de teste acoplado à implementação:

- Verificar ordem exata de chamadas internas que não fazem parte do contrato.
- Assertar sobre campos privados ou estado intermediário.
- Quebrar a cada refactor que preserva o comportamento.

Regra prática: se um refactor que mantém o comportamento quebra o teste, o teste está medindo a coisa errada.

## Mocks com parcimônia

- Mocke apenas **fronteiras**: clients HTTP, repositórios, serviços externos, relógio, geradores.
- Não mocke domínio puro, value objects nem `data class`: use o objeto real.
- Verifique interação (verify) só quando a interação **é** o comportamento (ex.: "deve publicar evento"). Caso contrário, asserte sobre o resultado.

## Casos variados

Sem `if`/`for`/`when` dentro do teste. Para múltiplas combinações de entrada/saída, use testes parametrizados — um caso por linha de dados, com falha rastreável ao caso específico.
