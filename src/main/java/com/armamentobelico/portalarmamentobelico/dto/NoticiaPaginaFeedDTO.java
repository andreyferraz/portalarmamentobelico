package com.armamentobelico.portalarmamentobelico.dto;

import java.util.List;

public record NoticiaPaginaFeedDTO(
        List<NoticiaFeedItemDTO> itens,
        int paginaAtual,
        int totalPaginas,
        long totalItens,
        String tagAtiva,
        String buscaAtiva) {

    public boolean temAnterior() {
        return paginaAtual > 1;
    }

    public boolean temProxima() {
        return paginaAtual < totalPaginas;
    }

    public boolean temFiltrosAtivos() {
        return (tagAtiva != null && !tagAtiva.isBlank()) || (buscaAtiva != null && !buscaAtiva.isBlank());
    }
}
