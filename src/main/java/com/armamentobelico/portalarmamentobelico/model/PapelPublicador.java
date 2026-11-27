package com.armamentobelico.portalarmamentobelico.model;

import java.util.Locale;

public enum PapelPublicador {
    ORGANIZADOR("organizer", "Organizador do evento", "Organizador"),
    AJUDANTE("helper", "Apenas ajudando na divulgação", "Ajudando na divulgação");

    private final String formValue;
    private final String descricao;
    private final String labelCurto;

    PapelPublicador(String formValue, String descricao, String labelCurto) {
        this.formValue = formValue;
        this.descricao = descricao;
        this.labelCurto = labelCurto;
    }

    public String getFormValue() {
        return formValue;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLabelCurto() {
        return labelCurto;
    }

    public static PapelPublicador fromValor(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Informe se você é o organizador ou está apenas ajudando na divulgação.");
        }
        String norm = valor.trim().toLowerCase(Locale.ROOT);
        if ("organizer".equals(norm) || "organizador".equals(norm)) {
            return ORGANIZADOR;
        }
        if ("helper".equals(norm) || "ajudante".equals(norm)) {
            return AJUDANTE;
        }
        throw new IllegalArgumentException("Papel de quem publica inválido.");
    }
}
