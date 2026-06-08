package com.staroscky.motordecisao.avaliacao.validador.agendamento;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;

public interface ValidadorAgendamento {

    boolean suporta(AvaliacaoContexto contexto);

    void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado);
}
