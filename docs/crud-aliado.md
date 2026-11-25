# Análise e Especificação: CRUD de Aliados

Este documento registra toda a especificação funcional e técnica necessária para implementar a persistência em banco de dados, a gestão administrativa completa (**Cadastrar**, **Listar**, **Editar**, **Ordenar**, **Ativar/Desativar** e **Excluir**) no painel `/admin/aliados` e a renderização dinâmica bilíngue da página pública [`src/main/resources/templates/aliados.html`](../src/main/resources/templates/aliados.html).

A especificação segue fielmente a arquitetura do projeto (`com.armamentobelico.portalarmamentobelico` — Java 21, Spring Boot 4.2, Spring Data JDBC, SQLite, Thymeleaf e Spring Security), mantendo total coerência com [`docs/crud-edicao-fanzine.md`](./crud-edicao-fanzine.md), [`docs/crud-noticia.md`](./crud-noticia.md), [`docs/crud-evento.md`](./crud-evento.md) e [`docs/crud-indicacao-banda.md`](./crud-indicacao-banda.md).

---

## 1. Diagnóstico do Estado Atual e Escopo

### 1.1. Estado Atual do Repositório
Atualmente:
- O template [`aliados.html`](../src/main/resources/templates/aliados.html) renderiza **6 cards estáticos** hardcoded dentro de `.allies-grid` (*Extremely Rotten Productions*, *Black Hole Productions*, *Mutilation Productions*, *Cogumelo Records*, *Nuclear War Now!* e *Irmandade dos Fanzines*), compostos por:
  - Cabeçalho com `data-i18n="allies_title"` e `data-i18n="allies_sub"`, mais caixa introdutória `.zine-box`.
  - Grade `.allies-grid` contendo cards `<article class="ally-card">`:
    - Quadro de logo `.ally-logo-frame` (atualmente usando `<span>` estilizado em texto).
    - Nome `<h3 class="pirata-title ally-name">`.
    - Subtítulo `<span class="ally-type">` combinando tipo e localização (ex.: `Selo / Distro • Dinamarca`) ou apenas tipo (`Publicações Físicas Irmãs`).
    - Descrição curta `<p class="ally-desc">`.
    - Botão de link externo `<a href="..." target="_blank" rel="noopener noreferrer" class="btn-brutal btn-outline-decay btn-sm">Visitar Cripta ↗</a>`.
- **Estilização já preparada em [`style.css`](../src/main/resources/static/css/style.css) (linhas 1773–1834):**
  - `.allies-grid`, `.ally-card`, `.ally-card:hover`, `.ally-logo-frame`, `.ally-name`, `.ally-type` e `.ally-desc`.
  - **Destaque:** `.ally-logo-frame img` (linhas 1808–1813) já está estilizado com `max-width: 100%; max-height: 100%; object-fit: contain; filter: grayscale(100%) contrast(120%);`, pronto para receber imagens de logo carregadas pelo admin.
- **Traduções já existentes em [`script.js`](../src/main/resources/static/js/script.js):**
  - `pt`: `nav_allies` (linha 298), `allies_title` (linha 338), `allies_sub` (linha 339).
  - `en`: `nav_allies` (linha 418), `allies_title` (linha 458), `allies_sub` (linha 459).
  - *Lacuna identificada:* o parágrafo introdutório dentro de `.zine-box` e o rótulo do botão `Visitar Cripta ↗` ainda não possuem chaves `data-i18n` em `script.js`.
