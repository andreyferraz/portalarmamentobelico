package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.armamentobelico.portalarmamentobelico.model.EstadoBrasil;
import com.armamentobelico.portalarmamentobelico.model.Evento;
import com.armamentobelico.portalarmamentobelico.model.PapelPublicador;
import com.armamentobelico.portalarmamentobelico.model.StatusModeracaoEvento;
import com.armamentobelico.portalarmamentobelico.repository.EventoRepository;

@SpringBootTest
class EventoRepositoryTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("evento-repo-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private EventoRepository eventoRepository;

    @BeforeEach
    void limparBanco() {
        eventoRepository.deleteAll();
    }

    private Evento novoEvento(
            String titulo,
            EstadoBrasil estado,
            LocalDate data,
            String horario,
            StatusModeracaoEvento status,
            Instant criadoEm) {
        Evento evento = new Evento();
        evento.setId(UUID.randomUUID());
        evento.setTitulo(titulo);
        evento.setFlyerPath("flyer-" + UUID.randomUUID() + ".webp");
        evento.setCidade("Cidade Teste");
        evento.setEstado(estado);
        evento.setDataEvento(data);
        evento.setHorario(horario);
        evento.setContatoOrganizador("(11) 99999-8888");
        evento.setWhatsappPublicador("(11) 98888-7777");
        evento.setPapelPublicador(PapelPublicador.ORGANIZADOR);
        evento.setAceitouRegras(true);
        evento.setStatusModeracao(status);
        evento.setCriadoEm(criadoEm);
        evento.setAtualizadoEm(criadoEm);
        evento.setNew(true);
        return evento;
    }

    @Test
    void deveRetornarApenasAprovadosOrdenadosComFuturosCrescenteEPassadosDecrescente() {
        LocalDate hoje = LocalDate.of(2026, 11, 26);
        Instant base = Instant.parse("2026-11-01T12:00:00Z");

        eventoRepository.save(novoEvento("Pendente Futuro", EstadoBrasil.SP, LocalDate.of(2026, 12, 1), "20:00",
                StatusModeracaoEvento.PENDENTE, base));
        eventoRepository.save(novoEvento("Recusado Futuro", EstadoBrasil.SP, LocalDate.of(2026, 12, 2), "20:00",
                StatusModeracaoEvento.RECUSADO, base.plusSeconds(1)));

        eventoRepository.save(novoEvento("Futuro Distante", EstadoBrasil.PR, LocalDate.of(2027, 1, 15), "18:00",
                StatusModeracaoEvento.APROVADO, base.plusSeconds(2)));
        eventoRepository.save(novoEvento("Hoje Tarde", EstadoBrasil.SP, LocalDate.of(2026, 11, 26), "21:00",
                StatusModeracaoEvento.APROVADO, base.plusSeconds(3)));
        eventoRepository.save(novoEvento("Hoje Cedo", EstadoBrasil.SP, LocalDate.of(2026, 11, 26), "19:00",
                StatusModeracaoEvento.APROVADO, base.plusSeconds(4)));
        eventoRepository.save(novoEvento("Passado Antigo", EstadoBrasil.RS, LocalDate.of(2026, 9, 5), "17:00",
                StatusModeracaoEvento.APROVADO, base.plusSeconds(5)));
        eventoRepository.save(novoEvento("Passado Recente", EstadoBrasil.RJ, LocalDate.of(2026, 11, 20), "22:00",
                StatusModeracaoEvento.APROVADO, base.plusSeconds(6)));

        List<Evento> aprovados = eventoRepository.findAprovadosOrdenados(hoje.toString(), null, "all");

        assertThat(aprovados)
                .extracting(Evento::getTitulo)
                .containsExactly(
                        "Hoje Cedo",
                        "Hoje Tarde",
                        "Futuro Distante",
                        "Passado Recente",
                        "Passado Antigo");
    }

    @Test
    void deveFiltrarAprovadosPorEstadoEStatusTemporal() {
        LocalDate hoje = LocalDate.of(2026, 11, 26);
        Instant base = Instant.parse("2026-11-01T12:00:00Z");

        eventoRepository.save(novoEvento("SP Futuro", EstadoBrasil.SP, LocalDate.of(2026, 12, 10), "20:00",
                StatusModeracaoEvento.APROVADO, base));
        eventoRepository.save(novoEvento("SP Passado", EstadoBrasil.SP, LocalDate.of(2026, 10, 10), "20:00",
                StatusModeracaoEvento.APROVADO, base.plusSeconds(1)));
        eventoRepository.save(novoEvento("MG Futuro", EstadoBrasil.MG, LocalDate.of(2026, 12, 12), "19:30",
                StatusModeracaoEvento.APROVADO, base.plusSeconds(2)));

        assertThat(eventoRepository.findAprovadosOrdenados(hoje.toString(), "SP", "all"))
                .extracting(Evento::getTitulo)
                .containsExactly("SP Futuro", "SP Passado");

        assertThat(eventoRepository.findAprovadosOrdenados(hoje.toString(), null, "upcoming"))
                .extracting(Evento::getTitulo)
                .containsExactly("SP Futuro", "MG Futuro");

        assertThat(eventoRepository.findAprovadosOrdenados(hoje.toString(), "SP", "past"))
                .extracting(Evento::getTitulo)
                .containsExactly("SP Passado");
    }

    @Test
    void deveContarPorStatusEOrdenarFilaAdminComPendentesPrimeiro() {
        Instant base = Instant.parse("2026-11-01T12:00:00Z");

        eventoRepository.save(novoEvento("Aprovado Antigo", EstadoBrasil.SP, LocalDate.of(2026, 12, 1), "20:00",
                StatusModeracaoEvento.APROVADO, base));
        eventoRepository.save(novoEvento("Pendente 1", EstadoBrasil.MG, LocalDate.of(2026, 12, 2), "20:00",
                StatusModeracaoEvento.PENDENTE, base.plusSeconds(10)));
        eventoRepository.save(novoEvento("Pendente 2", EstadoBrasil.SP, LocalDate.of(2026, 12, 3), "20:00",
                StatusModeracaoEvento.PENDENTE, base.plusSeconds(20)));
        eventoRepository.save(novoEvento("Recusado Recente", EstadoBrasil.RJ, LocalDate.of(2026, 12, 4), "20:00",
                StatusModeracaoEvento.RECUSADO, base.plusSeconds(30)));

        assertThat(eventoRepository.countByStatusModeracao(StatusModeracaoEvento.PENDENTE)).isEqualTo(2);
        assertThat(eventoRepository.countByStatusModeracao(StatusModeracaoEvento.APROVADO)).isEqualTo(1);
        assertThat(eventoRepository.countByStatusModeracao(StatusModeracaoEvento.RECUSADO)).isEqualTo(1);

        List<Evento> todosAdmin = eventoRepository.findParaAdmin(null, null);
        assertThat(todosAdmin)
                .extracting(Evento::getTitulo)
                .containsExactly("Pendente 1", "Pendente 2", "Recusado Recente", "Aprovado Antigo");

        assertThat(eventoRepository.findParaAdmin("PENDENTE", "SP"))
                .extracting(Evento::getTitulo)
                .containsExactly("Pendente 2");
    }
}
