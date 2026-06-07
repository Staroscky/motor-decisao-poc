package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.request.ChavePixAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.request.DadosOrigem;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.upstream.ChavePixCheckinResponse;
import com.staroscky.motordecisao.avaliacao.upstream.DadosBancarios;
import com.staroscky.motordecisao.avaliacao.validador.data.HorarioPermitidoValidador;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HorarioPermitidoValidadorTest {

    private final HorarioPermitidoValidador validador = new HorarioPermitidoValidador();

    @Test
    void suportaDataAtual() {
        assertThat(validador.suporta(contextoComData(LocalDate.now()), Instrumento.PIX)).isTrue();
    }

    @Test
    void naoSuportaDataFutura() {
        assertThat(validador.suporta(contextoComData(LocalDate.now().plusDays(1)), Instrumento.PIX)).isFalse();
    }

    private AvaliacaoContexto contextoComData(LocalDate data) {
        var request = new ChavePixAvaliacaoRequest(
            "checkin:chaves_pix:1:uuid", data, new DadosOrigem("0001", "12345-6")
        );
        var checkinDestino = new ChavePixCheckinResponse(
            "checkin:chaves_pix:1:uuid", "uuid",
            new DadosBancarios("60701190", "0001", "12345-6", "0", "CORRENTE")
        );
        return new AvaliacaoContexto(request, TipoCheckin.CHAVE_PIX, checkinDestino, Set.of(Instrumento.PIX));
    }
}
