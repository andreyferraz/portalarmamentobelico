package com.armamentobelico.portalarmamentobelico.model;

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
@Table("edicao_entrevista")
public class EdicaoEntrevista implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("edicao_id")
    private UUID edicaoId;

    @Column("banda")
    private String banda;

    @Column("pais")
    private String pais;

    @Column("estilo")
    private String estilo;

    @Column("ordem")
    private Integer ordem;

    @Column("indicacao_banda_id")
    private UUID indicacaoBandaId;

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
}
