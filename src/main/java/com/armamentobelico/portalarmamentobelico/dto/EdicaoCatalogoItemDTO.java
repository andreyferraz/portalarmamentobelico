package com.armamentobelico.portalarmamentobelico.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EdicaoCatalogoItemDTO(
        UUID id,
        Integer editionNumber,
        String editionLang,
        String detailUrl,
        String title,
        String subtitle,
        BigDecimal price,
        String currency,
        String cover,
        String date,
        String bands,
        boolean inStock,
        String stockStatus,
        Integer pages) {
}
