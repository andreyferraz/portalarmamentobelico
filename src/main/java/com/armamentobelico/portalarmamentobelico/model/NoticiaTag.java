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
@Table("noticia_tag")
public class NoticiaTag implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("noticia_id")
    private UUID noticiaId;

    @Column("nome")
    private String nome;

    @Column("estilo")
    private EstiloTag estilo;

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

    public String getCssClass() {
        return estilo != null ? estilo.getCssClass() : EstiloTag.DARK.getCssClass();
    }
}
