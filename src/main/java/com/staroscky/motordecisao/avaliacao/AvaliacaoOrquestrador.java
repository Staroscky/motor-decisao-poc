package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.data.PipelineDataSelecionada;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoOrquestrador {

    private final PipelineDataSelecionada pipelineDataSelecionada;
    private final PipelineAgendamento pipelineAgendamento;

    public AvaliacaoOrquestrador(PipelineDataSelecionada pipelineDataSelecionada,
                                  PipelineAgendamento pipelineAgendamento) {
        this.pipelineDataSelecionada = pipelineDataSelecionada;
        this.pipelineAgendamento = pipelineAgendamento;
    }

    public void executar(AvaliacaoContexto contexto) {
        pipelineDataSelecionada.executar(contexto);
        pipelineAgendamento.executar(contexto);
    }
}
