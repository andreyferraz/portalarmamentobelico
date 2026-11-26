package com.armamentobelico.portalarmamentobelico;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
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
import com.armamentobelico.portalarmamentobelico.dto.NoticiaFormDTO;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EstiloTag;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.repository.NoticiaRepository;
import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;

@SpringBootTest
@AutoConfigureMockMvc
class NoticiaPublicControllerTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("noticia-public-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticiaService noticiaService;

    @Autowired
    private NoticiaRepository noticiaRepository;

    @Autowired
    private EdicaoService edicaoService;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @BeforeEach
    void limparBanco() {
        noticiaRepository.deleteAll();
        edicaoRepository.deleteAll();
    }

    private MockMultipartFile criarPng(String fieldName, String name) throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile(fieldName, name, "image/png", baos.toByteArray());
    }

    private Edicao cadastrarEdicao(int numero, IdiomaEdicao idioma, String preco) throws IOException {
        EdicaoFormDTO form = new EdicaoFormDTO();
        form.setNumeroEdicao(numero);
        form.setIdioma(idioma);
        form.setTitulo("Armamento Bélico #" + numero + (idioma == IdiomaEdicao.EN ? " [ENG]" : ""));
        form.setSubtitulo("Subtítulo #" + numero);
        form.setMesLancamento(idioma == IdiomaEdicao.PT ? "Agosto" : "August");
        form.setAnoLancamento(2026);
        form.setAnoRevista("Ano 18");
        form.setPaginas(68);
        form.setPreco(new BigDecimal(preco));
        form.setStatusEstoque(StatusEstoque.AVAILABLE);
        form.setDescricao("Descrição #" + numero);
        form.setPublicado(true);
        form.setCapa(criarPng("capa", "capa-" + numero + "-" + idioma + ".png"));
        return edicaoService.criar(form);
    }

    @Test
    void deveOcultarSecaoNoticiasNaHomeQuandoVazioEMostrarEmptyStateNoFeed() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("class=\"home-news-section\""))))
                .andExpect(content().string(not(containsString("noticia-interna.html"))));

        mockMvc.perform(get("/noticias"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page-noticias\"")))
                .andExpect(content().string(containsString("Nenhuma notícia encontrada")))
                .andExpect(content().string(containsString("/assets/divisor-edicoes.png")));
    }

    @Test
    void deveRenderizarNoticiaPublicadaComCtaPtEnEOmitirRascunhoCom404() throws Exception {
        Edicao edPt = cadastrarEdicao(58, IdiomaEdicao.PT, "30.00");
        Edicao edEn = cadastrarEdicao(58, IdiomaEdicao.EN, "12.00");

        NoticiaFormDTO pub = new NoticiaFormDTO();
        pub.setTituloPt("Diabolizer: Entrevista Oficial");
        pub.setTituloEn("Diabolizer: Official Interview");
        pub.setCategoriaPt("Death Metal");
        pub.setCategoriaEn("Death Metal");
        pub.setAutor("Editoria Armamento Bélico");
        pub.setDataPublicacao(LocalDate.of(2026, 8, 24));
        pub.setResumoPt("Resumo em PT da entrevista.");
        pub.setResumoEn("English summary of the interview.");
        pub.setConteudoHtmlPt("<p class=\"interview-question\">Pergunta PT</p>");
        pub.setConteudoHtmlEn("<p class=\"interview-question\">Question EN</p>");
        pub.setEdicaoId(edPt.getId());
        pub.setPublicado(true);
        pub.setTags(List.of(new NoticiaFormDTO.TagFormItem("Death Metal", EstiloTag.BLOOD)));
        var salva = noticiaService.criar(pub);

        NoticiaFormDTO rascunho = new NoticiaFormDTO();
        rascunho.setTituloPt("Rascunho Secreto");
        rascunho.setCategoriaPt("Black Metal");
        rascunho.setAutor("Editoria Armamento Bélico");
        rascunho.setDataPublicacao(LocalDate.of(2026, 8, 25));
        rascunho.setResumoPt("Resumo rascunho");
        rascunho.setConteudoHtmlPt("<p>Texto rascunho</p>");
        rascunho.setPublicado(false);
        var salvaRascunho = noticiaService.criar(rascunho);

        // Detalhe publicado com CTA PT (R$) + EN (€), sem <figure> pois sem imagem
        mockMvc.perform(get("/noticias/" + salva.getSlug()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page-noticia\"")))
                .andExpect(content().string(containsString("Diabolizer: Entrevista Oficial")))
                .andExpect(content().string(containsString("Diabolizer: Official Interview")))
                .andExpect(content().string(containsString("R$ 30,00")))
                .andExpect(content().string(containsString("€ 12.00")))
                .andExpect(content().string(containsString("data-add-to-cart-id=\"" + edPt.getId() + "\"")))
                .andExpect(content().string(containsString("data-add-to-cart-id=\"" + edEn.getId() + "\"")))
                .andExpect(content().string(containsString("id=\"catalog-page-data\"")))
                .andExpect(content().string(containsString("id=\"store-config-data\"")))
                .andExpect(content().string(not(containsString("<figure class=\"article-featured-img\""))))
                .andExpect(content().string(not(containsString("US$"))));

        // Rascunho retorna 404
        mockMvc.perform(get("/noticias/" + salvaRascunho.getSlug()))
                .andExpect(status().isNotFound());

        // Feed com filtro por tag e card full-width sem miniatura
        mockMvc.perform(get("/noticias").param("tag", "Death Metal"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("news-feed-item--no-thumb")))
                .andExpect(content().string(containsString("Diabolizer: Entrevista Oficial")))
                .andExpect(content().string(not(containsString("Rascunho Secreto"))));

        // Home exibe seção com 1 notícia centralizada
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"home-news-section\"")))
                .andExpect(content().string(containsString("news-grid-duo--single")))
                .andExpect(content().string(containsString("/noticias/" + salva.getSlug())));
    }
}
