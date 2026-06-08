package com.staroscky.motordecisao.avaliacao.modelo.restricao;

public record RestricaoQrcode(String motivo, ContextoQrcode contexto) implements Restricao {

    public record ContextoQrcode(String tipo) {}
}
