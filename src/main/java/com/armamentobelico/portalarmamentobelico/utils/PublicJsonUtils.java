package com.armamentobelico.portalarmamentobelico.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.armamentobelico.portalarmamentobelico.config.LojaProperties;
import com.armamentobelico.portalarmamentobelico.dto.EdicaoCatalogoItemDTO;

public final class PublicJsonUtils {

    private PublicJsonUtils() {
    }

    public static String jsonStringOrNull(String value) {
        return value == null ? "null" : jsonString(value);
    }

    public static String jsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String jsonNumber(BigDecimal value) {
        return value != null ? value.toPlainString() : "0";
    }

    public static String serializarLojaConfig(LojaProperties props) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        sb.append("\"shipping\":{");
        sb.append("\"R$\":{")
                .append("\"baseFee\":").append(jsonNumber(props.getFrete().getBrl().getBase())).append(',')
                .append("\"additionalFee\":").append(jsonNumber(props.getFrete().getBrl().getAdicional()))
                .append("},");
        sb.append("\"€\":{")
                .append("\"baseFee\":").append(jsonNumber(props.getFrete().getEur().getBase())).append(',')
                .append("\"additionalFee\":").append(jsonNumber(props.getFrete().getEur().getAdicional()))
                .append('}');
        sb.append("},");
        sb.append("\"whatsappNumber\":").append(jsonString(props.getWhatsapp().getNumero())).append(',');
        sb.append("\"paypal\":{")
                .append("\"email\":").append(jsonString(props.getPaypal().getEmail())).append(',')
                .append("\"feePercent\":").append(jsonNumber(props.getPaypal().getTaxaPercentual())).append(',')
                .append("\"fixedFeeEur\":").append(jsonNumber(props.getPaypal().getTaxaFixaEur()))
                .append('}');
        sb.append('}');
        return sb.toString();
    }

    public static String serializarCatalogoPorIdioma(Map<String, List<EdicaoCatalogoItemDTO>> catalogo) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\"pt\":");
        appendListaCatalogo(sb, catalogo.getOrDefault("pt", List.of()));
        sb.append(",\"en\":");
        appendListaCatalogo(sb, catalogo.getOrDefault("en", List.of()));
        sb.append('}');
        return sb.toString();
    }

    private static void appendListaCatalogo(StringBuilder sb, List<EdicaoCatalogoItemDTO> itens) {
        sb.append('[');
        for (int i = 0; i < itens.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            EdicaoCatalogoItemDTO item = itens.get(i);
            sb.append('{');
            sb.append("\"id\":").append(jsonString(item.id().toString())).append(',');
            sb.append("\"editionNumber\":").append(item.editionNumber()).append(',');
            sb.append("\"editionLang\":").append(jsonString(item.editionLang())).append(',');
            sb.append("\"detailUrl\":").append(jsonString(item.detailUrl())).append(',');
            sb.append("\"title\":").append(jsonString(item.title())).append(',');
            sb.append("\"subtitle\":").append(jsonStringOrNull(item.subtitle())).append(',');
            sb.append("\"price\":").append(jsonNumber(item.price())).append(',');
            sb.append("\"currency\":").append(jsonString(item.currency())).append(',');
            sb.append("\"cover\":").append(jsonString(item.cover())).append(',');
            sb.append("\"date\":").append(jsonString(item.date())).append(',');
            sb.append("\"bands\":").append(jsonString(item.bands())).append(',');
            sb.append("\"inStock\":").append(item.inStock()).append(',');
            sb.append("\"stockStatus\":").append(jsonString(item.stockStatus())).append(',');
            sb.append("\"pages\":").append(item.pages() != null ? item.pages() : 0);
            sb.append('}');
        }
        sb.append(']');
    }
}
