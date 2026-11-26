package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;

@Controller
@RequestMapping("/admin")
public class AdminPagesController {

    private final EdicaoService edicaoService;
    private final NoticiaService noticiaService;

    public AdminPagesController(EdicaoService edicaoService, NoticiaService noticiaService) {
        this.edicaoService = edicaoService;
        this.noticiaService = noticiaService;
    }

    @GetMapping({ "", "/" })
    public String index(Model model) {
        EdicaoService.ResumoAdminEdicoes resumoEdicoes = edicaoService.contarResumoAdmin();
        NoticiaService.ResumoAdminNoticias resumoNoticias = noticiaService.contarResumoAdmin();
        model.addAttribute("resumoEdicoes", resumoEdicoes);
        model.addAttribute("totalEdicoesPublicadas", resumoEdicoes.totalPublicadas());
        model.addAttribute("resumoNoticias", resumoNoticias);
        model.addAttribute("totalNoticiasPublicadas", resumoNoticias.totalPublicadas());
        return "admin/index";
    }
}
