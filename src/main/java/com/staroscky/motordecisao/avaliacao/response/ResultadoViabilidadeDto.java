package com.staroscky.motordecisao.avaliacao.response;

import com.staroscky.motordecisao.avaliacao.modelo.Limite;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.Restricao;

import java.util.List;

public record ResultadoViabilidadeDto(
    boolean valido,
    List<Restricao> restricoes,
    int ordemMelhor,
    Limite limite
) {}
