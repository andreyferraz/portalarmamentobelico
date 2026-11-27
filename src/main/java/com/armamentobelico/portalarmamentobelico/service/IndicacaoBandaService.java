package com.armamentobelico.portalarmamentobelico.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.armamentobelico.portalarmamentobelico.dto.IndicacaoAdminFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.IndicacaoPublicCardDTO;
import com.armamentobelico.portalarmamentobelico.dto.IndicacaoPublicFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.VerificacaoBandaResponseDTO;
import com.armamentobelico.portalarmamentobelico.dto.VerificacaoBandaResponseDTO.EdicaoReferenciaDTO;
import com.armamentobelico.portalarmamentobelico.exceptions.ConflitoIndicacaoBandaException;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EdicaoEntrevista;
import com.armamentobelico.portalarmamentobelico.model.IndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.model.StatusIndicacaoBanda;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoEntrevistaRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoEntrevistaRepository.EntrevistaPublicadaRow;
import com.armamentobelico.portalarmamentobelico.repository.IndicacaoBandaRepository;
import com.armamentobelico.portalarmamentobelico.utils.ValidationUtils;

@Service
@Transactional
public class IndicacaoBandaService {

    public static final ZoneId ZONA_PORTAL = ZoneId.of("America/Sao_Paulo");

    private static final Pattern COMBINING_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    private static final List<StatusIndicacaoBanda> STATUS_ATIVOS_FILA = List.of(
            StatusIndicacaoBanda.PENDENTE,
            StatusIndicacaoBanda.APROVADA,
            StatusIndicacaoBanda.EM_ENTREVISTA,
            StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);

    private static final List<StatusIndicacaoBanda> STATUS_PUBLICOS = List.of(
            StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA,
            StatusIndicacaoBanda.EM_ENTREVISTA,
            StatusIndicacaoBanda.APROVADA);

    private static final List<StatusIndicacaoBanda> STATUS_NAO_RECUSADOS = List.of(
            StatusIndicacaoBanda.PENDENTE,
            StatusIndicacaoBanda.APROVADA,
            StatusIndicacaoBanda.EM_ENTREVISTA,
            StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA,
            StatusIndicacaoBanda.PUBLICADA);

    private static final Map<String, String> SINONIMOS_PAISES = Map.ofEntries(
            Map.entry("brazil", "brasil"),
            Map.entry("br", "brasil"),
            Map.entry("turkey", "turquia"),
            Map.entry("turkiye", "turquia"),
            Map.entry("usa", "estados unidos"),
            Map.entry("u.s.a.", "estados unidos"),
            Map.entry("u.s.", "estados unidos"),
            Map.entry("us", "estados unidos"),
            Map.entry("eua", "estados unidos"),
            Map.entry("e.u.a.", "estados unidos"),
            Map.entry("united states", "estados unidos"),
            Map.entry("united states of america", "estados unidos"),
            Map.entry("uk", "reino unido"),
            Map.entry("u.k.", "reino unido"),
            Map.entry("united kingdom", "reino unido"),
            Map.entry("england", "reino unido"),
            Map.entry("inglaterra", "reino unido"),
            Map.entry("great britain", "reino unido"),
            Map.entry("gra bretanha", "reino unido"),
            Map.entry("gra-bretanha", "reino unido"),
            Map.entry("scotland", "escocia"),
            Map.entry("germany", "alemanha"),
            Map.entry("deutschland", "alemanha"),
            Map.entry("finland", "finlandia"),
            Map.entry("suomi", "finlandia"),
            Map.entry("sweden", "suecia"),
            Map.entry("sverige", "suecia"),
            Map.entry("norway", "noruega"),
            Map.entry("norge", "noruega"),
            Map.entry("poland", "polonia"),
            Map.entry("polska", "polonia"),
            Map.entry("france", "franca"),
            Map.entry("italy", "italia"),
            Map.entry("spain", "espanha"),
            Map.entry("espana", "espanha"),
            Map.entry("greece", "grecia"),
            Map.entry("hellas", "grecia"),
            Map.entry("japan", "japao"),
            Map.entry("nihon", "japao"),
            Map.entry("ukraine", "ucrania"),
            Map.entry("czech republic", "republica tcheca"),
            Map.entry("czechia", "republica tcheca"),
            Map.entry("chequia", "republica tcheca"),
            Map.entry("netherlands", "paises baixos"),
            Map.entry("holland", "paises baixos"),
            Map.entry("holanda", "paises baixos"),
            Map.entry("belgium", "belgica"),
            Map.entry("switzerland", "suica"),
            Map.entry("denmark", "dinamarca"),
            Map.entry("uruguay", "uruguai"),
            Map.entry("paraguay", "paraguai"));

