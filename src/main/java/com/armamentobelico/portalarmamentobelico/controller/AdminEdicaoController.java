package com.armamentobelico.portalarmamentobelico.controller;

import java.util.ArrayList;
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

import com.armamentobelico.portalarmamentobelico.dto.EdicaoFormDTO;
import com.armamentobelico.portalarmamentobelico.exceptions.FileUploadException;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.IndicacaoBandaService;

@Controller
@RequestMapping("/admin/edicoes")
public class AdminEdicaoController {

    private final EdicaoService edicaoService;
    private final IndicacaoBandaService indicacaoBandaService;

    public AdminEdicaoController(EdicaoService edicaoService, IndicacaoBandaService indicacaoBandaService) {
        this.edicaoService = edicaoService;
        this.indicacaoBandaService = indicacaoBandaService;
    }

    @GetMapping({ "", "/" })
    public String listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) IdiomaEdicao idioma,
            @RequestParam(required = false) StatusEstoque statusEstoque,
            Model model) {
        EdicaoService.ResumoAdminEdicoes resumo = edicaoService.contarResumoAdmin();
        List<Edicao> edicoes = edicaoService.listarAdmin(busca, idioma, statusEstoque);

        model.addAttribute("edicoes", edicoes);
        model.addAttribute("idiomasPorNumero", edicaoService.buscarIdiomasPorNumero());
        model.addAttribute("resumoEdicoes", resumo);
        model.addAttribute("totalEdicoes", resumo.total());
        model.addAttribute("busca", busca != null ? busca : "");
        model.addAttribute("idiomaFiltro", idioma);
        model.addAttribute("statusFiltro", statusEstoque);
        model.addAttribute("idiomas", IdiomaEdicao.values());
        model.addAttribute("statusOptions", StatusEstoque.values());
        return "admin/edicoes/lista";
    }

    @GetMapping({ "/nova", "/novo" })
    public String nova(
            @RequestParam(required = false) UUID origemId,
            Model model,
            RedirectAttributes redirectAttributes) {
        EdicaoFormDTO form;
        if (origemId != null) {
            try {
                form = edicaoService.prepararFormularioVersaoComplementar(origemId);
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
                return "redirect:/admin/edicoes";
            }
        } else {
            form = new EdicaoFormDTO();
            form.setIdioma(IdiomaEdicao.PT);
            form.setStatusEstoque(StatusEstoque.AVAILABLE);
            form.setPublicado(Boolean.TRUE);
        }
        garantirLinhasIniciais(form);
        popularModeloFormulario(model, form, false);
        return "admin/edicoes/form";
    }

    @PostMapping
    public String criar(
            @ModelAttribute("edicaoForm") EdicaoFormDTO form,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Edicao criada = edicaoService.criar(form);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Edição #" + criada.getNumeroEdicao() + " (" + criada.getIdioma().getLabelAdmin()
                            + ") cadastrada com sucesso!");
            return "redirect:/admin/edicoes";
        } catch (IllegalArgumentException | FileUploadException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            garantirLinhasIniciais(form);
            popularModeloFormulario(model, form, false);
            return "admin/edicoes/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable UUID id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            EdicaoFormDTO form = edicaoService.carregarParaEdicao(id);
            garantirLinhasIniciais(form);
            popularModeloFormulario(model, form, true);
            return "admin/edicoes/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/admin/edicoes";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable UUID id,
            @ModelAttribute("edicaoForm") EdicaoFormDTO form,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Edicao atualizada = edicaoService.atualizar(id, form);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Edição #" + atualizada.getNumeroEdicao() + " (" + atualizada.getIdioma().getLabelAdmin()
                            + ") atualizada com sucesso!");
            return "redirect:/admin/edicoes";
        } catch (IllegalArgumentException | FileUploadException ex) {
            form.setId(id);
            edicaoService.buscarPorId(id).ifPresent(e -> form.setCapaAtualPath(e.getCapaPath()));
            model.addAttribute("mensagemErro", ex.getMessage());
            garantirLinhasIniciais(form);
            popularModeloFormulario(model, form, true);
            return "admin/edicoes/form";
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            edicaoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Edição excluída com sucesso.");
        } catch (IllegalArgumentException | FileUploadException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/admin/edicoes";
    }

    private void popularModeloFormulario(Model model, EdicaoFormDTO form, boolean modoEdicao) {
        EdicaoService.ResumoAdminEdicoes resumo = edicaoService.contarResumoAdmin();
        model.addAttribute("edicaoForm", form);
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("idiomas", IdiomaEdicao.values());
        model.addAttribute("statusOptions", StatusEstoque.values());
        model.addAttribute("maxFotosExtras", EdicaoService.MAX_FOTOS_EXTRAS);
        model.addAttribute("totalEdicoes", resumo.total());
        model.addAttribute("indicacoesElegiveis", indicacaoBandaService.listarElegiveisParaEdicao());
    }

    private void garantirLinhasIniciais(EdicaoFormDTO form) {
        if (form.getEntrevistas() == null) {
            form.setEntrevistas(new ArrayList<>());
        }
        if (form.getEntrevistas().isEmpty()) {
            form.getEntrevistas().add(new EdicaoFormDTO.EntrevistaItem());
        }
        if (form.getConteudosExtras() == null) {
            form.setConteudosExtras(new ArrayList<>());
        }
        if (form.getConteudosExtras().isEmpty()) {
            form.getConteudosExtras().add("");
        }
    }
}
