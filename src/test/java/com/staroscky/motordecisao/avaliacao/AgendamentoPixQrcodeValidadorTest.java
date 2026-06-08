package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.AgendamentoPixQrcodeValidador;
import com.staroscky.motordecisao.avaliacao.request.AgcontaAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.request.DadosOrigem;
import com.staroscky.motordecisao.avaliacao.request.DadosQrcode;
import com.staroscky.motordecisao.avaliacao.request.QrcodeAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.upstream.AgcontaCheckinResponse;
import com.staroscky.motordecisao.avaliacao.upstream.DadosBancarios;
import com.staroscky.motordecisao.avaliacao.upstream.QrcodeCheckinResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AgendamentoPixQrcodeValidadorTest {

    private static final DadosBancarios DADOS = new DadosBancarios("60701190", "0001", "12345-6", "0", "CORRENTE");

    private final AgendamentoPixQrcodeValidador validador = new AgendamentoPixQrcodeValidador();

    @Test
    void suportaContextoQrcode() {
        assertThat(validador.suporta(contextoQrcode("COB"))).isTrue();
    }

    @Test
    void naoSuportaContextoAgconta() {
        var request = new AgcontaAvaliacaoRequest(
            "checkin:agconta:1:uuid", LocalDate.now(), new DadosOrigem("0001", "12345-6")
        );
        var checkinDestino = new AgcontaCheckinResponse("checkin:agconta:1:uuid", "uuid", DADOS);
        var contexto = new AvaliacaoContexto(request, TipoCheckin.AGCONTA, checkinDestino,
            Set.of(Instrumento.PIX, Instrumento.TED));
        assertThat(validador.suporta(contexto)).isFalse();
    }

    @Test
    void validarAdicionaRestricaoQrCodeNaoPermite() {
        AvaliacaoContexto contexto = contextoQrcode("COB");
        ResultadoAgendamento resultado = new ResultadoAgendamento();

        validador.validar(contexto, resultado);

        assertThat(resultado.isPodeAgendar()).isFalse();
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
