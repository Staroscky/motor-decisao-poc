# PRD — BFF de Avaliação de Transação

## Resumo

Endpoint BFF responsável por avaliar quais instrumentos de pagamento estão disponíveis e viáveis para uma transação, tanto para execução na data selecionada quanto para agendamento futuro.

## Problema

Sistemas consumer precisam saber, antes de executar uma transação, quais instrumentos de pagamento (PIX, TED, TEF) estão disponíveis e quais restrições se aplicam para a data pretendida e para agendamento. Sem avaliação centralizada, cada sistema precisaria replicar lógica de elegibilidade, horários, limites e regras por instrumento.

## Objetivo

Centralizar a avaliação de viabilidade de instrumentos de pagamento: dado um `checkinId`, uma data e a conta de origem, retornar para cada instrumento disponível o resultado para a data selecionada e para agendamento futuro, com as restrições que se aplicam.

## Escopo

### Inclui

- Endpoint `POST /v1/avaliacao` com request polimórfico (QR Code, Chave Pix, Conta)
- Derivação automática dos instrumentos disponíveis a partir do tipo de checkin e do ISPB da instituição de destino
- Avaliação de viabilidade por instrumento para data selecionada
- Avaliação de viabilidade por instrumento para agendamento
- Restrições com contexto por instrumento (ex.: QR Code de tipo COB não permite agendamento)
- Resolução de próxima data disponível quando a data selecionada não é viável

### Não inclui

- Execução ou submissão da transação
- Persistência das avaliações realizadas
- Autenticação e autorização
- Definição de limites e regras de negócio de cada validador (escopo de implementação, não de produto)

## Fluxo esperado

O sistema cliente envia `checkinId`, `data` e `origem`. O BFF identifica o tipo de checkin pelo ID, consulta o ISPB da instituição de destino, deriva os instrumentos disponíveis e avalia cada um para a data selecionada e para agendamento. A resposta contém os instrumentos avaliados com viabilidade, restrições e ordenação.

## Critérios de sucesso

- `POST /v1/avaliacao` responde com a lista de instrumentos avaliados para qualquer tipo de checkin válido
- Cada instrumento retorna dois blocos: `dataSelecionada` e `agendamento`
- Restrições com contexto são retornadas por instrumento quando aplicável
- Derivação dos instrumentos segue a tabela:
  - `qrcode` ou `chaves_pix` → PIX
  - `agconta` + ISPB Itaú (`60701190`) → PIX, TEF
  - `agconta` + outro ISPB → PIX, TED
- TED retorna lista de finalidades disponíveis

## Restrições ou observações

- O tipo de checkin é inferido do próprio `checkinId` (prefixo `checkin:<tipo>:`) — sem campo discriminador no payload
- O polimorfismo de request e response usa `@JsonTypeInfo(use = DEDUCTION)` do Jackson
- O ISPB é buscado via cliente upstream após o parse do `checkinId`
