package com.staroscky.motordecisao.avaliacao.resolver;

public final class CheckinIdParser {

    private CheckinIdParser() {}

    public static TipoCheckin parse(String checkinId) {
        String[] partes = checkinId.split(":");
        if (partes.length < 2) throw new CheckinIdInvalidoException(checkinId);
        return switch (partes[1]) {
            case "qrcode"     -> TipoCheckin.QRCODE;
            case "chaves_pix" -> TipoCheckin.CHAVE_PIX;
            case "agconta"    -> TipoCheckin.AGCONTA;
            default           -> throw new CheckinIdInvalidoException(checkinId);
        };
    }
}
