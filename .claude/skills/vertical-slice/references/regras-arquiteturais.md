# Regras arquiteturais

## Direção das dependências dentro do módulo

```
controller  →  service  →  domain
                  │
                  ▼
              integration  →  integration/data
```

- `controller` depende de `service`, nunca o contrário.
- `service` orquestra `domain` e `integration`.
- `domain` é o núcleo: **não depende de nenhuma outra camada** do módulo nem de DTOs externos.
- `integration` depende de seus próprios DTOs em `integration/data`.

Regra prática: as setas apontam para dentro, em direção ao `domain`. Se uma dependência aponta de `domain` para fora, está invertida e deve ser corrigida.

## Encapsulamento entre módulos

- Um módulo só conversa com outro pela **interface pública na raiz** (ex.: `MotorDecisao.kt`).
- É proibido importar `controller`, `service`, `domain` ou `integration` de outro módulo.
- Se um módulo precisa de algo que hoje é interno de outro, o caminho é expor na interface pública do dono — não furar o encapsulamento.
- Cada módulo expõe **um** contrato público. Vários pontos de entrada são sinal de que a fatia deveria ser dividida.

## Tratamento de integrações externas

- Os DTOs em `integration/data/` espelham o contrato da API externa, com os nomes e formatos que ela usa. São descartáveis e mudam quando o fornecedor muda.
- Esses DTOs **não vazam** para `controller` nem para `domain`. A tradução DTO ↔ domínio acontece em `integration` ou `service`.
- Mudança no contrato externo deve ficar contida em `integration/`; idealmente o `domain` nem percebe.

## Como decidir onde um arquivo entra

1. É compartilhado por mais de um módulo? → `core/`.
2. É a forma como a API externa fala? → `integration/data/`.
3. Chama um sistema externo? → `integration/`.
4. É regra ou modelo de negócio puro? → `domain/`.
5. Orquestra um caso de uso? → `service/`.
6. É borda de entrada (REST/SDUI)? → `controller/`.
7. Outro módulo precisa chamar? → expor pela interface pública na raiz.

## Sinais de violação do padrão

- `import` de classe interna de outro módulo.
- DTO de `integration/data` aparecendo em assinatura de `controller` ou `domain`.
- Regra de negócio dentro de `controller` ou de um client de integração.
- Configuração de uma única feature morando em `core/`.
- Módulo expondo mais de uma interface pública na raiz.
