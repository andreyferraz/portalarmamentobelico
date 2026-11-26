package com.armamentobelico.portalarmamentobelico.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.armamentobelico.portalarmamentobelico.model.EstiloTag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoticiaFormDTO {

    private UUID id;
    private String slug;

    private String tituloPt;
    private String tituloEn;

    private String tagDestaquePt;
    private String tagDestaqueEn;

    private String categoriaPt;
    private String categoriaEn;

    private String autor;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPublicacao;

    private String resumoPt;
    private String resumoEn;

    private String conteudoHtmlPt;
    private String conteudoHtmlEn;

    private MultipartFile imagemDestaque;
    private String imagemAtualPath;
    private boolean removerImagemAtual;

    private String legendaImagemPt;
    private String legendaImagemEn;

    private UUID edicaoId;

    private String ctaTextoPt;
    private String ctaTextoEn;

    private Boolean publicado = Boolean.TRUE;

    private List<TagFormItem> tags = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagFormItem {
        private String nome;
        private EstiloTag estilo = EstiloTag.DARK;
    }
}
