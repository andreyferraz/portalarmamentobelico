package com.armamentobelico.portalarmamentobelico;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.armamentobelico.portalarmamentobelico.service.HtmlSanitizerService;

class HtmlSanitizerServiceTests {

    private final HtmlSanitizerService sanitizer = new HtmlSanitizerService();

    @Test
    void deveBloquearScriptsIframesEventHandlersEJavascriptUrls() {
        String malicioso = """
                <p>Texto legítimo</p>
                <script>alert('xss')</script>
                <img src="x" onerror="alert(1)">
                <iframe src="https://evil.example/embed"></iframe>
                <a href="javascript:alert(2)" onclick="evil()">Clique</a>
                <p class="interview-question evil-class" onmouseover="hack()">Pergunta</p>
                """;

        String limpo = sanitizer.sanitizar(malicioso);

        assertTrue(limpo.contains("<p>Texto legítimo</p>"));
        assertFalse(limpo.contains("<script"));
        assertFalse(limpo.contains("alert("));
        assertFalse(limpo.contains("<img"));
        assertFalse(limpo.contains("onerror"));
        assertFalse(limpo.contains("<iframe"));
        assertFalse(limpo.contains("javascript:"));
        assertFalse(limpo.contains("onclick"));
        assertFalse(limpo.contains("onmouseover"));
        assertFalse(limpo.contains("evil-class"));
        assertTrue(limpo.contains("class=\"interview-question\""));
    }

    @Test
    void devePreservarMarcacaoEditorialClassesDeEntrevistaELinksSeguros() {
        String editorial = """
                <h2>Subtítulo</h2>
                <p>Parágrafo com <strong>negrito</strong>, <em>itálico</em> e <u>sublinhado</u>.</p>
                <blockquote>"Death Metal é guerra espiritual."</blockquote>
                <ul><li>Item 1</li><li>Item 2</li></ul>
                <p class="interview-question">ARMAMENTO BÉLICO: Como surgiu a horda?</p>
                <p class="interview-answer">DIABOLIZER: Das vielas de Istambul.</p>
                <span class="interview-question">Pergunta inline</span>
                <a href="https://armamento.example/banda">Ouça aqui</a>
                """;

        String limpo = sanitizer.sanitizar(editorial);

        assertTrue(limpo.contains("<h2>Subtítulo</h2>"));
        assertTrue(limpo.contains("<strong>negrito</strong>"));
        assertTrue(limpo.contains("<em>itálico</em>"));
        assertTrue(limpo.contains("<u>sublinhado</u>"));
        assertTrue(limpo.contains("<blockquote>"));
        assertTrue(limpo.contains("<ul>"));
        assertTrue(limpo.contains("<p class=\"interview-question\">"));
        assertTrue(limpo.contains("<p class=\"interview-answer\">"));
        assertTrue(limpo.contains("<span class=\"interview-question\">"));
        assertTrue(limpo.contains("href=\"https://armamento.example/banda\""));
        assertTrue(limpo.contains("rel=\"noopener noreferrer\""));
        assertTrue(limpo.contains("target=\"_blank\""));
    }

    @Test
    void deveDetectarConteudoSemTextoVisivelAposSanitizacao() {
        assertFalse(sanitizer.temTextoVisivel(null));
        assertFalse(sanitizer.temTextoVisivel("   "));
        assertFalse(sanitizer.temTextoVisivel("<p><br></p>"));
        assertFalse(sanitizer.temTextoVisivel("<script>alert(1)</script>"));
        assertTrue(sanitizer.temTextoVisivel("<p>Manifesto underground</p>"));
    }
}
