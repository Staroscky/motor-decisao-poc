package com.staroscky.motordecisao.avaliacao.upstream;

public sealed interface CheckinResponse
    permits ChavePixCheckinResponse, AgcontaCheckinResponse, QrcodeCheckinResponse {

    String checkinId();
    String uuid();
    DadosBancarios dadosBancarios();
}
