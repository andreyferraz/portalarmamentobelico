package com.armamentobelico.portalarmamentobelico.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("indicacao_banda")
public class IndicacaoBanda implements Persistable<UUID> {

    private static final DateTimeFormatter DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    @Id
    @Column("id")
    private UUID id;

    @Column("nome_banda")
    private String nomeBanda;

    @Column("nome_banda_normalizado")
    private String nomeBandaNormalizado;

    @Column("pais")
    private String pais;

    @Column("pais_normalizado")
    private String paisNormalizado;

    @Column("regiao")
    private String regiao;

    @Column("titulo_material_fisico")
    private String tituloMaterialFisico;

    @Column("link_audicao")
    private String linkAudicao;

    @Column("status")
    private StatusIndicacaoBanda status;

    @Column("homonimo")
    private Boolean homonimo;

    @Column("esclarecimento_homonimo")
    private String esclarecimentoHomonimo;

    @Column("edicao_homonimo_referencia")
    private String edicaoHomonimoReferencia;

    @Column("motivo_recusa")
    private String motivoRecusa;

    @Column("edicao_publicada_id")
    private UUID edicaoPublicadaId;

    @Column("numero_edicao_publicada")
    private Integer numeroEdicaoPublicada;

    @Column("moderado_em")
    private Instant moderadoEm;

    @Column("publicada_em")
    private Instant publicadaEm;

    @Column("criado_em")
    private Instant criadoEm;

    @Column("atualizado_em")
    private Instant atualizadoEm;

    @Transient
    private boolean isNew;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public String getCriadoEmFormatado() {
        return criadoEm != null ? DATA_HORA_BR.format(criadoEm) : "—";
    }

    public String getLocalidadeFormatada() {
        if (regiao != null && !regiao.isBlank()) {
            return pais + " • " + regiao;
        }
        return pais;
    }
}
