package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.armamentobelico.portalarmamentobelico.dto.EdicaoFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.IndicacaoPublicCardDTO;
import com.armamentobelico.portalarmamentobelico.dto.IndicacaoPublicFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.VerificacaoBandaResponseDTO;
import com.armamentobelico.portalarmamentobelico.exceptions.ConflitoIndicacaoBandaException;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.IndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoEntrevistaRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.repository.IndicacaoBandaRepository;
import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.IndicacaoBandaService;

@SpringBootTest
class IndicacaoBandaServiceTests {

    private static final String TEST_DB = Path.of("target", "test-indicacao-service.db").toAbsolutePath().toString();
    private static final String TEST_UPLOADS = Path.of("target", "test-indicacao-uploads").toAbsolutePath().toString();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEST_DB);
        registry.add("upload.dir", () -> TEST_UPLOADS);
        registry.add("app.indicacoes.rate-limit.max-envios", () -> "3");
    }

    @Autowired
    private IndicacaoBandaService indicacaoService;

    @Autowired
    private EdicaoService edicaoService;

    @Autowired
    private IndicacaoBandaRepository indicacaoRepository;

    @Autowired
    private EdicaoEntrevistaRepository entrevistaRepository;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @BeforeEach
    void setup() {
        entrevistaRepository.deleteAll();
        indicacaoRepository.deleteAll();
        edicaoRepository.deleteAll();
        indicacaoService.limparRateLimit();
    }

    @Test
    void deveNormalizarChavesESinonimosDePaisPtEn() {
        assertThat(IndicacaoBandaService.normalizarChave("  Mórtem   Solis ")).isEqualTo("mortem solis");
        assertThat(IndicacaoBandaService.normalizarPais("Brazil")).isEqualTo("brasil");
        assertThat(IndicacaoBandaService.normalizarPais("Turquia")).isEqualTo("turquia");
        assertThat(IndicacaoBandaService.normalizarPais("Turkey")).isEqualTo("turquia");
        assertThat(IndicacaoBandaService.normalizarPais("USA")).isEqualTo("estados unidos");
        assertThat(IndicacaoBandaService.normalizarPais("United Kingdom")).isEqualTo("reino unido");
    }

    @Test
    void deveValidarHoneypotUrlSeguraERateLimit() {
        IndicacaoPublicFormDTO honeypot = criarForm("Coldwinter", "Brasil", "Demo K7", "https://coldwinter.bandcamp.com");
        honeypot.setWebsite("http://spam.example");
        assertThatThrownBy(() -> indicacaoService.cadastrarPublico(honeypot))
                .isInstanceOf(IllegalArgumentException.class);

        IndicacaoPublicFormDTO xss = criarForm("Coldwinter", "Brasil", "Demo K7", "javascript:alert(1)");
        assertThatThrownBy(() -> indicacaoService.cadastrarPublico(xss))
                .isInstanceOf(IllegalArgumentException.class);

        indicacaoService.verificarERegistrarLimiteIp("10.0.0.1");
        indicacaoService.verificarERegistrarLimiteIp("10.0.0.1");
        indicacaoService.verificarERegistrarLimiteIp("10.0.0.1");
        assertThatThrownBy(() -> indicacaoService.verificarERegistrarLimiteIp("10.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limite");
    }

    @Test
    void deveTratarConflitosEHomonimosComBloqueioDeFalsoHomonimo() {
        IndicacaoBanda primeira = indicacaoService.cadastrarPublico(
                criarForm("Mortem Solis", "Brasil", "Necro Rites (CD)", "https://mortemsolis.bandcamp.com/"));
        assertThat(primeira.getStatus()).isEqualTo(StatusIndicacaoBanda.PENDENTE);

        // Conflito sem esclarecimento deve falhar com ConflitoIndicacaoBandaException
        IndicacaoPublicFormDTO duplicada = criarForm("Mórtem Solis", "Brazil", "Outro", "https://outro.bandcamp.com");
        assertThatThrownBy(() -> indicacaoService.cadastrarPublico(duplicada))
                .isInstanceOf(ConflitoIndicacaoBandaException.class);

        // Falso homônimo com o mesmo link normalizado deve ser rejeitado
        IndicacaoPublicFormDTO falsoHomonimo = criarForm("Mortem Solis", "Brasil", "Outro", "https://mortemsolis.bandcamp.com");
        falsoHomonimo.setConfirmarHomonimo(true);
        falsoHomonimo.setEsclarecimentoHomonimo("Banda de death metal de Belo Horizonte formada em 2019.");
        assertThatThrownBy(() -> indicacaoService.cadastrarPublico(falsoHomonimo))
                .isInstanceOf(ConflitoIndicacaoBandaException.class)
                .hasMessageContaining("idêntico");

        // Homônimo legítimo com link distinto deve ser aceito como PENDENTE + homonimo=true
        IndicacaoPublicFormDTO homonimoValido = criarForm("Mortem Solis", "Brasil", "Demo 2024", "https://mortemsolisbh.bandcamp.com");
        homonimoValido.setConfirmarHomonimo(true);
        homonimoValido.setEsclarecimentoHomonimo("Outra horda de Black Metal de Belo Horizonte/MG formada em 2022.");
        IndicacaoBanda salva = indicacaoService.cadastrarPublico(homonimoValido);
        assertThat(salva.getStatus()).isEqualTo(StatusIndicacaoBanda.PENDENTE);
        assertThat(salva.getHomonimo()).isTrue();
    }

    @Test
    void deveExecutarCicloCompletoESincronizarComPublicacaoDespublicacaoEExclusaoDeEdicao() {
        IndicacaoBanda ind = indicacaoService.cadastrarPublico(
                criarForm("Diabolizer", "Turquia", "Khalkedonian Death", "https://diabolizer.bandcamp.com"));
        indicacaoService.aceitar(ind.getId());
        indicacaoService.iniciarEntrevista(ind.getId());
        indicacaoService.concluirEntrevista(ind.getId());

        List<IndicacaoPublicCardDTO> vitrineAntes = indicacaoService.listarPublicas();
        assertThat(vitrineAntes).hasSize(1);
        assertThat(vitrineAntes.get(0).status()).isEqualTo(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);

        // Publicar edição com entrevista (correspondência automática por nome + país em inglês)
        EdicaoFormDTO edicaoForm = criarEdicaoForm(58, IdiomaEdicao.PT, true, "Diabolizer", "Turkey", null);
        Edicao edicao58 = edicaoService.criar(edicaoForm);

        IndicacaoBanda aposPublicar = indicacaoService.buscarPorId(ind.getId()).orElseThrow();
        assertThat(aposPublicar.getStatus()).isEqualTo(StatusIndicacaoBanda.PUBLICADA);
        assertThat(aposPublicar.getNumeroEdicaoPublicada()).isEqualTo(58);
        assertThat(indicacaoService.listarPublicas()).isEmpty();

        VerificacaoBandaResponseDTO verificacao = indicacaoService.verificarBanda("Diabolizer", "Turquia");
        assertThat(verificacao.situacao()).isEqualTo("JA_ENTREVISTADA");
        assertThat(verificacao.edicoes()).extracting(VerificacaoBandaResponseDTO.EdicaoReferenciaDTO::numeroEdicao)
                .containsExactly(58);

        // Despublicar edição deve reverter para ENTREVISTA_CONCLUIDA
        EdicaoFormDTO editarDespublicar = edicaoService.carregarParaEdicao(edicao58.getId());
        editarDespublicar.setPublicado(false);
        edicaoService.atualizar(edicao58.getId(), editarDespublicar);

        IndicacaoBanda aposDespublicar = indicacaoService.buscarPorId(ind.getId()).orElseThrow();
        assertThat(aposDespublicar.getStatus()).isEqualTo(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);
        assertThat(aposDespublicar.getEdicaoPublicadaId()).isNull();

        // Republicar via vínculo explícito e depois excluir edição -> também reverte
        EdicaoFormDTO republicar = edicaoService.carregarParaEdicao(edicao58.getId());
        republicar.setPublicado(true);
        republicar.setEntrevistas(List.of(new EdicaoFormDTO.EntrevistaItem("Diabolizer", "Turquia", "Death Metal", ind.getId())));
        edicaoService.atualizar(edicao58.getId(), republicar);
        assertThat(indicacaoService.buscarPorId(ind.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusIndicacaoBanda.PUBLICADA);

        edicaoService.excluir(edicao58.getId());
        assertThat(indicacaoService.buscarPorId(ind.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);
    }

    private IndicacaoPublicFormDTO criarForm(String nome, String pais, String material, String url) {
        IndicacaoPublicFormDTO dto = new IndicacaoPublicFormDTO();
        dto.setBandName(nome);
        dto.setCountry(pais);
        dto.setRegion("SP");
        dto.setReleaseTitle(material);
        dto.setListenUrl(url);
        return dto;
    }

    private EdicaoFormDTO criarEdicaoForm(
            int numero,
            IdiomaEdicao idioma,
            boolean publicado,
            String banda,
            String pais,
            UUID indicacaoId) {
        EdicaoFormDTO form = new EdicaoFormDTO();
        form.setNumeroEdicao(numero);
        form.setIdioma(idioma);
        form.setTitulo("Armamento Bélico #" + numero);
        form.setMesLancamento("Agosto");
        form.setAnoLancamento(2026);
        form.setAnoRevista("Ano 18");
        form.setPaginas(68);
        form.setPreco(new BigDecimal("30.00"));
        form.setStatusEstoque(StatusEstoque.AVAILABLE);
        form.setDescricao("Descrição da edição");
        form.setPublicado(publicado);
        byte[] pngBytes = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89,
                0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
                0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01,
                0x0D, 0x0A, 0x2D, (byte) 0xB4,
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
        form.setCapa(new MockMultipartFile("capa", "capa.png", "image/png", pngBytes));
        form.setEntrevistas(List.of(new EdicaoFormDTO.EntrevistaItem(banda, pais, "Death Metal", indicacaoId)));
        return form;
    }
}
