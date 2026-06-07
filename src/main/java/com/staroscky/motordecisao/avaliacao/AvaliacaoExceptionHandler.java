package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.resolver.CheckinIdInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AvaliacaoExceptionHandler {

    @ExceptionHandler(CheckinIdInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleCheckinIdInvalido(CheckinIdInvalidoException ex) {
        return Map.of("erro", ex.getMessage());
    }
}
