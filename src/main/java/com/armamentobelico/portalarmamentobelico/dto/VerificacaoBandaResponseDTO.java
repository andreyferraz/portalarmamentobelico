package com.armamentobelico.portalarmamentobelico.dto;

import java.util.List;

import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;

public record VerificacaoBandaResponseDTO(
        String situacao,
        boolean exigeEsclarecimentoHomonimo,
        String mensagemPt,
        String mensagemEn,
        List<EdicaoReferenciaDTO> edicoes,
        StatusIndicacaoBanda statusIndicacaoExistente,
        String statusLabelPt,
        String statusLabelEn) {

    public record EdicaoReferenciaDTO(
            Integer numeroEdicao,
            String titulo,
            String url) {
    }

    public static VerificacaoBandaResponseDTO disponivel() {
        return new VerificacaoBandaResponseDTO(
                "DISPONIVEL",
                false,
                null,
                null,
                List.of(),
                null,
                null,
                null);
    }
}
