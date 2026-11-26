package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;

@ControllerAdvice(assignableTypes = {
        AdminPagesController.class,
        AdminEdicaoController.class,
        AdminNoticiaController.class
})
public class AdminNavigationModelAdvice {

    private final EdicaoService edicaoService;
    private final NoticiaService noticiaService;

    public AdminNavigationModelAdvice(EdicaoService edicaoService, NoticiaService noticiaService) {
        this.edicaoService = edicaoService;
        this.noticiaService = noticiaService;
    }

    @ModelAttribute("totalEdicoes")
    public long totalEdicoes() {
        return edicaoService.contarResumoAdmin().total();
    }

    @ModelAttribute("totalNoticias")
    public long totalNoticias() {
        return noticiaService.contarResumoAdmin().total();
    }
}
