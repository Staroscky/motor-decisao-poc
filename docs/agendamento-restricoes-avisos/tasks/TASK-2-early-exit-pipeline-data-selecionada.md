# TASK-2 — Corrigir PipelineDataSelecionada: early exit e reposicionamento de pacote

**Arquivo alvo:** `avaliacao/pipeline/data/PipelineDataSelecionada.java` (criar) + `avaliacao/pipeline/PipelineDataSelecionada.java` (deletar)
**Referência SPEC:** Seções 8.8, RF-13
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

`PipelineDataSelecionada` atualmente usa `stream().filter().forEach()` — o Java Stream API não tem mecanismo de interrupção, então mesmo que um validador adicione uma restrição (tornando `resultado.isValido() == false`), os validadores seguintes continuam executando. Isso é ineficiente e pode chamar validadores de I/O desnecessariamente.

O pipeline também precisa ser movido para o subpacote `pipeline/data/` para alinhar com a nova estrutura de pacotes.

## O que fazer

1. Criar `avaliacao/pipeline/data/PipelineDataSelecionada.java` com o loop corrigido:

```java
package com.staroscky.motordecisao.avaliacao.pipeline.data;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.validador.Validador;
import com.staroscky.motordecisao.avaliacao.validador.data.DataValidaValidador;
import com.staroscky.motordecisao.avaliacao.validador.data.HorarioPermitidoValidador;
import com.staroscky.motordecisao.avaliacao.validador.data.LimiteDiarioValidador;
import com.staroscky.motordecisao.avaliacao.validador.instituicao.LimiteInstituicaoValidador;
import com.staroscky.motordecisao.avaliacao.validador.instituicao.StatusInstituicaoValidador;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PipelineDataSelecionada {

    private final List<Validador> validadores;

    public PipelineDataSelecionada(
        StatusInstituicaoValidador statusInstituicao,
        LimiteInstituicaoValidador limiteInstituicao,
        HorarioPermitidoValidador horario,
        LimiteDiarioValidador limiteDiario,
        DataValidaValidador dataValida
    ) {
        this.validadores = List.of(
            statusInstituicao, limiteInstituicao, horario, limiteDiario, dataValida
        );
    }

    public void executar(AvaliacaoContexto contexto) {
        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado =
                contexto.getResultado(instrumento).getDataSelecionada();

            for (Validador validador : validadores) {
                if (!validador.suporta(contexto, instrumento)) continue;
                validador.validar(contexto, instrumento, resultado);
                if (!resultado.isValido()) break;
            }
        }
    }
}
```

2. Deletar `avaliacao/pipeline/PipelineDataSelecionada.java`.

3. Atualizar `AvaliacaoOrquestrador.java` — o import de `PipelineDataSelecionada` muda de `avaliacao.pipeline` para `avaliacao.pipeline.data`.

## Notas de implementação

- O early exit opera **por instrumento**: ao encontrar a primeira restrição em um instrumento, o loop interno quebra e o loop externo avança para o próximo instrumento.
- A ordem dos validadores no construtor não muda — a lógica de ordenação já estava correta.
- `AvaliacaoOrquestrador` tem dois imports de pipeline. Atualizar apenas o de `PipelineDataSelecionada` nesta task; o de `PipelineAgendamento` será atualizado na TASK-12.

## Critério de aceite

- [ ] `PipelineDataSelecionada` está em `avaliacao.pipeline.data` com o loop `for` e `if (!resultado.isValido()) break`
- [ ] Arquivo antigo em `avaliacao.pipeline` foi deletado
- [ ] `AvaliacaoOrquestrador` compila com o import atualizado
- [ ] Build compila sem erros
