package com.armamentobelico.portalarmamentobelico.model;

import java.math.BigDecimal;
import java.time.Instant;
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
@Table("edicao")
public class Edicao implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("numero_edicao")
    private Integer numeroEdicao;

    @Column("idioma")
    private IdiomaEdicao idioma;

    @Column("titulo")
    private String titulo;

    @Column("subtitulo")
    private String subtitulo;

    @Column("mes_lancamento")
    private String mesLancamento;

    @Column("ano_lancamento")
    private Integer anoLancamento;

    @Column("ano_revista")
    private String anoRevista;

    @Column("paginas")
    private Integer paginas;

    @Column("preco")
    private BigDecimal preco;

    @Column("moeda")
    private MoedaEdicao moeda;

    @Column("status_estoque")
    private StatusEstoque statusEstoque;

    @Column("quantidade_estoque")
    private Integer quantidadeEstoque;

    @Column("prazo_postagem")
    private String prazoPostagem;

    @Column("capa_path")
    private String capaPath;

    @Column("descricao")
    private String descricao;

    @Column("formato")
    private String formato;

    @Column("papel_impressao")
    private String papelImpressao;

    @Column("tiragem")
    private String tiragem;

    @Column("youtube_url")
    private String youtubeUrl;

    @Column("publicado")
    private Boolean publicado;

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

    public String getPrecoFormatado() {
        MoedaEdicao m = moeda != null ? moeda : (idioma != null ? idioma.getMoeda() : MoedaEdicao.BRL);
        return m.formatar(preco, idioma != null ? idioma : IdiomaEdicao.PT);
    }

    public String getPrecoFormatadoEm(IdiomaEdicao idiomaUi) {
        MoedaEdicao m = moeda != null ? moeda : (idioma != null ? idioma.getMoeda() : MoedaEdicao.BRL);
        return m.formatar(preco, idiomaUi != null ? idiomaUi : IdiomaEdicao.PT);
    }

    public String getUrlPublica() {
        String slug = idioma != null ? idioma.getSlug() : "pt";
        return "/edicoes/" + slug + "/" + numeroEdicao;
    }
}
