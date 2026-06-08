package com.staroscky.motordecisao.avaliacao.validador.agendamento.tef;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
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
