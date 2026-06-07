package com.staroscky.motordecisao.avaliacao.validador;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;

public interface Validador {

    boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento);

    void validar(AvaliacaoContexto contexto, Instrumento instrumento, ResultadoViabilidade resultado);
}
