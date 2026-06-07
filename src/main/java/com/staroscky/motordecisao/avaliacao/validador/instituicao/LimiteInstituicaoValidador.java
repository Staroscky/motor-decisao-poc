package com.staroscky.motordecisao.avaliacao.validador.instituicao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.validador.Validador;
import org.springframework.stereotype.Component;

@Component
public class LimiteInstituicaoValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return true;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: verificar limite da instituição para o instrumento
    }
}
