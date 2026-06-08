package com.staroscky.motordecisao.avaliacao.modelo;

import java.time.LocalDate;

public record Aviso(String codigo, LocalDate data, String descricao) {}
