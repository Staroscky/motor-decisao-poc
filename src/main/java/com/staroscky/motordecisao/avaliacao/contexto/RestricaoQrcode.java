package com.staroscky.motordecisao.avaliacao.contexto;

public record RestricaoQrcode(String motivo, ContextoQrcode contexto) implements Restricao {

    public record ContextoQrcode(String tipo) {}
}
