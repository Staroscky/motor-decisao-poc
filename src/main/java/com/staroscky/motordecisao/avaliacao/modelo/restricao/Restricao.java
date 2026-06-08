package com.staroscky.motordecisao.avaliacao.modelo.restricao;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(RestricaoGenerica.class),
    @JsonSubTypes.Type(RestricaoQrcode.class)
})
public sealed interface Restricao permits RestricaoGenerica, RestricaoQrcode {}
