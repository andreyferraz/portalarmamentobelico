package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EdicaoConteudoExtra;
import com.armamentobelico.portalarmamentobelico.model.EdicaoEntrevista;
import com.armamentobelico.portalarmamentobelico.model.EdicaoFoto;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.MoedaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoConteudoExtraRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoEntrevistaRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoFotoRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;

@SpringBootTest
class EdicaoRepositoryTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("repo-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private EdicaoRepository edicaoRepository;

    @Autowired
    private EdicaoFotoRepository fotoRepository;

    @Autowired
    private EdicaoEntrevistaRepository entrevistaRepository;

    @Autowired
    private EdicaoConteudoExtraRepository conteudoExtraRepository;

    @BeforeEach
    void limparBanco() {
        edicaoRepository.deleteAll();
    }

    private Edicao novaEdicao(int numero, IdiomaEdicao idioma, boolean publicado) {
        Instant agora = Instant.now();
        Edicao edicao = new Edicao();
        edicao.setId(UUID.randomUUID());
        edicao.setNumeroEdicao(numero);
        edicao.setIdioma(idioma);
        edicao.setMoeda(idioma.getMoeda());
        edicao.setTitulo("Armamento Bélico #" + numero + (idioma == IdiomaEdicao.EN ? " [ENG]" : ""));
        edicao.setSubtitulo("Subtítulo #" + numero);
        edicao.setMesLancamento(idioma == IdiomaEdicao.PT ? "Agosto" : "August");
        edicao.setAnoLancamento(2026);
        edicao.setAnoRevista(idioma == IdiomaEdicao.PT ? "Ano 18" : "Year 18");
        edicao.setPaginas(68);
        edicao.setPreco(idioma == IdiomaEdicao.PT ? new BigDecimal("30.00") : new BigDecimal("12.00"));
        edicao.setStatusEstoque(StatusEstoque.AVAILABLE);
        edicao.setQuantidadeEstoque(50);
        edicao.setCapaPath("capa-" + numero + "-" + idioma.getSlug() + ".webp");
        edicao.setDescricao("Descrição da edição #" + numero);
        edicao.setFormato("A5");
        edicao.setPapelImpressao("Offset 90g");
        edicao.setTiragem("300 cópias");
        edicao.setPublicado(publicado);
        edicao.setCriadoEm(agora);
        edicao.setAtualizadoEm(agora);
        edicao.setNew(true);
        return edicao;
    }

    @Test
    void devePermitirMesmoNumeroEmPortuguesEInglesMasBloquearDuplicataNoMesmoIdioma() {
        Edicao edicaoPt = edicaoRepository.save(novaEdicao(58, IdiomaEdicao.PT, true));
        Edicao edicaoEn = edicaoRepository.save(novaEdicao(58, IdiomaEdicao.EN, true));

        assertThat(edicaoRepository.findByNumeroEdicaoAndIdioma(58, IdiomaEdicao.PT)).isPresent();
        assertThat(edicaoRepository.findByNumeroEdicaoAndIdioma(58, IdiomaEdicao.EN)).isPresent();
        assertThat(edicaoRepository.existsByNumeroEdicaoAndIdioma(58, IdiomaEdicao.PT)).isTrue();
        assertThat(edicaoRepository.existsByNumeroEdicaoAndIdiomaAndIdNot(58, IdiomaEdicao.PT, edicaoPt.getId())).isFalse();
        assertThat(edicaoRepository.existsByNumeroEdicaoAndIdiomaAndIdNot(58, IdiomaEdicao.PT, edicaoEn.getId())).isTrue();

        Edicao duplicadaPt = novaEdicao(58, IdiomaEdicao.PT, true);
        assertThatThrownBy(() -> edicaoRepository.save(duplicadaPt))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("UNIQUE constraint failed");
    }

    @Test
    void deveConsultarAnteriorEProximaPublicadasFiltradasPorIdioma() {
        edicaoRepository.save(novaEdicao(55, IdiomaEdicao.PT, true));
        edicaoRepository.save(novaEdicao(56, IdiomaEdicao.PT, false)); // rascunho ignorado
        edicaoRepository.save(novaEdicao(57, IdiomaEdicao.PT, true));
        edicaoRepository.save(novaEdicao(58, IdiomaEdicao.PT, true));
        edicaoRepository.save(novaEdicao(56, IdiomaEdicao.EN, true));
        edicaoRepository.save(novaEdicao(58, IdiomaEdicao.EN, true));

        assertThat(edicaoRepository.findEdicaoAnteriorPublicada("PT", 57))
                .isPresent()
                .get()
                .extracting(Edicao::getNumeroEdicao)
                .isEqualTo(55);

        assertThat(edicaoRepository.findProximaEdicaoPublicada("PT", 57))
                .isPresent()
                .get()
                .extracting(Edicao::getNumeroEdicao)
                .isEqualTo(58);

        assertThat(edicaoRepository.findEdicaoAnteriorPublicada("EN", 58))
                .isPresent()
                .get()
                .extracting(Edicao::getNumeroEdicao)
                .isEqualTo(56);

        assertThat(edicaoRepository.findProximaEdicaoPublicada("EN", 58)).isEmpty();
    }

    @Test
    void deveRemoverFilhosEmCascataQuandoEdicaoForExcluida() {
        Edicao edicao = edicaoRepository.save(novaEdicao(58, IdiomaEdicao.PT, true));

        fotoRepository.save(new EdicaoFoto(UUID.randomUUID(), edicao.getId(), "extra1.webp", "Foto 1", 1, true));
        entrevistaRepository.save(new EdicaoEntrevista(UUID.randomUUID(), edicao.getId(), "Diabolizer", "Turquia", "Death Metal", 1, null, true));
        conteudoExtraRepository.save(new EdicaoConteudoExtra(UUID.randomUUID(), edicao.getId(), "70 resenhas", 1, true));

        assertThat(fotoRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId())).hasSize(1);
        assertThat(entrevistaRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId())).hasSize(1);
        assertThat(conteudoExtraRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId())).hasSize(1);

        edicaoRepository.deleteById(edicao.getId());

        assertThat(fotoRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId())).isEmpty();
        assertThat(entrevistaRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId())).isEmpty();
        assertThat(conteudoExtraRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId())).isEmpty();
    }

    @Test
    void deveFormatarMoedasCorretamenteParaRealEEuro() {
        assertThat(MoedaEdicao.BRL.formatar(new BigDecimal("30"), IdiomaEdicao.PT)).isEqualTo("R$ 30,00");
        assertThat(MoedaEdicao.EUR.formatar(new BigDecimal("12"), IdiomaEdicao.PT)).isEqualTo("€ 12,00");
        assertThat(MoedaEdicao.EUR.formatar(new BigDecimal("12"), IdiomaEdicao.EN)).isEqualTo("€ 12.00");
    }
}
