package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.resolver.Instrumento;
import com.staroscky.motordecisao.avaliacao.resolver.InstrumentoResolver;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumentoResolverTest {

    @Test
    void qrcodeRetornaPix() {
        assertThat(InstrumentoResolver.resolver(TipoCheckin.QRCODE, "qualquer"))
            .containsExactly(Instrumento.PIX);
    }

    @Test
    void chavePixRetornaPix() {
        assertThat(InstrumentoResolver.resolver(TipoCheckin.CHAVE_PIX, "qualquer"))
            .containsExactly(Instrumento.PIX);
    }

    @Test
    void agcontaItauRetornaPixTef() {
        assertThat(InstrumentoResolver.resolver(TipoCheckin.AGCONTA, "60701190"))
            .containsExactlyInAnyOrder(Instrumento.PIX, Instrumento.TEF);
    }

    @Test
    void agcontaOutroBancoRetornaPixTed() {
        assertThat(InstrumentoResolver.resolver(TipoCheckin.AGCONTA, "outro"))
            .containsExactlyInAnyOrder(Instrumento.PIX, Instrumento.TED);
    }

    @Test
    void agcontaIspbVazioRetornaPixTed() {
        assertThat(InstrumentoResolver.resolver(TipoCheckin.AGCONTA, ""))
            .containsExactlyInAnyOrder(Instrumento.PIX, Instrumento.TED);
    }
}
