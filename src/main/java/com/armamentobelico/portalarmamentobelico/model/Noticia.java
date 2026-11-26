package com.armamentobelico.portalarmamentobelico.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
@Table("noticia")
public class Noticia implements Persistable<UUID> {

    private static final String[] MESES_PT = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private static final DateTimeFormatter FORMATTER_EN = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    @Id
    @Column("id")
    private UUID id;

    @Column("slug")
    private String slug;

    @Column("titulo_pt")
    private String tituloPt;

    @Column("titulo_en")
    private String tituloEn;

    @Column("tag_destaque_pt")
    private String tagDestaquePt;

    @Column("tag_destaque_en")
    private String tagDestaqueEn;

    @Column("categoria_pt")
    private String categoriaPt;

    @Column("categoria_en")
    private String categoriaEn;

    @Column("autor")
    private String autor;

    @Column("data_publicacao")
    private LocalDate dataPublicacao;

    @Column("resumo_pt")
    private String resumoPt;

    @Column("resumo_en")
    private String resumoEn;

    @Column("conteudo_html_pt")
    private String conteudoHtmlPt;

    @Column("conteudo_html_en")
    private String conteudoHtmlEn;

    @Column("imagem_destaque_path")
    private String imagemDestaquePath;

    @Column("legenda_imagem_pt")
    private String legendaImagemPt;

    @Column("legenda_imagem_en")
    private String legendaImagemEn;

    @Column("edicao_id")
    private UUID edicaoId;

    @Column("cta_texto_pt")
    private String ctaTextoPt;

    @Column("cta_texto_en")
    private String ctaTextoEn;

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

    public boolean temVersaoEn() {
        return naoVazio(tituloEn) && naoVazio(resumoEn) && naoVazio(conteudoHtmlEn);
    }

    public String getDataFormatadaPt() {
        if (dataPublicacao == null) {
            return "";
        }
        String mes = MESES_PT[dataPublicacao.getMonthValue() - 1];
        return String.format("%02d de %s de %d", dataPublicacao.getDayOfMonth(), mes, dataPublicacao.getYear());
    }

    public String getDataFormatadaEn() {
        if (dataPublicacao == null) {
            return "";
        }
        return dataPublicacao.format(FORMATTER_EN);
    }

    public String getDataComDestaquePt() {
        String base = getDataFormatadaPt();
        return naoVazio(tagDestaquePt) ? base + " • " + tagDestaquePt : base;
    }

    public String getDataComDestaqueEn() {
        if (!temVersaoEn()) {
            return getDataComDestaquePt();
        }
        String base = getDataFormatadaEn();
        if (naoVazio(tagDestaqueEn)) {
            return base + " • " + tagDestaqueEn;
        }
        return naoVazio(tagDestaquePt) ? base + " • " + tagDestaquePt : base;
    }

    public String getTituloEnOuPt() {
        return temVersaoEn() ? tituloEn : tituloPt;
    }

    public String getResumoEnOuPt() {
        return temVersaoEn() ? resumoEn : resumoPt;
    }

    public String getCategoriaEnOuPt() {
        return temVersaoEn() && naoVazio(categoriaEn) ? categoriaEn : categoriaPt;
    }

    public String getTagDestaqueEnOuPt() {
        if (temVersaoEn() && naoVazio(tagDestaqueEn)) {
            return tagDestaqueEn;
        }
        return tagDestaquePt;
    }

    public String getLegendaImagemEnOuPt() {
        if (temVersaoEn() && naoVazio(legendaImagemEn)) {
            return legendaImagemEn;
        }
        return legendaImagemPt;
    }

    public String getUrlPublica() {
        return "/noticias/" + slug;
    }

    private static boolean naoVazio(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