    private final IndicacaoBandaRepository indicacaoRepository;
    private final EdicaoEntrevistaRepository entrevistaRepository;
    private final Clock clock;
    private final int maxEnviosPorIp;
    private final Duration janelaRateLimit;
    private final ConcurrentHashMap<String, Deque<Instant>> enviosPorIp = new ConcurrentHashMap<>();

    @Autowired
    public IndicacaoBandaService(
            IndicacaoBandaRepository indicacaoRepository,
            EdicaoEntrevistaRepository entrevistaRepository,
            @Value("${app.indicacoes.rate-limit.max-envios:5}") int maxEnviosPorIp,
            @Value("${app.indicacoes.rate-limit.janela-minutos:15}") long janelaMinutos) {
        this(indicacaoRepository, entrevistaRepository, Clock.system(ZONA_PORTAL), maxEnviosPorIp,
                Duration.ofMinutes(Math.max(1, janelaMinutos)));
    }

    public IndicacaoBandaService(
            IndicacaoBandaRepository indicacaoRepository,
            EdicaoEntrevistaRepository entrevistaRepository,
            Clock clock,
            int maxEnviosPorIp,
            Duration janelaRateLimit) {
        this.indicacaoRepository = indicacaoRepository;
        this.entrevistaRepository = entrevistaRepository;
        this.clock = clock;
        this.maxEnviosPorIp = Math.max(1, maxEnviosPorIp);
        this.janelaRateLimit = janelaRateLimit;
    }

    public record ResumoAdminIndicacoes(
            long total,
            long pendentes,
            long aprovadas,
            long emEntrevista,
            long concluidas,
            long publicadas,
            long recusadas) {
    }

