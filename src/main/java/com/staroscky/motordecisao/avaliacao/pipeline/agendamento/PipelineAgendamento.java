package com.staroscky.motordecisao.avaliacao.pipeline.agendamento;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix.PipelineAgendamentoPix;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted.PipelineAgendamentoTed;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef.PipelineAgendamentoTef;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PipelineAgendamento {

    private final Map<Instrumento, PipelineAgendamentoInstrumento> pipelines;

    public PipelineAgendamento(
        PipelineAgendamentoPix pix,
        PipelineAgendamentoTed ted,
        PipelineAgendamentoTef tef
    ) {
        this.pipelines = Map.of(Instrumento.PIX, pix, Instrumento.TED, ted, Instrumento.TEF, tef);
    }

    public void executar(AvaliacaoContexto contexto) {
        contexto.getInstrumentos().forEach(instrumento ->
            pipelines.get(instrumento).executar(contexto));
    }
}
