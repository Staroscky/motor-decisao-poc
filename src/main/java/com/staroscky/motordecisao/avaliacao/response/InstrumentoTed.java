package com.staroscky.motordecisao.avaliacao.response;

import java.util.List;

public record InstrumentoTed(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento,
    List<FinalidadeTed> finalidades
) implements InstrumentoAvaliado {

    public record FinalidadeTed(String id, String descricao) {}
}
