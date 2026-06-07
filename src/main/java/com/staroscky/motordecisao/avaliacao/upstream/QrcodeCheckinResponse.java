package com.staroscky.motordecisao.avaliacao.upstream;

// TODO: ajustar campos ao contrato real do upstream de qrcode
public record QrcodeCheckinResponse(
    String checkinId,
    String uuid,
    DadosBancarios dadosBancarios
) implements CheckinResponse {}
