package com.staroscky.motordecisao.avaliacao.contexto;

public class ResultadoInstrumento {

    private final ResultadoViabilidade dataSelecionada = new ResultadoViabilidade();
    private final ResultadoAgendamento agendamento     = new ResultadoAgendamento();

    public ResultadoViabilidade getDataSelecionada() {
        return dataSelecionada;
    }

    public ResultadoAgendamento getAgendamento() {
        return agendamento;
    }
}
