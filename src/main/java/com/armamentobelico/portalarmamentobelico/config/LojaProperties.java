package com.armamentobelico.portalarmamentobelico.config;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.MoedaEdicao;

@Component
@ConfigurationProperties(prefix = "loja")
public class LojaProperties {

    private Frete frete = new Frete();
    private Whatsapp whatsapp = new Whatsapp();
    private Paypal paypal = new Paypal();

    public record ResumoPayPalEur(BigDecimal baseLiquida, BigDecimal taxaPayPal, BigDecimal totalComTaxa) {
    }

    public static class Frete {
        private MoedaFrete brl = new MoedaFrete(new BigDecimal("13.00"), new BigDecimal("2.00"));
        private MoedaFrete eur = new MoedaFrete(new BigDecimal("10.00"), new BigDecimal("3.00"));

        public MoedaFrete getBrl() {
            return brl;
        }

        public void setBrl(MoedaFrete brl) {
            this.brl = brl;
        }

        public MoedaFrete getEur() {
            return eur;
        }

        public void setEur(MoedaFrete eur) {
            this.eur = eur;
        }
    }

    public static class MoedaFrete {
        private BigDecimal base;
        private BigDecimal adicional;

        public MoedaFrete() {
        }

        public MoedaFrete(BigDecimal base, BigDecimal adicional) {
            this.base = base;
            this.adicional = adicional;
        }

        public BigDecimal getBase() {
            return base;
        }

        public void setBase(BigDecimal base) {
            this.base = base;
        }

        public BigDecimal getAdicional() {
            return adicional;
        }

        public void setAdicional(BigDecimal adicional) {
            this.adicional = adicional;
        }
    }

    public static class Whatsapp {
        private String numero = "5527981076157";

        public String getNumero() {
            return numero;
        }

        public void setNumero(String numero) {
            this.numero = numero;
        }
    }

    public static class Paypal {
        private String email = "revistadeathmetal@hotmail.com";
        private BigDecimal taxaPercentual = new BigDecimal("0.064");
        private BigDecimal taxaFixaEur = new BigDecimal("0.35");

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public BigDecimal getTaxaPercentual() {
            return taxaPercentual;
        }

        public void setTaxaPercentual(BigDecimal taxaPercentual) {
            this.taxaPercentual = taxaPercentual;
        }

        public BigDecimal getTaxaFixaEur() {
            return taxaFixaEur;
        }

        public void setTaxaFixaEur(BigDecimal taxaFixaEur) {
            this.taxaFixaEur = taxaFixaEur;
        }
    }

    public Frete getFrete() {
        return frete;
    }

    public void setFrete(Frete frete) {
        this.frete = frete;
    }

    public Whatsapp getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(Whatsapp whatsapp) {
        this.whatsapp = whatsapp;
    }

    public Paypal getPaypal() {
        return paypal;
    }

    public void setPaypal(Paypal paypal) {
        this.paypal = paypal;
    }

    public BigDecimal calcularFrete(MoedaEdicao moeda, int quantidade) {
        if (quantidade <= 0 || moeda == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        MoedaFrete regra = moeda == MoedaEdicao.EUR ? frete.getEur() : frete.getBrl();
        BigDecimal base = regra.getBase() != null ? regra.getBase() : BigDecimal.ZERO;
        BigDecimal adicional = regra.getAdicional() != null ? regra.getAdicional() : BigDecimal.ZERO;
        return base.add(adicional.multiply(BigDecimal.valueOf(quantidade - 1L)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public ResumoPayPalEur calcularResumoPayPalEur(BigDecimal baseLiquida) {
        BigDecimal safeBase = baseLiquida == null ? BigDecimal.ZERO : baseLiquida.setScale(2, RoundingMode.HALF_UP);
        if (safeBase.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return new ResumoPayPalEur(zero, zero, zero);
        }
        BigDecimal pct = paypal.getTaxaPercentual() != null ? paypal.getTaxaPercentual() : BigDecimal.ZERO;
        BigDecimal fixa = paypal.getTaxaFixaEur() != null ? paypal.getTaxaFixaEur() : BigDecimal.ZERO;
        BigDecimal divisor = BigDecimal.ONE.subtract(pct);
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Taxa percentual do PayPal inválida: deve ser menor que 1.");
        }
        BigDecimal totalComTaxa = safeBase.add(fixa).divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal taxaPayPal = totalComTaxa.subtract(safeBase).setScale(2, RoundingMode.HALF_UP);
        return new ResumoPayPalEur(safeBase, taxaPayPal, totalComTaxa);
    }

    public String getFreteBrlBaseFormatado() {
        return MoedaEdicao.BRL.formatar(frete.getBrl().getBase(), IdiomaEdicao.PT);
    }

    public String getFreteBrlAdicionalFormatado() {
        return MoedaEdicao.BRL.formatar(frete.getBrl().getAdicional(), IdiomaEdicao.PT);
    }

    public String getFreteEurBaseFormatado() {
        return MoedaEdicao.EUR.formatar(frete.getEur().getBase(), IdiomaEdicao.EN);
    }

    public String getFreteEurAdicionalFormatado() {
        return MoedaEdicao.EUR.formatar(frete.getEur().getAdicional(), IdiomaEdicao.EN);
    }
}
