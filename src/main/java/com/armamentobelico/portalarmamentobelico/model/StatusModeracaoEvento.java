package com.armamentobelico.portalarmamentobelico.model;

public enum StatusModeracaoEvento {
    PENDENTE("Aguardando aprovação", "is-warning"),
    APROVADO("Aprovado", "is-success"),
    RECUSADO("Recusado", "is-danger");

    private final String labelAdmin;
    private final String badgeClass;

    StatusModeracaoEvento(String labelAdmin, String badgeClass) {
        this.labelAdmin = labelAdmin;
        this.badgeClass = badgeClass;
    }

    public String getLabelAdmin() {
        return labelAdmin;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
