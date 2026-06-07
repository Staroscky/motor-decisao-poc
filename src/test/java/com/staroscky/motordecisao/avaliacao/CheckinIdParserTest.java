package com.staroscky.motordecisao.avaliacao;

import com.staroscky.motordecisao.avaliacao.resolver.CheckinIdInvalidoException;
import com.staroscky.motordecisao.avaliacao.resolver.CheckinIdParser;
import com.staroscky.motordecisao.avaliacao.resolver.TipoCheckin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckinIdParserTest {

    @Test
    void parseQrcode() {
        assertThat(CheckinIdParser.parse("checkin:qrcode:1:uuid")).isEqualTo(TipoCheckin.QRCODE);
    }

    @Test
    void parseChavesPix() {
        assertThat(CheckinIdParser.parse("checkin:chaves_pix:1:uuid")).isEqualTo(TipoCheckin.CHAVE_PIX);
    }

    @Test
    void parseAgconta() {
        assertThat(CheckinIdParser.parse("checkin:agconta:1:uuid")).isEqualTo(TipoCheckin.AGCONTA);
    }

    @Test
    void parseMenosDeDoisSegmentosLancaExcecao() {
        assertThatThrownBy(() -> CheckinIdParser.parse("checkin"))
            .isInstanceOf(CheckinIdInvalidoException.class);
    }

    @Test
    void parseTipoDesconhecidoLancaExcecao() {
        assertThatThrownBy(() -> CheckinIdParser.parse("checkin:boleto:1:uuid"))
            .isInstanceOf(CheckinIdInvalidoException.class);
    }

    @Test
    void parseFormatoCompletoComUuid() {
        assertThat(CheckinIdParser.parse("checkin:qrcode:1:550e8400-e29b-41d4-a716"))
            .isEqualTo(TipoCheckin.QRCODE);
    }
}