- **Infraestrutura backend já disponível:**
  - SQLite (`armamentobelico.db`) inicializado via [`src/main/resources/schema.sql`](../src/main/resources/schema.sql), configurado em [`DatabaseConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/DatabaseConfig.java) e [`JdbcDialectConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/JdbcDialectConfig.java) (`UUID <-> String`, `Boolean <-> Integer`).
  - Entidades com `UUID` (`Persistable<UUID>`) seguindo o padrão de [`model/Admin.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/model/Admin.java).
  - Utilitários de validação em [`utils/ValidationUtils.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/utils/ValidationUtils.java).
  - Upload e conversão automática de imagens para **WebP** em [`service/FileUploadService.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/service/FileUploadService.java) (`salvarImagem`, `removerImagem`, `arquivoExiste`), com limite de `10MB` por arquivo / `30MB` por requisição configurado em [`application.properties`](../src/main/resources/application.properties).
  - Segurança em [`SecurityConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/SecurityConfig.java) protegendo `/admin` e `/admin/**` com CSRF ativo globalmente (`CsrfTokenInitializationFilter`), e rota pública `GET /aliados` atualmente em [`PublicPagesController.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/controller/PublicPagesController.java) (linhas 39–42).
- **Ainda não existem:**
  - Tabela `aliado` em `schema.sql`.
  - Model `Aliado`, DTOs, `AliadoRepository`, `AliadoService`, `AliadoPublicController` e `AdminAliadoController`.
  - Telas administrativas em `templates/admin/aliados/`.
  - Exposição dos arquivos de upload via `/uploads/**` (`WebMvcConfig` + inclusão de `/uploads/` em `SecurityConfig.CsrfTokenInitializationFilter.shouldNotFilter`), pré-requisito compartilhado com Edições, Notícias e Eventos.

### 1.2. Escopo Coberto por Este Documento
1. Persistência na tabela SQLite `aliado` com **curadoria exclusiva pelo Painel Admin** (não há formulário público em `aliados.html`).
2. **Suporte bilíngue (`PT` obrigatório + `EN` opcional)** para tipo, localização e descrição, com fallback automático para Português quando os campos em Inglês estiverem vazios.
3. **Upload opcional de logo em `.webp`** via `FileUploadService`, com **fallback tipográfico automático** dentro de `.ally-logo-frame` (usando `texto_logo` opcional ou o próprio `nome`) caso não haja imagem cadastrada.
4. **Subtítulo composto** (`tipo` + `localizacao` opcional, exibido como `"Tipo • Localização"` ou apenas `"Tipo"`), **ordenação manual** (`ordem_exibicao`) e controle de visibilidade (`ativo` 0/1) para ocultar aliados sem excluí-los.
5. CRUD completo no Painel Admin (`/admin/aliados`) e listagem pública dinâmica em `GET /aliados`.

---

## 2. Regras de Negócio e Validações

### 2.1. Curadoria Exclusiva e Visibilidade
- **Sem cadastro público:** Diferente de `/eventos` e `/indique`, apenas administradores autenticados podem criar, editar, reordenar, ativar/desativar ou excluir aliados.
- **Status `ativo`:**
  - `ativo = true` (`1`): aliado visível publicamente na grade `.allies-grid` em `GET /aliados`.
  - `ativo = false` (`0`): aliado oculto da página pública, preservado no banco e editável no painel `/admin/aliados` (útil para pausar uma parceria sem perder o histórico nem a logo).
  - Ao criar um novo aliado no admin, `ativo` vem marcado como `true` por padrão.

### 2.2. Suporte Bilíngue (`PT` Obrigatório + `EN` Opcional)
Seguindo o mesmo padrão consolidado em [`docs/crud-noticia.md`](./crud-noticia.md):
- **Nome (`nome`):** único (nomes próprios de selos, gravadoras e fanzines normalmente não mudam de idioma; ex.: *Extremely Rotten Productions*, *Cogumelo Records*).
- **Tipo (`tipo_pt` obrigatório, `tipo_en` opcional):** ex.: `"Selo / Distro"` / `"Label / Distro"`, `"Publicações Físicas Irmãs"` / `"Sister Print Zines"`.
- **Localização (`localizacao_pt` opcional, `localizacao_en` opcional):** ex.: `"Dinamarca"` / `"Denmark"`, `"Brasil"` / `"Brazil"`, `"Belo Horizonte"` / `"Belo Horizonte, Brazil"`. Pode ser deixada em branco quando o aliado for uma rede internacional ou coletivo sem país único.
- **Descrição (`descricao_pt` obrigatório, `descricao_en` opcional):** texto curto de apresentação do aliado.
- **Regra de fallback:** sempre que o visitante selecionar `EN` no seletor de idioma do portal:
  - Se `tipo_en` estiver vazio → exibe `tipo_pt`.
  - Se `localizacao_en` estiver vazia → exibe `localizacao_pt` (se preenchida).
  - Se `descricao_en` estiver vazia → exibe `descricao_pt`.

### 2.3. Composição do Subtítulo (`.ally-type`)
- Para o idioma **PT**:
  - Se `localizacao_pt` não for vazia → `"{tipo_pt} • {localizacao_pt}"` (ex.: `Selo / Distro • Dinamarca`).
  - Se `localizacao_pt` for vazia → `"{tipo_pt}"` (ex.: `Publicações Físicas Irmãs`).
- Para o idioma **EN** (aplicando o fallback):
  - `tipoEfetivoEn = !blank(tipo_en) ? tipo_en : tipo_pt`
  - `locEfetivaEn = !blank(localizacao_en) ? localizacao_en : localizacao_pt`
  - Se `locEfetivaEn` não for vazia → `"{tipoEfetivoEn} • {locEfetivaEn}"`; caso contrário → `"{tipoEfetivoEn}"`.

### 2.4. Logo Opcional e Fallback Tipográfico
- **Com imagem (`logo_path` preenchido):**
  - A imagem enviada no formulário (`MultipartFile logo`) é validada e convertida para `.webp` via `FileUploadService.salvarImagem(logo)`, salvando apenas o nome do arquivo gerado (`<uuid>.webp`) em `logo_path`.
  - Renderizada em `<div class="ally-logo-frame"><img th:src="@{/uploads/{f}(f=${aliado.logoPath})}" th:alt="${aliado.nome}" loading="lazy"></div>`, aproveitando o CSS já pronto em `style.css` (`object-fit: contain; filter: grayscale(100%) contrast(120%);`).
- **Sem imagem (`logo_path` nulo/vazio):**
  - Renderiza o fallback tipográfico estilizado dentro de `.ally-logo-frame`:
    - Usa `texto_logo` (campo opcional, ex.: `"NWN!"`, `"COGUMELO REC"`, `"ZINES UNIDOS"`) se preenchido;
    - Caso `texto_logo` também esteja vazio, utiliza o próprio `nome` do aliado como texto do emblema.
- **Substituição e remoção na edição:**
  - Se o admin enviar uma nova imagem na edição → salva a nova `.webp` primeiro, atualiza o registro e remove a anterior do disco com `FileUploadService.removerImagem(logoAntiga)`.
  - Se o admin marcar o checkbox **`removerLogo`** (sem enviar nova imagem) → define `logo_path = null` no banco e remove o arquivo físico `.webp` anterior do disco.
  - Se o admin **excluir** o aliado → remove o registro do banco e, caso exista `logo_path`, remove o arquivo `.webp` correspondente do disco.

### 2.5. Ordenação Pública e Administrativa
- **Campo `ordem_exibicao` (`INTEGER`, padrão `0`, faixa permitida `0` a `9999`):**
  - Quanto menor o número, mais cedo o card aparece na grade (ex.: `1`, `2`, `3`...).
  - Aliados com a mesma `ordem_exibicao` desempatam por `nome COLLATE NOCASE ASC`.
- **Ordenação na página pública (`GET /aliados`):**
  - Filtra apenas `ativo = 1`, ordenado por `ordem_exibicao ASC, nome COLLATE NOCASE ASC`.
- **Ordenação no Painel Admin (`GET /admin/aliados`):**
  - Exibe todos os aliados (ativos e inativos), ordenado por `ordem_exibicao ASC, nome COLLATE NOCASE ASC` (com filtros opcionais por status `todos` / `ativos` / `inativos` e busca textual por nome/tipo/localização).

### 2.6. Regras de Validação dos Campos
| Campo | Obrigatório | Regras de Validação |
| --- | :---: | --- |
| `nome` | Sim | Entre `2` e `120` caracteres após `trim`. |
| `tipoPt` | Sim | Entre `2` e `80` caracteres após `trim`. |
| `tipoEn` | Não | Se informado, entre `2` e `80` caracteres após `trim` (vazio é normalizado para `null`). |
| `localizacaoPt` | Não | Se informada, entre `2` e `80` caracteres após `trim` (vazio é normalizado para `null`). |
| `localizacaoEn` | Não | Se informada, entre `2` e `80` caracteres após `trim` (vazio é normalizado para `null`). |
| `descricaoPt` | Sim | Entre `10` e `500` caracteres após `trim`. |
| `descricaoEn` | Não | Se informada, entre `10` e `500` caracteres após `trim` (vazio é normalizado para `null`). |
| `urlSite` | Sim | URL absoluta válida com esquema estritamente `http://` ou `https://` e host não vazio (`URI.create`), máximo `300` caracteres. Rejeita `javascript:`, `data:`, `file:` etc. |
| `logo` (`MultipartFile`) | Não | Se enviado e não vazio: content-type de imagem válido (`image/jpeg`, `image/png`, `image/webp`, `image/gif`) e tamanho máximo de `10MB`. Convertido para `.webp` via `FileUploadService`. |
| `textoLogo` | Não | Máximo `40` caracteres após `trim` (vazio é normalizado para `null`). Usado apenas como fallback visual quando `logo_path` estiver ausente. |
| `ordemExibicao` | Não | Inteiro entre `0` e `9999` (padrão `0` quando omitido). |
| `ativo` | Sim | Booleano (`true` por padrão na criação). |

---

## 3. Modelagem de Dados (SQLite — `schema.sql`)

Adicionar a tabela `aliado` em [`src/main/resources/schema.sql`](../src/main/resources/schema.sql):

```sql
CREATE TABLE IF NOT EXISTS aliado (
    id              TEXT PRIMARY KEY,
    nome            TEXT NOT NULL,
    tipo_pt         TEXT NOT NULL,
    tipo_en         TEXT,
    localizacao_pt  TEXT,
    localizacao_en  TEXT,
    descricao_pt    TEXT NOT NULL,
    descricao_en    TEXT,
    url_site        TEXT NOT NULL,
    logo_path       TEXT,
    texto_logo      TEXT,
    ordem_exibicao  INTEGER NOT NULL DEFAULT 0 CHECK (ordem_exibicao >= 0 AND ordem_exibicao <= 9999),
    ativo           INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0, 1)),
    criado_em       TEXT NOT NULL,
    atualizado_em   TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_aliado_publico
    ON aliado (ativo, ordem_exibicao, nome);
```

### Notas de Compatibilidade com SQLite e Spring Data JDBC
- `id` é `UUID` persistido como `TEXT` usando os conversores já registrados em [`JdbcDialectConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/JdbcDialectConfig.java) (`UuidToStringConverter` / `StringToUuidConverter`).
- `ativo` é `Boolean` persistido como `INTEGER` (`0`/`1`), já suportado por `BooleanToIntegerConverter` / `IntegerToBooleanConverter` em `JdbcDialectConfig.java`.
- `criado_em` e `atualizado_em` seguem o formato ISO-8601 UTC (`Instant.now().toString()`), mantendo consistência com os demais módulos (`edicao`, `noticia`, `evento`, `indicacao_banda`).

---

## 4. Arquitetura Backend (`com.armamentobelico.portalarmamentobelico`)

### 4.1. Entidade (`model/Aliado.java`)
Segue o padrão `Persistable<UUID>` adotado em [`model/Admin.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/model/Admin.java), garantindo que novos registros com `UUID` pré-gerado executem `INSERT` em vez de `UPDATE` no Spring Data JDBC:

```java
@Table("aliado")
@Getter
@Setter
@NoArgsConstructor
public class Aliado implements Persistable<UUID> {

    @Id
    private UUID id;

    private String nome;
    private String tipoPt;
    private String tipoEn;
    private String localizacaoPt;
    private String localizacaoEn;
    private String descricaoPt;
    private String descricaoEn;
    private String urlSite;
    private String logoPath;
    private String textoLogo;
    private Integer ordemExibicao = 0;
    private Boolean ativo = Boolean.TRUE;
    private String criadoEm;
    private String atualizadoEm;

    @Transient
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew || id == null;
    }

    public void markAsNew() {
        this.isNew = true;
    }
}
```

### 4.2. DTOs (`dto/`)
- **`AliadoFormDTO`:** objeto de binding do formulário administrativo (`nome`, `tipoPt`, `tipoEn`, `localizacaoPt`, `localizacaoEn`, `descricaoPt`, `descricaoEn`, `urlSite`, `textoLogo`, `ordemExibicao`, `ativo`, `removerLogo`, `logoPathAtual` apenas para exibição no preview). O arquivo de imagem (`MultipartFile logo`) é recebido separadamente via `@RequestParam(value = "logo", required = false)` ou como campo do DTO (seguindo o padrão do projeto).
- **`AliadoPublicCardDTO`:** projeção enxuta e pronta para renderização em `aliados.html`, contendo:
  - `id` (`UUID`)
  - `nome` (`String`)
  - `subtituloPt` (`String` — já formatado como `"Tipo • Localização"` ou `"Tipo"`)
  - `subtituloEn` (`String` — já formatado com fallback automático para PT)
  - `descricaoPt` (`String`)
  - `descricaoEn` (`String` — já com fallback automático para PT)
  - `urlSite` (`String`)
  - `logoPath` (`String`, opcional)
  - `textoLogoEfetivo` (`String` — `textoLogo` se preenchido, senão `nome`)

### 4.3. Repositório (`repository/AliadoRepository.java`)
Interface estendendo `CrudRepository<Aliado, UUID>`:
- `@Query("SELECT * FROM aliado WHERE ativo = 1 ORDER BY ordem_exibicao ASC, nome COLLATE NOCASE ASC")`
  `List<Aliado> findAllAtivosOrdenados();`
- `@Query("SELECT * FROM aliado ORDER BY ordem_exibicao ASC, nome COLLATE NOCASE ASC")`
  `List<Aliado> findAllOrdenados();`
- `@Query("SELECT * FROM aliado WHERE ativo = :ativo ORDER BY ordem_exibicao ASC, nome COLLATE NOCASE ASC")`
  `List<Aliado> findByAtivoOrdenados(@Param("ativo") int ativo);`
- `@Query("SELECT COUNT(*) FROM aliado WHERE ativo = 1")`
  `long countAtivos();`
- `@Query("SELECT COUNT(*) FROM aliado WHERE ativo = 0")`
  `long countInativos();`

### 4.4. Serviço (`service/AliadoService.java`)
Responsabilidades:
1. **`listarParaPaginaPublica()`:** busca `findAllAtivosOrdenados()` e mapeia para `List<AliadoPublicCardDTO>` já com composição de subtítulo (`tipo • localizacao`) e fallbacks PT→EN resolvidos.
2. **`listarParaAdmin(String statusFiltro, String termoBusca)`:** lista aliados filtrando por `ativos` / `inativos` / `todos` e busca case-insensitive em `nome`, `tipoPt`, `tipoEn`, `localizacaoPt` e `localizacaoEn`.
3. **`buscarPorId(UUID id)`:** retorna `Aliado` ou lança exceção de domínio (`AliadoNaoEncontradoException` → HTTP 404).
4. **`criar(AliadoFormDTO dto, MultipartFile logoFile)`:**
   - Sanitiza (`trim`, normaliza strings vazias opcionais para `null`) e valida todos os campos (incluindo URL `http`/`https`).
   - Se `logoFile` estiver presente e não vazio, valida o tipo MIME/tamanho e chama `fileUploadService.salvarImagem(logoFile)`.
   - Gera `UUID.randomUUID()`, chama `markAsNew()`, preenche `criadoEm` e `atualizadoEm`, e persiste via `aliadoRepository.save(aliado)`.
   - **Rollback de arquivo em falha:** se `save` lançar exceção após salvar a logo, remove a imagem recém-criada via `fileUploadService.removerImagem(...)` para evitar arquivos órfãos.
5. **`atualizar(UUID id, AliadoFormDTO dto, MultipartFile logoFile)`:**
   - Carrega o registro existente, valida campos e gerencia a logo:
     - Novo arquivo enviado → salva nova `.webp`, atualiza `logoPath`, persiste e só então remove a logo anterior do disco.
     - Sem novo arquivo e `dto.isRemoverLogo()` verdadeiro → limpa `logoPath = null`, persiste e remove a logo anterior do disco.
     - Sem novo arquivo e `removerLogo` falso → mantém `logoPath` inalterado.
   - Atualiza `atualizadoEm` e persiste.
6. **`alternarStatus(UUID id)`:** inverte `ativo` (`true ↔ false`), atualiza `atualizadoEm` e persiste.
7. **`excluir(UUID id)`:** busca o registro, exclui do banco via `aliadoRepository.deleteById(id)` e, se houver `logoPath`, remove o arquivo físico `.webp` via `fileUploadService.removerImagem(logoPath)`.

### 4.5. Controladores (`controller/`)
- **`AliadoPublicController.java`:**
  - Assume o mapeamento `GET /aliados` (removendo o método estático `aliados()` de [`PublicPagesController.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/controller/PublicPagesController.java) para evitar conflito de rotas no Spring MVC).
  - Injeta `aliados` (`List<AliadoPublicCardDTO>`) no `Model` e retorna `"aliados"`.
- **`AdminAliadoController.java` (`@RequestMapping("/admin/aliados")`):**
  - Gerencia listagem, filtros, criação, edição, alternância de status (`ativo`) e exclusão com mensagens flash (`RedirectAttributes` — `mensagemSucesso` / `mensagemErro`).

---

## 5. Mapa de Rotas HTTP

### 5.1. Rota Pública
| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/aliados` | Renderiza `aliados.html` com todos os aliados `ativo = 1`, ordenados por `ordem_exibicao ASC, nome ASC`. |

### 5.2. Rotas Administrativas (Protegidas por `/admin/**` + CSRF)
| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/admin/aliados` | Lista todos os aliados com contadores (*Total*, *Ativos*, *Inativos*), filtro por status (`?status=todos\|ativos\|inativos`) e busca (`?q=...`). |
| `GET` | `/admin/aliados/novo` | Exibe o formulário de cadastro (`admin/aliados/form.html`). |
| `POST` | `/admin/aliados` | Valida e cria novo aliado (`multipart/form-data`), redirecionando para `/admin/aliados`. |
| `GET` | `/admin/aliados/{id}/editar` | Carrega o aliado no formulário de edição (`admin/aliados/form.html`) com preview da logo atual (se houver). |
| `POST` | `/admin/aliados/{id}` | Atualiza dados, ordem, status e/ou logo do aliado (`multipart/form-data`), redirecionando para `/admin/aliados`. |
| `POST` | `/admin/aliados/{id}/alternar-status` | Alterna `ativo` (`true ↔ false`) rapidamente direto da tabela de listagem. |
| `POST` | `/admin/aliados/{id}/excluir` | Exclui o aliado definitivamente do banco e remove sua logo `.webp` do disco (se existir). |

---

## 6. Contratos de Interface (Templates Thymeleaf, CSS e i18n)

### 6.1. Página Pública (`templates/aliados.html`)
Substitui os 6 cards estáticos por um loop Thymeleaf preservando integralmente o design visual existente:

```html
<!-- Estado vazio -->
<div class="zine-box" th:if="${#lists.isEmpty(aliados)}" style="text-align: center;">
    <p data-i18n="allies_empty" style="color: var(--color-paper-dim);">
        Nenhum aliado cadastrado no momento.
    </p>
</div>

<!-- Grade dinâmica de aliados -->
<div class="allies-grid" th:if="${!#lists.isEmpty(aliados)}">
    <article class="ally-card" th:each="aliado : ${aliados}">
        <div class="ally-logo-frame">
            <img th:if="${aliado.logoPath != null and !#strings.isEmpty(aliado.logoPath)}"
                 th:src="@{/uploads/{arquivo}(arquivo=${aliado.logoPath})}"
                 th:alt="${aliado.nome}"
                 loading="lazy">
            <span th:if="${aliado.logoPath == null or #strings.isEmpty(aliado.logoPath)}"
                  class="ally-logo-fallback"
                  th:text="${aliado.textoLogoEfetivo}">
                ALIADO
            </span>
        </div>

        <h3 class="pirata-title ally-name" th:text="${aliado.nome}">Nome do Aliado</h3>

        <span class="ally-type"
              th:attr="data-pt=${aliado.subtituloPt}, data-en=${aliado.subtituloEn}"
              th:text="${aliado.subtituloPt}">
            Tipo • Localização
        </span>

        <p class="ally-desc"
           th:attr="data-pt=${aliado.descricaoPt}, data-en=${aliado.descricaoEn}"
           th:text="${aliado.descricaoPt}">
            Descrição do aliado.
        </p>

        <a th:href="${aliado.urlSite}"
           target="_blank"
           rel="noopener noreferrer"
           class="btn-brutal btn-outline-decay btn-sm"
           data-i18n="allies_visit_btn">
            Visitar Cripta ↗
        </a>
    </article>
</div>
```

### 6.2. Extensão de i18n (`script.js`) e CSS (`style.css`)
- **Em [`script.js`](../src/main/resources/static/js/script.js):**
  - Adicionar chaves `allies_intro`, `allies_visit_btn` (`Visitar Cripta ↗` / `Visit Crypt ↗`) e `allies_empty` (`Nenhum aliado cadastrado no momento.` / `No allies registered at the moment.`) nos dicionários `pt` e `en`.
  - Na função `applyLanguage(lang)` (já prevista para atualização em [`docs/crud-noticia.md`](./crud-noticia.md)), garantir que todos os elementos com `[data-pt][data-en]` atualizem seu `textContent` para `el.dataset[lang] || el.dataset.pt`. Assim, ao clicar na bandeira `EN`/`PT` no cabeçalho, `.ally-type` e `.ally-desc` alternam instantaneamente no navegador sem recarregar a página.
- **Em [`style.css`](../src/main/resources/static/css/style.css):**
  - Adicionar classe utilitária `.ally-logo-fallback` para o `<span>` de fallback dentro de `.ally-logo-frame` (`font-family: var(--font-gothic); font-size: 1.35rem; color: #fff; text-align: center; line-height: 1.1; word-break: break-word;`), eliminando os estilos inline presentes no HTML estático atual.

### 6.3. Painel Administrativo (`templates/admin/aliados/`)
- **`templates/admin/aliados/lista.html`:**
  - Cabeçalho com botão **"+ Novo Aliado"** (`/admin/aliados/novo`) e badges de resumo (*Total*, *Ativos*, *Inativos*).
  - Filtros por status (`Todos`, `Ativos`, `Inativos`) e campo de busca (`q`).
  - Tabela exibindo: **Ordem** (`ordemExibicao`), **Logo** (miniatura `<img>` em `/uploads/...` ou badge tipográfico), **Nome & Link externo**, **Tipo / Localização (PT)**, indicador de **Tradução EN** (*Completa* / *Parcial* / *Apenas PT*), **Status** (*Ativo* / *Inativo*) e **Ações** (*Editar*, *Ativar/Desativar* via `POST` com CSRF, *Excluir* via `POST` com CSRF e confirmação).
- **`templates/admin/aliados/form.html`:**
  - Formulário `method="post" enctype="multipart/form-data"` com token CSRF (`th:action` para criação ou atualização).
  - Bloco **Dados Gerais**: `nome`, `urlSite`, `ordemExibicao` (padrão `0`) e checkbox `ativo`.
  - Bloco **Logo / Emblema**: input `type="file" name="logo" accept="image/jpeg,image/png,image/webp,image/gif"`, campo opcional `textoLogo` (usado se não houver imagem), e — no modo edição quando já existir logo — preview da imagem atual + checkbox `removerLogo`.
  - Bloco **Conteúdo em Português (Obrigatório)**: `tipoPt`, `localizacaoPt` (opcional) e `descricaoPt`.
  - Bloco **Conteúdo em Inglês (Opcional — fallback para PT)**: `tipoEn`, `localizacaoEn` e `descricaoEn`.
- **Navegação em [`templates/admin/index.html`](../src/main/resources/templates/admin/index.html):**
  - Adicionar card/link para `/admin/aliados` (*Aliados & Selos*) no menu principal do painel administrativo.

---

## 7. Segurança, Uploads e Boas Práticas

1. **Autenticação e CSRF:**
   - Todas as rotas `/admin/aliados/**` já ficam automaticamente protegidas pela regra `.requestMatchers("/admin", "/admin/**").authenticated()` em [`SecurityConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/SecurityConfig.java).
   - Todas as ações mutáveis (`POST /admin/aliados`, `POST /admin/aliados/{id}`, `POST /admin/aliados/{id}/alternar-status`, `POST /admin/aliados/{id}/excluir`) exigem token CSRF válido.
2. **Prevenção de XSS e Open Redirect/Protocol Injection:**
   - Todos os campos de texto são renderizados exclusivamente com `th:text` (nunca `th:utext`), garantindo escape HTML automático pelo Thymeleaf.
   - `urlSite` passa por validação rigorosa server-side aceitando apenas esquemas `http` ou `https` com host válido, impedindo injeção de links `javascript:` ou `data:` em `th:href`.
   - Links externos em `aliados.html` sempre utilizam `target="_blank" rel="noopener noreferrer"`.
3. **Exposição Segura de `/uploads/**` (Pré-requisito Compartilhado):**
   - Configurar `WebMvcConfig` mapeando `/uploads/**` para `file:<upload.dir>/` (especificado em [`docs/crud-edicao-fanzine.md`](./crud-edicao-fanzine.md)).
   - Garantir que `SecurityConfig.CsrfTokenInitializationFilter.shouldNotFilter` inclua `/uploads/`, evitando que o carregamento das logos crie sessões HTTP desnecessárias e preservando o teste existente `AdminLoginSecurityTests.staticAssetRequestDoesNotCreateSession`.
4. **Integridade de Arquivos no Disco:**
   - A conversão para `.webp` em `FileUploadService` gera nomes aleatórios baseados em `UUID`, eliminando risco de *path traversal* a partir do `originalFilename`.
   - Remoção controlada da logo antiga na substituição/remoção/exclusão e limpeza em caso de falha na persistência.

---

## 8. Roteiro de Implementação em Etapas com TDD (`Red → Green → Refactor`)

### Etapa 1 — Schema SQLite, Entidade e Repositório
- **Red:** Escrever testes de integração (`AliadoRepositoryTests`) validando inserção (`Persistable<UUID>`), busca de ativos ordenados (`ordem_exibicao ASC, nome ASC`), filtro por status `ativo` e contadores.
- **Green:** Adicionar tabela `aliado` e índice `idx_aliado_publico` em `schema.sql`; criar `model/Aliado.java` e `repository/AliadoRepository.java`.
- **Refactor:** Revisar constraints `CHECK` e nomes de colunas/índices.

### Etapa 2 — Regras de Negócio, Fallback Bilíngue e Upload (`AliadoService`)
- **Red:** Escrever testes unitários (`AliadoServiceTests` com Mockito para `AliadoRepository` e `FileUploadService`) cobrindo:
  - Validação de campos obrigatórios e rejeição de URLs com esquema inválido (`javascript:alert(1)`, sem host).
  - Composição de subtítulo com e sem `localizacao` e fallback automático PT→EN.
  - Fallback tipográfico (`textoLogo` vs. `nome`) quando `logoPath` é nulo.
  - Criação com e sem upload de logo, substituição de logo com remoção da antiga, remoção explícita (`removerLogo = true`), limpeza de arquivo órfão em erro de persistência, toggle de status `ativo` e exclusão removendo o `.webp`.
- **Green:** Implementar `AliadoFormDTO`, `AliadoPublicCardDTO`, exceção de domínio e `AliadoService`.
- **Refactor:** Extrair helpers coesos de normalização/validação de URL e composição de subtítulo.

### Etapa 3 — Controladores e Segurança (`MockMvc`)
- **Red:** Escrever testes `MockMvc` (`AliadoControllerTests`) validando:
  - `GET /aliados` público acessível sem login, retornando apenas aliados ativos na ordem esperada (e estado vazio quando não houver ativos).
  - Redirecionamento para `/admin/login` ao acessar `/admin/aliados` sem autenticação.
  - Fluxo autenticado completo com CSRF: `GET /admin/aliados`, `POST /admin/aliados` (multipart), `POST /admin/aliados/{id}`, `POST /admin/aliados/{id}/alternar-status` e `POST /admin/aliados/{id}/excluir`.
  - Bloqueio `403` em `POST` sem CSRF.
- **Green:** Criar `AliadoPublicController`, `AdminAliadoController` e remover `aliados()` de `PublicPagesController`. Garantir `/uploads/**` em `WebMvcConfig` e `SecurityConfig` (caso ainda não implementado).
- **Refactor:** Padronizar atributos de `Model`, flash messages e tratamento de 404.

### Etapa 4 — Templates Thymeleaf, CSS e i18n
- Atualizar `templates/aliados.html` para renderização dinâmica com fallback de logo e atributos `data-pt` / `data-en`.
- Criar `templates/admin/aliados/lista.html` e `templates/admin/aliados/form.html` e adicionar atalho em `templates/admin/index.html`.
- Adicionar `.ally-logo-fallback` em `style.css` e chaves `allies_intro`, `allies_visit_btn`, `allies_empty` + suporte `[data-pt][data-en]` em `script.js`.

### Etapa 5 — Verificação Final
- Executar `./mvnw test` garantindo 100% da suíte verde (incluindo `AdminLoginSecurityTests`).
- Validar manualmente o fluxo completo no navegador: criar aliado com logo e tradução EN, criar aliado sem logo (testando fallback tipográfico e fallback PT→EN), alterar `ordem_exibicao`, desativar/reativar, alternar idioma `PT`/`EN` em `/aliados` e excluir verificando a remoção do `.webp` no diretório `uploads/`.
