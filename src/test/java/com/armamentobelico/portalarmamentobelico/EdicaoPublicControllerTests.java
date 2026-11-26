package com.armamentobelico.portalarmamentobelico;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.armamentobelico.portalarmamentobelico.dto.EdicaoFormDTO;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.service.EdicaoService;

@SpringBootTest
@AutoConfigureMockMvc
class EdicaoPublicControllerTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("public-ctrl-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EdicaoService edicaoService;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @BeforeEach
    void limparBanco() {
        edicaoRepository.deleteAll();
    }

    private MockMultipartFile criarPng(String nome) throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile("capa", nome, "image/png", baos.toByteArray());
    }

    private void cadastrar(int numero, IdiomaEdicao idioma, String preco, boolean publicado) throws IOException {
        EdicaoFormDTO form = new EdicaoFormDTO();
        form.setNumeroEdicao(numero);
        form.setIdioma(idioma);
        form.setTitulo("Armamento Bélico #" + numero + (idioma == IdiomaEdicao.EN ? " [ENG]" : ""));
        form.setSubtitulo(idioma == IdiomaEdicao.PT ? "Edição Nacional" : "International English Edition");
        form.setMesLancamento(idioma == IdiomaEdicao.PT ? "Agosto" : "August");
        form.setAnoLancamento(2026);
        form.setAnoRevista(idioma == IdiomaEdicao.PT ? "Ano 18" : "Year 18");
        form.setPaginas(68);
        form.setPreco(new BigDecimal(preco));
        form.setStatusEstoque(StatusEstoque.AVAILABLE);
        form.setDescricao("Descrição completa #" + numero);
        form.setFormato("A5");
        form.setPapelImpressao("Offset 90g");
        form.setTiragem("300 cópias");
        form.setYoutubeUrl("https://www.youtube.com/watch?v=5J1o1DHkekE");
        form.setPublicado(publicado);
        form.setCapa(criarPng("capa-" + numero + ".png"));
        form.setEntrevistas(List.of(new EdicaoFormDTO.EntrevistaItem("Diabolizer", "Turquia", "Death Metal", null)));
        form.setConteudosExtras(List.of("70 resenhas sem filtros"));
        edicaoService.criar(form);
    }

    @Test
    void deveRenderizarPaginaPublicaPtEEnComJsonEmbutidoEVersaoAlternativa() throws Exception {
        cadastrar(57, IdiomaEdicao.PT, "25.00", true);
        cadastrar(58, IdiomaEdicao.PT, "30.00", true);
        cadastrar(58, IdiomaEdicao.EN, "12.00", true);

        mockMvc.perform(get("/edicoes/pt/58"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page-edicao\"")))
                .andExpect(content().string(containsString("Armamento Bélico #58")))
                .andExpect(content().string(containsString("R$ 30,00")))
                .andExpect(content().string(containsString("Português (Brasil)")))
                .andExpect(content().string(containsString("Diabolizer")))
                .andExpect(content().string(containsString("id=\"edition-page-data\"")))
                .andExpect(content().string(containsString("\"alternateLangUrl\":\"/edicoes/en/58\"")))
                .andExpect(content().string(containsString("href=\"/edicoes/pt/57\"")));

        mockMvc.perform(get("/edicoes/en/58"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Armamento Bélico #58 [ENG]")))
                .andExpect(content().string(containsString("€ 12.00")))
                .andExpect(content().string(containsString("English (International)")))
                .andExpect(content().string(containsString("\"alternateLangUrl\":\"/edicoes/pt/58\"")));
    }

    @Test
    void deveRedirecionarAtalhoPorNumeroERetornar404ParaRascunhoOuIdiomaInvalido() throws Exception {
        cadastrar(58, IdiomaEdicao.PT, "30.00", true);
        cadastrar(59, IdiomaEdicao.EN, "12.00", true);
        cadastrar(60, IdiomaEdicao.PT, "30.00", false); // rascunho

        mockMvc.perform(get("/edicoes/58"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/edicoes/pt/58"));

        mockMvc.perform(get("/edicoes/59"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/edicoes/en/59"));

        mockMvc.perform(get("/edicoes/pt/60"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/edicoes/60"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/edicoes/es/58"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveEmitirAlternateLangUrlNullQuandoEdicaoExistirEmApenasUmIdioma() throws Exception {
        cadastrar(55, IdiomaEdicao.PT, "25.00", true);

        mockMvc.perform(get("/edicoes/pt/55"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"alternateLangUrl\":null")))
                .andExpect(content().string(containsString("id=\"edition-single-lang-notice\"")))
                .andExpect(content().string(not(containsString("US$"))));
    }
}
