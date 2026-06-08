package com.staroscky.motordecisao.avaliacao.validador.agendamento.pix;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class ProximaDataPixValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        ResultadoAgendamento agendamento =
            contexto.getResultado(Instrumento.PIX).getAgendamento();
        return agendamento.isPodeAgendar()
            && !contexto.getResultado(Instrumento.PIX).getDataSelecionada().isValido();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: buscar próxima data disponível para PIX via upstream
    }
}
