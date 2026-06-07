package com.staroscky.motordecisao.avaliacao.validador.data;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.validador.Validador;
import org.springframework.stereotype.Component;

@Component
public class HorarioPermitidoValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return contexto.isOnline();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: verificar janela de horário permitido para o instrumento
        // se fora da janela: resultado.adicionarRestricao(new RestricaoGenerica("FORA_HORARIO_PERMITIDO"));
    }
}
