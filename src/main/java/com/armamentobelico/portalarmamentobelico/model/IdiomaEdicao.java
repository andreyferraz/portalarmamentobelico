package com.armamentobelico.portalarmamentobelico.model;

import java.util.Arrays;
import java.util.Optional;

public enum IdiomaEdicao {
    PT("pt", "Português", "Português (Brasil)", "Brazilian Portuguese", MoedaEdicao.BRL),
    EN("en", "Inglês", "Inglês (Internacional)", "English (International)", MoedaEdicao.EUR);

    private final String slug;
    private final String labelAdmin;
    private final String fichaTecnicaPt;
    private final String fichaTecnicaEn;
    private final MoedaEdicao moeda;

    IdiomaEdicao(String slug, String labelAdmin, String fichaTecnicaPt, String fichaTecnicaEn, MoedaEdicao moeda) {
        this.slug = slug;
        this.labelAdmin = labelAdmin;
        this.fichaTecnicaPt = fichaTecnicaPt;
        this.fichaTecnicaEn = fichaTecnicaEn;
        this.moeda = moeda;
    }

    public String getSlug() {
        return slug;
    }

    public String getLabelAdmin() {
        return labelAdmin;
    }

    public String getFichaTecnicaPt() {
        return fichaTecnicaPt;
    }

    public String getFichaTecnicaEn() {
        return fichaTecnicaEn;
    }

    public String getFichaTecnica(IdiomaEdicao idiomaUi) {
        return idiomaUi == EN ? fichaTecnicaEn : fichaTecnicaPt;
    }

    public MoedaEdicao getMoeda() {
        return moeda;
    }

    public IdiomaEdicao getComplementar() {
        return this == PT ? EN : PT;
    }

    public static Optional<IdiomaEdicao> fromSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        String normalized = slug.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(i -> i.slug.equals(normalized))
                .findFirst();
    }
}
