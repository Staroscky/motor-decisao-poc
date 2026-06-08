package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.tef;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamentoInstrumento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.tef.AgendamentoTefValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.tef.AvisoIntervaloTefValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.tef.ProximaDataTefValidador;
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
