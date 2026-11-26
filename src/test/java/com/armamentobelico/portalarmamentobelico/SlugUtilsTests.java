package com.armamentobelico.portalarmamentobelico;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.armamentobelico.portalarmamentobelico.utils.SlugUtils;

class SlugUtilsTests {

    @Test
    void deveGerarSlugNormalizandoAcentosPontuacaoEEspacos() {
        String slug = SlugUtils.gerarSlug("Diabolizer: A Fúria do Death Metal Turco na Edição #58!");
        assertEquals("diabolizer-a-furia-do-death-metal-turco-na-edicao-58", slug);
    }

    @Test
    void deveRemoverHifensDuplicadosENasExtremidades() {
        String slug = SlugUtils.gerarSlug("  --- Coldwinter (Brasil) / Fita K7 ---  ");
        assertEquals("coldwinter-brasil-fita-k7", slug);
    }

    @Test
    void deveRetornarVazioParaEntradaNulaOuSemCaracteresAlfanumericos() {
        assertEquals("", SlugUtils.gerarSlug(null));
        assertEquals("", SlugUtils.gerarSlug("   "));
        assertEquals("", SlugUtils.gerarSlug("☠✝!!!"));
    }
}
