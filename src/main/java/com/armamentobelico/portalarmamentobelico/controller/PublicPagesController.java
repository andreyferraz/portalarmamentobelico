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

}
