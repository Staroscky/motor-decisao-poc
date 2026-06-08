package com.staroscky.motordecisao.avaliacao.contexto;

import com.staroscky.motordecisao.avaliacao.request.AvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.upstream.CheckinResponse;
import com.staroscky.motordecisao.avaliacao.upstream.DadosBancarios;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AvaliacaoContexto {

    private final AvaliacaoRequest request;
    private final TipoCheckin tipoCheckin;
    private final CheckinResponse checkinDestino;
    private final Set<Instrumento> instrumentos;
    private final ConcurrentMap<Instrumento, ResultadoInstrumento> resultados =
        new ConcurrentHashMap<>();

    public AvaliacaoContexto(AvaliacaoRequest request, TipoCheckin tipoCheckin,
                              CheckinResponse checkinDestino, Set<Instrumento> instrumentos) {
        this.request = request;
        this.tipoCheckin = tipoCheckin;
        this.checkinDestino = checkinDestino;
        this.instrumentos = instrumentos;
    }

    public ResultadoInstrumento getResultado(Instrumento instrumento) {
        return resultados.computeIfAbsent(instrumento, k -> new ResultadoInstrumento());
    }

    public boolean isOnline() {
        return !request.data().isAfter(LocalDate.now());
    }

    public AvaliacaoRequest getRequest() {
        return request;
    }

    public TipoCheckin getTipoCheckin() {
        return tipoCheckin;
    }

    public CheckinResponse getCheckinDestino() {
        return checkinDestino;
    }

    public DadosBancarios getDadosBancariosDestino() {
        return checkinDestino.dadosBancarios();
    }

    public String getIspb() {
        return checkinDestino.dadosBancarios().ispb();
    }

    public Set<Instrumento> getInstrumentos() {
        return instrumentos;
    }
}
