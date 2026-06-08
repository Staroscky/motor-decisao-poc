# TASK-5 — Atualizar ResultadoInstrumento

**Arquivo alvo:** `avaliacao/contexto/ResultadoInstrumento.java` (modificar)
**Referência SPEC:** Seção 4.1
**Depende de:** TASK-3
**Bloqueada por:** nenhuma

---

## Contexto

`ResultadoInstrumento` tem hoje dois campos do tipo `ResultadoViabilidade`: `dataSelecionada` e `agendamento`. O campo `agendamento` deve passar a ser `ResultadoAgendamento`, que é o modelo correto para o bloco de agendamento.

## O que fazer

Atualizar `avaliacao/contexto/ResultadoInstrumento.java`:

```java
package com.staroscky.motordecisao.avaliacao.contexto;

public class ResultadoInstrumento {

    private final ResultadoViabilidade dataSelecionada = new ResultadoViabilidade();
    private final ResultadoAgendamento agendamento     = new ResultadoAgendamento();

    public ResultadoViabilidade getDataSelecionada() {
        return dataSelecionada;
    }

    public ResultadoAgendamento getAgendamento() {
        return agendamento;
    }
}
```

## Notas de implementação

- Apenas o tipo do campo `agendamento` e o seu getter mudam. `dataSelecionada` permanece `ResultadoViabilidade`.
- Esta é a mudança de maior impacto em cascata: qualquer código que chame `contexto.getResultado(instrumento).getAgendamento()` e espere um `ResultadoViabilidade` vai falhar na compilação. Esses pontos são tratados nas tasks subsequentes (TASK-7 a TASK-11).

## Critério de aceite

- [ ] `ResultadoInstrumento.getAgendamento()` retorna `ResultadoAgendamento`
- [ ] `ResultadoInstrumento.getDataSelecionada()` continua retornando `ResultadoViabilidade`
- [ ] Build pode falhar neste ponto nos sites de uso — isso é esperado e será resolvido nas tasks seguintes
