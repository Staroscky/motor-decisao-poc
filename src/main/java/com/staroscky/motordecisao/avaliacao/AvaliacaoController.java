package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.request.AvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.response.AvaliacaoResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/avaliacao")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @PostMapping
    public AvaliacaoResponse avaliar(@RequestBody AvaliacaoRequest request) {
        return service.avaliar(request);
    }
}
