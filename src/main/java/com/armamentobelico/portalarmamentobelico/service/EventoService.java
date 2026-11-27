package com.armamentobelico.portalarmamentobelico.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.armamentobelico.portalarmamentobelico.dto.EventoAdminFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.EventoPublicCardDTO;
import com.armamentobelico.portalarmamentobelico.dto.EventoPublicFormDTO;
import com.armamentobelico.portalarmamentobelico.model.EstadoBrasil;
import com.armamentobelico.portalarmamentobelico.model.Evento;
import com.armamentobelico.portalarmamentobelico.model.PapelPublicador;
import com.armamentobelico.portalarmamentobelico.model.StatusModeracaoEvento;
import com.armamentobelico.portalarmamentobelico.repository.EventoRepository;
import com.armamentobelico.portalarmamentobelico.utils.ValidationUtils;

@Service
@Transactional
public class EventoService {

    public static final ZoneId ZONA_PORTAL = ZoneId.of("America/Sao_Paulo");

    private static final Pattern WHATSAPP_PERMITIDO = Pattern.compile("^[\\d\\s()+-]{10,20}$");
    private static final DateTimeFormatter HORARIO_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Set<String> MIME_IMAGENS_ACEITAS = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif");

    private final EventoRepository eventoRepository;
    private final FileUploadService fileUploadService;
    private final Clock clock;
    private final int maxEnviosPorIp;
    private final Duration janelaRateLimit;
    private final ConcurrentHashMap<String, Deque<Instant>> enviosPorIp = new ConcurrentHashMap<>();

    @Autowired
    public EventoService(
            EventoRepository eventoRepository,
            FileUploadService fileUploadService,
            @Value("${app.eventos.rate-limit.max-envios:5}") int maxEnviosPorIp,
            @Value("${app.eventos.rate-limit.janela-minutos:15}") long janelaMinutos) {
        this(eventoRepository, fileUploadService, Clock.system(ZONA_PORTAL), maxEnviosPorIp,
                Duration.ofMinutes(Math.max(1, janelaMinutos)));
    }

    public EventoService(
            EventoRepository eventoRepository,
            FileUploadService fileUploadService,
            Clock clock,
            int maxEnviosPorIp,
            Duration janelaRateLimit) {
        this.eventoRepository = eventoRepository;
        this.fileUploadService = fileUploadService;
        this.clock = clock;
        this.maxEnviosPorIp = Math.max(1, maxEnviosPorIp);
        this.janelaRateLimit = janelaRateLimit;
    }

    public record ResumoAdminEventos(
            long total,
            long pendentes,
            long aprovados,
            long recusados) {
    }

    public LocalDate hojePortal() {
        return LocalDate.now(clock.withZone(ZONA_PORTAL));
    }

    public void limparRateLimit() {
        enviosPorIp.clear();
    }

