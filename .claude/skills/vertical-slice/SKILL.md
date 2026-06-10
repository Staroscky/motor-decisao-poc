---
name: vertical-slice
description: Padrão arquitetural do projeto, organizado por Vertical Slice. Use sempre que for criar um novo módulo/feature, decidir onde colocar um arquivo (controller, domain, service, integration), adicionar um client de API externa, expor uma capacidade de um módulo para outros, ou revisar se a estrutura de pastas e as dependências entre camadas seguem o padrão. Aplique mesmo quando o pedido não citar "vertical slice" explicitamente, sempre que envolver criação ou movimentação de arquivos de código de produção.
---

# Vertical Slice

Use esta Skill para criar e revisar código seguindo o padrão arquitetural do projeto: cada feature é uma fatia vertical autocontida, com camadas internas próprias e um único contrato público para comunicação com outros módulos.

## Arquivos da Skill

- Leia [references/estrutura.md](references/estrutura.md) para a estrutura de pastas canônica, o papel de cada camada e onde cada tipo de arquivo deve ficar.
- Leia [references/regras-arquiteturais.md](references/regras-arquiteturais.md) para as regras de dependência entre camadas, encapsulamento de módulo e tratamento de integrações externas.

## Regras obrigatórias

1. Cada feature é uma fatia vertical autocontida em seu próprio pacote raiz (ex.: `motorDecisao/`).
2. As camadas internas de um módulo são `controller`, `domain`, `service` e `integration`.
3. Código transversal e compartilhado entre módulos fica em `core/` (ex.: `ConfigGeral.kt`), nunca duplicado dentro das fatias.
4. A comunicação entre módulos acontece somente pela interface pública na raiz do módulo (ex.: `MotorDecisao.kt`). Nenhum módulo enxerga as camadas internas de outro.
5. Detalhes de integração externa (clients e DTOs da API externa) ficam isolados em `integration/`, com os DTOs em `integration/data/`.
6. Modelos de `domain` não dependem de DTOs de `integration/data`; a conversão acontece dentro de `integration` ou `service`.
7. Confirmar o nome do módulo antes de criar a pasta raiz e a interface pública.

## Execução resumida

1. Identificar se o trabalho cria um novo módulo, estende um existente ou adiciona uma integração.
2. Confirmar o nome do módulo e da interface pública quando for um módulo novo.
3. Posicionar cada arquivo na camada correta conforme `references/estrutura.md`.
4. Garantir que dependências externas entrem por `integration/` e não vazem para `domain`.
5. Expor para fora apenas a interface pública na raiz do módulo.
6. Revisar o resultado contra as regras de dependência em `references/regras-arquiteturais.md`.

## Comportamentos de qualidade

- Não criar camadas fora do conjunto padrão (`controller`, `domain`, `service`, `integration`) sem necessidade explícita.
- Não permitir que um módulo importe classes internas de outro módulo.
- Não deixar DTOs de API externa vazarem para `controller` ou `domain`.
- Não colocar configuração compartilhada dentro de uma fatia; ela pertence a `core/`.
- Não expor mais de um ponto de entrada por módulo além da interface pública.
- Sempre manter a fatia autocontida: tudo que a feature precisa vive dentro dela, exceto o transversal de `core/`.
