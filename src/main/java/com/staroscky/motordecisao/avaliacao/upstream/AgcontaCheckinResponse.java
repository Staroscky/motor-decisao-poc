package com.staroscky.motordecisao.avaliacao.upstream;

// TODO: ajustar campos ao contrato real do upstream de agconta
public record AgcontaCheckinResponse(
    String checkinId,
    String uuid,
    DadosBancarios dadosBancarios
) implements CheckinResponse {}
