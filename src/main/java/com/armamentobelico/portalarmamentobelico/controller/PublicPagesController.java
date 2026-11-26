package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;
import com.armamentobelico.portalarmamentobelico.utils.PublicJsonUtils;

@Controller
public class PublicPagesController {

    private static final int LIMITE_DESTAQUES_HOME = 4;

    private final EdicaoService edicaoService;
    private final NoticiaService noticiaService;

    public PublicPagesController(EdicaoService edicaoService, NoticiaService noticiaService) {
        this.edicaoService = edicaoService;
        this.noticiaService = noticiaService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bodyClass", "page-home");
        model.addAttribute("catalogPageDataJson",
                PublicJsonUtils.serializarCatalogoPorIdioma(
                        edicaoService.listarDestaquesHomePorIdioma(LIMITE_DESTAQUES_HOME)));
        model.addAttribute("ultimasNoticias", noticiaService.listarUltimasPublicadasHome());
        return "index";
    }

    @GetMapping("/historia")
    public String historia() {
        return "historia";
    }

    @GetMapping("/arquivo")
    public String arquivo(Model model) {
        model.addAttribute("bodyClass", "page-arquivo");
        model.addAttribute("catalogPageDataJson",
                PublicJsonUtils.serializarCatalogoPorIdioma(edicaoService.listarCatalogoPublicoPorIdioma()));
        return "arquivo";
    }

    @GetMapping("/noticias")
    public String noticias(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "1") int pagina,
            Model model) {
        model.addAttribute("bodyClass", "page-noticias");
        model.addAttribute("feed", noticiaService.listarFeedPublico(tag, busca, pagina));
        return "noticias";
    }

    @GetMapping("/eventos")
    public String eventos() {
        return "eventos";   
    }

    @GetMapping("/indique")
    public String indique() {
        return "indique";
    }

    @GetMapping("/aliados")
    public String aliados() {
        return "aliados";
    }

    @GetMapping("/assinatura")
    public String assinatura() {
        return "assinatura";
    }

    @GetMapping("/privacidade")
    public String privacidade() {
        return "privacidade";
    }

}
