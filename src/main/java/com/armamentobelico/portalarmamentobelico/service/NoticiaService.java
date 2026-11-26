package com.armamentobelico.portalarmamentobelico.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.armamentobelico.portalarmamentobelico.dto.EdicaoCatalogoItemDTO;
import com.armamentobelico.portalarmamentobelico.dto.NoticiaDetalheViewDTO;
import com.armamentobelico.portalarmamentobelico.dto.NoticiaFeedItemDTO;
import com.armamentobelico.portalarmamentobelico.dto.NoticiaFormDTO;
import com.armamentobelico.portalarmamentobelico.dto.NoticiaPaginaFeedDTO;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EstiloTag;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.Noticia;
import com.armamentobelico.portalarmamentobelico.model.NoticiaTag;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.repository.NoticiaRepository;
import com.armamentobelico.portalarmamentobelico.repository.NoticiaTagRepository;
import com.armamentobelico.portalarmamentobelico.utils.PublicJsonUtils;
import com.armamentobelico.portalarmamentobelico.utils.SlugUtils;
import com.armamentobelico.portalarmamentobelico.utils.ValidationUtils;

@Service
@Transactional
public class NoticiaService {

    public static final int MAX_TAGS = 8;
    public static final int TAMANHO_PAGINA_FEED = 10;
    public static final String AUTOR_PADRAO = "Editoria Armamento Bélico";

    private final NoticiaRepository noticiaRepository;
    private final NoticiaTagRepository noticiaTagRepository;
    private final EdicaoRepository edicaoRepository;
    private final HtmlSanitizerService htmlSanitizerService;
    private final FileUploadService fileUploadService;

    public NoticiaService(
            NoticiaRepository noticiaRepository,
            NoticiaTagRepository noticiaTagRepository,
            EdicaoRepository edicaoRepository,
            HtmlSanitizerService htmlSanitizerService,
            FileUploadService fileUploadService) {
        this.noticiaRepository = noticiaRepository;
        this.noticiaTagRepository = noticiaTagRepository;
        this.edicaoRepository = edicaoRepository;
        this.htmlSanitizerService = htmlSanitizerService;
        this.fileUploadService = fileUploadService;
    }

    public record ResumoAdminNoticias(
            long total,
            long totalPublicadas,
            long totalRascunhos,
            long totalComEn) {
    }

    @Transactional(readOnly = true)
    public ResumoAdminNoticias contarResumoAdmin() {
        List<Noticia> todas = noticiaRepository.findAll();
        long total = todas.size();
        long publicadas = todas.stream().filter(n -> Boolean.TRUE.equals(n.getPublicado())).count();
        long rascunhos = total - publicadas;
        long comEn = todas.stream().filter(Noticia::temVersaoEn).count();
        return new ResumoAdminNoticias(total, publicadas, rascunhos, comEn);
    }

    @Transactional(readOnly = true)
    public List<Noticia> listarAdmin(String busca, String status, String idioma) {
        List<Noticia> todas = noticiaRepository.findAllByOrderByDataPublicacaoDescCriadoEmDesc();
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);
        String statusNorm = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        String idiomaNorm = idioma == null ? "" : idioma.trim().toUpperCase(Locale.ROOT);

        return todas.stream()
                .filter(n -> switch (statusNorm) {
                    case "PUBLICADA" -> Boolean.TRUE.equals(n.getPublicado());
                    case "RASCUNHO" -> !Boolean.TRUE.equals(n.getPublicado());
                    default -> true;
                })
                .filter(n -> switch (idiomaNorm) {
                    case "EN", "BILINGUE" -> n.temVersaoEn();
                    case "PT", "SOMENTE_PT" -> !n.temVersaoEn();
                    default -> true;
                })
                .filter(n -> termo.isEmpty()
                        || contem(n.getTituloPt(), termo)
                        || contem(n.getTituloEn(), termo)
                        || contem(n.getSlug(), termo)
                        || contem(n.getCategoriaPt(), termo)
                        || contem(n.getCategoriaEn(), termo))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<UUID, Edicao> mapearEdicoesVinculadas(List<Noticia> noticias) {
        Map<UUID, Edicao> mapa = new HashMap<>();
        if (noticias == null || noticias.isEmpty()) {
            return mapa;
        }
        Set<UUID> ids = new LinkedHashSet<>();
        for (Noticia n : noticias) {
            if (n.getEdicaoId() != null) {
                ids.add(n.getEdicaoId());
            }
        }
        if (ids.isEmpty()) {
            return mapa;
        }
        for (Edicao edicao : edicaoRepository.findAllById(ids)) {
            mapa.put(edicao.getId(), edicao);
        }
        return mapa;
    }

