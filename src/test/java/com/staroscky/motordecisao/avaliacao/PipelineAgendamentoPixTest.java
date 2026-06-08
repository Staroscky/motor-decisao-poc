package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.RestricaoGenerica;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.AvisoIntervaloPixValidador;
import com.staroscky.motordecisao.avaliacao.pipeline.agendamento.pix.PipelineAgendamentoPix;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.AgendamentoPixQrcodeValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.AgendamentoPixValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.pix.ProximaDataPixValidador;
import com.staroscky.motordecisao.avaliacao.request.DadosOrigem;
import com.staroscky.motordecisao.avaliacao.request.DadosQrcode;
import com.staroscky.motordecisao.avaliacao.request.QrcodeAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.upstream.DadosBancarios;
import com.staroscky.motordecisao.avaliacao.upstream.QrcodeCheckinResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineAgendamentoPixTest {

    private static final DadosBancarios DADOS = new DadosBancarios("60701190", "0001", "12345-6", "0", "CORRENTE");

    @Test
    void earlyExit_interrompeAposRestricaoSemExecutarProximaData() {
        AtomicBoolean proximaDataExecutou = new AtomicBoolean(false);

        ValidadorAgendamento bloqueante = new ValidadorAgendamento() {
            @Override
            public boolean suporta(AvaliacaoContexto contexto) { return true; }
            @Override
            public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
                resultado.adicionarRestricao(new RestricaoGenerica("BLOQUEADO"));
            }
        };

        ValidadorAgendamento downstream = new ValidadorAgendamento() {
            @Override
            public boolean suporta(AvaliacaoContexto contexto) { return true; }
            @Override
            public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
                proximaDataExecutou.set(true);
            }
        };

        AvaliacaoContexto contexto = contextoQrcode("COB");
        ResultadoAgendamento resultado = contexto.getResultado(Instrumento.PIX).getAgendamento();

        for (ValidadorAgendamento validador : java.util.List.of(bloqueante, downstream)) {
            if (!validador.suporta(contexto)) continue;
            validador.validar(contexto, resultado);
            if (!resultado.isPodeAgendar()) break;
        }

        assertThat(resultado.isPodeAgendar()).isFalse();
        assertThat(proximaDataExecutou.get()).isFalse();
    }

    @Test
    void aviso_naoInterrompePipeline() {
        AtomicBoolean segundoExecutou = new AtomicBoolean(false);

        ValidadorAgendamento avisoValidador = new ValidadorAgendamento() {
            @Override
            public boolean suporta(AvaliacaoContexto contexto) { return true; }
            @Override
            public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
                resultado.adicionarAviso(new Aviso("FERIADO", LocalDate.now(), "Feriado"));
            }
        };

        ValidadorAgendamento segundo = new ValidadorAgendamento() {
            @Override
            public boolean suporta(AvaliacaoContexto contexto) { return true; }
            @Override
            public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
                segundoExecutou.set(true);
            }
        };

        AvaliacaoContexto contexto = contextoQrcode("COBV");
        ResultadoAgendamento resultado = contexto.getResultado(Instrumento.PIX).getAgendamento();

        for (ValidadorAgendamento validador : java.util.List.of(avisoValidador, segundo)) {
            if (!validador.suporta(contexto)) continue;
            validador.validar(contexto, resultado);
            if (!resultado.isPodeAgendar()) break;
        }

        assertThat(resultado.isPodeAgendar()).isTrue();
        assertThat(segundoExecutou.get()).isTrue();
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
