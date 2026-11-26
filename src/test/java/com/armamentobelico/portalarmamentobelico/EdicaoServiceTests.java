package com.armamentobelico.portalarmamentobelico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;

import com.armamentobelico.portalarmamentobelico.dto.EdicaoDetalhesDTO;
import com.armamentobelico.portalarmamentobelico.dto.EdicaoFormDTO;
import com.armamentobelico.portalarmamentobelico.model.Edicao;
import com.armamentobelico.portalarmamentobelico.model.EdicaoFoto;
import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.MoedaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoFotoRepository;
import com.armamentobelico.portalarmamentobelico.repository.EdicaoRepository;
import com.armamentobelico.portalarmamentobelico.service.EdicaoService;
import com.armamentobelico.portalarmamentobelico.service.FileUploadService;

@SpringBootTest
class EdicaoServiceTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("service-test.db"));
        registry.add("upload.dir", () -> tempDir.resolve("uploads").toString());
    }

    @Autowired
    private EdicaoService edicaoService;

    @Autowired
    private EdicaoRepository edicaoRepository;

    @Autowired
    private EdicaoFotoRepository fotoRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @BeforeEach
    void limparBanco() {
        edicaoRepository.deleteAll();
    }

    private MockMultipartFile criarPngMock(String nomeArquivo, Color cor) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(cor);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return new MockMultipartFile("capa", nomeArquivo, "image/png", baos.toByteArray());
    }

    private EdicaoFormDTO criarFormBase(int numero, IdiomaEdicao idioma, String preco) throws IOException {
        EdicaoFormDTO form = new EdicaoFormDTO();
        form.setNumeroEdicao(numero);
        form.setIdioma(idioma);
        form.setTitulo("Armamento Bélico #" + numero + (idioma == IdiomaEdicao.EN ? " [ENG]" : ""));
        form.setSubtitulo("Especial Underground");
        form.setMesLancamento(idioma == IdiomaEdicao.PT ? "Agosto" : "August");
        form.setAnoLancamento(2026);
        form.setAnoRevista(idioma == IdiomaEdicao.PT ? "Ano 18" : "Year 18");
        form.setPaginas(68);
        form.setPreco(new BigDecimal(preco));
        form.setStatusEstoque(StatusEstoque.AVAILABLE);
        form.setQuantidadeEstoque(100);
        form.setDescricao("Descrição completa da edição #" + numero);
        form.setFormato("A5 (14,8 × 21 cm)");
        form.setPapelImpressao("Offset 90g");
        form.setTiragem("300 cópias");
        form.setYoutubeUrl("https://www.youtube.com/watch?v=5J1o1DHkekE");
        form.setPublicado(true);
        form.setCapa(criarPngMock("capa-" + numero + ".png", Color.RED));
        form.setEntrevistas(List.of(
                new EdicaoFormDTO.EntrevistaItem("Diabolizer", "Turquia", "Death Metal", null)));
        form.setConteudosExtras(List.of("Mais de 70 resenhas"));
        return form;
    }

    @Test
    void deveDerivarMoedaAutomaticamenteDoIdioma() throws IOException {
        Edicao pt = edicaoService.criar(criarFormBase(58, IdiomaEdicao.PT, "30.00"));
        Edicao en = edicaoService.criar(criarFormBase(58, IdiomaEdicao.EN, "12.00"));

        assertThat(pt.getMoeda()).isEqualTo(MoedaEdicao.BRL);
        assertThat(pt.getPrecoFormatado()).isEqualTo("R$ 30,00");
        assertThat(fileUploadService.arquivoExiste(pt.getCapaPath())).isTrue();

        assertThat(en.getMoeda()).isEqualTo(MoedaEdicao.EUR);
        assertThat(en.getPrecoFormatado()).isEqualTo("€ 12.00");
        assertThat(en.getPrecoFormatadoEm(IdiomaEdicao.PT)).isEqualTo("€ 12,00");
        assertThat(fileUploadService.arquivoExiste(en.getCapaPath())).isTrue();
    }

    @Test
    void deveClonarVersaoComplementarDuplicandoArquivosFisicosComIndependencia() throws IOException {
        EdicaoFormDTO formPt = criarFormBase(58, IdiomaEdicao.PT, "30.00");
        formPt.setNovasFotos(List.of(criarPngMock("extra1.png", Color.BLUE)));
        formPt.setNovasFotosLegendas(List.of("Foto extra 1"));
        Edicao edicaoPt = edicaoService.criar(formPt);

        EdicaoFormDTO formEn = edicaoService.prepararFormularioVersaoComplementar(edicaoPt.getId());
        assertThat(formEn.getIdioma()).isEqualTo(IdiomaEdicao.EN);
        assertThat(formEn.getNumeroEdicao()).isEqualTo(58);
        assertThat(formEn.getPreco()).isNull();
        assertThat(formEn.getOrigemId()).isEqualTo(edicaoPt.getId());
        assertThat(formEn.getEntrevistas()).hasSize(1);

        formEn.setTitulo("Armamento Bélico #58 [ENG]");
        formEn.setMesLancamento("August");
        formEn.setAnoRevista("Year 18");
        formEn.setPreco(new BigDecimal("12.00"));
        formEn.setDescricao("Full English edition");
        formEn.setCapa(null); // Reaproveita capa da origem
        formEn.setCopiarImagensOrigem(true);

        Edicao edicaoEn = edicaoService.criar(formEn);

        assertThat(edicaoEn.getMoeda()).isEqualTo(MoedaEdicao.EUR);
        assertThat(edicaoEn.getCapaPath()).isNotEqualTo(edicaoPt.getCapaPath());
        assertThat(fileUploadService.arquivoExiste(edicaoPt.getCapaPath())).isTrue();
        assertThat(fileUploadService.arquivoExiste(edicaoEn.getCapaPath())).isTrue();

        List<EdicaoFoto> fotosPt = fotoRepository.findByEdicaoIdOrderByOrdemAsc(edicaoPt.getId());
        List<EdicaoFoto> fotosEn = fotoRepository.findByEdicaoIdOrderByOrdemAsc(edicaoEn.getId());
        assertThat(fotosEn).hasSize(1);
        assertThat(fotosEn.get(0).getArquivoPath()).isNotEqualTo(fotosPt.get(0).getArquivoPath());

        // Excluir PT não deve afetar os arquivos físicos da EN
        edicaoService.excluir(edicaoPt.getId());
        assertThat(fileUploadService.arquivoExiste(edicaoPt.getCapaPath())).isFalse();
        assertThat(fileUploadService.arquivoExiste(fotosPt.get(0).getArquivoPath())).isFalse();
        assertThat(fileUploadService.arquivoExiste(edicaoEn.getCapaPath())).isTrue();
        assertThat(fileUploadService.arquivoExiste(fotosEn.get(0).getArquivoPath())).isTrue();
    }

    @Test
    void deveBloquearDuplicataNoMesmoIdiomaELimiteDeFotosExtras() throws IOException {
        edicaoService.criar(criarFormBase(58, IdiomaEdicao.PT, "30.00"));

        EdicaoFormDTO duplicada = criarFormBase(58, IdiomaEdicao.PT, "35.00");
        assertThatThrownBy(() -> edicaoService.criar(duplicada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe a Edição #58 cadastrada em Português");

        EdicaoFormDTO muitasFotos = criarFormBase(59, IdiomaEdicao.PT, "30.00");
        List<MultipartFile> seisFotos = List.of(
                criarPngMock("1.png", Color.RED),
                criarPngMock("2.png", Color.GREEN),
                criarPngMock("3.png", Color.BLUE),
                criarPngMock("4.png", Color.YELLOW),
                criarPngMock("5.png", Color.CYAN),
                criarPngMock("6.png", Color.MAGENTA));
        muitasFotos.setNovasFotos(seisFotos);
        assertThatThrownBy(() -> edicaoService.criar(muitasFotos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Máximo de 5 fotos extras");
    }

    @Test
    void deveSubstituirCapaERemoverAntigaDoDiscoAoAtualizar() throws IOException {
        Edicao criada = edicaoService.criar(criarFormBase(58, IdiomaEdicao.PT, "30.00"));
        String capaAntiga = criada.getCapaPath();

        EdicaoFormDTO formEdit = edicaoService.carregarParaEdicao(criada.getId());
        formEdit.setTitulo("Armamento Bélico #58 — Atualizada");
        formEdit.setCapa(criarPngMock("nova-capa.png", Color.GREEN));

        Edicao atualizada = edicaoService.atualizar(criada.getId(), formEdit);

        assertThat(atualizada.getTitulo()).isEqualTo("Armamento Bélico #58 — Atualizada");
        assertThat(atualizada.getCapaPath()).isNotEqualTo(capaAntiga);
        assertThat(fileUploadService.arquivoExiste(capaAntiga)).isFalse();
        assertThat(fileUploadService.arquivoExiste(atualizada.getCapaPath())).isTrue();
    }

    @Test
    void deveRetornarDetalhesPublicosComVersaoAlternativaENavegacao() throws IOException {
        edicaoService.criar(criarFormBase(57, IdiomaEdicao.PT, "25.00"));
        edicaoService.criar(criarFormBase(58, IdiomaEdicao.PT, "30.00"));
        edicaoService.criar(criarFormBase(58, IdiomaEdicao.EN, "12.00"));

        EdicaoDetalhesDTO detalhesPt = edicaoService.buscarDetalhesPublicos(IdiomaEdicao.PT, 58).orElseThrow();
        assertThat(detalhesPt.versaoAlternativaUrl()).isEqualTo("/edicoes/en/58");
        assertThat(detalhesPt.edicaoAnterior()).isPresent();
        assertThat(detalhesPt.edicaoAnterior().get().getNumeroEdicao()).isEqualTo(57);
        assertThat(detalhesPt.proximaEdicao()).isEmpty();
        assertThat(detalhesPt.youtubeVideoId()).isEqualTo("5J1o1DHkekE");
        assertThat(detalhesPt.entrevistas()).hasSize(1);
        assertThat(detalhesPt.conteudosExtras()).hasSize(1);

        assertThat(edicaoService.resolverUrlPadraoPorNumero(58)).contains("/edicoes/pt/58");
    }

    @Test
    void deveListarDestaquesHomeApenasEmEstoqueECatalogoCompletoPorIdioma() throws IOException {
        for (int num = 54; num <= 58; num++) {
            EdicaoFormDTO form = criarFormBase(num, IdiomaEdicao.PT, "30.00");
            if (num == 56) {
                form.setStatusEstoque(StatusEstoque.LOW);
            } else if (num == 55) {
                form.setStatusEstoque(StatusEstoque.SOLDOUT);
            }
            edicaoService.criar(form);
        }
        EdicaoFormDTO rascunho = criarFormBase(59, IdiomaEdicao.PT, "30.00");
        rascunho.setPublicado(false);
        edicaoService.criar(rascunho);

        EdicaoFormDTO en58 = criarFormBase(58, IdiomaEdicao.EN, "12.00");
        edicaoService.criar(en58);

        var destaquesHome = edicaoService.listarDestaquesHomePorIdioma(4);
        assertThat(destaquesHome.get("pt"))
                .extracting(item -> item.editionNumber())
                .containsExactly(58, 57, 56, 54);
        assertThat(destaquesHome.get("pt").get(2).stockStatus()).isEqualTo("low");
        assertThat(destaquesHome.get("en"))
                .extracting(item -> item.editionNumber())
                .containsExactly(58);
        assertThat(destaquesHome.get("en").get(0).currency()).isEqualTo("€");

        var catalogoCompleto = edicaoService.listarCatalogoPublicoPorIdioma();
        assertThat(catalogoCompleto.get("pt"))
                .extracting(item -> item.editionNumber())
                .containsExactly(58, 57, 56, 55, 54);
        assertThat(catalogoCompleto.get("pt").get(0).bands()).contains("Diabolizer (Turquia)");
    }
}
