package com.staroscky.motordecisao.avaliacao.response;

import com.staroscky.motordecisao.avaliacao.contexto.Limite;
import com.staroscky.motordecisao.avaliacao.contexto.Restricao;

import java.util.List;

public record ResultadoViabilidadeDto(
    boolean valido,
    List<Restricao> restricoes,
    int ordemMelhor,
    Limite limite
) {}
