package com.armamentobelico.portalarmamentobelico.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.armamentobelico.portalarmamentobelico.config.LojaProperties;
import com.armamentobelico.portalarmamentobelico.utils.PublicJsonUtils;

@ControllerAdvice(assignableTypes = {
        PublicPagesController.class,
        EdicaoPublicController.class,
        NoticiaPublicController.class
})
public class PublicStoreModelAdvice {

    private final LojaProperties lojaProperties;

    public PublicStoreModelAdvice(LojaProperties lojaProperties) {
        this.lojaProperties = lojaProperties;
    }

    @ModelAttribute("lojaConfig")
    public LojaProperties lojaConfig() {
        return lojaProperties;
    }

    @ModelAttribute("lojaConfigJson")
    public String lojaConfigJson() {
        return PublicJsonUtils.serializarLojaConfig(lojaProperties);
    }
}
