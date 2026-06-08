# TASK-8 — Pipeline TED: validadores e PipelineAgendamentoTed

**Arquivo alvo:** `avaliacao/pipeline/agendamento/ted/` (criar — 4 arquivos)
**Referência SPEC:** Seção 8.6, RF-10
**Depende de:** TASK-5, TASK-6
**Bloqueada por:** nenhuma

---

## Contexto

TED precisa do seu próprio pipeline de agendamento com validadores específicos. Segue o mesmo padrão do pipeline PIX, mas sem o validador de QRCODE (que é exclusivo de PIX) e com lógica de negócio específica de TED nos stubs.

## O que fazer

### 1. `AgendamentoTedValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoTedValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return true;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: regras gerais de agendamento TED
    }
}
```

### 2. `ProximaDataTedValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class ProximaDataTedValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        ResultadoAgendamento agendamento =
            contexto.getResultado(Instrumento.TED).getAgendamento();
        return agendamento.isPodeAgendar()
            && !contexto.getResultado(Instrumento.TED).getDataSelecionada().isValido();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: buscar próxima data disponível para TED via upstream
    }
}
```

### 3. `AvisoIntervaloTedValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class AvisoIntervaloTedValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return contexto.getResultado(Instrumento.TED).getAgendamento().temProximaData();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: verificar feriados e fins de semana no intervalo e adicionar avisos
    }
}
```

### 4. `PipelineAgendamentoTed`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamentoInstrumento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PipelineAgendamentoTed implements PipelineAgendamentoInstrumento {

    private final List<ValidadorAgendamento> validadores;

    public PipelineAgendamentoTed(
        AgendamentoTedValidador agendamento,
        ProximaDataTedValidador proximaData,
        AvisoIntervaloTedValidador avisoIntervalo
    ) {
        this.validadores = List.of(agendamento, proximaData, avisoIntervalo);
    }

    @Override
    public void executar(AvaliacaoContexto contexto) {
        ResultadoAgendamento resultado =
            contexto.getResultado(Instrumento.TED).getAgendamento();

        for (ValidadorAgendamento validador : validadores) {
            if (!validador.suporta(contexto)) continue;
            validador.validar(contexto, resultado);
            if (!resultado.isPodeAgendar()) break;
        }
    }
}
```

## Notas de implementação

- `AgendamentoTedValidador.suporta()` retorna `true` incondicionalmente — TED é sempre avaliado quando presente nos instrumentos da request. Ajustar quando as regras de elegibilidade forem conhecidas.
- O padrão de `suporta()` em `ProximaDataTedValidador` e `AvisoIntervaloTedValidador` é idêntico ao PIX, apenas com `Instrumento.TED`.

## Critério de aceite

- [ ] Os 4 arquivos em `pipeline/agendamento/ted/` existem e compilam
- [ ] `ProximaDataTedValidador.suporta()` retorna `false` quando `isPodeAgendar()` é `false`
- [ ] `AvisoIntervaloTedValidador.suporta()` retorna `false` quando `temProximaData()` é `false`
- [ ] Build compila sem erros
