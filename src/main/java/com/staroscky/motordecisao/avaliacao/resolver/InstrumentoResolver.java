package com.staroscky.motordecisao.avaliacao.resolver;

import java.util.Set;

public final class InstrumentoResolver {

    private static final String ISPB_ITAU = "60701190";

    private InstrumentoResolver() {}

    public static Set<Instrumento> resolver(TipoCheckin tipo, String ispb) {
        return switch (tipo) {
            case QRCODE, CHAVE_PIX -> Set.of(Instrumento.PIX);
            case AGCONTA -> ispb.equals(ISPB_ITAU)
                ? Set.of(Instrumento.PIX, Instrumento.TEF)
                : Set.of(Instrumento.PIX, Instrumento.TED);
        };
    }
}
