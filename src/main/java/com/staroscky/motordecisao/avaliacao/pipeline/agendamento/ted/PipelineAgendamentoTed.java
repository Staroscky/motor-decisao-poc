package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.ted;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamentoInstrumento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ted.AgendamentoTedValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ted.AvisoIntervaloTedValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ted.ProximaDataTedValidador;
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
