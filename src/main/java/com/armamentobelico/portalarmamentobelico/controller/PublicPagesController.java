package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicPagesController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/historia")
    public String historia() {
        return "historia";
    }

    @GetMapping("/arquivo")
    public String arquivo() {
        return "arquivo";
    }

    @GetMapping("/noticias")
    public String noticias() {
        return "noticias";
    }

    @GetMapping("/eventos")
    public String eventos() {
        return "eventos";   
    }

    @GetMapping("/indique")
    public String indique() {
        return "indique";
    }

    @GetMapping("/aliados")
    public String aliados() {
        return "aliados";
    }

    @GetMapping("/assinatura")
    public String assinatura() {
        return "assinatura";
    }

    @GetMapping("/privacidade")
    public String privacidade() {
        return "privacidade";
    }

}
