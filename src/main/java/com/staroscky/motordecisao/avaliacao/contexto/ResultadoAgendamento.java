package com.staroscky.motordecisao.avaliacao.contexto;

import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.Restricao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResultadoAgendamento {

    private final List<Restricao> restricoes = new ArrayList<>();
    private final List<Aviso> avisos         = new ArrayList<>();
    private LocalDate proximaDataDisponivel;

    public void adicionarRestricao(Restricao restricao) {
        restricoes.add(restricao);
    }

    public void adicionarAviso(Aviso aviso) {
        avisos.add(aviso);
    }

    public boolean isPodeAgendar() {
        return restricoes.isEmpty();
    }

    public boolean temProximaData() {
        return proximaDataDisponivel != null;
    }

    public List<Restricao> getRestricoes() {
        return restricoes;
    }

    public List<Aviso> getAvisos() {
        return avisos;
    }

    public LocalDate getProximaDataDisponivel() {
        return proximaDataDisponivel;
    }

    public void setProximaDataDisponivel(LocalDate proximaDataDisponivel) {
        this.proximaDataDisponivel = proximaDataDisponivel;
    }
}
