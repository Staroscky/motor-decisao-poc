package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.mapper.AvaliacaoResponseMapper;
import com.staroscky.motordecisao.avaliacao.request.AvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.CheckinIdParser;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.InstrumentoResolver;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.response.AvaliacaoResponse;
import com.staroscky.motordecisao.avaliacao.upstream.CheckinClient;
import com.staroscky.motordecisao.avaliacao.upstream.CheckinResponse;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AvaliacaoService {

    private final CheckinClient checkinClient;
    private final AvaliacaoOrquestrador orquestrador;
    private final AvaliacaoResponseMapper mapper;

    public AvaliacaoService(CheckinClient checkinClient,
                             AvaliacaoOrquestrador orquestrador,
                             AvaliacaoResponseMapper mapper) {
        this.checkinClient = checkinClient;
        this.orquestrador = orquestrador;
        this.mapper = mapper;
    }

    public AvaliacaoResponse avaliar(AvaliacaoRequest request) {
        TipoCheckin tipo = CheckinIdParser.parse(request.checkinId());
        CheckinResponse checkinDestino = buscarCheckin(tipo, request.checkinId());
        Set<Instrumento> instrs = InstrumentoResolver.resolver(tipo, checkinDestino.dadosBancarios().ispb());

        AvaliacaoContexto contexto = new AvaliacaoContexto(request, tipo, checkinDestino, instrs);
        orquestrador.executar(contexto);

        return mapper.toResponse(contexto);
    }

    private CheckinResponse buscarCheckin(TipoCheckin tipo, String checkinId) {
        return switch (tipo) {
            case CHAVE_PIX -> checkinClient.buscarChavePix(checkinId);
            case AGCONTA   -> checkinClient.buscarAgconta(checkinId);
            case QRCODE    -> checkinClient.buscarQrcode(checkinId);
        };
    }
}
