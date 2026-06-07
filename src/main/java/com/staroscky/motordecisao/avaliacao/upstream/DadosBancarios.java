package com.staroscky.motordecisao.avaliacao.upstream;

public record DadosBancarios(
    String ispb,
    String agencia,
    String conta,
    String dac,
    String tipoConta
) {}
