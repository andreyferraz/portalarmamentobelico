package com.armamentobelico.portalarmamentobelico.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.armamentobelico.portalarmamentobelico.model.IdiomaEdicao;
import com.armamentobelico.portalarmamentobelico.model.StatusEstoque;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EdicaoFormDTO {

    private UUID id;
    private Integer numeroEdicao;
    private IdiomaEdicao idioma = IdiomaEdicao.PT;
    private String titulo;
    private String subtitulo;
    private String mesLancamento;
    private Integer anoLancamento;
    private String anoRevista;
    private Integer paginas;
    private BigDecimal preco;
    private StatusEstoque statusEstoque = StatusEstoque.AVAILABLE;
    private Integer quantidadeEstoque = 0;
    private String prazoPostagem;
    private String descricao;
    private String formato;
    private String papelImpressao;
    private String tiragem;
    private String youtubeUrl;
    private Boolean publicado = Boolean.TRUE;

    private MultipartFile capa;
    private String capaAtualPath;

    private UUID origemId;
    private boolean copiarImagensOrigem;

    private List<FotoExistenteItem> fotosExistentes = new ArrayList<>();
    private List<MultipartFile> novasFotos = new ArrayList<>();
    private List<String> novasFotosLegendas = new ArrayList<>();

    private List<EntrevistaItem> entrevistas = new ArrayList<>();
    private List<String> conteudosExtras = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FotoExistenteItem {
        private UUID id;
        private String arquivoPath;
        private String legenda;
        private Integer ordem;
        private boolean remover;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntrevistaItem {
        private String banda;
        private String pais;
        private String estilo;
        private UUID indicacaoBandaId;
    }
}
