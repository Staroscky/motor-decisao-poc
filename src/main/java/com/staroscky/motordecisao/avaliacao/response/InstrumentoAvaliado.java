package com.staroscky.motordecisao.avaliacao.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(InstrumentoPix.class),
    @JsonSubTypes.Type(InstrumentoTef.class),
    @JsonSubTypes.Type(InstrumentoTed.class)
})
public sealed interface InstrumentoAvaliado
    permits InstrumentoPix, InstrumentoTef, InstrumentoTed {}
