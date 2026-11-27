package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EdicaoEntrevista;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.IndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.model.MoedaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoEntrevistaRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.repository.IndicacaoBandaRepository;

@SpringBootTest
class IndicacaoBandaRepositoryTests {

    private static final String TEST_DB = Path.of("target", "test-indicacao-repo.db").toAbsolutePath().toString();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEST_DB);
    }

    @Autowired
    private IndicacaoBandaRepository indicacaoRepository;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @Autowired
    private EdicaoEntrevistaRepository entrevistaRepository;

    @BeforeEach
    void cleanDb() {
        entrevistaRepository.deleteAll();
        indicacaoRepository.deleteAll();
        edicaoRepository.deleteAll();
    }

    @Test
    void deveValidarRegrasDoEnumStatusIndicacaoBanda() {
        assertThat(StatusIndicacaoBanda.PENDENTE.isPublico()).isFalse();
        assertThat(StatusIndicacaoBanda.RECUSADA.isPublico()).isFalse();
        assertThat(StatusIndicacaoBanda.PUBLICADA.isPublico()).isFalse();
        assertThat(StatusIndicacaoBanda.APROVADA.isPublico()).isTrue();
        assertThat(StatusIndicacaoBanda.EM_ENTREVISTA.isPublico()).isTrue();
        assertThat(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA.isPublico()).isTrue();

        assertThat(StatusIndicacaoBanda.PENDENTE.podeTransicionarPara(StatusIndicacaoBanda.APROVADA)).isTrue();
        assertThat(StatusIndicacaoBanda.PENDENTE.podeTransicionarPara(StatusIndicacaoBanda.RECUSADA)).isTrue();
        assertThat(StatusIndicacaoBanda.PENDENTE.podeTransicionarPara(StatusIndicacaoBanda.PUBLICADA)).isFalse();
        assertThat(StatusIndicacaoBanda.PUBLICADA.podeTransicionarPara(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA)).isTrue();
        assertThat(StatusIndicacaoBanda.PUBLICADA.podeTransicionarPara(StatusIndicacaoBanda.PENDENTE)).isFalse();
    }

    @Test
    void devePersistirBuscarPorStatusEChaveNormalizada() {
        Instant agora = Instant.now();
        IndicacaoBanda aprovada = criarIndicacao("Diabolizer", "diabolizer", "Turquia", "turquia",
                StatusIndicacaoBanda.APROVADA, null, agora.minusSeconds(20));
        IndicacaoBanda pendente = criarIndicacao("Coldwinter", "coldwinter", "Brasil", "brasil",
                StatusIndicacaoBanda.PENDENTE, null, agora.minusSeconds(10));
        IndicacaoBanda concluida = criarIndicacao("Mortem Solis", "mortem solis", "Brasil", "brasil",
                StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA, null, agora);

        indicacaoRepository.saveAll(List.of(aprovada, pendente, concluida));

        assertThat(indicacaoRepository.countByStatus(StatusIndicacaoBanda.PENDENTE)).isEqualTo(1L);
        assertThat(indicacaoRepository.findByStatusInOrderByCriadoEmDesc(
                List.of(StatusIndicacaoBanda.APROVADA, StatusIndicacaoBanda.EM_ENTREVISTA, StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA)))
                .extracting(IndicacaoBanda::getNomeBanda)
                .containsExactly("Mortem Solis", "Diabolizer");

        assertThat(indicacaoRepository.findByNomeBandaNormalizadoAndPaisNormalizadoAndStatusIn(
                "diabolizer", "turquia", List.of(StatusIndicacaoBanda.APROVADA)))
                .hasSize(1);
    }

    @Test
    void deveAplicarOnDeleteSetNullQuandoEdicaoPublicadaForExcluidaEConsultarEntrevistasPublicadas() {
        Instant agora = Instant.now();
        Edicao edicao = new Edicao(
                UUID.randomUUID(),
                58,
                IdiomaEdicao.PT,
                "Armamento Bélico #58",
                "Subtítulo",
                "Agosto",
                2026,
                "Ano 18",
                68,
                new BigDecimal("30.00"),
                MoedaEdicao.BRL,
                StatusEstoque.AVAILABLE,
                10,
                null,
                "capa-58.webp",
                "Descrição",
                "A5",
                "Offset",
                "300",
                null,
                true,
                agora,
                agora,
                true);
        edicaoRepository.save(edicao);

        IndicacaoBanda publicada = criarIndicacao("Diabolizer", "diabolizer", "Turquia", "turquia",
                StatusIndicacaoBanda.PUBLICADA, edicao.getId(), agora);
        publicada.setNumeroEdicaoPublicada(58);
        indicacaoRepository.save(publicada);

        EdicaoEntrevista entrevista = new EdicaoEntrevista(
                UUID.randomUUID(),
                edicao.getId(),
                "Diabolizer",
                "Turquia",
                "Death Metal",
                1,
                publicada.getId(),
                true);
        entrevistaRepository.save(entrevista);

        List<EdicaoEntrevistaRepository.EntrevistaPublicadaRow> rows = entrevistaRepository.findEntrevistasDeEdicoesPublicadas();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).numeroEdicao()).isEqualTo(58);
        assertThat(rows.get(0).banda()).isEqualTo("Diabolizer");

        edicaoRepository.deleteById(edicao.getId());

        IndicacaoBanda aposDelete = indicacaoRepository.findById(publicada.getId()).orElseThrow();
        assertThat(aposDelete.getEdicaoPublicadaId()).isNull();
    }

    private IndicacaoBanda criarIndicacao(
            String nome,
            String nomeNorm,
            String pais,
            String paisNorm,
            StatusIndicacaoBanda status,
            UUID edicaoPublicadaId,
            Instant criadoEm) {
        return new IndicacaoBanda(
                UUID.randomUUID(),
                nome,
                nomeNorm,
                pais,
                paisNorm,
                "SP",
                "Album (CD)",
                "https://bandcamp.com/test",
                status,
                false,
                null,
                null,
                null,
                edicaoPublicadaId,
                null,
                null,
                null,
                criadoEm,
                criadoEm,
                true);
    }
}
