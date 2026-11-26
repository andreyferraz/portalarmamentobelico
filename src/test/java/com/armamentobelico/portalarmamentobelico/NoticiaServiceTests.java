package com.armamentobelico.portalarmamentobelico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.armamentobelico.portalarmamentobelico.dto.EdicaoFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.NoticiaDetalheViewDTO;
import com.armamentobelico.portalarmamentobelico.dto.NoticiaFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.NoticiaPaginaFeedDTO;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EstiloTag;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.Noticia;
import com.armamentobelico.portalarmamentobelico.model.NoticiaTag;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.repository.NoticiaRepository;
import com.armamentobelico.portalarmamentobelico.repository.NoticiaTagRepository;
import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.NoticiaService;

@SpringBootTest
class NoticiaServiceTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("noticia-service-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private NoticiaService noticiaService;

    @Autowired
    private NoticiaRepository noticiaRepository;

    @Autowired
    private NoticiaTagRepository noticiaTagRepository;

    @Autowired
    private EdicaoService edicaoService;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @BeforeEach
    void limparBanco() {
        noticiaRepository.deleteAll();
        edicaoRepository.deleteAll();
    }

    private MockMultipartFile criarPng(String fieldName, String fileName) throws IOException {
        BufferedImage img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, 12, 12);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile(fieldName, fileName, "image/png", baos.toByteArray());
    }

    private NoticiaFormDTO criarFormBase(String tituloPt) {
        NoticiaFormDTO form = new NoticiaFormDTO();
        form.setTituloPt(tituloPt);
        form.setCategoriaPt("Death Metal / Entrevista");
        form.setAutor("Editoria Armamento Bélico");
        form.setDataPublicacao(LocalDate.of(2026, 8, 24));
        form.setResumoPt("Resumo editorial da matéria em português.");
        form.setConteudoHtmlPt("<p>Texto principal <strong>brutal</strong>.</p>");
        form.setPublicado(true);
        return form;
    }

    private Edicao cadastrarEdicao(int numero, IdiomaEdicao idioma, String preco, boolean publicado) throws IOException {
        EdicaoFormDTO form = new EdicaoFormDTO();
        form.setNumeroEdicao(numero);
        form.setIdioma(idioma);
        form.setTitulo("Armamento Bélico #" + numero + (idioma == IdiomaEdicao.EN ? " [ENG]" : ""));
        form.setSubtitulo("Subtítulo #" + numero);
        form.setMesLancamento(idioma == IdiomaEdicao.PT ? "Agosto" : "August");
        form.setAnoLancamento(2026);
        form.setAnoRevista(idioma == IdiomaEdicao.PT ? "Ano 18" : "Year 18");
        form.setPaginas(68);
        form.setPreco(new BigDecimal(preco));
        form.setStatusEstoque(StatusEstoque.AVAILABLE);
        form.setDescricao("Descrição #" + numero);
        form.setPublicado(publicado);
        form.setCapa(criarPng("capa", "capa-" + numero + "-" + idioma + ".png"));
        return edicaoService.criar(form);
    }

    @Test
    void deveCriarNoticiaComSlugAutomaticoSanitizacaoHtmlTagsEImagemWebp() throws IOException {
        NoticiaFormDTO form = criarFormBase("Diabolizer: Fúria do Death Metal Turco #58");
        form.setConteudoHtmlPt("<p>Entrevista <script>alert(1)</script><span class=\"interview-question bad\">Pergunta</span></p>");
        form.setImagemDestaque(criarPng("imagemDestaque", "diabolizer.png"));
        form.setTags(List.of(
                new NoticiaFormDTO.TagFormItem("Death Metal", EstiloTag.DARK),
                new NoticiaFormDTO.TagFormItem("death metal", EstiloTag.BLOOD), // duplicata case-insensitive
                new NoticiaFormDTO.TagFormItem("Edição #58", EstiloTag.BLOOD)));

        Noticia criada = noticiaService.criar(form);

        assertEquals("diabolizer-furia-do-death-metal-turco-58", criada.getSlug());
        assertFalse(criada.getConteudoHtmlPt().contains("<script"));
        assertTrue(criada.getConteudoHtmlPt().contains("class=\"interview-question\""));
        assertFalse(criada.getConteudoHtmlPt().contains("bad"));
        assertNotNull(criada.getImagemDestaquePath());
        assertTrue(criada.getImagemDestaquePath().endsWith(".webp"));
        assertTrue(Files.exists(tempDir.resolve("uploads").resolve(criada.getImagemDestaquePath())));

        List<NoticiaTag> tags = noticiaTagRepository.findByNoticiaIdOrderByOrdemAsc(criada.getId());
        assertEquals(2, tags.size());
        assertEquals("Death Metal", tags.get(0).getNome());
        assertEquals("Edição #58", tags.get(1).getNome());
        assertEquals(EstiloTag.BLOOD, tags.get(1).getEstilo());
    }

    @Test
    void deveExigirTrioEssencialEnQuandoAlgumCampoEnForPreenchido() {
        NoticiaFormDTO form = criarFormBase("Notícia com EN parcial");
        form.setTituloEn("Partial English Title");
        // resumoEn e conteudoHtmlEn ausentes

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> noticiaService.criar(form));
        assertTrue(ex.getMessage().toLowerCase().contains("inglês"));
    }

    @Test
    void deveRejeitarSlugDuplicadoEConteudoSemTextoVisivel() {
        NoticiaFormDTO form1 = criarFormBase("Matéria Única");
        noticiaService.criar(form1);

        NoticiaFormDTO duplicado = criarFormBase("Matéria Única");
        assertThrows(IllegalArgumentException.class, () -> noticiaService.criar(duplicado));

        NoticiaFormDTO vazioHtml = criarFormBase("Matéria Vazia");
        vazioHtml.setConteudoHtmlPt("<p><br></p>");
        assertThrows(IllegalArgumentException.class, () -> noticiaService.criar(vazioHtml));
    }

    @Test
    void deveResolverEdicaoVinculadaPtEIrmaEnETratarOnDeleteSetNullECascade() throws IOException {
        Edicao edPt = cadastrarEdicao(58, IdiomaEdicao.PT, "30.00", true);
        Edicao edEn = cadastrarEdicao(58, IdiomaEdicao.EN, "12.00", true);

        NoticiaFormDTO form = criarFormBase("Diabolizer Edição 58");
        form.setEdicaoId(edPt.getId());
        form.setTags(List.of(new NoticiaFormDTO.TagFormItem("Death Metal", EstiloTag.BLOOD)));
        Noticia criada = noticiaService.criar(form);

        Optional<NoticiaDetalheViewDTO> detalheOpt = noticiaService.buscarDetalhesPublicos(criada.getSlug());
        assertTrue(detalheOpt.isPresent());
        NoticiaDetalheViewDTO detalhe = detalheOpt.get();
        assertNotNull(detalhe.edicaoVinculadaPt());
        assertNotNull(detalhe.edicaoVinculadaEn());
        assertEquals(edPt.getId(), detalhe.edicaoVinculadaPt().getId());
        assertEquals(edEn.getId(), detalhe.edicaoVinculadaEn().getId());
        assertTrue(detalhe.linkedEditionsCatalogJson().contains(edPt.getId().toString()));
        assertTrue(detalhe.linkedEditionsCatalogJson().contains(edEn.getId().toString()));

        // Excluir edição vinculada -> ON DELETE SET NULL na notícia
        edicaoService.excluir(edPt.getId());
        Noticia aposExcluirEdicao = noticiaRepository.findById(criada.getId()).orElseThrow();
        assertNull(aposExcluirEdicao.getEdicaoId());

        // Excluir notícia -> ON DELETE CASCADE em noticia_tag
        noticiaService.excluir(criada.getId());
        assertTrue(noticiaTagRepository.findByNoticiaIdOrderByOrdemAsc(criada.getId()).isEmpty());
    }

    @Test
    void devePaginarFeedEmDezItensEFiltrarPorTagEBuscaBilingue() {
        for (int i = 1; i <= 12; i++) {
            NoticiaFormDTO form = criarFormBase("Notícia Underground #" + i);
            form.setDataPublicacao(LocalDate.of(2026, 8, i));
            form.setTags(List.of(new NoticiaFormDTO.TagFormItem(i % 2 == 0 ? "Death Metal" : "Black Metal", EstiloTag.DARK)));
            if (i == 12) {
                form.setTituloEn("Exclusive Turkish Death Metal Interview");
                form.setResumoEn("English summary about Istanbul extreme scene.");
                form.setConteudoHtmlEn("<p>Full English content here.</p>");
            }
            noticiaService.criar(form);
        }

        NoticiaPaginaFeedDTO pag1 = noticiaService.listarFeedPublico(null, null, 1);
        assertEquals(10, pag1.itens().size());
        assertEquals(12, pag1.totalItens());
        assertEquals(2, pag1.totalPaginas());
        assertTrue(pag1.temProxima());
        assertFalse(pag1.temAnterior());

        NoticiaPaginaFeedDTO pag2 = noticiaService.listarFeedPublico(null, null, 2);
        assertEquals(2, pag2.itens().size());
        assertTrue(pag2.temAnterior());
        assertFalse(pag2.temProxima());

        NoticiaPaginaFeedDTO porTag = noticiaService.listarFeedPublico("Death Metal", null, 1);
        assertEquals(6, porTag.totalItens());

        NoticiaPaginaFeedDTO porBuscaEn = noticiaService.listarFeedPublico(null, "Turkish", 1);
        assertEquals(1, porBuscaEn.totalItens());
        assertEquals("Notícia Underground #12", porBuscaEn.itens().get(0).noticia().getTituloPt());

        assertEquals(2, noticiaService.listarUltimasPublicadasHome().size());
    }
}
