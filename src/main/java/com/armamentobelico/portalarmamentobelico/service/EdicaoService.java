package com.armamentobelico.portalarmamentobelico.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.armamentobelico.portalarmamentobelico.dto.EdicaoCatalogoItemDTO;
import com.armamentobelico.portalarmamentobelico.dto.EdicaoDetalhesDTO;
import com.armamentobelico.portalarmamentobelico.dto.EdicaoFormDTO;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EdicaoConteudoExtra;
import com.armamentobelico.portalarmamentobelico.model.EdicaoEntrevista;
import com.armamentobelico.portalarmamentobelico.model.EdicaoFoto;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoConteudoExtraRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoEntrevistaRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoFotoRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.utils.ValidationUtils;

@Service
@Transactional
public class EdicaoService {

    public static final int MAX_FOTOS_EXTRAS = 5;

    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|embed/|shorts/)|youtu\\.be/)([A-Za-z0-9_-]{11})");

    private final EdicaoRepository edicaoRepository;
    private final EdicaoFotoRepository fotoRepository;
    private final EdicaoEntrevistaRepository entrevistaRepository;
    private final EdicaoConteudoExtraRepository conteudoExtraRepository;
    private final FileUploadService fileUploadService;
    private final IndicacaoBandaService indicacaoBandaService;

    public EdicaoService(
            EdicaoRepository edicaoRepository,
            EdicaoFotoRepository fotoRepository,
            EdicaoEntrevistaRepository entrevistaRepository,
            EdicaoConteudoExtraRepository conteudoExtraRepository,
            FileUploadService fileUploadService,
            IndicacaoBandaService indicacaoBandaService) {
        this.edicaoRepository = edicaoRepository;
        this.fotoRepository = fotoRepository;
        this.entrevistaRepository = entrevistaRepository;
        this.conteudoExtraRepository = conteudoExtraRepository;
        this.fileUploadService = fileUploadService;
        this.indicacaoBandaService = indicacaoBandaService;
    }

    public record ResumoAdminEdicoes(
            long total,
            long totalPublicadas,
            long totalPt,
            long totalEn,
            long totalEsgotadas,
            long totalRascunhos) {
    }

    @Transactional(readOnly = true)
    public List<Edicao> listarAdmin(String busca, IdiomaEdicao idioma, StatusEstoque statusEstoque) {
        List<Edicao> todas = edicaoRepository.findAllByOrderByNumeroEdicaoDescIdiomaAsc();
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);

        return todas.stream()
                .filter(e -> idioma == null || e.getIdioma() == idioma)
                .filter(e -> statusEstoque == null || e.getStatusEstoque() == statusEstoque)
                .filter(e -> termo.isEmpty()
                        || String.valueOf(e.getNumeroEdicao()).contains(termo)
                        || (e.getTitulo() != null && e.getTitulo().toLowerCase(Locale.ROOT).contains(termo))
                        || (e.getSubtitulo() != null && e.getSubtitulo().toLowerCase(Locale.ROOT).contains(termo)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Integer, Set<IdiomaEdicao>> buscarIdiomasPorNumero() {
        Map<Integer, Set<IdiomaEdicao>> mapa = new HashMap<>();
        for (Edicao edicao : edicaoRepository.findAll()) {
            mapa.computeIfAbsent(edicao.getNumeroEdicao(), k -> EnumSet.noneOf(IdiomaEdicao.class))
                    .add(edicao.getIdioma());
        }
        return mapa;
    }

    @Transactional(readOnly = true)
    public ResumoAdminEdicoes contarResumoAdmin() {
        List<Edicao> todas = edicaoRepository.findAll();
        long total = todas.size();
        long publicadas = todas.stream().filter(e -> Boolean.TRUE.equals(e.getPublicado())).count();
        long pt = todas.stream().filter(e -> e.getIdioma() == IdiomaEdicao.PT).count();
        long en = todas.stream().filter(e -> e.getIdioma() == IdiomaEdicao.EN).count();
        long esgotadas = todas.stream().filter(e -> e.getStatusEstoque() == StatusEstoque.SOLDOUT).count();
        long rascunhos = total - publicadas;
        return new ResumoAdminEdicoes(total, publicadas, pt, en, esgotadas, rascunhos);
    }

    @Transactional(readOnly = true)
    public Optional<Edicao> buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return edicaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public EdicaoFormDTO carregarParaEdicao(UUID id) {
        Edicao edicao = edicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Edição não encontrada."));

        EdicaoFormDTO form = new EdicaoFormDTO();
        form.setId(edicao.getId());
        form.setNumeroEdicao(edicao.getNumeroEdicao());
        form.setIdioma(edicao.getIdioma());
        form.setTitulo(edicao.getTitulo());
        form.setSubtitulo(edicao.getSubtitulo());
        form.setMesLancamento(edicao.getMesLancamento());
        form.setAnoLancamento(edicao.getAnoLancamento());
        form.setAnoRevista(edicao.getAnoRevista());
        form.setPaginas(edicao.getPaginas());
        form.setPreco(edicao.getPreco());
        form.setStatusEstoque(edicao.getStatusEstoque());
        form.setQuantidadeEstoque(edicao.getQuantidadeEstoque());
        form.setPrazoPostagem(edicao.getPrazoPostagem());
        form.setDescricao(edicao.getDescricao());
        form.setFormato(edicao.getFormato());
        form.setPapelImpressao(edicao.getPapelImpressao());
        form.setTiragem(edicao.getTiragem());
        form.setYoutubeUrl(edicao.getYoutubeUrl());
        form.setPublicado(edicao.getPublicado());
        form.setCapaAtualPath(edicao.getCapaPath());

        List<EdicaoFormDTO.FotoExistenteItem> fotos = fotoRepository.findByEdicaoIdOrderByOrdemAsc(id).stream()
                .map(f -> new EdicaoFormDTO.FotoExistenteItem(
                        f.getId(), f.getArquivoPath(), f.getLegenda(), f.getOrdem(), false))
                .toList();
        form.setFotosExistentes(new ArrayList<>(fotos));

        List<EdicaoFormDTO.EntrevistaItem> entrevistas = entrevistaRepository.findByEdicaoIdOrderByOrdemAsc(id).stream()
                .map(ent -> new EdicaoFormDTO.EntrevistaItem(
                        ent.getBanda(), ent.getPais(), ent.getEstilo(), ent.getIndicacaoBandaId()))
                .toList();
        form.setEntrevistas(new ArrayList<>(entrevistas));

        List<String> extras = conteudoExtraRepository.findByEdicaoIdOrderByOrdemAsc(id).stream()
                .map(EdicaoConteudoExtra::getDescricao)
                .toList();
        form.setConteudosExtras(new ArrayList<>(extras));

        return form;
    }

    @Transactional(readOnly = true)
    public EdicaoFormDTO prepararFormularioVersaoComplementar(UUID origemId) {
        ValidationUtils.validarCampoObrigatorio(origemId, "origemId");
        Edicao origem = edicaoRepository.findById(origemId)
                .orElseThrow(() -> new IllegalArgumentException("Edição de origem não encontrada."));

        IdiomaEdicao idiomaComplementar = origem.getIdioma().getComplementar();
        if (edicaoRepository.existsByNumeroEdicaoAndIdioma(origem.getNumeroEdicao(), idiomaComplementar)) {
            throw new IllegalArgumentException(
                    "Já existe a Edição #" + origem.getNumeroEdicao() + " cadastrada em "
                            + idiomaComplementar.getLabelAdmin() + ".");
        }

        EdicaoFormDTO form = new EdicaoFormDTO();
        form.setOrigemId(origem.getId());
        form.setCopiarImagensOrigem(true);
        form.setNumeroEdicao(origem.getNumeroEdicao());
        form.setIdioma(idiomaComplementar);
        form.setAnoLancamento(origem.getAnoLancamento());
        form.setPaginas(origem.getPaginas());
        form.setFormato(origem.getFormato());
        form.setYoutubeUrl(origem.getYoutubeUrl());
        form.setStatusEstoque(StatusEstoque.AVAILABLE);
        form.setQuantidadeEstoque(0);
        form.setPublicado(Boolean.TRUE);
        form.setPreco(null);
        form.setCapaAtualPath(origem.getCapaPath());

        List<EdicaoFormDTO.FotoExistenteItem> fotosOrigem = fotoRepository.findByEdicaoIdOrderByOrdemAsc(origem.getId())
                .stream()
                .map(f -> new EdicaoFormDTO.FotoExistenteItem(
                        f.getId(), f.getArquivoPath(), f.getLegenda(), f.getOrdem(), false))
                .toList();
        form.setFotosExistentes(new ArrayList<>(fotosOrigem));

        List<EdicaoFormDTO.EntrevistaItem> entrevistas = entrevistaRepository
                .findByEdicaoIdOrderByOrdemAsc(origem.getId()).stream()
                .map(ent -> new EdicaoFormDTO.EntrevistaItem(
                        ent.getBanda(), ent.getPais(), ent.getEstilo(), ent.getIndicacaoBandaId()))
                .toList();
        form.setEntrevistas(new ArrayList<>(entrevistas));

        return form;
    }

    public Edicao criar(EdicaoFormDTO form) {
        validarCamposComuns(form, null);

        Edicao origem = null;
        if (form.getOrigemId() != null) {
            origem = edicaoRepository.findById(form.getOrigemId())
                    .orElseThrow(() -> new IllegalArgumentException("Edição de origem não encontrada."));
        }

        List<String> arquivosCriados = new ArrayList<>();
        try {
            String capaPath;
            if (hasUploadedFile(form.getCapa())) {
                capaPath = fileUploadService.salvarImagem(form.getCapa());
                arquivosCriados.add(capaPath);
            } else if (origem != null && form.isCopiarImagensOrigem()) {
                capaPath = fileUploadService.duplicarImagem(origem.getCapaPath());
                arquivosCriados.add(capaPath);
            } else {
                throw new IllegalArgumentException("A imagem de capa é obrigatória.");
            }

            List<FotoParaPersistir> fotosParaSalvar = montarFotosParaCriacao(form, origem, arquivosCriados);
            if (fotosParaSalvar.size() > MAX_FOTOS_EXTRAS) {
                throw new IllegalArgumentException("Máximo de " + MAX_FOTOS_EXTRAS + " fotos extras permitidas.");
            }

            Instant agora = Instant.now();
            Edicao edicao = new Edicao();
            edicao.setId(UUID.randomUUID());
            aplicarCamposFormulario(edicao, form);
            edicao.setCapaPath(capaPath);
            edicao.setCriadoEm(agora);
            edicao.setAtualizadoEm(agora);
            edicao.setNew(true);

            Edicao salva = edicaoRepository.save(edicao);
            salvarFotos(salva.getId(), fotosParaSalvar);
            List<EdicaoEntrevista> entrevistasSalvas = salvarEntrevistas(salva.getId(), form.getEntrevistas());
            salvarConteudosExtras(salva.getId(), form.getConteudosExtras());
            indicacaoBandaService.sincronizarComEdicao(salva, entrevistasSalvas);
            return salva;
        } catch (RuntimeException ex) {
            limparArquivosSilenciosamente(arquivosCriados);
            throw ex;
        }
    }

    public Edicao atualizar(UUID id, EdicaoFormDTO form) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        Edicao existente = edicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Edição não encontrada."));

        validarCamposComuns(form, id);

        List<EdicaoFoto> fotosAtuaisBanco = fotoRepository.findByEdicaoIdOrderByOrdemAsc(id);
        Map<UUID, EdicaoFoto> mapaFotosBanco = new HashMap<>();
        for (EdicaoFoto foto : fotosAtuaisBanco) {
            mapaFotosBanco.put(foto.getId(), foto);
        }

        List<String> arquivosCriados = new ArrayList<>();
        List<String> arquivosParaRemoverAposSalvar = new ArrayList<>();

        try {
            String novaCapaPath = existente.getCapaPath();
            if (hasUploadedFile(form.getCapa())) {
                novaCapaPath = fileUploadService.salvarImagem(form.getCapa());
                arquivosCriados.add(novaCapaPath);
                if (existente.getCapaPath() != null && !existente.getCapaPath().isBlank()) {
                    arquivosParaRemoverAposSalvar.add(existente.getCapaPath());
                }
            }

            List<FotoParaPersistir> fotosMantidas = new ArrayList<>();
            if (form.getFotosExistentes() != null && !form.getFotosExistentes().isEmpty()) {
                for (EdicaoFormDTO.FotoExistenteItem item : form.getFotosExistentes()) {
                    if (item == null || item.getId() == null) {
                        continue;
                    }
                    EdicaoFoto fotoBanco = mapaFotosBanco.get(item.getId());
                    if (fotoBanco == null) {
                        continue;
                    }
                    if (item.isRemover()) {
                        arquivosParaRemoverAposSalvar.add(fotoBanco.getArquivoPath());
                    } else {
                        fotosMantidas.add(new FotoParaPersistir(
                                fotoBanco.getArquivoPath(),
                                normalizarTextoOpcional(item.getLegenda())));
                    }
                }
            } else {
                for (EdicaoFoto fotoBanco : fotosAtuaisBanco) {
                    fotosMantidas.add(new FotoParaPersistir(fotoBanco.getArquivoPath(), fotoBanco.getLegenda()));
                }
            }

            List<FotoParaPersistir> novasFotos = salvarNovasFotosEnviadas(form, arquivosCriados);
            List<FotoParaPersistir> todasFotos = new ArrayList<>(fotosMantidas);
            todasFotos.addAll(novasFotos);

            if (todasFotos.size() > MAX_FOTOS_EXTRAS) {
                throw new IllegalArgumentException("Máximo de " + MAX_FOTOS_EXTRAS + " fotos extras permitidas.");
            }

            aplicarCamposFormulario(existente, form);
            existente.setCapaPath(novaCapaPath);
            existente.setAtualizadoEm(Instant.now());
            existente.setNew(false);

            Edicao atualizada = edicaoRepository.save(existente);

            fotoRepository.deleteByEdicaoId(id);
            salvarFotos(id, todasFotos);

            entrevistaRepository.deleteByEdicaoId(id);
            List<EdicaoEntrevista> entrevistasSalvas = salvarEntrevistas(id, form.getEntrevistas());

            conteudoExtraRepository.deleteByEdicaoId(id);
            salvarConteudosExtras(id, form.getConteudosExtras());

            indicacaoBandaService.sincronizarComEdicao(atualizada, entrevistasSalvas);

            limparArquivosSilenciosamente(arquivosParaRemoverAposSalvar);
            return atualizada;
        } catch (RuntimeException ex) {
            limparArquivosSilenciosamente(arquivosCriados);
            throw ex;
        }
    }

    public void excluir(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        Edicao edicao = edicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Edição não encontrada."));

        List<EdicaoFoto> fotos = fotoRepository.findByEdicaoIdOrderByOrdemAsc(id);

        indicacaoBandaService.reverterOuReatribuirPorEdicaoExcluida(id);
        edicaoRepository.deleteById(id);

        List<String> arquivosParaRemover = new ArrayList<>();
        if (edicao.getCapaPath() != null && !edicao.getCapaPath().isBlank()) {
            arquivosParaRemover.add(edicao.getCapaPath());
        }
        for (EdicaoFoto foto : fotos) {
            if (foto.getArquivoPath() != null && !foto.getArquivoPath().isBlank()) {
                arquivosParaRemover.add(foto.getArquivoPath());
            }
        }
        limparArquivosSilenciosamente(arquivosParaRemover);
    }

    @Transactional(readOnly = true)
    public Optional<EdicaoDetalhesDTO> buscarDetalhesPublicos(IdiomaEdicao idioma, Integer numeroEdicao) {
        if (idioma == null || numeroEdicao == null) {
            return Optional.empty();
        }
        Optional<Edicao> optEdicao = edicaoRepository.findByNumeroEdicaoAndIdiomaAndPublicadoTrue(numeroEdicao, idioma);
        if (optEdicao.isEmpty()) {
            return Optional.empty();
        }

        Edicao edicao = optEdicao.get();
        List<EdicaoFoto> fotos = fotoRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId());
        List<EdicaoEntrevista> entrevistas = entrevistaRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId());
        List<EdicaoConteudoExtra> extras = conteudoExtraRepository.findByEdicaoIdOrderByOrdemAsc(edicao.getId());

        Optional<Edicao> anterior = edicaoRepository.findEdicaoAnteriorPublicada(idioma.name(), numeroEdicao);
        Optional<Edicao> proxima = edicaoRepository.findProximaEdicaoPublicada(idioma.name(), numeroEdicao);

        IdiomaEdicao outroIdioma = idioma.getComplementar();
        String versaoAlternativaUrl = edicaoRepository
                .findByNumeroEdicaoAndIdiomaAndPublicadoTrue(numeroEdicao, outroIdioma)
                .map(Edicao::getUrlPublica)
                .orElse(null);

        String youtubeVideoId = extrairYoutubeVideoId(edicao.getYoutubeUrl());

        return Optional.of(new EdicaoDetalhesDTO(
                edicao, fotos, entrevistas, extras, anterior, proxima, versaoAlternativaUrl, youtubeVideoId));
    }

    @Transactional(readOnly = true)
    public Optional<String> resolverUrlPadraoPorNumero(Integer numeroEdicao) {
        if (numeroEdicao == null) {
            return Optional.empty();
        }
        List<Edicao> publicadas = edicaoRepository.findByNumeroEdicaoAndPublicadoTrue(numeroEdicao);
        if (publicadas.isEmpty()) {
            return Optional.empty();
        }
        return publicadas.stream()
                .filter(e -> e.getIdioma() == IdiomaEdicao.PT)
                .findFirst()
                .or(() -> publicadas.stream().filter(e -> e.getIdioma() == IdiomaEdicao.EN).findFirst())
                .map(Edicao::getUrlPublica);
    }

    public static String extrairYoutubeVideoId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url.trim());
        return matcher.find() ? matcher.group(1) : null;
    }

    private void validarCamposComuns(EdicaoFormDTO form, UUID idAtual) {
        ValidationUtils.validarCampoObrigatorio(form, "Dados da edição");
        ValidationUtils.validarCampoObrigatorio(form.getNumeroEdicao(), "Número da edição");
        ValidationUtils.validarCampoObrigatorio(form.getIdioma(), "Idioma da edição");
        ValidationUtils.validarCampoStringObrigatorio(form.getTitulo(), "Título");
        ValidationUtils.validarCampoStringObrigatorio(form.getMesLancamento(), "Mês de lançamento");
        ValidationUtils.validarCampoObrigatorio(form.getAnoLancamento(), "Ano de lançamento");
        ValidationUtils.validarCampoStringObrigatorio(form.getAnoRevista(), "Ano da revista");
        ValidationUtils.validarCampoObrigatorio(form.getPaginas(), "Número de páginas");
        ValidationUtils.validarCampoObrigatorio(form.getPreco(), "Preço");
        ValidationUtils.validarCampoObrigatorio(form.getStatusEstoque(), "Status de estoque");
        ValidationUtils.validarCampoStringObrigatorio(form.getDescricao(), "Descrição");

        if (form.getNumeroEdicao() <= 0) {
            throw new IllegalArgumentException("Número da edição deve ser maior que zero.");
        }
        if (form.getPaginas() <= 0) {
            throw new IllegalArgumentException("Número de páginas deve ser maior que zero.");
        }
        if (form.getAnoLancamento() < 1900 || form.getAnoLancamento() > 2100) {
            throw new IllegalArgumentException("Ano de lançamento inválido.");
        }
        if (form.getPreco().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo.");
        }
        if (form.getPreco().stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Preço deve ter no máximo 2 casas decimais.");
        }
        if (form.getQuantidadeEstoque() != null && form.getQuantidadeEstoque() < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa.");
        }

        if (form.getYoutubeUrl() != null && !form.getYoutubeUrl().isBlank()) {
            if (extrairYoutubeVideoId(form.getYoutubeUrl()) == null) {
                throw new IllegalArgumentException("URL do YouTube inválida. Informe um link válido de vídeo.");
            }
        }

        boolean duplicada = (idAtual == null)
                ? edicaoRepository.existsByNumeroEdicaoAndIdioma(form.getNumeroEdicao(), form.getIdioma())
                : edicaoRepository.existsByNumeroEdicaoAndIdiomaAndIdNot(form.getNumeroEdicao(), form.getIdioma(), idAtual);

        if (duplicada) {
            throw new IllegalArgumentException(
                    "Já existe a Edição #" + form.getNumeroEdicao() + " cadastrada em "
                            + form.getIdioma().getLabelAdmin() + ".");
        }
    }

    private void aplicarCamposFormulario(Edicao edicao, EdicaoFormDTO form) {
        edicao.setNumeroEdicao(form.getNumeroEdicao());
        edicao.setIdioma(form.getIdioma());
        // Regra de negócio: idioma define obrigatoriamente a moeda (PT -> BRL, EN -> EUR)
        edicao.setMoeda(form.getIdioma().getMoeda());
        edicao.setTitulo(form.getTitulo().trim());
        edicao.setSubtitulo(normalizarTextoOpcional(form.getSubtitulo()));
        edicao.setMesLancamento(form.getMesLancamento().trim());
        edicao.setAnoLancamento(form.getAnoLancamento());
        edicao.setAnoRevista(form.getAnoRevista().trim());
        edicao.setPaginas(form.getPaginas());
        edicao.setPreco(form.getPreco().setScale(2, RoundingMode.HALF_UP));
        edicao.setStatusEstoque(form.getStatusEstoque());
        edicao.setQuantidadeEstoque(form.getQuantidadeEstoque() != null ? form.getQuantidadeEstoque() : 0);
        edicao.setPrazoPostagem(normalizarTextoOpcional(form.getPrazoPostagem()));
        edicao.setDescricao(form.getDescricao().trim());
        edicao.setFormato(normalizarTextoOpcional(form.getFormato()));
        edicao.setPapelImpressao(normalizarTextoOpcional(form.getPapelImpressao()));
        edicao.setTiragem(normalizarTextoOpcional(form.getTiragem()));
        edicao.setYoutubeUrl(normalizarTextoOpcional(form.getYoutubeUrl()));
        edicao.setPublicado(Boolean.TRUE.equals(form.getPublicado()));
    }

    private List<FotoParaPersistir> montarFotosParaCriacao(
            EdicaoFormDTO form, Edicao origem, List<String> arquivosCriados) {
        List<FotoParaPersistir> resultado = new ArrayList<>();

        if (origem != null && form.isCopiarImagensOrigem()) {
            List<EdicaoFoto> fotosOrigem = fotoRepository.findByEdicaoIdOrderByOrdemAsc(origem.getId());
            Map<UUID, EdicaoFormDTO.FotoExistenteItem> selecaoForm = new HashMap<>();
            if (form.getFotosExistentes() != null) {
                for (EdicaoFormDTO.FotoExistenteItem item : form.getFotosExistentes()) {
                    if (item != null && item.getId() != null) {
                        selecaoForm.put(item.getId(), item);
                    }
                }
            }

            for (EdicaoFoto fotoOrigem : fotosOrigem) {
                EdicaoFormDTO.FotoExistenteItem item = selecaoForm.get(fotoOrigem.getId());
                if (item != null && item.isRemover()) {
                    continue;
                }
                String copiaPath = fileUploadService.duplicarImagem(fotoOrigem.getArquivoPath());
                arquivosCriados.add(copiaPath);
                String legenda = item != null ? normalizarTextoOpcional(item.getLegenda()) : fotoOrigem.getLegenda();
                resultado.add(new FotoParaPersistir(copiaPath, legenda));
            }
        }

        resultado.addAll(salvarNovasFotosEnviadas(form, arquivosCriados));
        return resultado;
    }

    private List<FotoParaPersistir> salvarNovasFotosEnviadas(EdicaoFormDTO form, List<String> arquivosCriados) {
        List<FotoParaPersistir> novas = new ArrayList<>();
        List<MultipartFile> arquivos = form.getNovasFotos();
        List<String> legendas = form.getNovasFotosLegendas();
        if (arquivos == null || arquivos.isEmpty()) {
            return novas;
        }
        for (int i = 0; i < arquivos.size(); i++) {
            MultipartFile arquivo = arquivos.get(i);
            if (!hasUploadedFile(arquivo)) {
                continue;
            }
            String salvo = fileUploadService.salvarImagem(arquivo);
            arquivosCriados.add(salvo);
            String legenda = (legendas != null && i < legendas.size())
                    ? normalizarTextoOpcional(legendas.get(i))
                    : null;
            novas.add(new FotoParaPersistir(salvo, legenda));
        }
        return novas;
    }

    private void salvarFotos(UUID edicaoId, List<FotoParaPersistir> fotos) {
        int ordem = 1;
        for (FotoParaPersistir f : fotos) {
            EdicaoFoto entidade = new EdicaoFoto(
                    UUID.randomUUID(), edicaoId, f.arquivoPath(), f.legenda(), ordem++, true);
            fotoRepository.save(entidade);
        }
    }

    private List<EdicaoEntrevista> salvarEntrevistas(UUID edicaoId, List<EdicaoFormDTO.EntrevistaItem> entrevistas) {
        List<EdicaoEntrevista> salvas = new ArrayList<>();
        if (entrevistas == null || entrevistas.isEmpty()) {
            return salvas;
        }
        int ordem = 1;
        for (EdicaoFormDTO.EntrevistaItem item : entrevistas) {
            if (item == null || isBlank(item.getBanda())) {
                continue;
            }
            String pais = isBlank(item.getPais()) ? "—" : item.getPais().trim();
            String estilo = isBlank(item.getEstilo()) ? "Metal Extremo" : item.getEstilo().trim();
            EdicaoEntrevista entidade = new EdicaoEntrevista(
                    UUID.randomUUID(), edicaoId, item.getBanda().trim(), pais, estilo, ordem++,
                    item.getIndicacaoBandaId(), true);
            salvas.add(entrevistaRepository.save(entidade));
        }
        return salvas;
    }

    private void salvarConteudosExtras(UUID edicaoId, List<String> conteudosExtras) {
        if (conteudosExtras == null || conteudosExtras.isEmpty()) {
            return;
        }
        int ordem = 1;
        for (String texto : conteudosExtras) {
            if (isBlank(texto)) {
                continue;
            }
            EdicaoConteudoExtra entidade = new EdicaoConteudoExtra(
                    UUID.randomUUID(), edicaoId, texto.trim(), ordem++, true);
            conteudoExtraRepository.save(entidade);
        }
    }

    private void limparArquivosSilenciosamente(List<String> arquivos) {
        for (String arquivo : arquivos) {
            try {
                fileUploadService.removerImagem(arquivo);
            } catch (RuntimeException ignored) {
                // Melhor esforço de limpeza em disco
            }
        }
    }

    private boolean hasUploadedFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static final int MAX_BANDAS_DESTAQUE_CARD = 10;

    @Transactional(readOnly = true)
    public Map<String, List<EdicaoCatalogoItemDTO>> listarCatalogoPublicoPorIdioma() {
        return montarCatalogoPorIdioma(false, Integer.MAX_VALUE);
    }

    @Transactional(readOnly = true)
    public Map<String, List<EdicaoCatalogoItemDTO>> listarDestaquesHomePorIdioma(int limite) {
        int limiteSeguro = Math.max(0, limite);
        return montarCatalogoPorIdioma(true, limiteSeguro);
    }

    private Map<String, List<EdicaoCatalogoItemDTO>> montarCatalogoPorIdioma(boolean apenasEmEstoque, int limitePorIdioma) {
        List<Edicao> publicadas = edicaoRepository.findByPublicadoTrueOrderByNumeroEdicaoDesc();
        Map<UUID, List<EdicaoEntrevista>> entrevistasPorEdicao = new HashMap<>();
        if (!publicadas.isEmpty()) {
            for (EdicaoEntrevista ent : entrevistaRepository.findAllByOrderByEdicaoIdAscOrdemAsc()) {
                entrevistasPorEdicao.computeIfAbsent(ent.getEdicaoId(), k -> new ArrayList<>()).add(ent);
            }
        }

        List<EdicaoCatalogoItemDTO> pt = new ArrayList<>();
        List<EdicaoCatalogoItemDTO> en = new ArrayList<>();

        for (Edicao edicao : publicadas) {
            if (apenasEmEstoque && (edicao.getStatusEstoque() == null || !edicao.getStatusEstoque().isInStock())) {
                continue;
            }
            List<EdicaoCatalogoItemDTO> destino = edicao.getIdioma() == IdiomaEdicao.EN ? en : pt;
            if (destino.size() >= limitePorIdioma) {
                continue;
            }
            destino.add(toCatalogoItemDTO(edicao, entrevistasPorEdicao.getOrDefault(edicao.getId(), List.of())));
        }

        Map<String, List<EdicaoCatalogoItemDTO>> resultado = new HashMap<>();
        resultado.put("pt", List.copyOf(pt));
        resultado.put("en", List.copyOf(en));
        return resultado;
    }

    private EdicaoCatalogoItemDTO toCatalogoItemDTO(Edicao edicao, List<EdicaoEntrevista> entrevistas) {
        String date = edicao.getMesLancamento() + " / " + edicao.getAnoLancamento() + " - " + edicao.getAnoRevista();
        String bands = formatarDestaquesEntrevistas(entrevistas, edicao.getDescricao());
        return new EdicaoCatalogoItemDTO(
                edicao.getId(),
                edicao.getNumeroEdicao(),
                edicao.getIdioma().getSlug(),
                edicao.getUrlPublica(),
                edicao.getTitulo(),
                edicao.getSubtitulo(),
                edicao.getPreco(),
                edicao.getMoeda().getSimbolo(),
                "/uploads/" + edicao.getCapaPath(),
                date,
                bands,
                edicao.getStatusEstoque().isInStock(),
                edicao.getStatusEstoque().name().toLowerCase(Locale.ROOT),
                edicao.getPaginas());
    }

    private String formatarDestaquesEntrevistas(List<EdicaoEntrevista> entrevistas, String descricaoFallback) {
        if (entrevistas == null || entrevistas.isEmpty()) {
            if (descricaoFallback == null) {
                return "";
            }
            String limpa = descricaoFallback.trim();
            return limpa.length() > 140 ? limpa.substring(0, 137) + "..." : limpa;
        }
        StringBuilder sb = new StringBuilder();
        int limite = Math.min(entrevistas.size(), MAX_BANDAS_DESTAQUE_CARD);
        for (int i = 0; i < limite; i++) {
            EdicaoEntrevista ent = entrevistas.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ent.getBanda());
            if (!isBlank(ent.getPais()) && !"—".equals(ent.getPais().trim())) {
                sb.append(" (").append(ent.getPais().trim()).append(')');
            }
        }
        if (entrevistas.size() > MAX_BANDAS_DESTAQUE_CARD) {
            sb.append("...");
        }
        return sb.toString();
    }

    private record FotoParaPersistir(String arquivoPath, String legenda) {
    }
}
