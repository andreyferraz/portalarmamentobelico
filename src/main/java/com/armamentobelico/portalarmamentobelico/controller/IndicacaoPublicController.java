package com.armamentobelico.portalarmamentobelico.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.armamentobelico.portalarmamentobelico.dto.IndicacaoPublicCardDTO;
import com.armamentobelico.portalarmamentobelico.dto.IndicacaoPublicFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.VerificacaoBandaResponseDTO;
import com.armamentobelico.portalarmamentobelico.exceptions.ConflitoIndicacaoBandaException;
import com.armamentobelico.portalarmamentobelico.model.IndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.service.IndicacaoBandaService;

@Controller
public class IndicacaoPublicController {

    private final IndicacaoBandaService indicacaoService;

    public IndicacaoPublicController(IndicacaoBandaService indicacaoService) {
        this.indicacaoService = indicacaoService;
    }

    @GetMapping("/indique")
    public String paginaIndique(Model model) {
        popularModeloPagina(model);
        if (!model.containsAttribute("indicacaoForm")) {
            model.addAttribute("indicacaoForm", new IndicacaoPublicFormDTO());
        }
        return "indique";
    }

    @PostMapping("/indique")
    public String submeterIndicacao(
            @ModelAttribute("indicacaoForm") IndicacaoPublicFormDTO form,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        // Honeypot anti-bot: descarta silenciosamente sem tocar no banco
        if (form.getWebsite() != null && !form.getWebsite().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "indicacaoSucesso",
                    "☠ Indicação recebida! Ela passará pela avaliação da curadoria antes de aparecer na lista pública.");
            return "redirect:/indique?enviado=true#indicar-banda";
        }

        try {
            indicacaoService.verificarERegistrarLimiteIp(extrairIpCliente(request));
            IndicacaoBanda salva = indicacaoService.cadastrarPublico(form);
            redirectAttributes.addFlashAttribute(
                    "indicacaoSucesso",
                    "☠ Indicação recebida! A horda \"" + salva.getNomeBanda() + "\" (" + salva.getPais()
                            + ") foi registrada e passará pela avaliação da curadoria antes de aparecer na lista pública.");
            return "redirect:/indique?enviado=true#indicar-banda";
        } catch (ConflitoIndicacaoBandaException ex) {
            popularModeloPagina(model);
            model.addAttribute("indicacaoForm", form);
            model.addAttribute("verificacaoConflito", ex.getVerificacao());
            model.addAttribute("indicacaoErro", ex.getMessage());
            return "indique";
        } catch (IllegalArgumentException ex) {
            popularModeloPagina(model);
            model.addAttribute("indicacaoForm", form);
            model.addAttribute("indicacaoErro", ex.getMessage());
            return "indique";
        }
    }

    @GetMapping("/api/indicacoes/verificar")
    @ResponseBody
    public ResponseEntity<VerificacaoBandaResponseDTO> verificarBanda(
            @RequestParam(defaultValue = "") String nome,
            @RequestParam(defaultValue = "") String pais) {
        return ResponseEntity.ok(indicacaoService.verificarBanda(nome, pais));
    }

    private void popularModeloPagina(Model model) {
        List<IndicacaoPublicCardDTO> indicacoesPublicas = indicacaoService.listarPublicas();
        model.addAttribute("bodyClass", "page-indique");
        model.addAttribute("indicacoesPublicas", indicacoesPublicas);
        model.addAttribute("totalIndicacoesPublicas", indicacoesPublicas.size());
    }

    private static String extrairIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
