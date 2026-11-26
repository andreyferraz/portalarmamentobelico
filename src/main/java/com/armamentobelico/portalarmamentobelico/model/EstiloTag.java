package com.armamentobelico.portalarmamentobelico.model;

public enum EstiloTag {
    DARK("dark", "Escura"),
    BLOOD("blood", "Vermelho Sangue");

    private final String cssClass;
    private final String labelAdmin;

    EstiloTag(String cssClass, String labelAdmin) {
        this.cssClass = cssClass;
        this.labelAdmin = labelAdmin;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getLabelAdmin() {
        return labelAdmin;
    }
}
