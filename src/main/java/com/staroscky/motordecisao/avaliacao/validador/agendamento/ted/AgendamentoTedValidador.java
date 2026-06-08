package com.staroscky.motordecisao.avaliacao.validador.agendamento.ted;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
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
