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
class PublicCatalogControllerTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("catalog-ctrl-test.db"));
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

    private void cadastrar(int numero, IdiomaEdicao idioma, String preco, StatusEstoque status, boolean publicado)
            throws IOException {
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
        form.setStatusEstoque(status);
        form.setDescricao("Descrição #" + numero);
        form.setPublicado(publicado);
        form.setCapa(criarPng("capa-" + numero + ".png"));
        form.setEntrevistas(List.of(new EdicaoFormDTO.EntrevistaItem("Diabolizer", "Turquia", "Death Metal", null)));
        edicaoService.criar(form);
    }

    @Test
    void deveRenderizarHomeComApenasQuatroEdicoesEmEstoqueEConfiguracaoLoja() throws Exception {
        cadastrar(53, IdiomaEdicao.PT, "25.00", StatusEstoque.AVAILABLE, true);
        cadastrar(54, IdiomaEdicao.PT, "25.00", StatusEstoque.AVAILABLE, true);
        cadastrar(55, IdiomaEdicao.PT, "25.00", StatusEstoque.SOLDOUT, true);
        cadastrar(56, IdiomaEdicao.PT, "25.00", StatusEstoque.LOW, true);
        cadastrar(57, IdiomaEdicao.PT, "30.00", StatusEstoque.AVAILABLE, true);
        cadastrar(58, IdiomaEdicao.PT, "30.00", StatusEstoque.AVAILABLE, true);
        cadastrar(59, IdiomaEdicao.PT, "30.00", StatusEstoque.AVAILABLE, false); // rascunho
        cadastrar(58, IdiomaEdicao.EN, "12.00", StatusEstoque.AVAILABLE, true);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page-home\"")))
                .andExpect(content().string(containsString("id=\"catalog-page-data\"")))
                .andExpect(content().string(containsString("id=\"store-config-data\"")))
                .andExpect(content().string(containsString("revistadeathmetal@hotmail.com")))
                .andExpect(content().string(containsString("0.064")))
                .andExpect(content().string(containsString("0.35")))
                .andExpect(content().string(containsString("href=\"/arquivo\"")))
                .andExpect(content().string(containsString("href=\"/assinatura\"")))
                .andExpect(content().string(containsString("/edicoes/pt/58")))
                .andExpect(content().string(containsString("/edicoes/pt/57")))
                .andExpect(content().string(containsString("/edicoes/pt/56")))
                .andExpect(content().string(containsString("/edicoes/pt/54")))
                .andExpect(content().string(not(containsString("/edicoes/pt/55")))) // esgotada fora da Home
                .andExpect(content().string(not(containsString("/edicoes/pt/53")))) // 5ª em estoque fora do limite 4
                .andExpect(content().string(not(containsString("/edicoes/pt/59")))) // rascunho
                .andExpect(content().string(containsString("/edicoes/en/58")))
                .andExpect(content().string(containsString("id=\"cart-currency-conflict-dialog\"")))
                .andExpect(content().string(containsString("id=\"paypal-checkout-confirm-dialog\"")));
    }

    @Test
    void deveRenderizarArquivoComTodasEdicoesPublicadasIncluindoEsgotadas() throws Exception {
        cadastrar(55, IdiomaEdicao.PT, "25.00", StatusEstoque.SOLDOUT, true);
        cadastrar(58, IdiomaEdicao.PT, "30.00", StatusEstoque.LOW, true);
        cadastrar(59, IdiomaEdicao.PT, "30.00", StatusEstoque.AVAILABLE, false); // rascunho
        cadastrar(58, IdiomaEdicao.EN, "12.00", StatusEstoque.AVAILABLE, true);

        mockMvc.perform(get("/arquivo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page-arquivo\"")))
                .andExpect(content().string(containsString("/assets/divisor-edicoes.png")))
                .andExpect(content().string(containsString("id=\"catalog-page-data\"")))
                .andExpect(content().string(containsString("/edicoes/pt/58")))
                .andExpect(content().string(containsString("/edicoes/pt/55")))
                .andExpect(content().string(containsString("/edicoes/en/58")))
                .andExpect(content().string(not(containsString("/edicoes/pt/59"))));
    }

    @Test
    void deveRenderizarCatalogoVazioSemDadosMockadosQuandoBancoEstiverVazio() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"pt\":[],\"en\":[]}")));

        mockMvc.perform(get("/arquivo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"pt\":[],\"en\":[]}")));
    }
}
