package com.staroscky.motordecisao.avaliacao.upstream;

// TODO: ajustar campos ao contrato real do upstream de chaves_pix
public record ChavePixCheckinResponse(
    String checkinId,
    String uuid,
    DadosBancarios dadosBancarios
) implements CheckinResponse {}
