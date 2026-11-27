package com.armamentobelico.portalarmamentobelico.dto;

import java.util.UUID;

import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IndicacaoAdminFormDTO {

    private UUID id;
    private String nomeBanda;
    private String pais;
    private String regiao;
    private String tituloMaterialFisico;
    private String linkAudicao;
    private Boolean homonimo;
    private String esclarecimentoHomonimo;
    private String edicaoHomonimoReferencia;
    private String motivoRecusa;
    private StatusIndicacaoBanda statusAtual;
    private Integer numeroEdicaoPublicada;
}
