package com.armamentobelico.portalarmamentobelico.controller;

import java.security.Principal;
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

import com.armamentobelico.portalarmamentobelico.dto.EventoAdminFormDTO;
import com.armamentobelico.portalarmamentobelico.exceptions.FileUploadException;
import com.armamentobelico.portalarmamentobelico.model.EstadoBrasil;
import com.armamentobelico.portalarmamentobelico.model.Evento;
import com.armamentobelico.portalarmamentobelico.model.PapelPublicador;
import com.armamentobelico.portalarmamentobelico.model.StatusModeracaoEvento;
import com.armamentobelico.portalarmamentobelico.service.EventoService;

@Controller
@RequestMapping("/admin/eventos")
public class AdminEventoController {

    private final EventoService eventoService;

    public AdminEventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping({ "", "/" })
    public String listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String busca,
            Model model) {
        EventoService.ResumoAdminEventos resumo = eventoService.contarResumoAdmin();
        String statusEfetivo = (status == null || status.isBlank())
                ? (resumo.pendentes() > 0 ? StatusModeracaoEvento.PENDENTE.name() : "TODOS")
                : eventoService.normalizarFiltroStatusAdmin(status);
        String estadoEfetivo = eventoService.normalizarFiltroEstadoPublico(estado);
        String buscaLimpa = busca != null ? busca.trim() : "";

        List<Evento> eventos = eventoService.listarParaAdmin(statusEfetivo, estadoEfetivo, buscaLimpa);

        model.addAttribute("eventos", eventos);
        model.addAttribute("resumoEventos", resumo);
        model.addAttribute("estados", EstadoBrasil.values());
        model.addAttribute("hojePortal", eventoService.hojePortal());
        model.addAttribute("statusFiltro", statusEfetivo);
        model.addAttribute("estadoFiltro", "all".equals(estadoEfetivo) ? "" : estadoEfetivo);
        model.addAttribute("busca", buscaLimpa);
        return "admin/eventos/lista";
    }

    @GetMapping({ "/novo", "/nova" })
    public String novo(Model model) {
        EventoAdminFormDTO form = new EventoAdminFormDTO();
        form.setPapelPublicador(PapelPublicador.ORGANIZADOR);
        form.setStatusModeracao(StatusModeracaoEvento.APROVADO);
        popularModeloFormulario(model, form, false);
        return "admin/eventos/form";
    }

    @PostMapping
    public String criar(
            @ModelAttribute("eventoForm") EventoAdminFormDTO form,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Evento criado = eventoService.criarEventoAdmin(form, resolverAdmin(principal));
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Evento \"" + criado.getTitulo() + "\" cadastrado com sucesso!");
            return "redirect:/admin/eventos";
        } catch (IllegalArgumentException | FileUploadException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            popularModeloFormulario(model, form, false);
            return "admin/eventos/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable UUID id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            EventoAdminFormDTO form = eventoService.carregarParaEdicao(id);
            popularModeloFormulario(model, form, true);
            return "admin/eventos/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/admin/eventos";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable UUID id,
            @ModelAttribute("eventoForm") EventoAdminFormDTO form,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Evento atualizado = eventoService.atualizarEventoAdmin(id, form, resolverAdmin(principal));
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Evento \"" + atualizado.getTitulo() + "\" atualizado com sucesso!");
            return "redirect:/admin/eventos";
        } catch (IllegalArgumentException | FileUploadException ex) {
            form.setId(id);
            eventoService.buscarPorId(id).ifPresent(ev -> form.setFlyerPathAtual(ev.getFlyerPath()));
            model.addAttribute("mensagemErro", ex.getMessage());
            popularModeloFormulario(model, form, true);
            return "admin/eventos/form";
        }
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(
            @PathVariable UUID id,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String estadoFiltro,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            Evento aprovado = eventoService.aprovar(id, resolverAdmin(principal));
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Evento \"" + aprovado.getTitulo() + "\" aprovado e publicado na agenda!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, estadoFiltro);
    }

    @PostMapping("/{id}/recusar")
    public String recusar(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivoRecusa,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String estadoFiltro,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            Evento recusado = eventoService.recusar(id, motivoRecusa, resolverAdmin(principal));
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Evento \"" + recusado.getTitulo() + "\" marcado como recusado.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, estadoFiltro);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(
            @PathVariable UUID id,
            @RequestParam(required = false) String statusFiltro,
            @RequestParam(required = false) String estadoFiltro,
            RedirectAttributes redirectAttributes) {
        try {
            eventoService.excluir(id);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Evento e flyer excluídos definitivamente.");
        } catch (IllegalArgumentException | FileUploadException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return construirRedirectLista(statusFiltro, estadoFiltro);
    }

    private void popularModeloFormulario(Model model, EventoAdminFormDTO form, boolean modoEdicao) {
        model.addAttribute("eventoForm", form);
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("estados", EstadoBrasil.values());
        model.addAttribute("papeisPublicador", PapelPublicador.values());
        model.addAttribute("statusModeracaoLista", StatusModeracaoEvento.values());
    }

    private String construirRedirectLista(String statusFiltro, String estadoFiltro) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/eventos");
        if (statusFiltro != null && !statusFiltro.isBlank()) {
            builder.queryParam("status", eventoService.normalizarFiltroStatusAdmin(statusFiltro));
        }
        String estadoNorm = eventoService.normalizarFiltroEstadoPublico(estadoFiltro);
        if (!"all".equals(estadoNorm)) {
            builder.queryParam("estado", estadoNorm);
        }
        return "redirect:" + builder.build().toUriString();
    }

    private static String resolverAdmin(Principal principal) {
        return principal != null ? principal.getName() : "admin";
    }
}
