package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.request.AgcontaAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.request.DadosOrigem;
import com.staroscky.motordecisao.avaliacao.request.DadosQrcode;
import com.staroscky.motordecisao.avaliacao.request.QrcodeAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.upstream.AgcontaCheckinResponse;
import com.staroscky.motordecisao.avaliacao.upstream.DadosBancarios;
import com.staroscky.motordecisao.avaliacao.upstream.QrcodeCheckinResponse;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.PermiteAgendamentoValidador;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermiteAgendamentoValidadorTest {

    private static final DadosBancarios DADOS = new DadosBancarios("60701190", "0001", "12345-6", "0", "CORRENTE");

    private final PermiteAgendamentoValidador validador = new PermiteAgendamentoValidador();

    @Test
    void suportaPixComQrcode() {
        assertThat(validador.suporta(contextoQrcode("COB"), Instrumento.PIX)).isTrue();
    }

    @Test
    void naoSuportaTedComQrcode() {
        assertThat(validador.suporta(contextoQrcode("COB"), Instrumento.TED)).isFalse();
    }

    @Test
    void naoSuportaPixComAgconta() {
        var request = new AgcontaAvaliacaoRequest(
            "checkin:agconta:1:uuid", LocalDate.now(), new DadosOrigem("0001", "12345-6")
        );
        var checkinDestino = new AgcontaCheckinResponse("checkin:agconta:1:uuid", "uuid", DADOS);
        var contexto = new AvaliacaoContexto(request, TipoCheckin.AGCONTA, checkinDestino,
            Set.of(Instrumento.PIX, Instrumento.TED));
        assertThat(validador.suporta(contexto, Instrumento.PIX)).isFalse();
    }

    @Test
    void validarAdicionaRestricaoQrCodeNaoPermite() {
        AvaliacaoContexto contexto = contextoQrcode("COB");
        ResultadoViabilidade resultado = new ResultadoViabilidade();

        validador.validar(contexto, Instrumento.PIX, resultado);

        assertThat(resultado.isValido()).isFalse();
        assertThat(resultado.getRestricoes()).hasSize(1);
    }

    private AvaliacaoContexto contextoQrcode(String tipoQrcode) {
        var request = new QrcodeAvaliacaoRequest(
            "checkin:qrcode:1:uuid", LocalDate.now(), new DadosOrigem("0001", "12345-6"),
            new DadosQrcode("emv", tipoQrcode)
        );
        var checkinDestino = new QrcodeCheckinResponse("checkin:qrcode:1:uuid", "uuid", DADOS);
        return new AvaliacaoContexto(request, TipoCheckin.QRCODE, checkinDestino, Set.of(Instrumento.PIX));
    }
}
