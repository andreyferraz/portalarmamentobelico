package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.armamentobelico.portalarmamentobelico.service.EdicaoService;

@Controller
@RequestMapping("/admin")
public class AdminPagesController {

    private final EdicaoService edicaoService;

    public AdminPagesController(EdicaoService edicaoService) {
        this.edicaoService = edicaoService;
    }

    @GetMapping({ "", "/" })
    public String index(Model model) {
        EdicaoService.ResumoAdminEdicoes resumo = edicaoService.contarResumoAdmin();
        model.addAttribute("resumoEdicoes", resumo);
        model.addAttribute("totalEdicoes", resumo.total());
        model.addAttribute("totalEdicoesPublicadas", resumo.totalPublicadas());
        return "admin/index";
    }
}
