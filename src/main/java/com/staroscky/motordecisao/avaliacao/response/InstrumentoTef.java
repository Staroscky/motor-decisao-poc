package com.staroscky.motordecisao.avaliacao.response;

public record InstrumentoTef(
    String tipo,
    ResultadoViabilidadeDto dataSelecionada,
    AvaliacaoAgendamento agendamento
) implements InstrumentoAvaliado {}
