package com.staroscky.motordecisao.avaliacao.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(QrcodeAvaliacaoRequest.class),
    @JsonSubTypes.Type(ChavePixAvaliacaoRequest.class),
    @JsonSubTypes.Type(AgcontaAvaliacaoRequest.class)
})
public sealed interface AvaliacaoRequest
    permits QrcodeAvaliacaoRequest, ChavePixAvaliacaoRequest, AgcontaAvaliacaoRequest {

    String checkinId();
    LocalDate data();
    DadosOrigem origem();
}