    @Transactional(readOnly = true)
    public List<Edicao> listarEdicoesParaVinculo() {
        return edicaoRepository.findAllByOrderByNumeroEdicaoDescIdiomaAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Noticia> buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return noticiaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public NoticiaFormDTO carregarParaEdicao(UUID id) {
        Noticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notícia não encontrada."));

        NoticiaFormDTO form = new NoticiaFormDTO();
        form.setId(noticia.getId());
        form.setSlug(noticia.getSlug());
        form.setTituloPt(noticia.getTituloPt());
        form.setTituloEn(noticia.getTituloEn());
        form.setTagDestaquePt(noticia.getTagDestaquePt());
        form.setTagDestaqueEn(noticia.getTagDestaqueEn());
        form.setCategoriaPt(noticia.getCategoriaPt());
        form.setCategoriaEn(noticia.getCategoriaEn());
        form.setAutor(noticia.getAutor());
        form.setDataPublicacao(noticia.getDataPublicacao());
        form.setResumoPt(noticia.getResumoPt());
        form.setResumoEn(noticia.getResumoEn());
        form.setConteudoHtmlPt(noticia.getConteudoHtmlPt());
        form.setConteudoHtmlEn(noticia.getConteudoHtmlEn());
        form.setImagemAtualPath(noticia.getImagemDestaquePath());
        form.setLegendaImagemPt(noticia.getLegendaImagemPt());
        form.setLegendaImagemEn(noticia.getLegendaImagemEn());
        form.setEdicaoId(noticia.getEdicaoId());
        form.setCtaTextoPt(noticia.getCtaTextoPt());
        form.setCtaTextoEn(noticia.getCtaTextoEn());
        form.setPublicado(Boolean.TRUE.equals(noticia.getPublicado()));

        List<NoticiaFormDTO.TagFormItem> tags = new ArrayList<>();
        for (NoticiaTag tag : noticiaTagRepository.findByNoticiaIdOrderByOrdemAsc(id)) {
            tags.add(new NoticiaFormDTO.TagFormItem(tag.getNome(), tag.getEstilo()));
        }
        form.setTags(tags);
        return form;
    }

    public Noticia criar(NoticiaFormDTO form) {
        ValidationUtils.validarCampoObrigatorio(form, "Formulário da notícia");
        String htmlPtLimpo = validarESanitizarFormulario(form, null);
        String htmlEnLimpo = sanitizarOpcional(form.getConteudoHtmlEn());
        String slug = resolverEValidarSlug(form.getSlug(), form.getTituloPt(), null);

        String imagemPath = null;
        if (arquivoEnviado(form.getImagemDestaque())) {
            imagemPath = fileUploadService.salvarImagem(form.getImagemDestaque());
        }

        Instant agora = Instant.now();
        Noticia noticia = new Noticia();
        noticia.setId(UUID.randomUUID());
        noticia.setSlug(slug);
        aplicarCampos(noticia, form, htmlPtLimpo, htmlEnLimpo);
        noticia.setImagemDestaquePath(imagemPath);
        noticia.setCriadoEm(agora);
        noticia.setAtualizadoEm(agora);
        noticia.setNew(true);

        Noticia salva = noticiaRepository.save(noticia);
        salvarTags(salva.getId(), form.getTags());
        return salva;
    }

    public Noticia atualizar(UUID id, NoticiaFormDTO form) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        ValidationUtils.validarCampoObrigatorio(form, "Formulário da notícia");

        Noticia existente = noticiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notícia não encontrada."));

        String htmlPtLimpo = validarESanitizarFormulario(form, id);
        String htmlEnLimpo = sanitizarOpcional(form.getConteudoHtmlEn());
        String slug = resolverEValidarSlug(form.getSlug(), form.getTituloPt(), id);

        String imagemAnterior = existente.getImagemDestaquePath();
        String novaImagemPath = imagemAnterior;

        if (arquivoEnviado(form.getImagemDestaque())) {
            novaImagemPath = fileUploadService.salvarImagem(form.getImagemDestaque());
            if (imagemAnterior != null && !imagemAnterior.isBlank()) {
                fileUploadService.removerImagem(imagemAnterior);
            }
        } else if (form.isRemoverImagemAtual()) {
            if (imagemAnterior != null && !imagemAnterior.isBlank()) {
                fileUploadService.removerImagem(imagemAnterior);
            }
            novaImagemPath = null;
        }

        existente.setSlug(slug);
        aplicarCampos(existente, form, htmlPtLimpo, htmlEnLimpo);
        existente.setImagemDestaquePath(novaImagemPath);
        existente.setAtualizadoEm(Instant.now());
        existente.setNew(false);

        Noticia salva = noticiaRepository.save(existente);
        noticiaTagRepository.deleteByNoticiaId(id);
        salvarTags(id, form.getTags());
        return salva;
    }

