package com.staroscky.motordecisao.avaliacao.request;

import java.time.LocalDate;

public record QrcodeAvaliacaoRequest(
    String checkinId,
    LocalDate data,
    DadosOrigem origem,
    DadosQrcode qrcode
) implements AvaliacaoRequest {}
