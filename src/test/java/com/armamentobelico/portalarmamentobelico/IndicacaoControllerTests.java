package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
import com.armamentobelico.portalarmamentobelico.service.IndicacaoBandaService;

@SpringBootTest
@AutoConfigureMockMvc
class IndicacaoControllerTests {

    private static final String TEST_DB = Path.of("target", "test-indicacao-controller.db").toAbsolutePath().toString();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEST_DB);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IndicacaoBandaRepository indicacaoRepository;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @Autowired
    private EdicaoEntrevistaRepository entrevistaRepository;

    @Autowired
    private IndicacaoBandaService indicacaoService;

    @BeforeEach
    void cleanDb() {
        entrevistaRepository.deleteAll();
        indicacaoRepository.deleteAll();
        edicaoRepository.deleteAll();
        indicacaoService.limparRateLimit();
    }

    @Test
    void paginaPublicaDeveExibirApenasStatusPublicosEmOrdemEditorialSemVazarCamposInternos() throws Exception {
        Instant agora = Instant.now();
        IndicacaoBanda pendente = criarIndicacao("Banda Pendente", "Brasil", StatusIndicacaoBanda.PENDENTE, agora);
        pendente.setEsclarecimentoHomonimo("ESCLARECIMENTO_SECRETO");

        IndicacaoBanda recusada = criarIndicacao("Banda Recusada", "Brasil", StatusIndicacaoBanda.RECUSADA, agora);
        recusada.setMotivoRecusa("MOTIVO_SECRETO");

        IndicacaoBanda publicada = criarIndicacao("Banda Publicada", "Turquia", StatusIndicacaoBanda.PUBLICADA, agora);
        IndicacaoBanda radar = criarIndicacao("Horda Radar", "Brasil", StatusIndicacaoBanda.APROVADA, agora);
        IndicacaoBanda entrevista = criarIndicacao("Horda Entrevista", "Chile", StatusIndicacaoBanda.EM_ENTREVISTA, agora);
        IndicacaoBanda concluida = criarIndicacao("Horda Concluida", "Polônia", StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA, agora);

        indicacaoRepository.saveAll(List.of(pendente, recusada, publicada, radar, entrevista, concluida));

        MvcResult result = mockMvc.perform(get("/indique"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Horda Concluida")))
                .andExpect(content().string(containsString("Horda Entrevista")))
                .andExpect(content().string(containsString("Horda Radar")))
                .andExpect(content().string(not(containsString("Banda Pendente"))))
                .andExpect(content().string(not(containsString("Banda Recusada"))))
                .andExpect(content().string(not(containsString("Banda Publicada"))))
                .andExpect(content().string(not(containsString("ESCLARECIMENTO_SECRETO"))))
                .andExpect(content().string(not(containsString("MOTIVO_SECRETO"))))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html.indexOf("Horda Concluida")).isLessThan(html.indexOf("Horda Entrevista"));
        assertThat(html.indexOf("Horda Entrevista")).isLessThan(html.indexOf("Horda Radar"));
    }

    @Test
    void apiVerificarESubmissaoPublicaDevemValidarDuplicidadeHomonimoEMassAssignment() throws Exception {
        Instant agora = Instant.now();
        Edicao ed58 = new Edicao(
                UUID.randomUUID(), 58, IdiomaEdicao.PT, "Armamento Bélico #58", null,
                "Agosto", 2026, "Ano 18", 68, new BigDecimal("30.00"), MoedaEdicao.BRL,
                StatusEstoque.AVAILABLE, 5, null, "capa.webp", "Desc", null, null, null, null,
                true, agora, agora, true);
        edicaoRepository.save(ed58);
        entrevistaRepository.save(new EdicaoEntrevista(
                UUID.randomUUID(), ed58.getId(), "Diabolizer", "Turquia", "Death Metal", 1, null, true));

        mockMvc.perform(get("/api/indicacoes/verificar").param("nome", "Diabolizer").param("pais", "Turkey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("JA_ENTREVISTADA"))
                .andExpect(jsonPath("$.edicoes[0].numeroEdicao").value(58))
                .andExpect(jsonPath("$.edicoes[0].url").value("/edicoes/58"));

        // Mass assignment: status=APROVADA enviado no POST deve ser ignorado e salvo como PENDENTE
        mockMvc.perform(post("/indique")
                        .with(csrf())
                        .param("bandName", "Coldwinter")
                        .param("country", "Brasil")
                        .param("region", "SC")
                        .param("releaseTitle", "The Winter Has Come (K7)")
                        .param("listenUrl", "https://coldwinter.bandcamp.com")
                        .param("status", "APROVADA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/indique?enviado=true#indicar-banda"));

        List<IndicacaoBanda> salvas = indicacaoRepository.findAll();
        assertThat(salvas).hasSize(1);
        assertThat(salvas.get(0).getStatus()).isEqualTo(StatusIndicacaoBanda.PENDENTE);

        // Conflito com Diabolizer sem esclarecimento deve re-renderizar com alerta
        mockMvc.perform(post("/indique")
                        .with(csrf())
                        .param("bandName", "Diabolizer")
                        .param("country", "Turquia")
                        .param("releaseTitle", "Demo 2025")
                        .param("listenUrl", "https://diabolizer2.bandcamp.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Edição #58")));

        // Com esclarecimento de homônimo válido -> cria PENDENTE + homonimo=true
        mockMvc.perform(post("/indique")
                        .with(csrf())
                        .param("bandName", "Diabolizer")
                        .param("country", "Turquia")
                        .param("releaseTitle", "Demo 2025")
                        .param("listenUrl", "https://diabolizer2.bandcamp.com")
                        .param("confirmarHomonimo", "true")
                        .param("esclarecimentoHomonimo", "Outra banda de Black Metal de Ankara formada em 2021."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/indique?enviado=true#indicar-banda"));

        assertThat(indicacaoRepository.countByStatus(StatusIndicacaoBanda.PENDENTE)).isEqualTo(2L);
    }

    @Test
    void painelAdminDeveExigirAuthCsrfEExecutarFluxoCompletoDeCuradoria() throws Exception {
        mockMvc.perform(get("/admin/indicacoes"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/admin/indicacoes/nova").with(user("admin").roles("ADMIN")))
                .andExpect(status().is4xxClientError());

        IndicacaoBanda ind = criarIndicacao("Krisiun Tribute", "Brasil", StatusIndicacaoBanda.PENDENTE, Instant.now());
        indicacaoRepository.save(ind);

        // Aceitar
        mockMvc.perform(post("/admin/indicacoes/{id}/aceitar", ind.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(indicacaoRepository.findById(ind.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusIndicacaoBanda.APROVADA);

        // Iniciar entrevista
        mockMvc.perform(post("/admin/indicacoes/{id}/em-entrevista", ind.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(indicacaoRepository.findById(ind.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusIndicacaoBanda.EM_ENTREVISTA);

        // Concluir entrevista
        mockMvc.perform(post("/admin/indicacoes/{id}/concluir-entrevista", ind.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(indicacaoRepository.findById(ind.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);

        // Editar
        mockMvc.perform(post("/admin/indicacoes/{id}/editar", ind.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("nomeBanda", "Krisiun Official")
                        .param("pais", "Brasil")
                        .param("regiao", "RS")
                        .param("tituloMaterialFisico", "Black Force Domain (CD)")
                        .param("linkAudicao", "https://krisiun.bandcamp.com"))
                .andExpect(status().is3xxRedirection());
        assertThat(indicacaoRepository.findById(ind.getId()).orElseThrow().getNomeBanda())
                .isEqualTo("Krisiun Official");

        // Recusar com motivo
        mockMvc.perform(post("/admin/indicacoes/{id}/recusar", ind.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("motivoRecusa", "Já entrevistada anteriormente"))
                .andExpect(status().is3xxRedirection());
        IndicacaoBanda recusada = indicacaoRepository.findById(ind.getId()).orElseThrow();
        assertThat(recusada.getStatus()).isEqualTo(StatusIndicacaoBanda.RECUSADA);
        assertThat(recusada.getMotivoRecusa()).isEqualTo("Já entrevistada anteriormente");

        // Excluir
        mockMvc.perform(post("/admin/indicacoes/{id}/excluir", ind.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(indicacaoRepository.findById(ind.getId())).isEmpty();
    }

    private IndicacaoBanda criarIndicacao(String nome, String pais, StatusIndicacaoBanda status, Instant agora) {
        return new IndicacaoBanda(
                UUID.randomUUID(),
                nome,
                IndicacaoBandaService.normalizarChave(nome),
                pais,
                IndicacaoBandaService.normalizarPais(pais),
                "SP",
                "Álbum (CD)",
                "https://bandcamp.com/" + IndicacaoBandaService.normalizarChave(nome).replace(" ", ""),
                status,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                agora,
                agora,
                true);
    }
}
