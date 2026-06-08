# PRD — Agendamento: Restrições, Avisos e Pipelines por Instrumento

## Resumo

Evolução do bloco `agendamento` para separar restrições bloqueantes (`Restricao`) de avisos informativos (`Aviso`), com early exit no pipeline a partir da primeira restrição e resposta enriquecida com `proximaDataDisponivel` e `avisos`. Os pipelines de agendamento passam a ser separados por instrumento (PIX, TED, TEF).

## Problema

O bloco `agendamento` hoje usa o mesmo modelo do bloco `dataSelecionada` (`ResultadoViabilidade`), sem distinção entre o que bloqueia o agendamento e o que é apenas informativo. Isso impede:

- early exit correto no pipeline (avisos não devem interromper, restrições sim)
- retorno de sugestão de próxima data disponível quando a data selecionada não é viável
- retorno de avisos sobre datas problemáticas no intervalo (feriados, fins de semana)
- ordenação clara de validadores por instrumento, com validadores sem I/O antes dos que fazem chamadas upstream

## Objetivo

Retornar, para cada instrumento, um bloco `agendamento` que responda de forma independente três perguntas:
1. O agendamento é possível? (`podeAgendar` + `restricoes`)
2. Qual a próxima data válida, quando necessário? (`proximaDataDisponivel`)
3. Existem datas problemáticas no intervalo? (`avisos`)

O bloco `agendamento` deve ser completamente independente do bloco `dataSelecionada` — são perguntas diferentes com respostas independentes.

## Escopo

### Inclui

- Novo modelo interno `ResultadoAgendamento` com `isPodeAgendar()` derivado das restrições
- Novo tipo `Aviso` (codigo, data, descricao)
- Novo record de response `AvaliacaoAgendamento` com serialização condicional de `proximaDataDisponivel` e `avisos`
- Nova interface `ValidadorAgendamento` para os validadores do bloco de agendamento
- Pipelines separados por instrumento: `PipelineAgendamentoPix`, `PipelineAgendamentoTed`, `PipelineAgendamentoTef`
- Early exit no pipeline: interrompe na primeira restrição, nunca em aviso
- Validadores `ProximaData*` por instrumento (executa apenas quando `podeAgendar` e `dataSelecionada` inválida)
- Validadores `AvisoIntervalo*` por instrumento (executa apenas quando `proximaDataDisponivel` foi populada)
- Realocação de `Restricao`, `RestricaoGenerica` e `RestricaoQrcode` para pacote `modelo/restricao/`

### Não inclui

- Alterações em `PipelineDataSelecionada` ou `ResultadoViabilidade`
- Implementação real das regras de negócio dos validadores `ProximaData*` e `AvisoIntervalo*` (regras upstream são TODOs)
- Implementação da regra de agendamento geral para PIX (chave + agconta) — valida elegibilidade, sem I/O
- Autenticação, persistência ou tracing adicional

## Fluxo esperado

Ao chamar `POST /v1/avaliacao`, para cada instrumento disponível o pipeline de agendamento executa os validadores na ordem registrada. Validadores bloqueantes e sem I/O rodam primeiro; se qualquer um adicionar uma restrição, o loop encerra sem chamar validadores de I/O. Caso o agendamento seja permitido e a data selecionada seja inválida, `ProximaData*` busca a próxima data disponível. Em seguida, `AvisoIntervalo*` verifica se há datas problemáticas no intervalo entre a data da request e a próxima data encontrada.

A resposta de cada instrumento passa a incluir `agendamento` com os campos acima.

## Critérios de sucesso

- `dataSelecionada.valido: true` e `agendamento.podeAgendar: false` coexistem corretamente (ex.: QR Code COB)
- `podeAgendar` é sempre derivado da lista de restrições — nunca um campo diretamente mutável
- `proximaDataDisponivel` está ausente quando `dataSelecionada` é válida ou agendamento é bloqueado
- `avisos` está ausente quando o intervalo não tem datas problemáticas
- O pipeline de agendamento nunca chama validadores de I/O quando já há uma restrição bloqueante

## Restrições ou observações

- Invariante de design: o bloco `agendamento` nunca usa `dataSelecionada.isValido()` para decidir se o agendamento é possível — a única exceção permitida é `ProximaData*`, que usa esse estado apenas para decidir se a sugestão é necessária
- A ordem de registro dos validadores em cada pipeline é explícita e importa para o funcionamento do early exit
- `Restricao` permanece como `sealed interface` — não adicionar campos sem subtipo correspondente
