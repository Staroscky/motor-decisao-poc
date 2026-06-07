package com.staroscky.motordecisao.avaliacao.pipeline;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.validador.Validador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.PermiteAgendamentoValidador;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ProximaDataValidador;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PipelineAgendamento {

    private final List<Validador> validadores;

    public PipelineAgendamento(
        PermiteAgendamentoValidador permiteAgendamento,
        ProximaDataValidador proximaData
    ) {
        this.validadores = List.of(permiteAgendamento, proximaData);
    }

    public void executar(AvaliacaoContexto contexto) {
        for (Instrumento instrumento : contexto.getInstrumentos()) {
            ResultadoViabilidade resultado =
                contexto.getResultado(instrumento).getAgendamento();
            validadores.stream()
                .filter(v -> v.suporta(contexto, instrumento))
                .forEach(v -> v.validar(contexto, instrumento, resultado));
        }
    }
}
