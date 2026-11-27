package com.armamentobelico.portalarmamentobelico.exceptions;

import com.armamentobelico.portalarmamentobelico.dto.VerificacaoBandaResponseDTO;

public class ConflitoIndicacaoBandaException extends RuntimeException {

    private final VerificacaoBandaResponseDTO verificacao;

    public ConflitoIndicacaoBandaException(String message, VerificacaoBandaResponseDTO verificacao) {
        super(message);
        this.verificacao = verificacao;
    }

    public VerificacaoBandaResponseDTO getVerificacao() {
        return verificacao;
    }
}
