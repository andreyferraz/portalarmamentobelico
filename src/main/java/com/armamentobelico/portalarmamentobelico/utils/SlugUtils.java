package com.armamentobelico.portalarmamentobelico.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern COMBINING_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");

    private SlugUtils() {
    }

    public static String gerarSlug(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        String semAcentos = COMBINING_DIACRITICS.matcher(normalizado).replaceAll("");
        String minusculo = semAcentos.toLowerCase(Locale.ROOT);
        String hifenizado = NON_ALPHANUMERIC.matcher(minusculo).replaceAll("-");
        return EDGE_HYPHENS.matcher(hifenizado).replaceAll("");
    }
}
