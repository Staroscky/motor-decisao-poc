package com.staroscky.motordecisao.avaliacao.request;

import java.time.LocalDate;

public record AgcontaAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem
) implements AvaliacaoRequest {}
