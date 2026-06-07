package com.staroscky.motordecisao.avaliacao.request;

import java.time.LocalDate;

public record ChavePixAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem
) implements AvaliacaoRequest {}
