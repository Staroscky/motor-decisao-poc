package com.staroscky.motordecisao.avaliacao.response;

public record InstrumentoPix(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    ResultadoViabilidadeDto agendamento
) implements InstrumentoAvaliado {}
