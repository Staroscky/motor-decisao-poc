package com.staroscky.motordecisao.avaliacao.upstream;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Paths a confirmar com a equipe responsável pelo upstream de checkin
@FeignClient(name = "checkin-client", url = "${avaliacao.upstream.checkin.url}")
public interface CheckinClient {

    @GetMapping("/v1/checkin/chaves-pix/{checkinId}")
    ChavePixCheckinResponse buscarChavePix(@PathVariable("checkinId") String checkinId);

    @GetMapping("/v1/checkin/agconta/{checkinId}")
    AgcontaCheckinResponse buscarAgconta(@PathVariable("checkinId") String checkinId);

    @GetMapping("/v1/checkin/qrcode/{checkinId}")
    QrcodeCheckinResponse buscarQrcode(@PathVariable("checkinId") String checkinId);
}