    public static String normalizarChave(String valor) {
        if (valor == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(valor, Normalizer.Form.NFD);
        String withoutDiacritics = COMBINING_DIACRITICS.matcher(decomposed).replaceAll("");
        return MULTIPLE_SPACES.matcher(withoutDiacritics.trim().toLowerCase(Locale.ROOT)).replaceAll(" ");
    }

    public static String normalizarPais(String pais) {
        String chave = normalizarChave(pais);
        if (chave.isEmpty()) {
            return "";
        }
        return SINONIMOS_PAISES.getOrDefault(chave, chave);
    }

    public static String normalizarUrlComparacao(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase(Locale.ROOT) : "";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase(Locale.ROOT) : "";
            int port = uri.getPort();
            String path = uri.getPath() != null ? uri.getPath().replaceAll("/+$", "") : "";
            String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";
            return scheme + "://" + host + (port != -1 ? ":" + port : "") + path + query;
        } catch (URISyntaxException ex) {
            return url.trim().toLowerCase(Locale.ROOT).replaceAll("/+$", "");
        }
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
                        "Limite temporário de envios atingido. Aguarde alguns minutos antes de enviar outra indicação.");
            }
            timestamps.addLast(agora);
        }
    }

    @Transactional(readOnly = true)
    public VerificacaoBandaResponseDTO verificarBanda(String nomeBanda, String pais) {
        String nomeLimpo = nomeBanda != null ? nomeBanda.trim() : "";
        String paisLimpo = pais != null ? pais.trim() : "";
        if (nomeLimpo.length() < 2 || paisLimpo.length() < 2) {
            return VerificacaoBandaResponseDTO.disponivel();
        }

        String nomeNorm = normalizarChave(nomeLimpo);
        String paisNorm = normalizarPais(paisLimpo);

        // Fonte 1: Entrevistas em edições publicadas + indicações marcadas como PUBLICADA
        Set<Integer> numerosEdicoes = new TreeSet<>(Comparator.reverseOrder());
        for (EntrevistaPublicadaRow row : entrevistaRepository.findEntrevistasDeEdicoesPublicadas()) {
            if (normalizarChave(row.banda()).equals(nomeNorm) && normalizarPais(row.pais()).equals(paisNorm)) {
                numerosEdicoes.add(row.numeroEdicao());
            }
        }
        List<IndicacaoBanda> publicadas = indicacaoRepository.findByNomeBandaNormalizadoAndPaisNormalizadoAndStatusIn(
                nomeNorm, paisNorm, List.of(StatusIndicacaoBanda.PUBLICADA));
        for (IndicacaoBanda ind : publicadas) {
            if (ind.getNumeroEdicaoPublicada() != null) {
                numerosEdicoes.add(ind.getNumeroEdicaoPublicada());
            }
        }

        if (!numerosEdicoes.isEmpty()) {
            List<EdicaoReferenciaDTO> edicoes = numerosEdicoes.stream()
                    .map(num -> new EdicaoReferenciaDTO(num, "Edição #" + num, "/edicoes/" + num))
                    .toList();
            String listaPt = numerosEdicoes.stream().map(n -> "Edição #" + n).collect(Collectors.joining(", "));
            String listaEn = numerosEdicoes.stream().map(n -> "Issue #" + n).collect(Collectors.joining(", "));
            return new VerificacaoBandaResponseDTO(
                    "JA_ENTREVISTADA",
                    true,
                    "A banda \"" + nomeLimpo + "\" (" + paisLimpo + ") já foi entrevistada na " + listaPt
                            + " do Armamento Bélico Zine.",
                    "The band \"" + nomeLimpo + "\" (" + paisLimpo + ") was already interviewed in " + listaEn
                            + " of Armamento Bélico Zine.",
                    edicoes,
                    null,
                    null,
                    null);
        }

        // Fonte 2: Fila ativa de indicações (PENDENTE, APROVADA, EM_ENTREVISTA, ENTREVISTA_CONCLUIDA)
        List<IndicacaoBanda> ativas = indicacaoRepository.findByNomeBandaNormalizadoAndPaisNormalizadoAndStatusIn(
                nomeNorm, paisNorm, STATUS_ATIVOS_FILA);
        if (!ativas.isEmpty()) {
            IndicacaoBanda destaque = ativas.stream()
                    .min(Comparator.comparingInt(i -> i.getStatus().getPrioridadeVitrine()))
                    .orElse(ativas.get(0));
            StatusIndicacaoBanda st = destaque.getStatus();
            if (st.isPublico()) {
                return new VerificacaoBandaResponseDTO(
                        "JA_INDICADA",
                        true,
                        "A banda \"" + nomeLimpo + "\" (" + paisLimpo + ") já consta no radar de indicações ("
                                + st.getLabelPublicoPt() + ").",
                        "The band \"" + nomeLimpo + "\" (" + paisLimpo + ") is already on the suggestion radar ("
                                + st.getLabelPublicoEn() + ").",
                        List.of(),
                        st,
                        st.getLabelPublicoPt(),
                        st.getLabelPublicoEn());
            }
            return new VerificacaoBandaResponseDTO(
                    "JA_INDICADA",
                    true,
                    "Já existe uma indicação registrada para a banda \"" + nomeLimpo + "\" (" + paisLimpo
                            + ") aguardando avaliação da curadoria.",
                    "There is already a submitted suggestion for \"" + nomeLimpo + "\" (" + paisLimpo
                            + ") awaiting curation review.",
                    List.of(),
                    StatusIndicacaoBanda.PENDENTE,
                    null,
                    null);
        }

        return VerificacaoBandaResponseDTO.disponivel();
    }

    public IndicacaoBanda cadastrarPublico(IndicacaoPublicFormDTO dto) {
        ValidationUtils.validarCampoObrigatorio(dto, "Formulário de indicação");

        if (dto.getWebsite() != null && !dto.getWebsite().trim().isEmpty()) {
            throw new IllegalArgumentException("Submissão rejeitada.");
        }

        String nomeBanda = validarTamanhoTexto(dto.getBandName(), "Nome da banda", 2, 120);
        String pais = validarTamanhoTexto(dto.getCountry(), "País", 2, 80);
        String regiao = normalizarOpcional(dto.getRegion(), "Estado / Região", 80);
        String tituloMaterial = validarTamanhoTexto(dto.getReleaseTitle(), "Título do material físico", 2, 140);
        String linkAudicao = validarLinkAudicao(dto.getListenUrl());

        String nomeNorm = normalizarChave(nomeBanda);
        String paisNorm = normalizarPais(pais);

        VerificacaoBandaResponseDTO verificacao = verificarBanda(nomeBanda, pais);
        boolean isHomonimo = !"DISPONIVEL".equals(verificacao.situacao());
        String esclarecimento = null;
        String edicaoRef = null;

        if (isHomonimo) {
            if (!dto.isConfirmarHomonimo()) {
                throw new ConflitoIndicacaoBandaException(
                        verificacao.mensagemPt(), verificacao);
            }
            String textoEsclarecimento = dto.getEsclarecimentoHomonimo() != null
                    ? dto.getEsclarecimentoHomonimo().trim()
                    : "";
            if (textoEsclarecimento.length() < 15 || textoEsclarecimento.length() > 500) {
                throw new ConflitoIndicacaoBandaException(
                        "Para indicar uma banda homônima, confirme a caixa de verificação e descreva a diferença entre as bandas (entre 15 e 500 caracteres).",
                        verificacao);
            }

            String linkNorm = normalizarUrlComparacao(linkAudicao);
            List<IndicacaoBanda> existentes = indicacaoRepository.findByNomeBandaNormalizadoAndPaisNormalizadoAndStatusIn(
                    nomeNorm, paisNorm, STATUS_NAO_RECUSADOS);
            boolean mesmoLink = existentes.stream()
                    .anyMatch(existing -> normalizarUrlComparacao(existing.getLinkAudicao()).equals(linkNorm));
            if (mesmoLink) {
                throw new ConflitoIndicacaoBandaException(
                        "O link de audição informado é idêntico ao de uma indicação já registrada para \""
                                + nomeBanda + "\" (" + pais + "). Caso trate-se de outra banda homônima, informe o link próprio dela.",
                        verificacao);
            }

            esclarecimento = textoEsclarecimento;
            if (!verificacao.edicoes().isEmpty()) {
                edicaoRef = verificacao.edicoes().stream()
                        .map(e -> "#" + e.numeroEdicao())
                        .collect(Collectors.joining(", "));
            }
        }

        Instant agora = Instant.now(clock);
        IndicacaoBanda indicacao = new IndicacaoBanda(
                UUID.randomUUID(),
                nomeBanda,
                nomeNorm,
                pais,
                paisNorm,
                regiao,
                tituloMaterial,
                linkAudicao,
                StatusIndicacaoBanda.PENDENTE,
                isHomonimo,
                esclarecimento,
                edicaoRef,
                null,
                null,
                null,
                null,
                null,
                agora,
                agora,
                true);

        return indicacaoRepository.save(indicacao);
    }

    @Transactional(readOnly = true)
    public List<IndicacaoPublicCardDTO> listarPublicas() {
        return indicacaoRepository.findByStatusInOrderByCriadoEmDesc(STATUS_PUBLICOS).stream()
                .sorted(Comparator
                        .comparingInt((IndicacaoBanda i) -> i.getStatus().getPrioridadeVitrine())
                        .thenComparing(IndicacaoBanda::getCriadoEm, Comparator.reverseOrder()))
                .map(IndicacaoPublicCardDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoAdminIndicacoes contarResumoAdmin() {
        long pendentes = indicacaoRepository.countByStatus(StatusIndicacaoBanda.PENDENTE);
        long aprovadas = indicacaoRepository.countByStatus(StatusIndicacaoBanda.APROVADA);
        long emEntrevista = indicacaoRepository.countByStatus(StatusIndicacaoBanda.EM_ENTREVISTA);
        long concluidas = indicacaoRepository.countByStatus(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);
        long publicadas = indicacaoRepository.countByStatus(StatusIndicacaoBanda.PUBLICADA);
        long recusadas = indicacaoRepository.countByStatus(StatusIndicacaoBanda.RECUSADA);
        long total = pendentes + aprovadas + emEntrevista + concluidas + publicadas + recusadas;
        return new ResumoAdminIndicacoes(total, pendentes, aprovadas, emEntrevista, concluidas, publicadas, recusadas);
    }

    @Transactional(readOnly = true)
    public String normalizarFiltroStatusAdmin(String status) {
        if (status == null || status.isBlank() || "TODAS".equalsIgnoreCase(status.trim())) {
            return "TODAS";
        }
        String upper = status.trim().toUpperCase(Locale.ROOT);
        for (StatusIndicacaoBanda st : StatusIndicacaoBanda.values()) {
            if (st.name().equals(upper)) {
                return st.name();
            }
        }
        return "TODAS";
    }

    @Transactional(readOnly = true)
    public List<IndicacaoBanda> listarParaAdmin(String statusFiltro, String busca) {
        String statusNorm = normalizarFiltroStatusAdmin(statusFiltro);
        List<IndicacaoBanda> base = "TODAS".equals(statusNorm)
                ? indicacaoRepository.findAllByOrderByCriadoEmDesc()
                : indicacaoRepository.findByStatusOrderByCriadoEmDesc(StatusIndicacaoBanda.valueOf(statusNorm));

        String termo = normalizarChave(busca);
        String termoPais = normalizarPais(busca);
        if (termo.isEmpty()) {
            return base;
        }
        return base.stream()
                .filter(ind -> ind.getNomeBandaNormalizado().contains(termo)
                        || ind.getPaisNormalizado().contains(termoPais)
                        || normalizarChave(ind.getPais()).contains(termo)
                        || normalizarChave(ind.getTituloMaterialFisico()).contains(termo))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<IndicacaoBanda> buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return indicacaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public IndicacaoAdminFormDTO carregarParaEdicao(UUID id) {
        IndicacaoBanda ind = buscarObrigatoria(id);
        IndicacaoAdminFormDTO form = new IndicacaoAdminFormDTO();
        form.setId(ind.getId());
        form.setNomeBanda(ind.getNomeBanda());
        form.setPais(ind.getPais());
        form.setRegiao(ind.getRegiao());
        form.setTituloMaterialFisico(ind.getTituloMaterialFisico());
        form.setLinkAudicao(ind.getLinkAudicao());
        form.setHomonimo(ind.getHomonimo());
        form.setEsclarecimentoHomonimo(ind.getEsclarecimentoHomonimo());
        form.setEdicaoHomonimoReferencia(ind.getEdicaoHomonimoReferencia());
        form.setMotivoRecusa(ind.getMotivoRecusa());
        form.setStatusAtual(ind.getStatus());
        form.setNumeroEdicaoPublicada(ind.getNumeroEdicaoPublicada());
        return form;
    }

    public IndicacaoBanda aceitar(UUID id) {
        IndicacaoBanda ind = buscarObrigatoria(id);
        validarTransicao(ind, StatusIndicacaoBanda.APROVADA);
        ind.setStatus(StatusIndicacaoBanda.APROVADA);
        ind.setMotivoRecusa(null);
        atualizarAuditoriaModeracao(ind);
        return indicacaoRepository.save(ind);
    }

    public IndicacaoBanda recusar(UUID id, String motivo) {
        IndicacaoBanda ind = buscarObrigatoria(id);
        validarTransicao(ind, StatusIndicacaoBanda.RECUSADA);
        ind.setStatus(StatusIndicacaoBanda.RECUSADA);
        ind.setMotivoRecusa(normalizarOpcional(motivo, "Motivo da recusa", 500));
        atualizarAuditoriaModeracao(ind);
        return indicacaoRepository.save(ind);
    }

    public IndicacaoBanda iniciarEntrevista(UUID id) {
        IndicacaoBanda ind = buscarObrigatoria(id);
        validarTransicao(ind, StatusIndicacaoBanda.EM_ENTREVISTA);
        ind.setStatus(StatusIndicacaoBanda.EM_ENTREVISTA);
        atualizarAuditoriaModeracao(ind);
        return indicacaoRepository.save(ind);
    }

    public IndicacaoBanda concluirEntrevista(UUID id) {
        IndicacaoBanda ind = buscarObrigatoria(id);
        validarTransicao(ind, StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);
        ind.setStatus(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);
        atualizarAuditoriaModeracao(ind);
        return indicacaoRepository.save(ind);
    }

    public IndicacaoBanda reverterEtapa(UUID id, StatusIndicacaoBanda destino) {
        ValidationUtils.validarCampoObrigatorio(destino, "Status de destino");
        IndicacaoBanda ind = buscarObrigatoria(id);
        if (ind.getStatus() == StatusIndicacaoBanda.EM_ENTREVISTA && destino != StatusIndicacaoBanda.APROVADA) {
            throw new IllegalArgumentException("Reversão inválida a partir de Em Entrevista.");
        }
        if (ind.getStatus() == StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA && destino != StatusIndicacaoBanda.EM_ENTREVISTA) {
            throw new IllegalArgumentException("Reversão inválida a partir de Entrevista Concluída.");
        }
        if (ind.getStatus() == StatusIndicacaoBanda.PUBLICADA && destino != StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA) {
            throw new IllegalArgumentException("Reversão inválida a partir de Publicada em Edição.");
        }
        validarTransicao(ind, destino);

        if (ind.getStatus() == StatusIndicacaoBanda.PUBLICADA) {
            ind.setEdicaoPublicadaId(null);
            ind.setNumeroEdicaoPublicada(null);
            ind.setPublicadaEm(null);
        }
        ind.setStatus(destino);
        atualizarAuditoriaModeracao(ind);
        return indicacaoRepository.save(ind);
    }

    public IndicacaoBanda atualizarAdmin(UUID id, IndicacaoAdminFormDTO dto) {
        ValidationUtils.validarCampoObrigatorio(dto, "Dados da indicação");
        IndicacaoBanda ind = buscarObrigatoria(id);

        String nomeBanda = validarTamanhoTexto(dto.getNomeBanda(), "Nome da banda", 2, 120);
        String pais = validarTamanhoTexto(dto.getPais(), "País", 2, 80);
        String regiao = normalizarOpcional(dto.getRegiao(), "Estado / Região", 80);
        String tituloMaterial = validarTamanhoTexto(dto.getTituloMaterialFisico(), "Título do material físico", 2, 140);
        String linkAudicao = validarLinkAudicao(dto.getLinkAudicao());

        ind.setNomeBanda(nomeBanda);
        ind.setNomeBandaNormalizado(normalizarChave(nomeBanda));
        ind.setPais(pais);
        ind.setPaisNormalizado(normalizarPais(pais));
        ind.setRegiao(regiao);
        ind.setTituloMaterialFisico(tituloMaterial);
        ind.setLinkAudicao(linkAudicao);
        ind.setEsclarecimentoHomonimo(normalizarOpcional(dto.getEsclarecimentoHomonimo(), "Esclarecimento de homônimo", 500));
        ind.setMotivoRecusa(normalizarOpcional(dto.getMotivoRecusa(), "Motivo da recusa", 500));
        ind.setAtualizadoEm(Instant.now(clock));
        ind.setNew(false);
        return indicacaoRepository.save(ind);
    }

    public void excluir(UUID id) {
        IndicacaoBanda ind = buscarObrigatoria(id);
        entrevistaRepository.clearIndicacaoBandaId(ind.getId());
        indicacaoRepository.deleteById(ind.getId());
    }

    @Transactional(readOnly = true)
    public List<IndicacaoBanda> listarElegiveisParaEdicao() {
        return indicacaoRepository.findByStatusInOrderByCriadoEmDesc(STATUS_PUBLICOS).stream()
                .sorted(Comparator
                        .comparingInt((IndicacaoBanda i) -> i.getStatus().getPrioridadeVitrine())
                        .thenComparing(IndicacaoBanda::getNomeBanda, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void sincronizarComEdicao(Edicao edicao, List<EdicaoEntrevista> entrevistas) {
        if (edicao == null || edicao.getId() == null) {
            return;
        }
        Instant agora = Instant.now(clock);

        if (Boolean.TRUE.equals(edicao.getPublicado()) && entrevistas != null) {
            Map<UUID, IndicacaoBanda> paraPromover = new LinkedHashMap<>();
            for (EdicaoEntrevista ent : entrevistas) {
                if (ent == null) {
                    continue;
                }
                if (ent.getIndicacaoBandaId() != null) {
                    indicacaoRepository.findById(ent.getIndicacaoBandaId()).ifPresent(ind -> {
                        if (ind.getStatus().isPublico() || ind.getStatus() == StatusIndicacaoBanda.PUBLICADA) {
                            paraPromover.put(ind.getId(), ind);
                        }
                    });
                } else if (ent.getBanda() != null && !ent.getBanda().isBlank()) {
                    String nomeNorm = normalizarChave(ent.getBanda());
                    String paisNorm = normalizarPais(ent.getPais());
                    if (!nomeNorm.isEmpty() && !paisNorm.isEmpty()) {
                        List<IndicacaoBanda> candidatas = indicacaoRepository
                                .findByNomeBandaNormalizadoAndPaisNormalizadoAndStatusIn(
                                        nomeNorm, paisNorm, STATUS_PUBLICOS);
                        if (candidatas.size() == 1) {
                            IndicacaoBanda unica = candidatas.get(0);
                            paraPromover.put(unica.getId(), unica);
                        }
                    }
                }
            }

            for (IndicacaoBanda ind : paraPromover.values()) {
                ind.setStatus(StatusIndicacaoBanda.PUBLICADA);
                ind.setEdicaoPublicadaId(edicao.getId());
                ind.setNumeroEdicaoPublicada(edicao.getNumeroEdicao());
                if (ind.getPublicadaEm() == null) {
                    ind.setPublicadaEm(agora);
                }
                ind.setAtualizadoEm(agora);
                ind.setNew(false);
                indicacaoRepository.save(ind);
            }
        }

        reconciliarVinculadasAEdicao(edicao.getId(), null, agora);
    }

    public void reverterOuReatribuirPorEdicaoExcluida(UUID edicaoId) {
        if (edicaoId == null) {
            return;
        }
        reconciliarVinculadasAEdicao(edicaoId, edicaoId, Instant.now(clock));
    }

    private void reconciliarVinculadasAEdicao(UUID edicaoId, UUID edicaoExcluidaId, Instant agora) {
        List<IndicacaoBanda> vinculadas = indicacaoRepository.findByEdicaoPublicadaId(edicaoId);
        if (vinculadas.isEmpty()) {
            return;
        }

        List<EntrevistaPublicadaRow> entrevistasPublicadas = entrevistaRepository.findEntrevistasDeEdicoesPublicadas();

        for (IndicacaoBanda ind : vinculadas) {
            if (ind.getStatus() != StatusIndicacaoBanda.PUBLICADA) {
                continue;
            }
            Optional<EntrevistaPublicadaRow> outraPublicada = entrevistasPublicadas.stream()
                    .filter(row -> edicaoExcluidaId == null || !edicaoExcluidaId.equals(row.edicaoId()))
                    .filter(row -> ind.getId().equals(row.indicacaoBandaId())
                            || (normalizarChave(row.banda()).equals(ind.getNomeBandaNormalizado())
                                    && normalizarPais(row.pais()).equals(ind.getPaisNormalizado())))
                    .findFirst();

            if (outraPublicada.isPresent()) {
                EntrevistaPublicadaRow row = outraPublicada.get();
                if (!row.edicaoId().equals(ind.getEdicaoPublicadaId())
                        || !row.numeroEdicao().equals(ind.getNumeroEdicaoPublicada())) {
                    ind.setEdicaoPublicadaId(row.edicaoId());
                    ind.setNumeroEdicaoPublicada(row.numeroEdicao());
                    ind.setAtualizadoEm(agora);
                    ind.setNew(false);
                    indicacaoRepository.save(ind);
                }
            } else {
                ind.setStatus(StatusIndicacaoBanda.ENTREVISTA_CONCLUIDA);
                ind.setEdicaoPublicadaId(null);
                ind.setNumeroEdicaoPublicada(null);
                ind.setPublicadaEm(null);
                ind.setAtualizadoEm(agora);
                ind.setNew(false);
                indicacaoRepository.save(ind);
            }
        }
    }

    private IndicacaoBanda buscarObrigatoria(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return indicacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Indicação de banda não encontrada."));
    }

    private void validarTransicao(IndicacaoBanda ind, StatusIndicacaoBanda destino) {
        if (!ind.getStatus().podeTransicionarPara(destino)) {
            throw new IllegalArgumentException(
                    "Transição não permitida de \"" + ind.getStatus().getLabelAdmin()
                            + "\" para \"" + destino.getLabelAdmin() + "\".");
        }
    }

    private void atualizarAuditoriaModeracao(IndicacaoBanda ind) {
        Instant agora = Instant.now(clock);
        ind.setModeradoEm(agora);
        ind.setAtualizadoEm(agora);
        ind.setNew(false);
    }

    private static String validarTamanhoTexto(String valor, String nomeCampo, int min, int max) {
        ValidationUtils.validarCampoStringObrigatorio(valor, nomeCampo);
        String limpo = valor.trim();
        if (limpo.length() < min || limpo.length() > max) {
            throw new IllegalArgumentException(
                    nomeCampo + " deve ter entre " + min + " e " + max + " caracteres.");
        }
        return limpo;
    }

    private static String normalizarOpcional(String valor, String nomeCampo, int max) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        if (limpo.isEmpty()) {
            return null;
        }
        if (limpo.length() > max) {
            throw new IllegalArgumentException(nomeCampo + " deve ter no máximo " + max + " caracteres.");
        }
        return limpo;
    }

    private static String validarLinkAudicao(String url) {
        ValidationUtils.validarCampoStringObrigatorio(url, "Link para ouvir a banda");
        String limpo = url.trim();
        if (limpo.length() > 500) {
            throw new IllegalArgumentException("Link para ouvir a banda deve ter no máximo 500 caracteres.");
        }
        try {
            URI uri = new URI(limpo);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null
                    || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                    || host == null || host.isBlank()) {
                throw new IllegalArgumentException("Informe uma URL válida iniciando com http:// ou https://.");
            }
            return limpo;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Informe uma URL válida iniciando com http:// ou https://.");
        }
    }
}
