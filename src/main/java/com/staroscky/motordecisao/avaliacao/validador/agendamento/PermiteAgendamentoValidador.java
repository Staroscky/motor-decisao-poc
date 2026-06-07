package com.staroscky.motordecisao.avaliacao.validador.agendamento;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.RestricaoQrcode;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.request.QrcodeAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.validador.Validador;
import org.springframework.stereotype.Component;

@Component
public class PermiteAgendamentoValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return instrumento == Instrumento.PIX
            && contexto.getTipoCheckin() == TipoCheckin.QRCODE;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
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
