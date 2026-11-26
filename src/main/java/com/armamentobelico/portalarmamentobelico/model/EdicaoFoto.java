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
@Table("edicao_foto")
public class EdicaoFoto implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("edicao_id")
    private UUID edicaoId;

    @Column("arquivo_path")
    private String arquivoPath;

    @Column("legenda")
    private String legenda;

    @Column("ordem")
    private Integer ordem;

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
