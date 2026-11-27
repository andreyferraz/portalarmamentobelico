package com.armamentobelico.portalarmamentobelico.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EventoPublicCardDTO(
        UUID id,
        String titulo,
        String flyerUrl,
        String cidade,
        String estado,
        LocalDate dataEvento,
        String dataFormatada,
        String horario,
        String contatoOrganizador,
        boolean passado) {
}
