package com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.PipelineAgendamentoInstrumento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.AgendamentoPixQrcodeValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.AgendamentoPixValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.AvisoIntervaloPixValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.ProximaDataPixValidador;
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
