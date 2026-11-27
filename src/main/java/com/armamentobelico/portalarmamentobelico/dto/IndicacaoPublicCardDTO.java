package com.armamentobelico.portalarmamentobelico.dto;

import com.armamentobelico.portalarmamentobelico.model.IndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;

public record IndicacaoPublicCardDTO(
        String nomeBanda,
        String pais,
        String regiao,
        String tituloMaterialFisico,
        String linkAudicao,
        StatusIndicacaoBanda status,
        String statusLabelPt,
        String statusLabelEn,
        String statusCssClass) {

    public static IndicacaoPublicCardDTO fromEntity(IndicacaoBanda entity) {
        StatusIndicacaoBanda st = entity.getStatus();
        return new IndicacaoPublicCardDTO(
                entity.getNomeBanda(),
                entity.getPais(),
                entity.getRegiao(),
                entity.getTituloMaterialFisico(),
                entity.getLinkAudicao(),
                st,
                st.getLabelPublicoPt(),
                st.getLabelPublicoEn(),
                st.getCssClass());
    }

    public String localidadeFormatada() {
        if (regiao != null && !regiao.isBlank()) {
            return pais + " • " + regiao;
        }
        return pais;
    }
}
