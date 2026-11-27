package com.armamentobelico.portalarmamentobelico.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.armamentobelico.portalarmamentobelico.dto.IndicacaoAdminFormDTO;
import com.armamentobelico.portalarmamentobelico.model.IndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.service.IndicacaoBandaService;

@Controller
@RequestMapping("/admin/indicacoes")
public class AdminIndicacaoController {

    private final IndicacaoBandaService indicacaoService;

    public AdminIndicacaoController(IndicacaoBandaService indicacaoService) {
        this.indicacaoService = indicacaoService;
    }

    @GetMapping({ "", "/" })
    public String listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            Model model) {
        IndicacaoBandaService.ResumoAdminIndicacoes resumo = indicacaoService.contarResumoAdmin();
        String statusEfetivo = (status == null || status.isBlank())
                ? (resumo.pendentes() > 0 ? StatusIndicacaoBanda.PENDENTE.name() : "TODAS")
                : indicacaoService.normalizarFiltroStatusAdmin(status);
        String buscaLimpa = busca != null ? busca.trim() : "";

        List<IndicacaoBanda> indicacoes = indicacaoService.listarParaAdmin(statusEfetivo, buscaLimpa);

        model.addAttribute("indicacoes", indicacoes);
        model.addAttribute("resumoIndicacoes", resumo);
        model.addAttribute("statusFiltro", statusEfetivo);
        model.addAttribute("busca", buscaLimpa);
        return "admin/indicacoes/lista";
    }

    @PostMapping("/{id}/aceitar")
    public String aceitar(
            @PathVariable UUID id,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String busca,
            RedirectAttributes redirectAttributes) {
        try {
            IndicacaoBanda aceita = indicacaoService.aceitar(id);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Horda \"" + aceita.getNomeBanda() + "\" aceita e movida para No Radar!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, busca);
    }

    @PostMapping("/{id}/recusar")
    public String recusar(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivoRecusa,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String busca,
            RedirectAttributes redirectAttributes) {
        try {
            IndicacaoBanda recusada = indicacaoService.recusar(id, motivoRecusa);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Indicação de \"" + recusada.getNomeBanda() + "\" marcada como recusada.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, busca);
    }

    @PostMapping("/{id}/em-entrevista")
    public String iniciarEntrevista(
            @PathVariable UUID id,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String busca,
            RedirectAttributes redirectAttributes) {
        try {
            IndicacaoBanda atualizada = indicacaoService.iniciarEntrevista(id);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Horda \"" + atualizada.getNomeBanda() + "\" marcada como Em Entrevista.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, busca);
    }

    @PostMapping("/{id}/concluir-entrevista")
    public String concluirEntrevista(
            @PathVariable UUID id,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String busca,
            RedirectAttributes redirectAttributes) {
        try {
            IndicacaoBanda atualizada = indicacaoService.concluirEntrevista(id);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Entrevista com \"" + atualizada.getNomeBanda() + "\" marcada como concluída!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, busca);
    }

    @PostMapping("/{id}/reverter")
    public String reverter(
            @PathVariable UUID id,
            @RequestParam StatusIndicacaoBanda destino,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String busca,
            RedirectAttributes redirectAttributes) {
        try {
            IndicacaoBanda atualizada = indicacaoService.reverterEtapa(id, destino);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Horda \"" + atualizada.getNomeBanda() + "\" revertida para \"" + destino.getLabelAdmin() + "\".");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, busca);
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable UUID id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            IndicacaoAdminFormDTO form = indicacaoService.carregarParaEdicao(id);
            model.addAttribute("indicacaoForm", form);
            return "admin/indicacoes/formulario";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/admin/indicacoes";
        }
    }

    @PostMapping("/{id}/editar")
    public String salvarEdicao(
            @PathVariable UUID id,
            @ModelAttribute("indicacaoForm") IndicacaoAdminFormDTO form,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            IndicacaoBanda atualizada = indicacaoService.atualizarAdmin(id, form);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Indicação de \"" + atualizada.getNomeBanda() + "\" atualizada com sucesso!");
            return "redirect:/admin/indicacoes";
        } catch (IllegalArgumentException ex) {
            form.setId(id);
            indicacaoService.buscarPorId(id).ifPresent(ind -> {
                form.setStatusAtual(ind.getStatus());
                form.setHomonimo(ind.getHomonimo());
                form.setEdicaoHomonimoReferencia(ind.getEdicaoHomonimoReferencia());
                form.setNumeroEdicaoPublicada(ind.getNumeroEdicaoPublicada());
            });
            model.addAttribute("mensagemErro", ex.getMessage());
            model.addAttribute("indicacaoForm", form);
            return "admin/indicacoes/formulario";
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(
            @PathVariable UUID id,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String busca,
            RedirectAttributes redirectAttributes) {
        try {
            indicacaoService.excluir(id);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Indicação excluída permanentemente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, busca);
    }

    private String construirRedirectLista(String statusFiltro, String busca) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/indicacoes");
        if (statusFiltro != null && !statusFiltro.isBlank()) {
            builder.queryParam("status", indicacaoService.normalizarFiltroStatusAdmin(statusFiltro));
        }
        if (busca != null && !busca.isBlank()) {
            builder.queryParam("busca", busca.trim());
        }
        return "redirect:" + builder.build().toUriString();
    }
}
