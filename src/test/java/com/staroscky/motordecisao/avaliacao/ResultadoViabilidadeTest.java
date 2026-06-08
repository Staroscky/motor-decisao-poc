package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.modelo.restricao.RestricaoGenerica;
import com.staroscky.motordecisao.avaliacao.contexto.ResultadoViabilidade;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoViabilidadeTest {

    @Test
    void inicialmenteValido() {
        assertThat(new ResultadoViabilidade().isValido()).isTrue();
    }

    @Test
    void adicionarRestricaoSetaInvalido() {
        ResultadoViabilidade resultado = new ResultadoViabilidade();
        resultado.adicionarRestricao(new RestricaoGenerica("MOTIVO"));
        assertThat(resultado.isValido()).isFalse();
    }

    @Test
    void adicionarRestricaoAcumulaNaLista() {
        ResultadoViabilidade resultado = new ResultadoViabilidade();
        resultado.adicionarRestricao(new RestricaoGenerica("MOTIVO_1"));
        resultado.adicionarRestricao(new RestricaoGenerica("MOTIVO_2"));
        assertThat(resultado.getRestricoes()).hasSize(2);
    }
}
