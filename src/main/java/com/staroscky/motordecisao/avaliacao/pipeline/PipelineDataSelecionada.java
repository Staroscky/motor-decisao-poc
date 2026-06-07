package com.staroscky.motordecisao.avaliacao.pipeline;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.validador.Validador;
import com.staroscky.motordecisao.avaliacao.validador.data.DataValidaValidador;
import com.staroscky.motordecisao.avaliacao.validador.data.HorarioPermitidoValidador;
import com.staroscky.motordecisao.avaliacao.validador.data.LimiteDiarioValidador;
import com.staroscky.motordecisao.avaliacao.validador.instituicao.LimiteInstituicaoValidador;
import com.staroscky.motordecisao.avaliacao.validador.instituicao.StatusInstituicaoValidador;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PipelineDataSelecionada {

    private final List<Validador> validadores;

    public PipelineDataSelecionada(
        StatusInstituicaoValidador statusInstituicao,
        LimiteInstituicaoValidador limiteInstituicao,
        HorarioPermitidoValidador horario,
        LimiteDiarioValidador limiteDiario,
        DataValidaValidador dataValida
    ) {
        this.validadores = List.of(
            statusInstituicao, limiteInstituicao, horario, limiteDiario, dataValida
        );
    }

    public void executar(AvaliacaoContexto contexto) {
        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado =
                contexto.getResultado(instrumento).getDataSelecionada();
            validadores.stream()
                .filter(v -> v.suporta(contexto, instrumento))
                .forEach(v -> v.validar(contexto, instrumento, resultado));
        }
    }
}
