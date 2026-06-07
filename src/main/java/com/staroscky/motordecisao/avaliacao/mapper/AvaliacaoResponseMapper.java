package com.staroscky.motordecisao.avaliacao.mapper;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoInstrumento;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.response.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class AvaliacaoResponseMapper {

    public AvaliacaoResponse toResponse(AvaliacaoContexto contexto) {
        List<InstrumentoAvaliado> instrumentos = contexto.getInstrumentos().stream()
            .sorted(Comparator.comparingInt(Instrumento::ordinal))
            .map(instrumento -> toInstrumentoAvaliado(instrumento, contexto))
            .toList();
        return new AvaliacaoResponse(instrumentos);
    }

    private InstrumentoAvaliado toInstrumentoAvaliado(Instrumento instrumento,
                                                       AvaliacaoContexto contexto) {
        ResultadoInstrumento resultado = contexto.getResultado(instrumento);
        ResultadoViabilidadeDto dataSelecionada = toDto(resultado.getDataSelecionada());
        ResultadoViabilidadeDto agendamento     = toDto(resultado.getAgendamento());

        return switch (instrumento) {
            case PIX -> new InstrumentoPix("PIX", dataSelecionada, agendamento);
            case TEF -> new InstrumentoTef("TEF", dataSelecionada, agendamento);
            case TED -> new InstrumentoTed("TED", dataSelecionada, agendamento, finalidadesTed());
        };
    }

    private ResultadoViabilidadeDto toDto(ResultadoViabilidade rv) {
        return new ResultadoViabilidadeDto(
            rv.isValido(),
            List.copyOf(rv.getRestricoes()),
            rv.getOrdemMelhor(),
            rv.getLimite()
        );
    }

    private List<InstrumentoTed.FinalidadeTed> finalidadesTed() {
        // TODO: buscar finalidades de configuração ou upstream
        return List.of(
            new InstrumentoTed.FinalidadeTed("01", "Crédito em Conta"),
            new InstrumentoTed.FinalidadeTed("03", "DOC/TED")
        );
    }
}
