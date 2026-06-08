package com.staroscky.motordecisao.avaliacao.validador.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.RestricaoQrcode;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.request.QrcodeAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoPixQrcodeValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        return contexto.getTipoCheckin() == TipoCheckin.QRCODE;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        QrcodeAvaliacaoRequest req = (QrcodeAvaliacaoRequest) contexto.getRequest();
        if (!tipoPermiteAgendamento(req.qrcode().tipo())) {
            resultado.adicionarRestricao(new RestricaoQrcode(
                "QR_CODE_NAO_PERMITE",
                new RestricaoQrcode.ContextoQrcode(req.qrcode().tipo())
            ));
        }
    }

    private boolean tipoPermiteAgendamento(String tipoQrcode) {
        // TODO: definir quais tipos permitem agendamento (ex.: "COBV" sim, "COB" não)
        return false;
    }
}
