package com.armamentobelico.portalarmamentobelico.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.armamentobelico.portalarmamentobelico.dto.EventoPublicCardDTO;
import com.armamentobelico.portalarmamentobelico.dto.EventoPublicFormDTO;
import com.armamentobelico.portalarmamentobelico.exceptions.FileUploadException;
import com.armamentobelico.portalarmamentobelico.model.EstadoBrasil;
import com.armamentobelico.portalarmamentobelico.model.Evento;
import com.armamentobelico.portalarmamentobelico.service.EventoService;

@Controller
public class EventoPublicController {

    private final EventoService eventoService;

    public EventoPublicController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/eventos")
    public String listarEventos(
            @RequestParam(defaultValue = "all") String estado,
            @RequestParam(defaultValue = "all") String status,
            Model model) {
        String estadoSelecionado = eventoService.normalizarFiltroEstadoPublico(estado);
        String statusSelecionado = eventoService.normalizarFiltroTemporalPublico(status);
        List<EventoPublicCardDTO> eventos = eventoService.listarAprovadosPublico(estadoSelecionado, statusSelecionado);

        model.addAttribute("bodyClass", "page-eventos");
        model.addAttribute("eventos", eventos);
        model.addAttribute("estados", EstadoBrasil.values());
        model.addAttribute("estadoSelecionado", estadoSelecionado);
        model.addAttribute("statusSelecionado", statusSelecionado);
        model.addAttribute("totalEventos", eventos.size());
        model.addAttribute("hojeIso", eventoService.hojePortal().toString());
        if (!model.containsAttribute("eventoForm")) {
            model.addAttribute("eventoForm", new EventoPublicFormDTO());
        }
        return "eventos";
    }

    @PostMapping("/eventos")
    public String submeterEvento(
            @ModelAttribute("eventoForm") EventoPublicFormDTO form,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        // Honeypot anti-bot: descarta silenciosamente sem tocar em disco ou banco
        if (form.getWebsite() != null && !form.getWebsite().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "eventoSucesso",
                    "☠ Evento enviado com sucesso! Ele está aguardando análise e aprovação manual do editor antes de ser publicado na agenda.");
            return "redirect:/eventos#cadastrar-evento";
        }

        try {
            eventoService.verificarERegistrarLimiteIp(extrairIpCliente(request));
            Evento salvo = eventoService.submeterEventoPublico(form);
            redirectAttributes.addFlashAttribute(
                    "eventoSucesso",
                    "☠ Evento \"" + salvo.getTitulo() + "\" (" + salvo.getCidade() + "/" + salvo.getEstado().name()
                            + ") enviado com sucesso! Ele está aguardando análise e aprovação manual do editor antes de ser publicado na agenda.");
        } catch (IllegalArgumentException | FileUploadException ex) {
            form.setFlyer(null);
            redirectAttributes.addFlashAttribute("eventoErro", ex.getMessage());
            redirectAttributes.addFlashAttribute("eventoForm", form);
        }
        return "redirect:/eventos#cadastrar-evento";
    }

    private static String extrairIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
