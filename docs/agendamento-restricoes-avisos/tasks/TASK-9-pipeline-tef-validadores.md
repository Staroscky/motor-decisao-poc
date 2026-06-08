# TASK-9 — Pipeline TEF: validadores e PipelineAgendamentoTef

**Arquivo alvo:** `avaliacao/pipeline/agendamento/tef/` (criar — 4 arquivos)
**Referência SPEC:** Seção 8.6, RF-10
**Depende de:** TASK-5, TASK-6
**Bloqueada por:** nenhuma

---

## Contexto

TEF precisa do seu próprio pipeline de agendamento. A estrutura é idêntica ao TED — três validadores (sem o validador de QRCODE de PIX) com stubs para lógica de negócio específica de TEF.

## O que fazer

### 1. `AgendamentoTefValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoTefValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return true;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: regras gerais de agendamento TEF
    }
}
```

### 2. `ProximaDataTefValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class ProximaDataTefValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        ResultadoAgendamento agendamento =
            contexto.getResultado(Instrumento.TEF).getAgendamento();
        return agendamento.isPodeAgendar()
            && !contexto.getResultado(Instrumento.TEF).getDataSelecionada().isValido();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: buscar próxima data disponível para TEF via upstream
    }
}
```

### 3. `AvisoIntervaloTefValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class AvisoIntervaloTefValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return contexto.getResultado(Instrumento.TEF).getAgendamento().temProximaData();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: verificar feriados e fins de semana no intervalo e adicionar avisos
    }
}
```

### 4. `PipelineAgendamentoTef`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamentoInstrumento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PipelineAgendamentoTef implements PipelineAgendamentoInstrumento {

    private final List<ValidadorAgendamento> validadores;

    public PipelineAgendamentoTef(
        AgendamentoTefValidador agendamento,
        ProximaDataTefValidador proximaData,
        AvisoIntervaloTefValidador avisoIntervalo
    ) {
        this.validadores = List.of(agendamento, proximaData, avisoIntervalo);
    }

    @Override
    public void executar(AvaliacaoContexto contexto) {
        ResultadoAgendamento resultado =
            contexto.getResultado(Instrumento.TEF).getAgendamento();

        for (ValidadorAgendamento validador : validadores) {
            if (!validador.suporta(contexto)) continue;
            validador.validar(contexto, resultado);
            if (!resultado.isPodeAgendar()) break;
        }
    }
}
```

## Notas de implementação

- TEF é usado exclusivamente em contas Itaú (ISPB `60701190`) — isso é resolvido em `InstrumentoResolver`, não aqui. `AgendamentoTefValidador` pode futuramente checar regras adicionais de TEF.
- O padrão dos `suporta()` e do loop é idêntico ao TED, apenas com `Instrumento.TEF`.

## Critério de aceite

- [ ] Os 4 arquivos em `pipeline/agendamento/tef/` existem e compilam
- [ ] `ProximaDataTefValidador.suporta()` verifica `isPodeAgendar()` antes de `dataSelecionada`
- [ ] Build compila sem erros
