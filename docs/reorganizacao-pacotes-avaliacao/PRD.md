# PRD — Reorganização dos Pacotes de `avaliacao/`

## Resumo

Redefinir as regras de organização dos subpacotes dentro de `avaliacao/` e aplicá-las ao código atual, tornando a estrutura previsível e fácil de manter conforme o projeto cresce.

## Problema

A evolução orgânica do módulo `avaliacao/` criou duas ambiguidades estruturais que dificultam a manutenção:

1. **Dois pacotes para tipos de domínio sem fronteira clara:** `contexto/` contém o contexto de execução (`AvaliacaoContexto`) e agregados de resultado mutáveis (`ResultadoInstrumento`, `ResultadoViabilidade`, `ResultadoAgendamento`), mas também o value object `Limite`. O pacote `modelo/` contém os value objects `Aviso` e `Restricao*`. Não existe regra documentada para decidir onde um novo tipo de domínio deve ir — a evidência é o fato de `Limite` estar em `contexto/` enquanto `Aviso` está em `modelo/`, sem justificativa aparente.

2. **Validators em dois lugares sem critério explícito:** `validador/` contém a interface `Validador` e os validators de viabilidade (`data/`, `instituicao/`). Mas a interface `ValidadorAgendamento` e os validators de agendamento (`AgendamentoPix*`, `ProximaData*`, `AvisoIntervalo*`) ficam em `pipeline/agendamento/pix|ted|tef/`. Um desenvolvedor que busca "todos os validators" encontrará apenas metade deles em `validador/`.

## Objetivo

Após a entrega, o projeto deve ter regras de organização de pacotes explícitas e aplicadas:
- Um novo tipo de domínio imutável sempre vai para `modelo/`
- Um novo validator sempre vai para `validador/`
- `pipeline/` contém apenas orquestração de pipeline, sem validators embutidos

## Escopo

### Inclui

- Definição documentada das regras de fronteira de cada subpacote
- Mover `Limite.java` de `contexto/` para `modelo/`
- Mover `ValidadorAgendamento.java` de `pipeline/agendamento/` para `validador/agendamento/`
- Mover os 10 validators de agendamento de `pipeline/agendamento/pix|ted|tef/` para `validador/agendamento/pix|ted|tef/`
- Atualizar declarações de pacote e imports em todos os arquivos afetados

### Não inclui

- Alterar comportamento de nenhum validator, pipeline ou mapper
- Mover `Instrumento` e `TipoCheckin` de `resolver/` (são coesos com o resolver)
- Criar novos validators ou pipelines
- Alterar testes além do necessário para compilar

## Fluxo esperado

Após a reorganização, um desenvolvedor que precise adicionar um novo validator de agendamento para PIX sabe que cria o arquivo em `validador/agendamento/pix/`, registra na lista do `PipelineAgendamentoPix` e pronto — sem precisar descobrir se validators ficam em `pipeline/` ou em `validador/`.

## Critérios de sucesso

- Todos os tipos de domínio imutáveis estão em `modelo/` (incluindo `Limite`)
- Todos os validators e interfaces de validator estão em `validador/` (incluindo `ValidadorAgendamento` e os validators de agendamento por instrumento)
- `pipeline/` contém apenas classes de pipeline (sem validator, sem interface de validator)
- Build e todos os testes existentes passam sem regressão
- Nenhum comportamento funcional foi alterado

## Restrições ou observações

- A feature `agendamento-restricoes-avisos` está em implementação simultânea — a reorganização deve ser aplicada sobre o estado final esperado dessa feature (classes já nos novos pacotes), não sobre o estado intermediário
- A SPEC da feature `agendamento-restricoes-avisos` documentou a decisão de co-locar validators com pipelines — esta reorganização revisa essa decisão com base na inconsistência observada
- Sem Lombok no projeto — nenhuma anotação nova necessária
