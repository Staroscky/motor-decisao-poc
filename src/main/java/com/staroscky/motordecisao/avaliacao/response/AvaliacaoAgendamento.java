package com.staroscky.motordecisao.avaliacao.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.Restricao;

import java.time.LocalDate;
import java.util.List;

public record AvaliacaoAgendamento(
    boolean podeAgendar,
    List<Restricao> restricoes,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    LocalDate proximaDataDisponivel,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<Aviso> avisos
) {}