    public void verificarERegistrarLimiteIp(String ipCliente) {
        String chave = (ipCliente == null || ipCliente.isBlank()) ? "desconhecido" : ipCliente.trim();
        Instant agora = Instant.now(clock);
        Instant limiteJanela = agora.minus(janelaRateLimit);

        Deque<Instant> timestamps = enviosPorIp.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(limiteJanela)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxEnviosPorIp) {
                throw new IllegalArgumentException(
                        "Limite temporário de envios atingido. Aguarde alguns minutos antes de enviar outro evento.");
            }
            timestamps.addLast(agora);
        }
    }

    public Evento submeterEventoPublico(EventoPublicFormDTO dto) {
        ValidationUtils.validarCampoObrigatorio(dto, "Formulário do evento");

        if (dto.getWebsite() != null && !dto.getWebsite().trim().isEmpty()) {
            throw new IllegalArgumentException("Submissão rejeitada.");
        }
        if (!dto.isRulesAgree()) {
            throw new IllegalArgumentException(
                    "Você precisa confirmar que o evento atende às diretrizes da cena underground.");
        }

        String titulo = validarTexto(dto.getTitle(), "Título do evento", 3, 120);
        String cidade = validarTexto(dto.getCity(), "Cidade", 2, 100);
        EstadoBrasil estado = EstadoBrasil.fromSigla(dto.getState());
        LocalDate dataEvento = dto.getDate();
        ValidationUtils.validarCampoObrigatorio(dataEvento, "Data do evento");
        if (dataEvento.isBefore(hojePortal())) {
            throw new IllegalArgumentException(
                    "A data do evento não pode ser anterior à data atual.");
        }
        String horario = validarENormalizarHorario(dto.getTime());
        String contatoOrganizador = validarTexto(dto.getOrganizerContact(), "Contato do organizador", 3, 150);
        String whatsappPublicador = validarWhatsapp(dto.getPublisherWhatsapp());
        PapelPublicador papelPublicador = PapelPublicador.fromValor(dto.getPublisherRole());
        validarFlyerObrigatorio(dto.getFlyer());

        String flyerPath = fileUploadService.salvarImagem(dto.getFlyer());
        try {
            Instant agora = Instant.now(clock);
            Evento evento = new Evento();
            evento.setId(UUID.randomUUID());
            evento.setTitulo(titulo);
            evento.setFlyerPath(flyerPath);
            evento.setCidade(cidade);
            evento.setEstado(estado);
            evento.setDataEvento(dataEvento);
            evento.setHorario(horario);
            evento.setContatoOrganizador(contatoOrganizador);
            evento.setWhatsappPublicador(whatsappPublicador);
            evento.setPapelPublicador(papelPublicador);
            evento.setAceitouRegras(true);
            evento.setStatusModeracao(StatusModeracaoEvento.PENDENTE);
            evento.setMotivoRecusa(null);
            evento.setModeradoEm(null);
            evento.setModeradoPor(null);
            evento.setCriadoEm(agora);
            evento.setAtualizadoEm(agora);
            evento.setNew(true);
            return eventoRepository.save(evento);
        } catch (RuntimeException ex) {
            removerImagemSilenciosamente(flyerPath);
            throw ex;
        }
    }

    public Evento criarEventoAdmin(EventoAdminFormDTO dto, String adminUsername) {
        ValidationUtils.validarCampoObrigatorio(dto, "Formulário do evento");

        String titulo = validarTexto(dto.getTitulo(), "Título do evento", 3, 120);
        String cidade = validarTexto(dto.getCidade(), "Cidade", 2, 100);
        ValidationUtils.validarCampoObrigatorio(dto.getEstado(), "Estado (UF)");
        ValidationUtils.validarCampoObrigatorio(dto.getDataEvento(), "Data do evento");
        String horario = validarENormalizarHorario(dto.getHorario());
        String contatoOrganizador = validarTexto(dto.getContatoOrganizador(), "Contato do organizador", 3, 150);
        String whatsappPublicador = validarWhatsapp(dto.getWhatsappPublicador());
        PapelPublicador papel = dto.getPapelPublicador() != null ? dto.getPapelPublicador() : PapelPublicador.ORGANIZADOR;
        StatusModeracaoEvento status = dto.getStatusModeracao() != null
                ? dto.getStatusModeracao()
                : StatusModeracaoEvento.APROVADO;
        String motivoRecusa = status == StatusModeracaoEvento.RECUSADO
                ? normalizarMotivoRecusa(dto.getMotivoRecusa())
                : null;
        validarFlyerObrigatorio(dto.getFlyer());

        String flyerPath = fileUploadService.salvarImagem(dto.getFlyer());
        try {
            Instant agora = Instant.now(clock);
            Evento evento = new Evento();
            evento.setId(UUID.randomUUID());
            evento.setTitulo(titulo);
            evento.setFlyerPath(flyerPath);
            evento.setCidade(cidade);
            evento.setEstado(dto.getEstado());
            evento.setDataEvento(dto.getDataEvento());
            evento.setHorario(horario);
            evento.setContatoOrganizador(contatoOrganizador);
            evento.setWhatsappPublicador(whatsappPublicador);
            evento.setPapelPublicador(papel);
            evento.setAceitouRegras(true);
            evento.setStatusModeracao(status);
            evento.setMotivoRecusa(motivoRecusa);
            if (status != StatusModeracaoEvento.PENDENTE) {
                evento.setModeradoEm(agora);
                evento.setModeradoPor(normalizarAdmin(adminUsername));
            }
            evento.setCriadoEm(agora);
            evento.setAtualizadoEm(agora);
            evento.setNew(true);
            return eventoRepository.save(evento);
        } catch (RuntimeException ex) {
            removerImagemSilenciosamente(flyerPath);
            throw ex;
        }
    }

    public Evento atualizarEventoAdmin(UUID id, EventoAdminFormDTO dto, String adminUsername) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        ValidationUtils.validarCampoObrigatorio(dto, "Formulário do evento");

        Evento existente = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        String titulo = validarTexto(dto.getTitulo(), "Título do evento", 3, 120);
        String cidade = validarTexto(dto.getCidade(), "Cidade", 2, 100);
        ValidationUtils.validarCampoObrigatorio(dto.getEstado(), "Estado (UF)");
        ValidationUtils.validarCampoObrigatorio(dto.getDataEvento(), "Data do evento");
        String horario = validarENormalizarHorario(dto.getHorario());
        String contatoOrganizador = validarTexto(dto.getContatoOrganizador(), "Contato do organizador", 3, 150);
        String whatsappPublicador = validarWhatsapp(dto.getWhatsappPublicador());
        PapelPublicador papel = dto.getPapelPublicador() != null ? dto.getPapelPublicador() : existente.getPapelPublicador();
        StatusModeracaoEvento novoStatus = dto.getStatusModeracao() != null
                ? dto.getStatusModeracao()
                : existente.getStatusModeracao();
        String motivoRecusa = novoStatus == StatusModeracaoEvento.RECUSADO
                ? normalizarMotivoRecusa(dto.getMotivoRecusa())
                : null;

        String flyerAnterior = existente.getFlyerPath();
        String novoFlyerPath = flyerAnterior;
        boolean substituiuFlyer = false;

        if (arquivoEnviado(dto.getFlyer())) {
            validarTipoImagem(dto.getFlyer());
            novoFlyerPath = fileUploadService.salvarImagem(dto.getFlyer());
            substituiuFlyer = true;
        }

        try {
            Instant agora = Instant.now(clock);
            StatusModeracaoEvento statusAnterior = existente.getStatusModeracao();

            existente.setTitulo(titulo);
            existente.setFlyerPath(novoFlyerPath);
            existente.setCidade(cidade);
            existente.setEstado(dto.getEstado());
            existente.setDataEvento(dto.getDataEvento());
            existente.setHorario(horario);
            existente.setContatoOrganizador(contatoOrganizador);
            existente.setWhatsappPublicador(whatsappPublicador);
            existente.setPapelPublicador(papel);
            existente.setStatusModeracao(novoStatus);
            existente.setMotivoRecusa(motivoRecusa);

            if (novoStatus != statusAnterior) {
                if (novoStatus == StatusModeracaoEvento.PENDENTE) {
                    existente.setModeradoEm(null);
                    existente.setModeradoPor(null);
                } else {
                    existente.setModeradoEm(agora);
                    existente.setModeradoPor(normalizarAdmin(adminUsername));
                }
            }

            existente.setAtualizadoEm(agora);
            existente.setNew(false);

            Evento salvo = eventoRepository.save(existente);
            if (substituiuFlyer && flyerAnterior != null && !flyerAnterior.isBlank()) {
                fileUploadService.removerImagem(flyerAnterior);
            }
            return salvo;
        } catch (RuntimeException ex) {
            if (substituiuFlyer) {
                removerImagemSilenciosamente(novoFlyerPath);
            }
            throw ex;
        }
    }

    public Evento aprovar(UUID id, String adminUsername) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        Instant agora = Instant.now(clock);
        evento.setStatusModeracao(StatusModeracaoEvento.APROVADO);
        evento.setMotivoRecusa(null);
        evento.setModeradoEm(agora);
        evento.setModeradoPor(normalizarAdmin(adminUsername));
        evento.setAtualizadoEm(agora);
        evento.setNew(false);
        return eventoRepository.save(evento);
    }

    public Evento recusar(UUID id, String motivoRecusa, String adminUsername) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        Instant agora = Instant.now(clock);
        evento.setStatusModeracao(StatusModeracaoEvento.RECUSADO);
        evento.setMotivoRecusa(normalizarMotivoRecusa(motivoRecusa));
        evento.setModeradoEm(agora);
        evento.setModeradoPor(normalizarAdmin(adminUsername));
        evento.setAtualizadoEm(agora);
        evento.setNew(false);
        return eventoRepository.save(evento);
    }

    public void excluir(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        String flyerPath = evento.getFlyerPath();
        eventoRepository.deleteById(id);

        if (flyerPath != null && !flyerPath.isBlank()) {
            fileUploadService.removerImagem(flyerPath);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Evento> buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return eventoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public EventoAdminFormDTO carregarParaEdicao(UUID id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        EventoAdminFormDTO form = new EventoAdminFormDTO();
        form.setId(evento.getId());
        form.setTitulo(evento.getTitulo());
        form.setFlyerPathAtual(evento.getFlyerPath());
        form.setCidade(evento.getCidade());
        form.setEstado(evento.getEstado());
        form.setDataEvento(evento.getDataEvento());
        form.setHorario(evento.getHorario());
        form.setContatoOrganizador(evento.getContatoOrganizador());
        form.setWhatsappPublicador(evento.getWhatsappPublicador());
        form.setPapelPublicador(evento.getPapelPublicador());
        form.setStatusModeracao(evento.getStatusModeracao());
        form.setMotivoRecusa(evento.getMotivoRecusa());
        return form;
    }

    @Transactional(readOnly = true)
    public List<EventoPublicCardDTO> listarAprovadosPublico(String estado, String statusTemporal) {
        LocalDate hoje = hojePortal();
        String estadoNorm = normalizarFiltroEstadoPublico(estado);
        String statusNorm = normalizarFiltroTemporalPublico(statusTemporal);

        return eventoRepository.findAprovadosOrdenados(hoje.toString(), estadoNorm, statusNorm)
                .stream()
                .map(ev -> new EventoPublicCardDTO(
                        ev.getId(),
                        ev.getTitulo(),
                        "/uploads/" + ev.getFlyerPath(),
                        ev.getCidade(),
                        ev.getEstado().name(),
                        ev.getDataEvento(),
                        ev.getDataFormatada(),
                        ev.getHorario(),
                        ev.getContatoOrganizador(),
                        ev.isPassado(hoje)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Evento> listarParaAdmin(String statusModeracao, String estado, String busca) {
        String statusNorm = normalizarFiltroStatusAdmin(statusModeracao);
        String estadoNorm = normalizarFiltroEstadoPublico(estado);
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);

        List<Evento> lista = eventoRepository.findParaAdmin(statusNorm, estadoNorm);
        if (termo.isEmpty()) {
            return lista;
        }
        return lista.stream()
                .filter(ev -> contem(ev.getTitulo(), termo)
                        || contem(ev.getCidade(), termo)
                        || contem(ev.getContatoOrganizador(), termo)
                        || contem(ev.getWhatsappPublicador(), termo))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoAdminEventos contarResumoAdmin() {
        long pendentes = eventoRepository.countByStatusModeracao(StatusModeracaoEvento.PENDENTE);
        long aprovados = eventoRepository.countByStatusModeracao(StatusModeracaoEvento.APROVADO);
        long recusados = eventoRepository.countByStatusModeracao(StatusModeracaoEvento.RECUSADO);
        return new ResumoAdminEventos(pendentes + aprovados + recusados, pendentes, aprovados, recusados);
    }

    public String normalizarFiltroEstadoPublico(String estado) {
        if (estado == null || estado.isBlank() || "all".equalsIgnoreCase(estado.trim())
                || "TODOS".equalsIgnoreCase(estado.trim())) {
            return "all";
        }
        try {
            return EstadoBrasil.fromSigla(estado).name();
        } catch (IllegalArgumentException ex) {
            return "all";
        }
    }

    public String normalizarFiltroTemporalPublico(String statusTemporal) {
        if (statusTemporal == null || statusTemporal.isBlank()) {
            return "all";
        }
        String norm = statusTemporal.trim().toLowerCase(Locale.ROOT);
        if ("upcoming".equals(norm) || "past".equals(norm)) {
            return norm;
        }
        return "all";
    }

    public String normalizarFiltroStatusAdmin(String status) {
        if (status == null || status.isBlank() || "TODOS".equalsIgnoreCase(status.trim())
                || "ALL".equalsIgnoreCase(status.trim())) {
            return "TODOS";
        }
        try {
            return StatusModeracaoEvento.valueOf(status.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException ex) {
            return "TODOS";
        }
    }

    private String validarTexto(String valor, String nomeCampo, int min, int max) {
        ValidationUtils.validarCampoStringObrigatorio(valor, nomeCampo);
        String limpo = valor.trim();
        if (limpo.length() < min) {
            throw new IllegalArgumentException(nomeCampo + " deve ter pelo menos " + min + " caracteres.");
        }
        if (limpo.length() > max) {
            throw new IllegalArgumentException(nomeCampo + " deve ter no máximo " + max + " caracteres.");
        }
        return limpo;
    }

    private String validarENormalizarHorario(String horario) {
        ValidationUtils.validarCampoStringObrigatorio(horario, "Horário");
        String limpo = horario.trim();
        try {
            LocalTime parsed = LocalTime.parse(limpo, HORARIO_FORMATTER);
            return parsed.format(HORARIO_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Horário inválido. Use o formato HH:mm.");
        }
    }

    private String validarWhatsapp(String whatsapp) {
        ValidationUtils.validarCampoStringObrigatorio(whatsapp, "WhatsApp de quem publica");
        String limpo = whatsapp.trim();
        if (!WHATSAPP_PERMITIDO.matcher(limpo).matches()) {
            throw new IllegalArgumentException(
                    "Informe um número de WhatsApp válido com DDD (entre 10 e 20 caracteres).");
        }
        String digitos = limpo.replaceAll("\\D", "");
        if (digitos.length() < 10) {
            throw new IllegalArgumentException(
                    "Informe um número de WhatsApp válido com DDD (mínimo 10 dígitos).");
        }
        return limpo;
    }

    private void validarFlyerObrigatorio(MultipartFile flyer) {
        if (!arquivoEnviado(flyer)) {
            throw new IllegalArgumentException("A imagem do flyer do evento é obrigatória.");
        }
        validarTipoImagem(flyer);
    }

    private void validarTipoImagem(MultipartFile flyer) {
        String contentType = flyer.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String baseType = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
            if (!MIME_IMAGENS_ACEITAS.contains(baseType)) {
                throw new IllegalArgumentException(
                        "Formato de imagem inválido. Envie o flyer em JPG, PNG, WEBP ou GIF.");
            }
        }
    }

    private String normalizarMotivoRecusa(String motivo) {
        if (motivo == null) {
            return null;
        }
        String limpo = motivo.trim();
        if (limpo.isEmpty()) {
            return null;
        }
        if (limpo.length() > 500) {
            throw new IllegalArgumentException("O motivo da recusa deve ter no máximo 500 caracteres.");
        }
        return limpo;
    }

    private String normalizarAdmin(String adminUsername) {
        return (adminUsername == null || adminUsername.isBlank()) ? "admin" : adminUsername.trim();
    }

    private void removerImagemSilenciosamente(String flyerPath) {
        try {
            fileUploadService.removerImagem(flyerPath);
        } catch (RuntimeException ignored) {
            // Evita mascarar exceção original de persistência
        }
    }

    private static boolean arquivoEnviado(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private static boolean contem(String campo, String termoLower) {
        return campo != null && campo.toLowerCase(Locale.ROOT).contains(termoLower);
    }
}
