package com.armamentobelico.portalarmamentobelico;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.armamentobelico.portalarmamentobelico.model.Noticia;
import com.armamentobelico.portalarmamentobelico.repository.NoticiaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AdminNoticiaControllerTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("admin-noticia-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticiaRepository noticiaRepository;

    @BeforeEach
    void limparBanco() {
        noticiaRepository.deleteAll();
    }

    private MockMultipartFile criarPng() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile("imagemDestaque", "destaque.png", "image/png", baos.toByteArray());
    }

    @Test
    void deveExigirAutenticacaoECsrfNoModuloAdminNoticias() throws Exception {
        mockMvc.perform(get("/admin/noticias"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=required"));

        mockMvc.perform(post("/admin/noticias")
                        .with(user("admin").roles("ADMIN"))
                        .param("tituloPt", "Sem CSRF"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveCriarEditarListarEExcluirNoticiaViaAdmin() throws Exception {
        mockMvc.perform(get("/admin/noticias/nova").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/vendor/quill/quill.js")));

        mockMvc.perform(multipart("/admin/noticias")
                        .file(criarPng())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("tituloPt", "Diabolizer na Edição 58")
                        .param("categoriaPt", "Death Metal")
                        .param("autor", "Editoria Armamento Bélico")
                        .param("dataPublicacao", "2026-08-24")
                        .param("resumoPt", "Entrevista exclusiva com a horda turca.")
                        .param("conteudoHtmlPt", "<p>Corpo <strong>oficial</strong></p><script>alert(1)</script>")
                        .param("publicado", "true")
                        .param("tags[0].nome", "Death Metal")
                        .param("tags[0].estilo", "BLOOD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/noticias"));

        Noticia salva = noticiaRepository.findBySlug("diabolizer-na-edicao-58").orElseThrow();
        assertTrue(salva.getImagemDestaquePath().endsWith(".webp"));

        mockMvc.perform(get("/admin/noticias").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Diabolizer na Edição 58")));

        mockMvc.perform(post("/admin/noticias/" + salva.getId() + "/excluir")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/noticias"));

        assertEquals(0, noticiaRepository.count());
    }
}