    public void excluir(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        Noticia existente = noticiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notícia não encontrada."));

        String imagemPath = existente.getImagemDestaquePath();
        noticiaRepository.deleteById(id);

        if (imagemPath != null && !imagemPath.isBlank()) {
            fileUploadService.removerImagem(imagemPath);
        }
    }

    @Transactional(readOnly = true)
    public List<Noticia> listarUltimasPublicadasHome() {
        return noticiaRepository.findUltimasDuasPublicadas();
    }

    @Transactional(readOnly = true)
    public NoticiaPaginaFeedDTO listarFeedPublico(String tag, String busca, int pagina) {
        String tagLimpa = trimOuNull(tag);
        String buscaLimpa = trimOuNull(busca);

        List<Noticia> base = tagLimpa != null
                ? noticiaRepository.findPublicadasPorTag(tagLimpa)
                : noticiaRepository.findByPublicadoTrueOrderByDataPublicacaoDescCriadoEmDesc();

        if (base.isEmpty()) {
            return new NoticiaPaginaFeedDTO(List.of(), 1, 1, 0, tagLimpa, buscaLimpa);
        }

        Map<UUID, List<NoticiaTag>> tagsPorNoticia = carregarTagsEmLote(base);
        List<NoticiaFeedItemDTO> filtradas = new ArrayList<>();
        String termo = buscaLimpa == null ? "" : buscaLimpa.toLowerCase(Locale.ROOT);

        for (Noticia n : base) {
            List<NoticiaTag> tags = tagsPorNoticia.getOrDefault(n.getId(), List.of());
            if (termo.isEmpty() || correspondeBuscaPublica(n, tags, termo)) {
                filtradas.add(new NoticiaFeedItemDTO(n, tags));
            }
        }

        long totalItens = filtradas.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) totalItens / TAMANHO_PAGINA_FEED));
        int paginaAtual = Math.max(1, Math.min(pagina, totalPaginas));

        int inicio = (paginaAtual - 1) * TAMANHO_PAGINA_FEED;
        int fim = Math.min(inicio + TAMANHO_PAGINA_FEED, filtradas.size());
        List<NoticiaFeedItemDTO> itensPagina = inicio < filtradas.size()
                ? filtradas.subList(inicio, fim)
                : List.of();

        return new NoticiaPaginaFeedDTO(itensPagina, paginaAtual, totalPaginas, totalItens, tagLimpa, buscaLimpa);
    }

    @Transactional(readOnly = true)
    public Optional<NoticiaDetalheViewDTO> buscarDetalhesPublicos(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        Optional<Noticia> opt = noticiaRepository.findBySlugAndPublicadoTrue(slug.trim());
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        Noticia noticia = opt.get();
        List<NoticiaTag> tags = noticiaTagRepository.findByNoticiaIdOrderByOrdemAsc(noticia.getId());

        Edicao edicaoPt = null;
        Edicao edicaoEn = null;

        if (noticia.getEdicaoId() != null) {
            Optional<Edicao> vinculadaOpt = edicaoRepository.findById(noticia.getEdicaoId())
                    .filter(e -> Boolean.TRUE.equals(e.getPublicado()));
            if (vinculadaOpt.isPresent()) {
                Edicao vinculada = vinculadaOpt.get();
                if (vinculada.getIdioma() == IdiomaEdicao.PT) {
                    edicaoPt = vinculada;
                    edicaoEn = edicaoRepository
                            .findByNumeroEdicaoAndIdiomaAndPublicadoTrue(vinculada.getNumeroEdicao(), IdiomaEdicao.EN)
                            .orElse(vinculada);
                } else {
                    edicaoEn = vinculada;
                    edicaoPt = edicaoRepository
                            .findByNumeroEdicaoAndIdiomaAndPublicadoTrue(vinculada.getNumeroEdicao(), IdiomaEdicao.PT)
                            .orElse(vinculada);
                }
            }
        }

        String linkedCatalogJson = serializarEdicoesVinculadasParaCarrinho(edicaoPt, edicaoEn);
        return Optional.of(new NoticiaDetalheViewDTO(noticia, tags, edicaoPt, edicaoEn, linkedCatalogJson));
    }

    private String validarESanitizarFormulario(NoticiaFormDTO form, UUID idAtual) {
        ValidationUtils.validarCampoStringObrigatorio(form.getTituloPt(), "Título (PT)");
        ValidationUtils.validarCampoStringObrigatorio(form.getCategoriaPt(), "Categoria (PT)");
        ValidationUtils.validarCampoStringObrigatorio(form.getResumoPt(), "Resumo (PT)");
        ValidationUtils.validarCampoObrigatorio(form.getDataPublicacao(), "Data de publicação");

        if (!htmlSanitizerService.temTextoVisivel(form.getConteudoHtmlPt())) {
            throw new IllegalArgumentException("Conteúdo da notícia (PT) é obrigatório e não pode estar vazio.");
        }

        boolean algumCampoEn = naoVazio(form.getTituloEn())
                || naoVazio(form.getTagDestaqueEn())
                || naoVazio(form.getCategoriaEn())
                || naoVazio(form.getResumoEn())
                || htmlSanitizerService.temTextoVisivel(form.getConteudoHtmlEn())
                || naoVazio(form.getLegendaImagemEn())
                || naoVazio(form.getCtaTextoEn());

        if (algumCampoEn) {
            if (!naoVazio(form.getTituloEn())
                    || !naoVazio(form.getResumoEn())
                    || !htmlSanitizerService.temTextoVisivel(form.getConteudoHtmlEn())) {
                throw new IllegalArgumentException(
                        "Para publicar a versão em inglês, preencha Título (EN), Resumo (EN) e Conteúdo (EN).");
            }
        }

        if (form.getEdicaoId() != null && !edicaoRepository.existsById(form.getEdicaoId())) {
            throw new IllegalArgumentException("A edição vinculada informada não foi encontrada.");
        }

        return htmlSanitizerService.sanitizar(form.getConteudoHtmlPt());
    }

    private String sanitizarOpcional(String html) {
        if (!htmlSanitizerService.temTextoVisivel(html)) {
            return null;
        }
        return htmlSanitizerService.sanitizar(html);
    }

    private String resolverEValidarSlug(String slugInformado, String tituloPt, UUID idAtual) {
        String origem = naoVazio(slugInformado) ? slugInformado : tituloPt;
        String slug = SlugUtils.gerarSlug(origem);
        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Não foi possível gerar um slug válido. Informe letras ou números.");
        }

        boolean duplicado = idAtual == null
                ? noticiaRepository.existsBySlug(slug)
                : noticiaRepository.existsBySlugAndIdNot(slug, idAtual);
        if (duplicado) {
            throw new IllegalArgumentException("Já existe outra notícia cadastrada com o slug '" + slug + "'.");
        }
        return slug;
    }

    private void aplicarCampos(Noticia noticia, NoticiaFormDTO form, String htmlPtLimpo, String htmlEnLimpo) {
        noticia.setTituloPt(form.getTituloPt().trim());
        noticia.setTituloEn(trimOuNull(form.getTituloEn()));
        noticia.setTagDestaquePt(trimOuNull(form.getTagDestaquePt()));
        noticia.setTagDestaqueEn(trimOuNull(form.getTagDestaqueEn()));
        noticia.setCategoriaPt(form.getCategoriaPt().trim());
        noticia.setCategoriaEn(trimOuNull(form.getCategoriaEn()));
        noticia.setAutor(naoVazio(form.getAutor()) ? form.getAutor().trim() : AUTOR_PADRAO);
        noticia.setDataPublicacao(form.getDataPublicacao() != null ? form.getDataPublicacao() : LocalDate.now());
        noticia.setResumoPt(form.getResumoPt().trim());
        noticia.setResumoEn(trimOuNull(form.getResumoEn()));
        noticia.setConteudoHtmlPt(htmlPtLimpo);
        noticia.setConteudoHtmlEn(htmlEnLimpo);
        noticia.setLegendaImagemPt(trimOuNull(form.getLegendaImagemPt()));
        noticia.setLegendaImagemEn(trimOuNull(form.getLegendaImagemEn()));
        noticia.setEdicaoId(form.getEdicaoId());
        noticia.setCtaTextoPt(trimOuNull(form.getCtaTextoPt()));
        noticia.setCtaTextoEn(trimOuNull(form.getCtaTextoEn()));
        noticia.setPublicado(Boolean.TRUE.equals(form.getPublicado()));
    }

    private void salvarTags(UUID noticiaId, List<NoticiaFormDTO.TagFormItem> itens) {
        if (itens == null || itens.isEmpty()) {
            return;
        }
        Set<String> vistas = new LinkedHashSet<>();
        int ordem = 1;
        for (NoticiaFormDTO.TagFormItem item : itens) {
            if (ordem > MAX_TAGS) {
                break;
            }
            if (item == null || !naoVazio(item.getNome())) {
                continue;
            }
            String nomeLimpo = item.getNome().trim().replaceAll("\\s+", " ");
            String chave = nomeLimpo.toLowerCase(Locale.ROOT);
            if (!vistas.add(chave)) {
                continue;
            }
            NoticiaTag tag = new NoticiaTag();
            tag.setId(UUID.randomUUID());
            tag.setNoticiaId(noticiaId);
            tag.setNome(nomeLimpo);
            tag.setEstilo(item.getEstilo() != null ? item.getEstilo() : EstiloTag.DARK);
            tag.setOrdem(ordem++);
            tag.setNew(true);
            noticiaTagRepository.save(tag);
        }
    }

    private Map<UUID, List<NoticiaTag>> carregarTagsEmLote(List<Noticia> noticias) {
        List<UUID> ids = noticias.stream().map(Noticia::getId).toList();
        Map<UUID, List<NoticiaTag>> mapa = new HashMap<>();
        if (ids.isEmpty()) {
            return mapa;
        }
        for (NoticiaTag tag : noticiaTagRepository.findByNoticiaIdInOrderByOrdemAsc(ids)) {
            mapa.computeIfAbsent(tag.getNoticiaId(), k -> new ArrayList<>()).add(tag);
        }
        return mapa;
    }

    private boolean correspondeBuscaPublica(Noticia n, List<NoticiaTag> tags, String termo) {
        if (contem(n.getTituloPt(), termo)
                || contem(n.getTituloEn(), termo)
                || contem(n.getResumoPt(), termo)
                || contem(n.getResumoEn(), termo)
                || contem(n.getCategoriaPt(), termo)
                || contem(n.getCategoriaEn(), termo)
                || contem(n.getTagDestaquePt(), termo)
                || contem(n.getTagDestaqueEn(), termo)) {
            return true;
        }
        for (NoticiaTag tag : tags) {
            if (contem(tag.getNome(), termo)) {
                return true;
            }
        }
        return false;
    }

    private String serializarEdicoesVinculadasParaCarrinho(Edicao edicaoPt, Edicao edicaoEn) {
        Map<String, List<EdicaoCatalogoItemDTO>> catalogo = new LinkedHashMap<>();
        List<EdicaoCatalogoItemDTO> ptList = new ArrayList<>();
        List<EdicaoCatalogoItemDTO> enList = new ArrayList<>();

        if (edicaoPt != null) {
            ptList.add(toCatalogoItem(edicaoPt));
        }
        if (edicaoEn != null) {
            enList.add(toCatalogoItem(edicaoEn));
        }

        catalogo.put("pt", ptList);
        catalogo.put("en", enList);
        return PublicJsonUtils.serializarCatalogoPorIdioma(catalogo);
    }

    private EdicaoCatalogoItemDTO toCatalogoItem(Edicao e) {
        String date = (e.getMesLancamento() != null ? e.getMesLancamento() : "")
                + (e.getAnoLancamento() != null ? " / " + e.getAnoLancamento() : "");
        return new EdicaoCatalogoItemDTO(
                e.getId(),
                e.getNumeroEdicao(),
                e.getIdioma().getSlug(),
                e.getUrlPublica(),
                e.getTitulo(),
                e.getSubtitulo(),
                e.getPreco(),
                e.getMoeda().getSimbolo(),
                "/uploads/" + e.getCapaPath(),
                date.trim(),
                "",
                e.getStatusEstoque().isInStock(),
                e.getStatusEstoque().name().toLowerCase(Locale.ROOT),
                e.getPaginas());
    }

    private static boolean arquivoEnviado(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private static boolean naoVazio(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private static String trimOuNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean contem(String campo, String termoLower) {
        return campo != null && campo.toLowerCase(Locale.ROOT).contains(termoLower);
    }
}
