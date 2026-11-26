package com.armamentobelico.portalarmamentobelico.model;

public enum StatusEstoque {
    AVAILABLE(
            "is-available",
            "fa-solid fa-circle-check",
            "Disponível",
            "Available",
            "Disponível em estoque",
            "In stock"),
    LOW(
            "is-low",
            "fa-solid fa-fire",
            "Últimas unidades",
            "Last copies",
            "Últimas cópias em estoque",
            "Last copies remaining"),
    SOLDOUT(
            "is-soldout",
            "fa-solid fa-skull",
            "Esgotada",
            "Sold Out",
            "Tiragem esgotada",
            "Print run sold out");

    private final String cssClass;
    private final String iconClass;
    private final String labelPt;
    private final String labelEn;
    private final String inlineLabelPt;
    private final String inlineLabelEn;

    StatusEstoque(String cssClass, String iconClass, String labelPt, String labelEn,
            String inlineLabelPt, String inlineLabelEn) {
        this.cssClass = cssClass;
        this.iconClass = iconClass;
        this.labelPt = labelPt;
        this.labelEn = labelEn;
        this.inlineLabelPt = inlineLabelPt;
        this.inlineLabelEn = inlineLabelEn;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getIconClass() {
        return iconClass;
    }

    public String getLabelPt() {
        return labelPt;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public String getLabel(IdiomaEdicao idioma) {
        return idioma == IdiomaEdicao.EN ? labelEn : labelPt;
    }

    public String getInlineLabel(IdiomaEdicao idioma) {
        return idioma == IdiomaEdicao.EN ? inlineLabelEn : inlineLabelPt;
    }

    public boolean isInStock() {
        return this != SOLDOUT;
    }
}
