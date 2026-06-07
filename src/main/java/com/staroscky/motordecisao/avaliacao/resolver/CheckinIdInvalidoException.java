package com.staroscky.motordecisao.avaliacao.resolver;

public class CheckinIdInvalidoException extends RuntimeException {

    public CheckinIdInvalidoException(String checkinId) {
        super("checkinId inválido: " + checkinId);
    }
}
