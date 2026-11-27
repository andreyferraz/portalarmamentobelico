package com.armamentobelico.portalarmamentobelico.model;

import java.util.EnumSet;
import java.util.Set;

public enum StatusIndicacaoBanda {

    PENDENTE("Pendente", false, null, null, "is-warning", "", 99),
    RECUSADA("Recusada", false, null, null, "is-danger", "", 99),
    APROVADA("Aceita (No Radar)", true, "No Radar", "On Radar", "is-info", "is-radar", 3),
    EM_ENTREVISTA("Em Entrevista", true, "Em Entrevista", "Interviewing", "is-accent", "is-interviewing", 2),
    ENTREVISTA_CONCLUIDA("Entrevista Concluída", true, "Entrevista Concluída", "Interview Done", "is-success", "is-done", 1),
    PUBLICADA("Publicada em Edição", false, null, null, "is-neutral", "", 99);

    private final String labelAdmin;
    private final boolean publico;
    private final String labelPublicoPt;
    private final String labelPublicoEn;
    private final String adminBadgeClass;
    private final String publicCssClass;
    private final int prioridadeVitrine;

    StatusIndicacaoBanda(
            String labelAdmin,
            boolean publico,
            String labelPublicoPt,
            String labelPublicoEn,
            String adminBadgeClass,
            String publicCssClass,
            int prioridadeVitrine) {
        this.labelAdmin = labelAdmin;
        this.publico = publico;
        this.labelPublicoPt = labelPublicoPt;
        this.labelPublicoEn = labelPublicoEn;
        this.adminBadgeClass = adminBadgeClass;
        this.publicCssClass = publicCssClass;
        this.prioridadeVitrine = prioridadeVitrine;
    }

    public String getLabelAdmin() {
        return labelAdmin;
    }

    public boolean isPublico() {
        return publico;
    }

    public String getLabelPublicoPt() {
        return labelPublicoPt;
    }

    public String getLabelPublicoEn() {
        return labelPublicoEn;
    }

    public String getAdminBadgeClass() {
        return adminBadgeClass;
    }

    public String getPublicCssClass() {
        return publicCssClass;
    }

    public String getCssClass() {
        return publicCssClass;
    }

    public int getPrioridadeVitrine() {
        return prioridadeVitrine;
    }

    public Set<StatusIndicacaoBanda> getTransicoesPermitidas() {
        return switch (this) {
            case PENDENTE -> EnumSet.of(APROVADA, RECUSADA);
            case RECUSADA -> EnumSet.of(APROVADA);
            case APROVADA -> EnumSet.of(EM_ENTREVISTA, RECUSADA, PUBLICADA);
            case EM_ENTREVISTA -> EnumSet.of(ENTREVISTA_CONCLUIDA, APROVADA, RECUSADA, PUBLICADA);
            case ENTREVISTA_CONCLUIDA -> EnumSet.of(EM_ENTREVISTA, RECUSADA, PUBLICADA);
            case PUBLICADA -> EnumSet.of(ENTREVISTA_CONCLUIDA);
        };
    }

    public boolean podeTransicionarPara(StatusIndicacaoBanda destino) {
        return destino != null && getTransicoesPermitidas().contains(destino);
    }
}
