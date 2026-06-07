package com.staroscky.motordecisao.avaliacao.contexto;

public class ResultadoInstrumento {

    private final ResultadoViabilidade dataSelecionada = new ResultadoViabilidade();
    private final ResultadoViabilidade agendamento     = new ResultadoViabilidade();

    public ResultadoViabilidade getDataSelecionada() {
        return dataSelecionada;
    }

    public ResultadoViabilidade getAgendamento() {
        return agendamento;
    }
}
