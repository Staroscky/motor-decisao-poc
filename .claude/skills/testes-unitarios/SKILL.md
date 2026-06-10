---
name: testes-unitarios
description: Boas práticas de testes unitários (Kotlin/Spring Boot, JUnit 5, Mockito). Use sempre que for escrever, criar ou revisar um teste, decidir o que testar e o que não testar, nomear casos de teste, montar fixtures/mocks, cobrir edge cases e erros, ou avaliar a qualidade de uma suíte existente. Aplique também ao implementar uma feature que exige testes e ao decidir a fronteira entre teste unitário e teste de integração, mesmo quando o pedido não citar "teste" explicitamente.
---

# Testes Unitários

Use esta Skill para escrever e revisar testes unitários consistentes, rápidos e que testam comportamento, não implementação.

## Arquivos da Skill

- Leia [references/principios.md](references/principios.md) para os princípios (FIRST, AAA, o que testar e o que não testar, nomenclatura).
- Leia [references/kotlin-spring.md](references/kotlin-spring.md) para as convenções da stack (JUnit 5, Mockito, fixtures, sealed classes, fronteira com integração).

## Regras obrigatórias

1. Cada teste segue a estrutura Arrange–Act–Assert (ou given–when–then), com as três partes visíveis.
2. O nome do teste descreve o comportamento esperado, não o método chamado.
3. Um teste verifica um comportamento; várias asserções são aceitáveis apenas se descrevem o mesmo comportamento.
4. Testar comportamento observável e contratos, nunca detalhes internos de implementação.
5. Testes são determinísticos e isolados: sem ordem implícita, sem estado compartilhado, sem dependência de relógio, rede ou I/O real.
6. Mockar apenas colaboradores nas fronteiras (clients, repositórios, serviços externos). Não mockar o domínio puro nem tipos de valor.
7. Cobrir caminho feliz, edge cases e comportamentos de erro relevantes.
8. Sem lógica condicional (`if`/`for`/`when`) dentro do teste; casos variados viram testes parametrizados.

## Execução resumida

1. Identificar a unidade sob teste e seus colaboradores reais.
2. Separar o que é domínio puro (testar direto) do que é fronteira (mockar).
3. Escrever os casos: caminho feliz, edge cases e erros, um comportamento por teste.
4. Nomear cada teste pelo comportamento e estruturar em AAA.
5. Revisar contra `references/principios.md` e as convenções de `references/kotlin-spring.md`.

## Comportamentos de qualidade

- Não testar getters/setters triviais, `data class` gerada ou o próprio framework.
- Não acoplar o teste à implementação a ponto de qualquer refactor quebrá-lo.
- Não usar mocks onde um objeto real simples serviria.
- Não deixar o teste depender de outro teste para passar.
- Não transformar teste unitário em teste de integração disfarçado; integração externa usa as ferramentas de teste de integração apropriadas, na camada adequada.
- Sempre tornar a falha do teste legível: quando quebra, deve apontar o comportamento violado.
