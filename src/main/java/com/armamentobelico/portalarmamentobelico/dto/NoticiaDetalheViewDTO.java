package com.armamentobelico.portalarmamentobelico.dto;

import java.util.List;

import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.Noticia;
import com.armamentobelico.portalarmamentobelico.model.NoticiaTag;

public record NoticiaDetalheViewDTO(
        Noticia noticia,
        List<NoticiaTag> tags,
        Edicao edicaoVinculadaPt,
        Edicao edicaoVinculadaEn,
        String linkedEditionsCatalogJson) {

    public boolean temEdicaoVinculada() {
        return edicaoVinculadaPt != null || edicaoVinculadaEn != null;
    }
}
