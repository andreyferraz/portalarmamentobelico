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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.armamentobelico.portalarmamentobelico.dto.EventoAdminFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.EventoPublicCardDTO;
import com.armamentobelico.portalarmamentobelico.dto.EventoPublicFormDTO;
import com.armamentobelico.portalarmamentobelico.model.EstadoBrasil;
import com.armamentobelico.portalarmamentobelico.model.Evento;
import com.armamentobelico.portalarmamentobelico.model.PapelPublicador;
import com.armamentobelico.portalarmamentobelico.model.StatusModeracaoEvento;
import com.armamentobelico.portalarmamentobelico.repository.EventoRepository;
import com.armamentobelico.portalarmamentobelico.service.EventoService;

@SpringBootTest
class EventoServiceTests {

    private static final ZoneId ZONA_SP = ZoneId.of("America/Sao_Paulo");

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("evento-service-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private EventoService eventoService;

    @Autowired
    private EventoRepository eventoRepository;

    @BeforeEach
    void limparBanco() {
        eventoRepository.deleteAll();
        eventoService.limparRateLimit();
    }

    private MockMultipartFile criarPng(String fieldName, String fileName) throws IOException {
        BufferedImage img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 12, 12);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile(fieldName, fileName, "image/png", baos.toByteArray());
    }

    private EventoPublicFormDTO criarPublicFormValido() throws IOException {
        EventoPublicFormDTO form = new EventoPublicFormDTO();
        form.setTitle("Necro Ritual Fest VI");
        form.setFlyer(criarPng("flyer", "flyer.png"));
        form.setCity("São Paulo");
        form.setState("SP");
        form.setDate(LocalDate.now(ZONA_SP).plusDays(10));
        form.setTime("20:00");
        form.setOrganizerContact("(11) 99123-4567 / @necroritual");
        form.setPublisherWhatsapp("(11) 98888-7777");
        form.setPublisherRole("organizer");
        form.setRulesAgree(true);
        return form;
    }

    private EventoAdminFormDTO criarAdminFormValido() throws IOException {
        EventoAdminFormDTO form = new EventoAdminFormDTO();
        form.setTitulo("Culto das Catacumbas");
        form.setFlyer(criarPng("flyer", "catacumbas.png"));
        form.setCidade("Belo Horizonte");
        form.setEstado(EstadoBrasil.MG);
        form.setDataEvento(LocalDate.now(ZONA_SP).plusDays(5));
        form.setHorario("19:30");
        form.setContatoOrganizador("contato@catacumbas.org");
        form.setWhatsappPublicador("(31) 98444-1122");
        form.setPapelPublicador(PapelPublicador.AJUDANTE);
        form.setStatusModeracao(StatusModeracaoEvento.APROVADO);
        return form;
    }

    @Test
    void deveSubmeterEventoPublicoComoPendenteComFlyerWebp() throws IOException {
        Evento salvo = eventoService.submeterEventoPublico(criarPublicFormValido());

        assertNotNull(salvo.getId());
        assertEquals(StatusModeracaoEvento.PENDENTE, salvo.getStatusModeracao());
        assertEquals(PapelPublicador.ORGANIZADOR, salvo.getPapelPublicador());
        assertEquals(EstadoBrasil.SP, salvo.getEstado());
        assertTrue(salvo.getFlyerPath().endsWith(".webp"));
        assertTrue(Files.exists(tempDir.resolve("uploads").resolve(salvo.getFlyerPath())));
        assertNull(salvo.getModeradoPor());
        assertNull(salvo.getModeradoEm());

        // Evento pendente não aparece na listagem pública
        assertTrue(eventoService.listarAprovadosPublico("all", "all").isEmpty());
    }

