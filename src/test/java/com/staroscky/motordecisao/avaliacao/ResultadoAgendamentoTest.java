package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.contexto.ResultadoAgendamento;
import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.RestricaoGenerica;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoAgendamentoTest {

    @Test
    void isPodeAgendar_verdadeiroQuandoSemRestricoes() {
        assertThat(new ResultadoAgendamento().isPodeAgendar()).isTrue();
    }

    @Test
    void isPodeAgendar_falsoAposAdicionarRestricao() {
        ResultadoAgendamento resultado = new ResultadoAgendamento();
        resultado.adicionarRestricao(new RestricaoGenerica("MOTIVO"));
        assertThat(resultado.isPodeAgendar()).isFalse();
    }

    @Test
    void adicionarAviso_naoAfetaIsPodeAgendar() {
        ResultadoAgendamento resultado = new ResultadoAgendamento();
        resultado.adicionarAviso(new Aviso("FERIADO", LocalDate.now(), "Feriado nacional"));
        assertThat(resultado.isPodeAgendar()).isTrue();
    }

    @Test
    void temProximaData_falsoQuandoNull() {
        assertThat(new ResultadoAgendamento().temProximaData()).isFalse();
    }

    @Test
    void temProximaData_verdadeiroAposSet() {
        ResultadoAgendamento resultado = new ResultadoAgendamento();
        resultado.setProximaDataDisponivel(LocalDate.now().plusDays(1));
        assertThat(resultado.temProximaData()).isTrue();
    }
}
