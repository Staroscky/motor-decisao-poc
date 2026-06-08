# TASK-7 — Pipeline PIX: validadores e PipelineAgendamentoPix

**Arquivo alvo:** `avaliacao/pipeline/agendamento/pix/` (criar — 5 arquivos) + `validador/agendamento/` (deletar 2 arquivos) + teste (atualizar)
**Referência SPEC:** Seções 8.6, 8.7, RF-03, RF-04, RF-05, RF-06
**Depende de:** TASK-5, TASK-6
**Bloqueada por:** nenhuma

---

## Contexto

Esta task cria todos os validadores de agendamento específicos para PIX e o pipeline que os orquestra com early exit. Também remove os validadores antigos (`PermiteAgendamentoValidador` e `ProximaDataValidador`) que são substituídos aqui e nas tasks de TED e TEF.

## O que fazer

### 1. `AgendamentoPixQrcodeValidador` (substitui `PermiteAgendamentoValidador`)

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.RestricaoQrcode;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.request.QrcodeAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoPixQrcodeValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return contexto.getTipoCheckin() == TipoCheckin.QRCODE;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        QrcodeAvaliacaoRequest req = (QrcodeAvaliacaoRequest) contexto.getRequest();
        if (!tipoPermiteAgendamento(req.qrcode().tipo())) {
            resultado.adicionarRestricao(new RestricaoQrcode(
                "QR_CODE_NAO_PERMITE",
                new RestricaoQrcode.ContextoQrcode(req.qrcode().tipo())
            ));
        }
    }

    private boolean tipoPermiteAgendamento(String tipoQrcode) {
        // TODO: definir quais tipos permitem agendamento (ex.: "COBV" sim, "COB" não)
        return false;
    }
}
```

### 2. `AgendamentoPixValidador` (cobre chave e agconta)

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoPixValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return contexto.getTipoCheckin() != TipoCheckin.QRCODE;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: regras gerais de agendamento PIX para chave e agconta
    }
}
```

### 3. `ProximaDataPixValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class ProximaDataPixValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        ResultadoAgendamento agendamento =
            contexto.getResultado(Instrumento.PIX).getAgendamento();
        return agendamento.isPodeAgendar()
            && !contexto.getResultado(Instrumento.PIX).getDataSelecionada().isValido();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: buscar próxima data disponível para PIX via upstream
    }
}
```

### 4. `AvisoIntervaloPixValidador`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class AvisoIntervaloPixValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return contexto.getResultado(Instrumento.PIX).getAgendamento().temProximaData();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: verificar feriados e fins de semana no intervalo e adicionar avisos
    }
}
```

### 5. `PipelineAgendamentoPix`

```java
package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamentoInstrumento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PipelineAgendamentoPix implements PipelineAgendamentoInstrumento {

    private final List<ValidadorAgendamento> validadores;

    public PipelineAgendamentoPix(
        AgendamentoPixQrcodeValidador agendamentoQrcode,
        AgendamentoPixValidador agendamento,
        ProximaDataPixValidador proximaData,
        AvisoIntervaloPixValidador avisoIntervalo
    ) {
        this.validadores = List.of(agendamentoQrcode, agendamento, proximaData, avisoIntervalo);
    }

    @Override
    public void executar(AvaliacaoContexto contexto) {
        ResultadoAgendamento resultado =
            contexto.getResultado(Instrumento.PIX).getAgendamento();

        for (ValidadorAgendamento validador : validadores) {
            if (!validador.suporta(contexto)) continue;
            validador.validar(contexto, resultado);
            if (!resultado.isPodeAgendar()) break;
        }
    }
}
```

### 6. Deletar classes antigas

- Deletar `avaliacao/validador/agendamento/PermiteAgendamentoValidador.java`
- Deletar `avaliacao/validador/agendamento/ProximaDataValidador.java`

### 7. Atualizar teste

Renomear `PermiteAgendamentoValidadorTest` para `AgendamentoPixQrcodeValidadorTest` e atualizar:
- Instanciar `AgendamentoPixQrcodeValidador` no lugar de `PermiteAgendamentoValidador`
- Substituir `ResultadoViabilidade resultado` por `ResultadoAgendamento resultado`
- Ajustar asserção: `resultado.isPodeAgendar()` em vez de `resultado.isValido()`
- Remover parâmetro `Instrumento` dos calls de `suporta()` e `validar()`

## Notas de implementação

- A ordem dos validadores no construtor de `PipelineAgendamentoPix` é explícita e importa: `QrcodeValidador` → `PixValidador` → `ProximaData` → `AvisoIntervalo`. Bloqueantes sem I/O antes dos que dependem de estado computado.
- `ProximaDataPixValidador.suporta()` verifica `isPodeAgendar()` **antes** de `isValido()` — esse é o comportamento correto que o antigo `ProximaDataValidador` não tinha.
- `AvisoIntervaloPixValidador` nunca chama `adicionarRestricao()` — apenas `adicionarAviso()`.

## Critério de aceite

- [ ] Os 5 arquivos em `pipeline/agendamento/pix/` existem e compilam
- [ ] `AgendamentoPixQrcodeValidador.suporta()` retorna `true` apenas para `TipoCheckin.QRCODE`
- [ ] `ProximaDataPixValidador.suporta()` retorna `false` quando `isPodeAgendar()` é `false`, independente de `dataSelecionada`
- [ ] `AvisoIntervaloPixValidador.suporta()` retorna `false` quando `temProximaData()` é `false`
- [ ] `PipelineAgendamentoPix.executar()` interrompe após restrição (early exit)
- [ ] Arquivos antigos em `validador/agendamento/` foram deletados
- [ ] `AgendamentoPixQrcodeValidadorTest` passa com o novo modelo
- [ ] Build compila sem erros
