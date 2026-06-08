package com.staroscky.motordecisao.avaliacao.validador.agendamento.tef;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.validador.agendamento.ValidadorAgendamento;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import org.springframework.stereotype.Component;

@Component
public class ProximaDataTefValidador implements ValidadorAgendamento {

    @Override
    public boolean suporta(AvaliacaoContexto contexto) {
        ResultadoAgendamento agendamento =
            contexto.getResultado(Instrumento.TEF).getAgendamento();
        return agendamento.isPodeAgendar()
            && !contexto.getResultado(Instrumento.TEF).getDataSelecionada().isValido();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, ResultadoAgendamento resultado) {
        // TODO: buscar próxima data disponível para TEF via upstream
    }
}
