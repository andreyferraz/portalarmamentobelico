package com.armamentobelico.portalarmamentobelico.dto;

import java.util.List;
import java.util.Optional;

import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EdicaoConteudoExtra;
import com.armamentobelico.portalarmamentobelico.model.EdicaoEntrevista;
import com.armamentobelico.portalarmamentobelico.model.EdicaoFoto;

public record EdicaoDetalhesDTO(
        Edicao edicao,
        List<EdicaoFoto> fotos,
        List<EdicaoEntrevista> entrevistas,
        List<EdicaoConteudoExtra> conteudosExtras,
        Optional<Edicao> edicaoAnterior,
        Optional<Edicao> proximaEdicao,
        String versaoAlternativaUrl,
        String youtubeVideoId) {
}
