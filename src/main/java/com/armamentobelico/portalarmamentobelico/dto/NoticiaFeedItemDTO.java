package com.armamentobelico.portalarmamentobelico.dto;

import java.util.List;

import com.armamentobelico.portalarmamentobelico.model.Noticia;
import com.armamentobelico.portalarmamentobelico.model.NoticiaTag;

public record NoticiaFeedItemDTO(
        Noticia noticia,
        List<NoticiaTag> tags) {
}
