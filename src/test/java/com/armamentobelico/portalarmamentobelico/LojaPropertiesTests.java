package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

import com.armamentobelico.portalarmamentobelico.config.LojaProperties;
import com.armamentobelico.portalarmamentobelico.model.MoedaEdicao;

class LojaPropertiesTests {

    @Test
    void deveCalcularFreteProgressivoEmBrlEEur() {
        LojaProperties props = new LojaProperties();

        assertThat(props.calcularFrete(MoedaEdicao.BRL, 0)).isEqualByComparingTo("0.00");
        assertThat(props.calcularFrete(MoedaEdicao.BRL, 1)).isEqualByComparingTo("13.00");
        assertThat(props.calcularFrete(MoedaEdicao.BRL, 3)).isEqualByComparingTo("17.00");

        assertThat(props.calcularFrete(MoedaEdicao.EUR, 0)).isEqualByComparingTo("0.00");
        assertThat(props.calcularFrete(MoedaEdicao.EUR, 1)).isEqualByComparingTo("10.00");
        assertThat(props.calcularFrete(MoedaEdicao.EUR, 3)).isEqualByComparingTo("16.00");
    }

    @Test
    void deveCalcularTaxaPayPalEurComRepasseExatoGrossUp() {
        LojaProperties props = new LojaProperties();

        // Subtotal € 12.00 + Frete € 10.00 = € 22.00 líquidos
        // Gross-up: (22.00 + 0.35) / (1 - 0.064) = 22.35 / 0.936 = 23.8782... -> 23.88
        // Taxa PayPal = 23.88 - 22.00 = 1.88
        LojaProperties.ResumoPayPalEur resumo = props.calcularResumoPayPalEur(new BigDecimal("22.00"));
        assertThat(resumo.totalComTaxa()).isEqualByComparingTo("23.88");
        assertThat(resumo.taxaPayPal()).isEqualByComparingTo("1.88");

        // Verifica que o valor líquido recebido após 6,4% + € 0,35 cobre os € 22,00
        BigDecimal liquidoRecebido = resumo.totalComTaxa()
                .multiply(BigDecimal.ONE.subtract(props.getPaypal().getTaxaPercentual()))
                .subtract(props.getPaypal().getTaxaFixaEur())
                .setScale(2, RoundingMode.HALF_UP);
        assertThat(liquidoRecebido).isEqualByComparingTo("22.00");

        // Valor zero não gera taxa
        LojaProperties.ResumoPayPalEur zero = props.calcularResumoPayPalEur(BigDecimal.ZERO);
        assertThat(zero.totalComTaxa()).isEqualByComparingTo("0.00");
        assertThat(zero.taxaPayPal()).isEqualByComparingTo("0.00");
    }
}
