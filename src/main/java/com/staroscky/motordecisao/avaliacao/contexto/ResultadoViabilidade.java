package com.staroscky.motordecisao.avaliacao.contexto;

import com.staroscky.motordecisao.avaliacao.modelo.Limite;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.Restricao;

import java.util.ArrayList;
import java.util.List;

public class ResultadoViabilidade {

    private boolean valido = true;
    private int ordemMelhor;
    private Limite limite;
    private final List<Restricao> restricoes = new ArrayList<>();

    public void adicionarRestricao(Restricao restricao) {
        this.restricoes.add(restricao);
        this.valido = false;
    }

    public boolean isValido() {
        return valido;
    }

    public int getOrdemMelhor() {
        return ordemMelhor;
    }

    public void setOrdemMelhor(int ordemMelhor) {
        this.ordemMelhor = ordemMelhor;
    }

    public Limite getLimite() {
        return limite;
    }

    public void setLimite(Limite limite) {
        this.limite = limite;
    }

    public List<Restricao> getRestricoes() {
        return restricoes;
    }
}
