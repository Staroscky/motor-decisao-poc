package com.staroscky.motordecisao.avaliacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.staroscky.motordecisao.avaliacao.contexto.AvaliacaoContexto;
import com.staroscky.motordecisao.avaliacao.mapper.AvaliacaoResponseMapper;
import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.RestricaoGenerica;
import com.staroscky.motordecisao.avaliacao.request.ChavePixAvaliacaoRequest;
import com.staroscky.motordecisao.avaliacao.request.DadosOrigem;
import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import com.staroscky.motordecisao.avaliacao.response.AvaliacaoResponse;
import com.staroscky.motordecisao.avaliacao.response.InstrumentoPix;
import com.staroscky.motordecisao.avaliacao.upstream.ChavePixCheckinResponse;
import com.staroscky.motordecisao.avaliacao.upstream.DadosBancarios;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoResponseMapperTest {

    private final AvaliacaoResponseMapper mapper = new AvaliacaoResponseMapper();
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @Test
    void cenario1_podeAgendarSemProximaDataSemAvisos() throws Exception {
        AvaliacaoContexto contexto = contexto();

        AvaliacaoResponse response = mapper.toResponse(contexto);
        InstrumentoPix pix = (InstrumentoPix) response.instrumentos().get(0);

        assertThat(pix.agendamento().podeAgendar()).isTrue();
        assertThat(pix.agendamento().restricoes()).isEmpty();
        assertThat(pix.agendamento().proximaDataDisponivel()).isNull();
        assertThat(pix.agendamento().avisos()).isEmpty();

        JsonNode json = objectMapper.valueToTree(pix.agendamento());
        assertThat(json.has("proximaDataDisponivel")).isFalse();
        assertThat(json.has("avisos")).isFalse();
        assertThat(json.has("restricoes")).isTrue();
    }

    @Test
    void cenario2_naoPodeAgendar_restricaoPresente() throws Exception {
        AvaliacaoContexto contexto = contexto();
        contexto.getResultado(Instrumento.PIX).getAgendamento()
            .adicionarRestricao(new RestricaoGenerica("QR_CODE_NAO_PERMITE"));

        AvaliacaoResponse response = mapper.toResponse(contexto);
        InstrumentoPix pix = (InstrumentoPix) response.instrumentos().get(0);

        assertThat(pix.agendamento().podeAgendar()).isFalse();
        assertThat(pix.agendamento().restricoes()).hasSize(1);

        JsonNode json = objectMapper.valueToTree(pix.agendamento());
        assertThat(json.has("proximaDataDisponivel")).isFalse();
        assertThat(json.has("avisos")).isFalse();
    }

    @Test
    void cenario3_podeAgendar_proximaDataPresente_semAvisos() throws Exception {
        AvaliacaoContexto contexto = contexto();
        LocalDate proxima = LocalDate.now().plusDays(2);
        contexto.getResultado(Instrumento.PIX).getAgendamento()
            .setProximaDataDisponivel(proxima);

        AvaliacaoResponse response = mapper.toResponse(contexto);
        InstrumentoPix pix = (InstrumentoPix) response.instrumentos().get(0);

        assertThat(pix.agendamento().podeAgendar()).isTrue();
        assertThat(pix.agendamento().proximaDataDisponivel()).isEqualTo(proxima);

        JsonNode json = objectMapper.valueToTree(pix.agendamento());
        assertThat(json.has("proximaDataDisponivel")).isTrue();
        assertThat(json.has("avisos")).isFalse();
    }

    @Test
    void cenario4_podeAgendar_proximaDataPresente_comAvisos() throws Exception {
        AvaliacaoContexto contexto = contexto();
        LocalDate proxima = LocalDate.now().plusDays(3);
        contexto.getResultado(Instrumento.PIX).getAgendamento()
            .setProximaDataDisponivel(proxima);
        contexto.getResultado(Instrumento.PIX).getAgendamento()
            .adicionarAviso(new Aviso("FERIADO_NACIONAL", LocalDate.now().plusDays(1), "Corpus Christi"));

        AvaliacaoResponse response = mapper.toResponse(contexto);
        InstrumentoPix pix = (InstrumentoPix) response.instrumentos().get(0);

        assertThat(pix.agendamento().podeAgendar()).isTrue();
        assertThat(pix.agendamento().avisos()).hasSize(1);

        JsonNode json = objectMapper.valueToTree(pix.agendamento());
        assertThat(json.has("avisos")).isTrue();
        assertThat(json.get("avisos").isArray()).isTrue();
    }

    private AvaliacaoContexto contexto() {
        var request = new ChavePixAvaliacaoRequest(
            "checkin:chaves_pix:1:uuid", LocalDate.now(), new DadosOrigem("0001", "12345-6")
        );
        var checkin = new ChavePixCheckinResponse(
            "checkin:chaves_pix:1:uuid", "uuid",
            new DadosBancarios("60701190", "0001", "12345-6", "0", "CORRENTE")
        );
        return new AvaliacaoContexto(request, TipoCheckin.CHAVE_PIX, checkin, Set.of(Instrumento.PIX));
    }
}