    @Test
    void deveRejeitarSubmissaoPublicaComRegrasDesmarcadasWhatsappCurtoUfInvalidaDataPassadaOuFlyerVazio()
            throws IOException {
        EventoPublicFormDTO semRegras = criarPublicFormValido();
        semRegras.setRulesAgree(false);
        assertThrows(IllegalArgumentException.class, () -> eventoService.submeterEventoPublico(semRegras));

        EventoPublicFormDTO whatsappCurto = criarPublicFormValido();
        whatsappCurto.setPublisherWhatsapp("119888");
        assertThrows(IllegalArgumentException.class, () -> eventoService.submeterEventoPublico(whatsappCurto));

        EventoPublicFormDTO ufInvalida = criarPublicFormValido();
        ufInvalida.setState("XX");
        assertThrows(IllegalArgumentException.class, () -> eventoService.submeterEventoPublico(ufInvalida));

        EventoPublicFormDTO dataPassada = criarPublicFormValido();
        dataPassada.setDate(LocalDate.now(ZONA_SP).minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> eventoService.submeterEventoPublico(dataPassada));

        EventoPublicFormDTO semFlyer = criarPublicFormValido();
        semFlyer.setFlyer(new MockMultipartFile("flyer", "", "image/png", new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> eventoService.submeterEventoPublico(semFlyer));

        assertEquals(0, eventoRepository.count());
    }

    @Test
    void deveAprovarERecusarEventoComAuditoriaELimpezaDeMotivo() throws IOException {
        Evento pendente = eventoService.submeterEventoPublico(criarPublicFormValido());

        Evento recusado = eventoService.recusar(pendente.getId(), "Banda cristã fora das diretrizes", "admin");
        assertEquals(StatusModeracaoEvento.RECUSADO, recusado.getStatusModeracao());
        assertEquals("Banda cristã fora das diretrizes", recusado.getMotivoRecusa());
        assertEquals("admin", recusado.getModeradoPor());
        assertNotNull(recusado.getModeradoEm());
        assertTrue(eventoService.listarAprovadosPublico("all", "all").isEmpty());

        Evento aprovado = eventoService.aprovar(pendente.getId(), "editor2");
        assertEquals(StatusModeracaoEvento.APROVADO, aprovado.getStatusModeracao());
        assertNull(aprovado.getMotivoRecusa());
        assertEquals("editor2", aprovado.getModeradoPor());
        assertNotNull(aprovado.getModeradoEm());

        List<EventoPublicCardDTO> publicos = eventoService.listarAprovadosPublico("SP", "upcoming");
        assertEquals(1, publicos.size());
        assertEquals("Necro Ritual Fest VI", publicos.get(0).titulo());
        assertFalse(publicos.get(0).passado());
    }

    @Test
    void deveAtualizarEventoAdminMantendoOuSubstituindoFlyerEExcluirComLimpezaDeDisco() throws IOException {
        Evento criado = eventoService.criarEventoAdmin(criarAdminFormValido(), "admin");
        String flyerInicial = criado.getFlyerPath();
        assertTrue(Files.exists(tempDir.resolve("uploads").resolve(flyerInicial)));

        // Edição sem novo arquivo mantém flyer anterior
        EventoAdminFormDTO edicaoSemFlyer = eventoService.carregarParaEdicao(criado.getId());
        edicaoSemFlyer.setTitulo("Culto das Catacumbas II");
        edicaoSemFlyer.setFlyer(null);
        Evento atualizado1 = eventoService.atualizarEventoAdmin(criado.getId(), edicaoSemFlyer, "admin");
        assertEquals("Culto das Catacumbas II", atualizado1.getTitulo());
        assertEquals(flyerInicial, atualizado1.getFlyerPath());
        assertTrue(Files.exists(tempDir.resolve("uploads").resolve(flyerInicial)));

        // Edição com novo arquivo substitui e remove o .webp anterior
        EventoAdminFormDTO edicaoComFlyer = eventoService.carregarParaEdicao(criado.getId());
        edicaoComFlyer.setFlyer(criarPng("flyer", "novo-flyer.png"));
        Evento atualizado2 = eventoService.atualizarEventoAdmin(criado.getId(), edicaoComFlyer, "admin");
        String novoFlyer = atualizado2.getFlyerPath();
        assertFalse(flyerInicial.equals(novoFlyer));
        assertFalse(Files.exists(tempDir.resolve("uploads").resolve(flyerInicial)));
        assertTrue(Files.exists(tempDir.resolve("uploads").resolve(novoFlyer)));

        // Exclusão remove banco e arquivo .webp
        eventoService.excluir(criado.getId());
        assertEquals(0, eventoRepository.count());
        assertFalse(Files.exists(tempDir.resolve("uploads").resolve(novoFlyer)));
    }
}
