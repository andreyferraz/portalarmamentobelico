package com.armamentobelico.portalarmamentobelico.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.armamentobelico.portalarmamentobelico.dto.EdicaoDetalhesDTO;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EdicaoFoto;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.service.EdicaoService;

@Controller
@RequestMapping("/edicoes")
public class EdicaoPublicController {

    private final EdicaoService edicaoService;

    public EdicaoPublicController(EdicaoService edicaoService) {
        this.edicaoService = edicaoService;
    }

    @GetMapping("/{idioma}/{numero}")
    public String detalhes(
            @PathVariable String idioma,
            @PathVariable Integer numero,
            Model model) {
        IdiomaEdicao idiomaEdicao = IdiomaEdicao.fromSlug(idioma)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idioma de edição inválido."));

        EdicaoDetalhesDTO detalhes = edicaoService.buscarDetalhesPublicos(idiomaEdicao, numero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Edição não encontrada."));

        Edicao edicao = detalhes.edicao();
        boolean isPt = edicao.getIdioma() == IdiomaEdicao.PT;

        model.addAttribute("bodyClass", "page-edicao");
        model.addAttribute("pageTitle", edicao.getTitulo().toUpperCase() + " | Armamento Bélico Zine");
        model.addAttribute("detalhes", detalhes);
        model.addAttribute("edicao", edicao);
        model.addAttribute("isPt", isPt);
        model.addAttribute("fotos", detalhes.fotos());
        model.addAttribute("entrevistas", detalhes.entrevistas());
        model.addAttribute("conteudosExtras", detalhes.conteudosExtras());
        model.addAttribute("edicaoAnterior", detalhes.edicaoAnterior().orElse(null));
        model.addAttribute("proximaEdicao", detalhes.proximaEdicao().orElse(null));
        model.addAttribute("versaoAlternativaUrl", detalhes.versaoAlternativaUrl());
        model.addAttribute("youtubeVideoId", detalhes.youtubeVideoId());
        model.addAttribute("editionPageDataJson", serializarPageDataJson(detalhes));
        return "edicao";
    }

    @GetMapping("/{numero}")
    public String redirecionarPadrao(@PathVariable Integer numero) {
        String url = edicaoService.resolverUrlPadraoPorNumero(numero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Edição não encontrada."));
        return "redirect:" + url;
    }

    private String serializarPageDataJson(EdicaoDetalhesDTO detalhes) {
        Edicao e = detalhes.edicao();
        List<EdicaoFoto> fotos = detalhes.fotos();
        StringBuilder sb = new StringBuilder(512);
        sb.append('{');
        sb.append("\"id\":").append(jsonString(e.getId().toString())).append(',');
        sb.append("\"editionNumber\":").append(e.getNumeroEdicao()).append(',');
        sb.append("\"editionLang\":").append(jsonString(e.getIdioma().getSlug())).append(',');
        sb.append("\"alternateLangUrl\":").append(jsonStringOrNull(detalhes.versaoAlternativaUrl())).append(',');
        sb.append("\"title\":").append(jsonString(e.getTitulo())).append(',');
        sb.append("\"price\":").append(e.getPreco() != null ? e.getPreco().toPlainString() : "0").append(',');
        sb.append("\"currency\":").append(jsonString(e.getMoeda().getSimbolo())).append(',');
        sb.append("\"inStock\":").append(e.getStatusEstoque().isInStock()).append(',');
        sb.append("\"stockStatus\":").append(jsonString(e.getStatusEstoque().name().toLowerCase())).append(',');
        sb.append("\"cover\":").append(jsonString("/uploads/" + e.getCapaPath())).append(',');
        sb.append("\"extraPhotos\":[");
        for (int i = 0; i < fotos.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            EdicaoFoto f = fotos.get(i);
            sb.append('{')
                    .append("\"src\":").append(jsonString("/uploads/" + f.getArquivoPath())).append(',')
                    .append("\"caption\":").append(jsonStringOrNull(f.getLegenda()))
                    .append('}');
        }
        sb.append("],");
        sb.append("\"youtubeUrl\":").append(jsonStringOrNull(e.getYoutubeUrl()));
        sb.append('}');
        return sb.toString();
    }

    private String jsonStringOrNull(String value) {
        return value == null ? "null" : jsonString(value);
    }

    private String jsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
