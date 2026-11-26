package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

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
import org.springframework.test.web.servlet.MvcResult;

import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.MoedaEdicao;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AdminEdicaoControllerTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("admin-ctrl-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @BeforeEach
    void limparBanco() {
        edicaoRepository.deleteAll();
    }

    private MockMultipartFile criarCapaPng(String nome) throws IOException {
        BufferedImage img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, 12, 12);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile("capa", nome, "image/png", baos.toByteArray());
    }

    @Test
    void deveExigirAutenticacaoNasRotasAdminEdicoes() throws Exception {
        mockMvc.perform(get("/admin/edicoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=required"));
    }

    @Test
    void deveCriarEditarClonarEExcluirEdicaoViaPainelAdminComCsrf() throws Exception {
        // 1. Criar edição PT via multipart + CSRF
        mockMvc.perform(multipart("/admin/edicoes")
                        .file(criarCapaPng("capa-58.png"))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("numeroEdicao", "58")
                        .param("idioma", "PT")
                        .param("titulo", "Armamento Bélico #58")
                        .param("subtitulo", "Especial Death & Black Metal")
                        .param("mesLancamento", "Agosto")
                        .param("anoLancamento", "2026")
                        .param("anoRevista", "Ano 18")
                        .param("paginas", "68")
                        .param("preco", "30.00")
                        .param("statusEstoque", "AVAILABLE")
                        .param("quantidadeEstoque", "50")
                        .param("descricao", "Descrição oficial da edição 58.")
                        .param("formato", "A5 (14,8 × 21 cm)")
                        .param("papelImpressao", "Offset 90g")
                        .param("tiragem", "300 cópias")
                        .param("publicado", "true")
                        .param("entrevistas[0].banda", "Diabolizer")
                        .param("entrevistas[0].pais", "Turquia")
                        .param("entrevistas[0].estilo", "Death Metal")
                        .param("conteudosExtras[0]", "70 resenhas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/edicoes"));

        Edicao edicaoPt = edicaoRepository.findByNumeroEdicaoAndIdioma(58, IdiomaEdicao.PT).orElseThrow();
        assertThat(edicaoPt.getMoeda()).isEqualTo(MoedaEdicao.BRL);

        // 2. Verificar listagem e atalho "+ Versão EN"
        mockMvc.perform(get("/admin/edicoes").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Armamento Bélico #58")))
                .andExpect(content().string(containsString("R$ 30,00")))
                .andExpect(content().string(containsString("+ Versão EN")))
                .andExpect(content().string(containsString("origemId=" + edicaoPt.getId())));

        // 3. Verificar imagem servida em /uploads/** sem criar sessão
        MvcResult uploadResult = mockMvc.perform(get("/uploads/" + edicaoPt.getCapaPath()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(uploadResult.getRequest().getSession(false)).isNull();

        // 4. Abrir formulário de versão complementar EN
        mockMvc.perform(get("/admin/edicoes/nova")
                        .param("origemId", edicaoPt.getId().toString())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Criar versão Inglês da Edição #58")))
                .andExpect(content().string(containsString("EUR")));

        // 5. Excluir edição via POST com CSRF
        mockMvc.perform(post("/admin/edicoes/" + edicaoPt.getId() + "/excluir")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/edicoes"));

        assertThat(edicaoRepository.findById(edicaoPt.getId())).isEmpty();
    }
}
