package com.armamentobelico.portalarmamentobelico.controller;

import java.time.LocalDate;
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

import com.armamentobelico.portalarmamentobelico.dto.NoticiaFormDTO;
import com.armamentobelico.portalarmamentobelico.exceptions.FileUploadException;
import com.armamentobelico.portalarmamentobelico.model.EstiloTag;
import com.armamentobelico.portalarmamentobelico.model.Noticia;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;

@Controller
@RequestMapping("/admin/noticias")
public class AdminNoticiaController {

    private final NoticiaService noticiaService;

    public AdminNoticiaController(NoticiaService noticiaService) {
        this.noticiaService = noticiaService;
    }

    @GetMapping({ "", "/" })
    public String listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String idioma,
            Model model) {
        NoticiaService.ResumoAdminNoticias resumo = noticiaService.contarResumoAdmin();
        List<Noticia> noticias = noticiaService.listarAdmin(busca, status, idioma);

        model.addAttribute("noticias", noticias);
        model.addAttribute("edicoesPorId", noticiaService.mapearEdicoesVinculadas(noticias));
        model.addAttribute("resumoNoticias", resumo);
        model.addAttribute("busca", busca != null ? busca : "");
        model.addAttribute("statusFiltro", status != null ? status : "");
        model.addAttribute("idiomaFiltro", idioma != null ? idioma : "");
        return "admin/noticias/lista";
    }

    @GetMapping({ "/nova", "/novo" })
    public String nova(Model model) {
        NoticiaFormDTO form = new NoticiaFormDTO();
        form.setAutor(NoticiaService.AUTOR_PADRAO);
        form.setDataPublicacao(LocalDate.now());
        form.setPublicado(Boolean.TRUE);
        garantirLinhasIniciais(form);
        popularModeloFormulario(model, form, false);
        return "admin/noticias/form";
    }

    @PostMapping
    public String criar(
            @ModelAttribute("noticiaForm") NoticiaFormDTO form,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Noticia criada = noticiaService.criar(form);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Notícia \"" + criada.getTituloPt() + "\" cadastrada com sucesso!");
            return "redirect:/admin/noticias";
        } catch (IllegalArgumentException | FileUploadException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            garantirLinhasIniciais(form);
            popularModeloFormulario(model, form, false);
            return "admin/noticias/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable UUID id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            NoticiaFormDTO form = noticiaService.carregarParaEdicao(id);
            garantirLinhasIniciais(form);
            popularModeloFormulario(model, form, true);
            return "admin/noticias/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/admin/noticias";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable UUID id,
            @ModelAttribute("noticiaForm") NoticiaFormDTO form,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Noticia atualizada = noticiaService.atualizar(id, form);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Notícia \"" + atualizada.getTituloPt() + "\" atualizada com sucesso!");
            return "redirect:/admin/noticias";
        } catch (IllegalArgumentException | FileUploadException ex) {
            form.setId(id);
            noticiaService.buscarPorId(id).ifPresent(n -> form.setImagemAtualPath(n.getImagemDestaquePath()));
            model.addAttribute("mensagemErro", ex.getMessage());
            garantirLinhasIniciais(form);
            popularModeloFormulario(model, form, true);
            return "admin/noticias/form";
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            noticiaService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Notícia excluída com sucesso.");
        } catch (IllegalArgumentException | FileUploadException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/admin/noticias";
    }

    private void popularModeloFormulario(Model model, NoticiaFormDTO form, boolean modoEdicao) {
        model.addAttribute("noticiaForm", form);
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("edicoesDisponiveis", noticiaService.listarEdicoesParaVinculo());
        model.addAttribute("estilosTag", EstiloTag.values());
        model.addAttribute("maxTags", NoticiaService.MAX_TAGS);
    }

    private void garantirLinhasIniciais(NoticiaFormDTO form) {
        if (form.getTags() == null) {
            form.setTags(new ArrayList<>());
        }
        if (form.getTags().isEmpty()) {
            form.getTags().add(new NoticiaFormDTO.TagFormItem("", EstiloTag.DARK));
        }
    }
}
