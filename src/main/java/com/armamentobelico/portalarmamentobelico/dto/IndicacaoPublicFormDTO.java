package com.armamentobelico.portalarmamentobelico.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IndicacaoPublicFormDTO {

    private String bandName;
    private String country;
    private String region;
    private String releaseTitle;
    private String listenUrl;
    private boolean confirmarHomonimo;
    private String esclarecimentoHomonimo;
    private String website;
}
