package com.armamentobelico.portalarmamentobelico.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum MoedaEdicao {
    BRL("R$"),
    EUR("€");

    private final String simbolo;

    MoedaEdicao(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String formatar(BigDecimal valor, IdiomaEdicao idiomaUi) {
        BigDecimal safeValor = (valor != null ? valor : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        String numero = safeValor.toPlainString();
        if (this == BRL || idiomaUi == IdiomaEdicao.PT) {
            return simbolo + " " + numero.replace('.', ',');
        }
        return simbolo + " " + numero;
    }
}
