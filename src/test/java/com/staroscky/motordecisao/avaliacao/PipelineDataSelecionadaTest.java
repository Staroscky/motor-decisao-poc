package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.RestricaoGenerica;
import com.staroscky.motordecisao.avaliacao.request.ChavePixAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.request.DadosOrigem;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.upstream.ChavePixCheckinResponse;
import com.staroscky.motordecisao.avaliacao.upstream.DadosBancarios;
import com.staroscky.motordecisao.avaliacao.validador.Validador;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineDataSelecionadaTest {

    @Test
    void earlyExit_interrompeAposRestricaoNoDataSelecionada() {
        AtomicBoolean segundoExecutou = new AtomicBoolean(false);

        Validador primeiroValidador = new Validador() {
            @Override
            public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) { return true; }
            @Override
            public void validar(AvaliacaoContexto contexto, Instrumento instrumento, ResultadoViabilidade resultado) {
                resultado.adicionarRestricao(new RestricaoGenerica("BLOQUEADO"));
            }
        };

        Validador segundoValidador = new Validador() {
            @Override
            public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) { return true; }
            @Override
            public void validar(AvaliacaoContexto contexto, Instrumento instrumento, ResultadoViabilidade resultado) {
                segundoExecutou.set(true);
            }
        };

        AvaliacaoContexto contexto = contextoChavePix();
        List<Validador> validadores = List.of(primeiroValidador, segundoValidador);

        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado = contexto.getResultado(instrumento).getDataSelecionada();
            for (Validador validador : validadores) {
                if (!validador.suporta(contexto, instrumento)) continue;
                validador.validar(contexto, instrumento, resultado);
                if (!resultado.isValido()) break;
            }
        }

        assertThat(segundoExecutou.get()).isFalse();
    }

    private AvaliacaoContexto contextoChavePix() {
        var request = new ChavePixAvaliacaoRequest(
            "checkin:chaves_pix:1:uuid", LocalDate.now(), new DadosOrigem("0001", "12345-6")
        );
        var checkin = new ChavePixCheckinResponse(
            "checkin:chaves_pix:1:uuid", "uuid",
            new DadosBancarios("60701190", "0001", "12345-6", "0", "CORRENTE")
        );
        return new AvaliacaoContexto(request, TipoCheckin.CHAVE_PIX, checkin, Set.of(Instrumento.PIX));
    }
}
