package com.staroscky.motordecisao.avaliacao.validador.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
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
