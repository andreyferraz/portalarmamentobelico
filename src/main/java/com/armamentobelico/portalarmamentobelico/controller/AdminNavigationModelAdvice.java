package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.EventoService;
import com.armamentobelico.portalarmamentobelico.service.IndicacaoBandaService;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;

@ControllerAdvice(assignableTypes = {
        AdminPagesController.class,
        AdminEdicaoController.class,
        AdminNoticiaController.class,
        AdminEventoController.class,
        AdminIndicacaoController.class
})
public class AdminNavigationModelAdvice {

    private final EdicaoService edicaoService;
    private final NoticiaService noticiaService;
    private final EventoService eventoService;
    private final IndicacaoBandaService indicacaoBandaService;

    public AdminNavigationModelAdvice(
            EdicaoService edicaoService,
            NoticiaService noticiaService,
            EventoService eventoService,
            IndicacaoBandaService indicacaoBandaService) {
        this.edicaoService = edicaoService;
        this.noticiaService = noticiaService;
        this.eventoService = eventoService;
        this.indicacaoBandaService = indicacaoBandaService;
    }

    @ModelAttribute("totalEdicoes")
    public long totalEdicoes() {
        return edicaoService.contarResumoAdmin().total();
    }

    @ModelAttribute("totalNoticias")
    public long totalNoticias() {
        return noticiaService.contarResumoAdmin().total();
    }

    @ModelAttribute("eventosPendentes")
    public long eventosPendentes() {
        return eventoService.contarResumoAdmin().pendentes();
    }

    @ModelAttribute("indicacoesPendentes")
    public long indicacoesPendentes() {
        return indicacaoBandaService.contarResumoAdmin().pendentes();
    }
}
