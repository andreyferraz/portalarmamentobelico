package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.armamentobelico.portalarmamentobelico.dto.NoticiaDetalheViewDTO;
import com.armamentobelico.portalarmamentobelico.model.Noticia;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;

@Controller
@RequestMapping("/noticias")
public class NoticiaPublicController {

    private final NoticiaService noticiaService;

    public NoticiaPublicController(NoticiaService noticiaService) {
        this.noticiaService = noticiaService;
    }

    @GetMapping("/{slug}")
    public String detalhes(@PathVariable String slug, Model model) {
        NoticiaDetalheViewDTO detalhes = noticiaService.buscarDetalhesPublicos(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notícia não encontrada."));

        Noticia noticia = detalhes.noticia();
        model.addAttribute("bodyClass", "page-noticia");
        model.addAttribute("pageTitle", noticia.getTituloPt() + " | Armamento Bélico Zine");
        model.addAttribute("detalhes", detalhes);
        model.addAttribute("noticia", noticia);
        model.addAttribute("tags", detalhes.tags());
        model.addAttribute("edicaoVinculadaPt", detalhes.edicaoVinculadaPt());
        model.addAttribute("edicaoVinculadaEn", detalhes.edicaoVinculadaEn());
        model.addAttribute("catalogPageDataJson", detalhes.linkedEditionsCatalogJson());
        return "noticia";
    }
}
