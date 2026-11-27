package com.armamentobelico.portalarmamentobelico;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
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

import com.armamentobelico.portalarmamentobelico.dto.EventoAdminFormDTO;
import com.armamentobelico.portalarmamentobelico.model.EstadoBrasil;
import com.armamentobelico.portalarmamentobelico.model.Evento;
import com.armamentobelico.portalarmamentobelico.model.PapelPublicador;
import com.armamentobelico.portalarmamentobelico.model.StatusModeracaoEvento;
import com.armamentobelico.portalarmamentobelico.repository.EventoRepository;
import com.armamentobelico.portalarmamentobelico.service.EventoService;

@SpringBootTest
@AutoConfigureMockMvc
class EventoControllerTests {

    private static final ZoneId ZONA_SP = ZoneId.of("America/Sao_Paulo");

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("evento-controller-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoService eventoService;

    @BeforeEach
    void limparBanco() {
        eventoRepository.deleteAll();
        eventoService.limparRateLimit();
    }

    private MockMultipartFile criarPng(String fieldName, String fileName) throws IOException {
        BufferedImage img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 12, 12);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile(fieldName, fileName, "image/png", baos.toByteArray());
    }

    @Test
    void deveSubmeterEventoPublicoComoPendenteIgnorandoMassAssignmentEExigindoCsrf() throws Exception {
        LocalDate dataFutura = LocalDate.now(ZONA_SP).plusDays(15);

        // Sem CSRF -> 403
        mockMvc.perform(multipart("/eventos")
                        .file(criarPng("flyer", "flyer.png"))
                        .param("title", "Sem CSRF Fest"))
                .andExpect(status().isForbidden());

        // Honeypot preenchido -> descarta sem salvar
        mockMvc.perform(multipart("/eventos")
                        .file(criarPng("flyer", "bot.png"))
                        .with(csrf())
                        .param("title", "Spam Bot Fest")
                        .param("city", "São Paulo")
                        .param("state", "SP")
                        .param("date", dataFutura.toString())
                        .param("time", "20:00")
                        .param("organizerContact", "@spam")
                        .param("publisherWhatsapp", "(11) 98888-7777")
                        .param("publisherRole", "organizer")
                        .param("rulesAgree", "true")
                        .param("website", "https://spam.example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/eventos#cadastrar-evento"));
        assertEquals(0, eventoRepository.count());

        // Envio válido com tentativa de mass assignment (statusModeracao=APROVADO)
        mockMvc.perform(multipart("/eventos")
                        .file(criarPng("flyer", "ritual.png"))
                        .with(csrf())
                        .param("title", "Ritual Profano Fest")
                        .param("city", "São Paulo")
                        .param("state", "SP")
                        .param("date", dataFutura.toString())
                        .param("time", "20:00")
                        .param("organizerContact", "(11) 99123-4567 / @ritual")
                        .param("publisherWhatsapp", "(11) 97777-6666")
                        .param("publisherRole", "organizer")
                        .param("rulesAgree", "true")
                        .param("statusModeracao", "APROVADO")
                        .param("moderadoPor", "invasor"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/eventos#cadastrar-evento"))
                .andExpect(flash().attributeExists("eventoSucesso"));

        List<Evento> salvos = eventoRepository.findAll();
        assertEquals(1, salvos.size());
        Evento salvo = salvos.get(0);
        assertEquals(StatusModeracaoEvento.PENDENTE, salvo.getStatusModeracao());
        assertNull(salvo.getModeradoPor());
    }

    @Test
    void deveExibirApenasEventosAprovadosNaPaginaPublicaSemExporWhatsappDoPublicador() throws Exception {
        LocalDate dataFutura = LocalDate.now(ZONA_SP).plusDays(20);

        EventoAdminFormDTO aprovadoForm = new EventoAdminFormDTO();
        aprovadoForm.setTitulo("Massacre Underground SP");
        aprovadoForm.setFlyer(criarPng("flyer", "aprovado.png"));
        aprovadoForm.setCidade("São Paulo");
        aprovadoForm.setEstado(EstadoBrasil.SP);
        aprovadoForm.setDataEvento(dataFutura);
        aprovadoForm.setHorario("21:00");
        aprovadoForm.setContatoOrganizador("@massacreprod");
        aprovadoForm.setWhatsappPublicador("(11) 91234-5678");
        aprovadoForm.setPapelPublicador(PapelPublicador.ORGANIZADOR);
        aprovadoForm.setStatusModeracao(StatusModeracaoEvento.APROVADO);
        eventoService.criarEventoAdmin(aprovadoForm, "admin");

        EventoAdminFormDTO pendenteForm = new EventoAdminFormDTO();
        pendenteForm.setTitulo("Evento Ainda Pendente");
        pendenteForm.setFlyer(criarPng("flyer", "pendente.png"));
        pendenteForm.setCidade("Curitiba");
        pendenteForm.setEstado(EstadoBrasil.PR);
        pendenteForm.setDataEvento(dataFutura);
        pendenteForm.setHorario("19:00");
        pendenteForm.setContatoOrganizador("@pendenteprod");
        pendenteForm.setWhatsappPublicador("(41) 99999-0000");
        pendenteForm.setPapelPublicador(PapelPublicador.AJUDANTE);
        pendenteForm.setStatusModeracao(StatusModeracaoEvento.PENDENTE);
        eventoService.criarEventoAdmin(pendenteForm, "admin");

        mockMvc.perform(get("/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Massacre Underground SP")))
                .andExpect(content().string(containsString("@massacreprod")))
                .andExpect(content().string(not(containsString("Evento Ainda Pendente"))))
                .andExpect(content().string(not(containsString("(11) 91234-5678"))))
                .andExpect(content().string(not(containsString("912345678"))))
                .andExpect(content().string(containsString("/assets/divisor-edicoes.png")));
    }

    @Test
    void deveExigirLoginNoAdminEExecutarFluxoCompletoDeModeracaoECrud() throws Exception {
        mockMvc.perform(get("/admin/eventos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=required"));

        LocalDate dataFutura = LocalDate.now(ZONA_SP).plusDays(30);

        // Criação direta pelo admin
        mockMvc.perform(multipart("/admin/eventos")
                        .file(criarPng("flyer", "admin-evento.png"))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("titulo", "Blasfêmia Open Air")
                        .param("cidade", "Belo Horizonte")
                        .param("estado", "MG")
                        .param("dataEvento", dataFutura.toString())
                        .param("horario", "18:30")
                        .param("contatoOrganizador", "contato@blasfemia.org")
                        .param("whatsappPublicador", "(31) 98765-4321")
                        .param("papelPublicador", "ORGANIZADOR")
                        .param("statusModeracao", "PENDENTE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"));

        Evento salvo = eventoRepository.findAll().get(0);
        assertEquals(StatusModeracaoEvento.PENDENTE, salvo.getStatusModeracao());

        // Listagem admin mostra WhatsApp de validação do publicador
        mockMvc.perform(get("/admin/eventos").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Blasfêmia Open Air")))
                .andExpect(content().string(containsString("(31) 98765-4321")))
                .andExpect(content().string(containsString("https://wa.me/5531987654321")));

        // Recusar com motivo
        mockMvc.perform(post("/admin/eventos/" + salvo.getId() + "/recusar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("motivoRecusa", "Flyer ilegível"))
                .andExpect(status().is3xxRedirection());

        Evento recusado = eventoRepository.findById(salvo.getId()).orElseThrow();
        assertEquals(StatusModeracaoEvento.RECUSADO, recusado.getStatusModeracao());
        assertEquals("Flyer ilegível", recusado.getMotivoRecusa());

        // Aprovar
        mockMvc.perform(post("/admin/eventos/" + salvo.getId() + "/aprovar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Evento aprovado = eventoRepository.findById(salvo.getId()).orElseThrow();
        assertEquals(StatusModeracaoEvento.APROVADO, aprovado.getStatusModeracao());
        assertNull(aprovado.getMotivoRecusa());
        assertEquals("admin", aprovado.getModeradoPor());

        // Editar
        mockMvc.perform(get("/admin/eventos/" + salvo.getId() + "/editar")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar Evento")))
                .andExpect(content().string(containsString("Blasf&ecirc;mia Open Air")));

        mockMvc.perform(multipart("/admin/eventos/" + salvo.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("titulo", "Blasfêmia Open Air II")
                        .param("cidade", "Belo Horizonte")
                        .param("estado", "MG")
                        .param("dataEvento", dataFutura.toString())
                        .param("horario", "19:00")
                        .param("contatoOrganizador", "contato@blasfemia.org")
                        .param("whatsappPublicador", "(31) 98765-4321")
                        .param("papelPublicador", "ORGANIZADOR")
                        .param("statusModeracao", "APROVADO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/eventos"));

        Evento editado = eventoRepository.findById(salvo.getId()).orElseThrow();
        assertEquals("Blasfêmia Open Air II", editado.getTitulo());
        String flyerPath = editado.getFlyerPath();
        assertTrue(Files.exists(tempDir.resolve("uploads").resolve(flyerPath)));

        // Excluir definitivamente
        mockMvc.perform(post("/admin/eventos/" + salvo.getId() + "/excluir")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertEquals(0, eventoRepository.count());
        assertFalse(Files.exists(tempDir.resolve("uploads").resolve(flyerPath)));
    }
}
