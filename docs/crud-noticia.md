# Análise e Especificação: CRUD de Notícias (PT Obrigatório & EN Opcional)

Este documento registra toda a especificação funcional e técnica necessária para implementar o CRUD completo de **Notícias** no portal **Armamento Bélico**, tomando como base os templates [`src/main/resources/templates/noticia.html`](../src/main/resources/templates/noticia.html), [`src/main/resources/templates/noticias.html`](../src/main/resources/templates/noticias.html) e [`src/main/resources/templates/index.html`](../src/main/resources/templates/index.html), o comportamento atual em [`src/main/resources/static/js/script.js`](../src/main/resources/static/js/script.js), os estilos em [`src/main/resources/static/css/style.css`](../src/main/resources/static/css/style.css) e a arquitetura existente no projeto (`com.armamentobelico.portalarmamentobelico` — Spring Boot 4.2, Java 21, Spring Data JDBC, SQLite, Thymeleaf e Spring Security), mantendo total coerência com [`docs/crud-edicao-fanzine.md`](./crud-edicao-fanzine.md).

---

## 1. Diagnóstico do Estado Atual e Escopo

### 1.1. Estado Atual do Repositório
Atualmente:
- O template [`noticia.html`](../src/main/resources/templates/noticia.html) contém a estrutura visual completa da matéria interna (cabeçalho com `tape-tag blood`, título `pirata-title`, metadados de data/autor/categoria, imagem de destaque com `figcaption`, corpo `.article-body` com blocos `.lang-content-pt` e `.lang-content-en`, citações `<blockquote>`, perguntas/respostas de entrevista com `.interview-question` / `.interview-answer` e card CTA promocional no rodapé da matéria).
  - Os links atuais são estáticos (`noticias.html`, `assets/images/capa-edicao-58.png`) e o card CTA em inglês ainda exibe `US$ 12.00` hardcoded (enquanto a regra oficial definida em [`crud-edicao-fanzine.md`](./crud-edicao-fanzine.md) usa exclusivamente `R$` para `PT` e `€` para `EN`).
- O template [`noticias.html`](../src/main/resources/templates/noticias.html) lista 4 notícias estáticas dentro de `.news-feed-list`, exibindo miniatura, data + destaque, título, resumo (`.news-excerpt`), tags (`.news-tags` com `tape-tag dark` e `tape-tag blood`) e links estáticos `noticia-interna.html?id=1..4`.
- O template [`index.html`](../src/main/resources/templates/index.html) exibe a seção `.home-news-section` ("2 Últimas Notícias Publicadas") com 2 cards estáticos (`.news-card-home`) apontando para `noticia-interna.html?id=1` e `id=2`.
- Em [`script.js`](../src/main/resources/static/js/script.js), a função `applyLanguage(lang)` atualiza botões `.lang-btn` e elementos `[data-i18n]`, mas **ainda não alterna** os blocos `.lang-content-pt` / `.lang-content-en` nem os textos dinâmicos bilíngues das notícias.
- O backend já dispõe de:
  - SQLite (`armamentobelico.db`) inicializado via [`src/main/resources/schema.sql`](../src/main/resources/schema.sql), configurado em [`DatabaseConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/DatabaseConfig.java) e [`JdbcDialectConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/JdbcDialectConfig.java) (com conversores `UUID <-> String` e `Boolean <-> Integer`).
  - Entidades com `UUID` (`Persistable<UUID>`) seguindo o padrão de [`model/Admin.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/model/Admin.java).
  - Segurança configurada em [`SecurityConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/SecurityConfig.java) protegendo `/admin` e `/admin/**` com CSRF ativo.
  - Upload e conversão automática para **WebP** em [`service/FileUploadService.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/service/FileUploadService.java).
  - Rotas públicas básicas em [`PublicPagesController.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/controller/PublicPagesController.java) (`GET /` e `GET /noticias`).
- Ainda **não existem**:
  - Tabelas `noticia` e `noticia_tag` no `schema.sql`.
  - Dependência `org.jsoup:jsoup` no [`pom.xml`](../pom.xml) para sanitização HTML anti-XSS do editor Rich Text.
  - Models, DTOs, repositórios, serviços (`HtmlSanitizerService`, `SlugUtils`, `NoticiaService`) e controllers administrativos/públicos para Notícias.
  - Telas administrativas em `templates/admin/noticias/`.

### 1.2. Escopo Coberto por Este Documento
Esta especificação cobre **de ponta a ponta**:
1. Persistência SQLite (`noticia`, `noticia_tag` e vínculo opcional com `edicao`).
2. Painel Administrativo completo (`/admin/noticias` — listar, filtrar, criar, editar com editor Rich Text e abas PT/EN, publicar/despublicar e excluir).
3. Página interna dinâmica de leitura da notícia (`GET /noticias/{slug}` renderizando `templates/noticia.html`).
4. Feed público dinâmico de notícias (`GET /noticias` renderizando `templates/noticias.html`, com suporte a filtro por tag/busca).
5. Alimentação dinâmica dos **2 cards de últimas notícias** na Home (`GET /` em `templates/index.html`).

---

## 2. Regras de Negócio

1. **Modelo Bilíngue no Mesmo Cadastro (`PT` obrigatório + `EN` opcional):**
   - Cada notícia corresponde a **um único registro** na tabela `noticia`, compartilhando `slug`, `imagem_destaque_path`, `data_publicacao`, `autor`, `edicao_id`, `publicado` e tags.
   - Os campos em **Português (`_pt`) são obrigatórios**: `titulo_pt`, `resumo_pt`, `conteudo_html_pt`.
   - Os campos em **Inglês (`_en`) são opcionais**: `titulo_en`, `tag_destaque_en`, `categoria_en`, `resumo_en`, `conteudo_html_en`, `legenda_imagem_en`, `cta_texto_en`.
   - **Regra de coerência da tradução:** Se o administrador preencher parcialmente o conteúdo em inglês (ex.: preencher apenas `titulo_en` sem `conteudo_html_en`, ou vice-versa), a validação do serviço orienta a preencher ambos (`titulo_en` e `conteudo_html_en`) para considerar a tradução EN completa.
   - **Comportamento de fallback na leitura pública (`EN` ativo):**
     - Se a notícia possuir tradução em inglês (`temVersaoEn() == true`), o portal exibe título, tag de destaque, categoria, legenda, corpo HTML e CTA em inglês.
     - Se a notícia existir apenas em português (`temVersaoEn() == false`), a página interna mantém a leitura em português e exibe um banner discreto (`#news-pt-only-notice`) avisando o visitante: *"This article was published in Portuguese only."* No feed `/noticias` e na Home `/`, o card exibe os textos em português sem quebrar o layout.

2. **URL Amigável (`slug`) Única:**
   - Toda notícia é acessada publicamente em `/noticias/{slug}` (ex.: `/noticias/diabolizer-furia-death-metal-turco-edicao-58`).
   - O `slug` é gerado automaticamente a partir de `titulo_pt` (minúsculas, sem acentos, palavras separadas por hífen, somente `[a-z0-9-]`), mas pode ser ajustado manualmente pelo administrador no formulário.
   - O `slug` deve ser único na tabela `noticia` (validado na criação e edição).

3. **Corpo da Matéria com Editor Rich Text + Sanitização Obrigatória no Backend:**
   - O administrador redige `conteudo_html_pt` (e opcionalmente `conteudo_html_en`) em um **editor visual Rich Text** (ex.: **Quill 2**), suportando negrito, itálico, subtítulos (`h2`, `h3`), citações em destaque (`blockquote`), listas (`ul`, `ol`), links (`a`) e marcação semântica de perguntas/respostas de entrevista (`.interview-question` e `.interview-answer`), preservando fielmente o visual editorial de `noticia.html`.
   - **Regra absoluta de segurança (anti-XSS):** Como `noticia.html` renderiza o corpo com `th:utext`, **todo HTML recebido do formulário DEVE ser sanitizado no servidor** via `HtmlSanitizerService` (usando `org.jsoup:jsoup`) antes de ser persistido. Nunca confiar apenas na sanitização do editor no navegador.

4. **Vínculo Opcional com Edição do Fanzine (Card CTA Dinâmico):**
   - Uma notícia pode ser **opcionalmente vinculada** a uma edição cadastrada na tabela `edicao` (`edicao_id`, FK `ON DELETE SET NULL`).
   - Quando `edicao_id` for `null`, nenhum card promocional de edição é exibido no rodapé da notícia.
   - Quando `edicao_id` estiver preenchido e a edição estiver publicada:
     - Na leitura em **PT**, o card CTA aponta para a edição em Português (exibindo preço em `R$` e botão de compra/link para `/edicoes/pt/{numero}` — caso a edição esteja esgotada, exibe link "Ver detalhes da Edição #{numero}" em vez do botão de adicionar ao carrinho).
     - Na leitura em **EN**, o backend verifica se existe versão irmã publicada com o mesmo `numero_edicao` em `IdiomaEdicao.EN`: se existir, exibe o card CTA da versão EN em Euro (`€`) apontando para `/edicoes/en/{numero}`; caso contrário, faz fallback para a edição vinculada original.
   - O administrador pode informar um texto promocional personalizado (`cta_texto_pt` / `cta_texto_en`) ou deixar em branco para usar automaticamente o subtítulo/resumo da própria edição vinculada.

5. **Publicação e Ordenação Editorial:**
   - Campo `publicado` (`true`/`false`) permite salvar rascunhos sem exibi-los no site público (retornando `404` em `/noticias/{slug}` quando `publicado = false`).
   - Campo `data_publicacao` (`LocalDate`, padrão = data atual) permite ordenar matérias retroativas ou recentes no feed `/noticias` e nos **2 cards da Home (`/`)** sempre por `data_publicacao DESC, criado_em DESC`.

---

## 3. Mapeamento Completo de Dados (`noticia.html`, `noticias.html` e `index.html`)

### 3.1. Dados Principais da Notícia (Tabela `noticia`)
| Campo | Tipo Java / SQLite | Obrigatório | Origem / Uso em `noticia.html`, `noticias.html` e `index.html` |
| :--- | :--- | :---: | :--- |
| `id` | `UUID` (`TEXT`) | Sim | Identificador único (padrão `Persistable<UUID>`, como em `Admin`) |
| `slug` | `String` (`TEXT` `UNIQUE`) | Sim | URL canônica `/noticias/{slug}` (gerado de `tituloPt`, editável) |
| `tituloPt` | `String` (`TEXT`) | Sim | `#article-main-title`, `<title>`, cards em `noticias.html` e `index.html` |
| `tituloEn` | `String` (`TEXT`) | Não | Título exibido quando o visitante seleciona `EN` |
| `tagDestaquePt` | `String` (`TEXT`) | Não | `.tape-tag.blood` no topo de `noticia.html` (ex.: `ENTREVISTA DE CAPA • EDIÇÃO #58`) e sufixo da data nos cards |
| `tagDestaqueEn` | `String` (`TEXT`) | Não | Versão em inglês do selo de destaque (ex.: `COVER INTERVIEW • ISSUE #58`) |
| `categoriaPt` | `String` (`TEXT`) | Sim | `.article-meta` em `noticia.html` (ex.: `Death Metal / Entrevista Internacional`) |
| `categoriaEn` | `String` (`TEXT`) | Não | Versão em inglês da categoria |
| `autor` | `String` (`TEXT`) | Sim | `.article-meta` (`✍ Por: ...`), valor padrão `Editoria Armamento Bélico` |
| `dataPublicacao` | `LocalDate` (`TEXT` ISO `yyyy-MM-dd`) | Sim | `.article-meta` (`📅 24 de Agosto de 2026`) e `.news-date` nos cards; critério principal de ordenação |
| `resumoPt` | `String` (`TEXT`) | Sim | `.news-excerpt` no feed `noticias.html` e nos 2 cards de `index.html` (e meta description) |
| `resumoEn` | `String` (`TEXT`) | Não | Resumo exibido nos cards quando `EN` estiver ativo |
| `conteudoHtmlPt` | `String` (`TEXT`) | Sim | Bloco `.lang-content-pt` dentro de `.article-body` (HTML sanitizado via JSoup) |
| `conteudoHtmlEn` | `String` (`TEXT`) | Não | Bloco `.lang-content-en` dentro de `.article-body` (HTML sanitizado via JSoup) |
| `imagemDestaquePath` | `String` (`TEXT`) | Não* | `.article-featured-img img` em `noticia.html` e `.news-feed-thumb img` em `noticias.html` (*opcional para notas rápidas; recomendado para o feed) |
| `legendaImagemPt` | `String` (`TEXT`) | Não | `<figcaption>` abaixo da imagem de destaque em `noticia.html` |
| `legendaImagemEn` | `String` (`TEXT`) | Não | Versão em inglês do `<figcaption>` |
| `edicaoId` | `UUID` (`TEXT`, FK) | Não | Vínculo opcional com `edicao(id)` para renderizar o card CTA de compra no rodapé da matéria |
| `ctaTextoPt` | `String` (`TEXT`) | Não | Texto opcional do parágrafo no card CTA em PT (se vazio, usa resumo da edição vinculada) |
| `ctaTextoEn` | `String` (`TEXT`) | Não | Texto opcional do parágrafo no card CTA em EN |
| `publicado` | `Boolean` (`INTEGER` `0`/`1`) | Sim | Controla visibilidade pública (`1` = publicado, `0` = rascunho) |
| `criadoEm` | `Instant` / `String` (`TEXT` ISO-8601) | Sim (auto) | Auditoria de criação e desempate na ordenação |
| `atualizadoEm` | `Instant` / `String` (`TEXT` ISO-8601) | Sim (auto) | Auditoria da última atualização |

### 3.2. Relacionamento 1:N — Tags da Notícia (`noticia_tag`)
Mapeia diretamente `<div class="news-tags">` em `noticias.html` (ex.: `Death Metal`, `Edição #58`, `Turquia`):
- `id` (`UUID` / `TEXT`)
- `noticiaId` (`UUID` / `TEXT`, FK → `noticia(id) ON DELETE CASCADE`)
- `nome` (`String` / `TEXT`, obrigatório — ex.: `Death Metal`, `Black Metal`, `Fita K7`)
- `estilo` (`EstiloTag` / `TEXT`: `DARK` → `tape-tag dark`, `BLOOD` → `tape-tag blood`)
- `ordem` (`Integer` / `INTEGER`)

---

## 4. Estrutura de Banco de Dados (`schema.sql` e `DatabaseConfig.java`)

### 4.1. Atualização em `src/main/resources/schema.sql`
Seguindo a convenção SQLite + UUID em `TEXT` e assumindo a tabela `edicao` especificada em [`docs/crud-edicao-fanzine.md`](./crud-edicao-fanzine.md):

```sql
CREATE TABLE IF NOT EXISTS noticia (
    id TEXT PRIMARY KEY,
    slug TEXT NOT NULL UNIQUE,
    titulo_pt TEXT NOT NULL,
    titulo_en TEXT,
    tag_destaque_pt TEXT,
    tag_destaque_en TEXT,
    categoria_pt TEXT NOT NULL,
    categoria_en TEXT,
    autor TEXT NOT NULL DEFAULT 'Editoria Armamento Bélico',
    data_publicacao TEXT NOT NULL,
    resumo_pt TEXT NOT NULL,
    resumo_en TEXT,
    conteudo_html_pt TEXT NOT NULL,
    conteudo_html_en TEXT,
    imagem_destaque_path TEXT,
    legenda_imagem_pt TEXT,
    legenda_imagem_en TEXT,
    edicao_id TEXT,
    cta_texto_pt TEXT,
    cta_texto_en TEXT,
    publicado INTEGER NOT NULL DEFAULT 1,
    criado_em TEXT NOT NULL,
    atualizado_em TEXT NOT NULL,
    FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_noticia_publicado_data
    ON noticia (publicado, data_publicacao DESC, criado_em DESC);

CREATE TABLE IF NOT EXISTS noticia_tag (
    id TEXT PRIMARY KEY,
    noticia_id TEXT NOT NULL,
    nome TEXT NOT NULL,
    estilo TEXT NOT NULL DEFAULT 'DARK' CHECK (estilo IN ('DARK', 'BLOOD')),
    ordem INTEGER NOT NULL,
    FOREIGN KEY (noticia_id) REFERENCES noticia(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_noticia_tag_noticia_ordem
    ON noticia_tag (noticia_id, ordem ASC);
```

### 4.2. Integridade Referencial no SQLite (`DatabaseConfig.java`)
Assim como documentado em [`crud-edicao-fanzine.md`](./crud-edicao-fanzine.md), garantir `config.enforceForeignKeys(true)` no `SQLiteDataSource` em [`DatabaseConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/DatabaseConfig.java) para que:
- Excluir uma notícia remova automaticamente suas linhas em `noticia_tag` (`ON DELETE CASCADE`).
- Excluir uma edição vinculada defina automaticamente `noticia.edicao_id = NULL` (`ON DELETE SET NULL`), preservando a notícia sem quebrar a integridade.

---

## 5. Camada Backend (Spring Boot)

### 5.1. Nova Dependência no `pom.xml`
Adicionar **JSoup** para sanitização segura de HTML no servidor:
```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.18.3</version>
</dependency>
```

### 5.2. Models, Enums e DTOs (`model/` e `dto/`)
- **`EstiloTag` (enum)**:
  - `DARK("dark")` → renderiza `tape-tag dark`
  - `BLOOD("blood")` → renderiza `tape-tag blood`
- **Entidades Spring Data JDBC** (implementando `Persistable<UUID>` como em `Admin`):
  - `Noticia` (`@Table("noticia")`), com método auxiliar `public boolean temVersaoEn()` retornando `true` quando `tituloEn` e `conteudoHtmlEn` não estão em branco.
  - `NoticiaTag` (`@Table("noticia_tag")`).
- **`NoticiaFormDTO`**:
  - Campos de texto PT/EN (`tituloPt`, `tituloEn`, `slug`, `tagDestaquePt`, `tagDestaqueEn`, `categoriaPt`, `categoriaEn`, `autor`, `dataPublicacao`, `resumoPt`, `resumoEn`, `conteudoHtmlPt`, `conteudoHtmlEn`, `legendaImagemPt`, `legendaImagemEn`, `ctaTextoPt`, `ctaTextoEn`), `UUID edicaoId`, `boolean publicado`, `MultipartFile imagemDestaque`, flag `boolean removerImagemAtual` e lista ordenada de tags (`List<TagFormItem>` com `nome` e `estilo`).
- **`NoticiaDetalheViewDTO`** (ou atributos estruturados no `Model`):
  - Agrupa `Noticia`, `List<NoticiaTag>`, `Edicao edicaoVinculadaPt` (opcional) e `Edicao edicaoVinculadaEn` (opcional, resolvida pelo mesmo `numero_edicao` quando a edição vinculada tiver versão irmã publicada), além das datas formatadas em PT (`24 de Agosto de 2026`) e em EN (`August 24, 2026`).

### 5.3. Utilitários e Sanitização (`utils/` e `service/`)
- **`SlugUtils`**:
  - Converte título em slug URL-safe: aplica `Normalizer.Form.NFD` para remover diacríticos, converte para minúsculas, substitui caracteres não alfanuméricos por `-`, remove hífens duplicados e hífens nas extremidades.
- **`HtmlSanitizerService`**:
  - Sanitiza `conteudoHtmlPt` e `conteudoHtmlEn` com `Jsoup.clean(html, Safelist)`:
    - **Tags permitidas:** `p`, `br`, `strong`, `b`, `em`, `i`, `u`, `h2`, `h3`, `blockquote`, `ul`, `ol`, `li`, `a`, `span`.
    - **Atributos permitidos:**
      - `a`: `href` (somente protocolos `http`, `https`, `mailto`), forçando `rel="noopener noreferrer"` e `target="_blank"`.
      - `span` e `p`: atributo `class` validado explicitamente para aceitar **apenas** `interview-question` ou `interview-answer` (qualquer outra classe é removida).
    - **Bloqueios garantidos:** remove `<script>`, `<iframe>`, `<style>`, `<object>`, `<embed>`, manipuladores `on*` (`onclick`, `onerror`, etc.) e URLs `javascript:` / `data:`.

### 5.4. Repositories (`repository/`)
- **`NoticiaRepository extends ListCrudRepository<Noticia, UUID>`**:
  - `Optional<Noticia> findBySlug(String slug)`
  - `Optional<Noticia> findBySlugAndPublicadoTrue(String slug)`
  - `boolean existsBySlug(String slug)`
  - `boolean existsBySlugAndIdNot(String slug, UUID id)`
  - `List<Noticia> findAllByOrderByDataPublicacaoDescCriadoEmDesc()`
  - `List<Noticia> findByPublicadoTrueOrderByDataPublicacaoDescCriadoEmDesc()`
  - `@Query("SELECT * FROM noticia WHERE publicado = 1 ORDER BY data_publicacao DESC, criado_em DESC LIMIT 2")` → `List<Noticia> findUltimasDuasPublicadas()`
  - `@Query("SELECT DISTINCT n.* FROM noticia n JOIN noticia_tag t ON t.noticia_id = n.id WHERE n.publicado = 1 AND LOWER(t.nome) = LOWER(:tag) ORDER BY n.data_publicacao DESC, n.criado_em DESC")` → `List<Noticia> findPublicadasPorTag(String tag)`
- **`NoticiaTagRepository extends ListCrudRepository<NoticiaTag, UUID>`**:
  - `List<NoticiaTag> findByNoticiaIdOrderByOrdemAsc(UUID noticiaId)`
  - `List<NoticiaTag> findByNoticiaIdInOrderByOrdemAsc(Collection<UUID> noticiaIds)` (evita problema de consultas N+1 no feed `/noticias`)
  - `@Modifying @Query("DELETE FROM noticia_tag WHERE noticia_id = :noticiaId") void deleteByNoticiaId(UUID noticiaId)`

### 5.5. Service (`service/NoticiaService.java`)
- **Validações** (reutilizando `ValidationUtils`):
  - Obrigatórios: `tituloPt`, `categoriaPt`, `autor`, `dataPublicacao`, `resumoPt`, `conteudoHtmlPt` (verificando texto não vazio após sanitização, impedindo salvar apenas `<p><br></p>`).
  - Coerência EN: se qualquer campo principal de EN (`tituloEn` ou `conteudoHtmlEn`) for informado, exigir o outro campo para garantir uma versão EN consistente.
  - Geração e unicidade do `slug`: se `slug` vier em branco, gerar a partir de `tituloPt`; validar `existsBySlug` / `existsBySlugAndIdNot` retornando mensagem amigável em caso de colisão.
  - Se `edicaoId` for informado, validar que a edição existe no `EdicaoRepository`.
- **Sanitização:** aplicar `HtmlSanitizerService.sanitizar(...)` em `conteudoHtmlPt` e (quando presente) `conteudoHtmlEn`.
- **Ciclo de vida da imagem de destaque (`FileUploadService`):**
  - Upload opcional convertido automaticamente para `.webp`.
  - Na edição: se nova imagem for enviada, salva a nova e exclui a antiga do disco; se `removerImagemAtual = true`, exclui o arquivo antigo e grava `null`.
  - Na exclusão da notícia: remove o arquivo `.webp` do disco caso `imagemDestaquePath != null`.
- **Tags (`noticia_tag`):** normaliza espaços, remove duplicatas (case-insensitive), limita a um número razoável (ex.: até 8 tags por notícia) e persiste ordenadas.

### 5.6. Controllers (`controller/`)
- **`AdminNoticiaController` (`/admin/noticias`)**:
  - `GET /admin/noticias` — Lista notícias com busca por título/slug e filtros por status (`Todas`, `Publicadas`, `Rascunhos`) e idioma (`Todas`, `Com tradução EN`, `Somente PT`).
  - `GET /admin/noticias/nova` — Exibe formulário com lista de edições disponíveis para vínculo opcional.
  - `POST /admin/noticias` — Valida, sanitiza HTML, processa imagem WebP, persiste notícia + tags e redireciona com mensagem flash.
  - `GET /admin/noticias/{id}/editar` — Carrega notícia, tags e edições no formulário.
  - `POST /admin/noticias/{id}` — Atualiza notícia, tags e imagem.
  - `POST /admin/noticias/{id}/excluir` — Exclui notícia e limpa imagem `.webp` do disco.
- **`NoticiaPublicController`**:
  - `GET /noticias/{slug}` — Busca `findBySlugAndPublicadoTrue(slug)` (retornando `404` se inexistente ou rascunho), carrega tags e resolve eventual edição vinculada (`PT` e irmã `EN`), renderizando `templates/noticia.html`.
- **Atualização em `PublicPagesController`**:
  - `GET /noticias` (com `@RequestParam(required = false) String tag` e `@RequestParam(required = false) String busca`) — Carrega notícias publicadas e suas tags (sem N+1), renderizando `templates/noticias.html`.
  - `GET /` — Injeta `ultimasNoticias` (`findUltimasDuasPublicadas()`) no `Model` de `templates/index.html`.

---

## 6. Camada Frontend & Templates

### 6.1. Painel Administrativo (`templates/admin/`)
1. **Dashboard (`templates/admin/index.html`)**:
   - Exibir cards de navegação lado a lado para **Edições do Fanzine (`/admin/edicoes`)** e **Notícias (`/admin/noticias`)**, com contadores rápidos (publicadas, rascunhos, com tradução EN).
2. **Listagem (`templates/admin/noticias/lista.html`)**:
   - Filtros por busca textual, status (`Publicada` / `Rascunho`) e cobertura bilíngue (`PT + EN` / `Somente PT`).
   - Tabela com miniatura (`/uploads/...`), data de publicação, título PT, badges de idioma (`PT` + `EN` quando traduzida), categoria, edição vinculada (ex.: `#58` se houver), status e ações: **Ver no site**, **Editar** e **Excluir** (com confirmação).
3. **Formulário (`templates/admin/noticias/form.html`)**:
   - `enctype="multipart/form-data"` e `th:action` (com `_csrf` automático).
   - Organização em blocos claros:
     1. **Configurações Gerais & Publicação**: `slug` (auto-preenchido a partir do título PT com botão de editar), `dataPublicacao`, `autor`, checkbox `publicado`, **Edição Vinculada (opcional)** (`<select>` listando edições cadastradas).
     2. **Imagem de Destaque**: upload com preview (`.webp` via `FileUploadService`) e opção de remover imagem atual na edição.
     3. **Conteúdo em Abas (`Português — Obrigatório` | `Inglês — Opcional`)**:
        - Em cada aba: `titulo`, `tagDestaque`, `categoria`, `legendaImagem`, `resumo`, **Editor Rich Text (Quill 2)** sincronizado com `<textarea hidden>` (incluindo botões para Negrito, Itálico, Subtítulo, Citação, Lista, Link e estilo **Pergunta / Resposta de Entrevista**) e `ctaTexto` opcional.
     4. **Tags do Feed**: campos dinâmicos para adicionar/remover tags com nome e estilo (`DARK` ou `BLOOD`).

### 6.2. Páginas Públicas e `script.js`
- **`templates/noticia.html`**:
  - Converter `href="noticias.html"` para `th:href="@{/noticias}"`.
  - Renderizar `<title>` dinamicamente com `tituloPt`.
  - Adicionar `data-text-pt` / `data-text-en` (quando houver tradução) no `#article-main-title`, selo `.tape-tag.blood`, categoria, data formatada e `<figcaption>`, permitindo troca instantânea ao clicar nas bandeiras `PT` / `EN`.
  - Exibir imagem de destaque (`/uploads/{imagemDestaquePath}`) apenas quando existir (`th:if="${noticia.imagemDestaquePath != null}"`).
  - Renderizar `.lang-content-pt` com `th:utext="${noticia.conteudoHtmlPt}"` e `.lang-content-en` (quando `noticia.temVersaoEn()`) com `th:utext="${noticia.conteudoHtmlEn}"`.
  - Incluir banner discreto inicialmente oculto (`#news-pt-only-notice`, `hidden`) exibido apenas quando o visitante escolher `EN` em uma matéria sem tradução para inglês.
  - Renderizar o card CTA da edição vinculada (`th:if="${edicaoVinculadaPt != null}"`) dentro de cada bloco de idioma com preço formatado na moeda correta (`R$` em PT; `€` em EN quando existir versão EN publicada) e link `th:href="@{/edicoes/{idioma}/{num}(...)}`" (removendo completamente o mock `US$ 12.00` e os textos fixos da Edição #58).
- **`templates/noticias.html`**:
  - Substituir os 4 cards estáticos por `th:each="noticia : ${noticias}"`.
  - Corrigir `<img src="assets/images/divisor-edicoes.png">` para `th:src="@{/assets/divisor-edicoes.png}"`.
  - Apontar links dos cards para `th:href="@{/noticias/{slug}(slug=${noticia.slug})}"`.
  - Tornar cada tag em `.news-tags` um link de filtro `th:href="@{/noticias(tag=${tag.nome})}"`, exibindo botão "Limpar filtro" quando houver filtro ativo e estado vazio amigável caso não existam notícias publicadas.
  - Incluir `data-text-pt` / `data-text-en` no título e resumo dos cards que possuam tradução em inglês.
- **`templates/index.html`**:
  - Substituir os 2 cards fixos de `.news-grid-duo` por `th:each="noticia : ${ultimasNoticias}"`, apontando para `th:href="@{/noticias/{slug}(slug=${noticia.slug})}"` e mantendo fallback/ocultação limpa caso não haja notícias publicadas ainda.
- **`static/js/script.js`**:
  - Estender `applyLanguage(lang)` para:
    1. Atualizar elementos com `[data-text-pt]` / `[data-text-en]` (título, resumo, legenda, selo, categoria e data de notícias).
    2. Alternar `.lang-content-pt` e `.lang-content-en` dentro de `.news-single-article`: se `lang === 'en'` e existir `.lang-content-en` com conteúdo, mostrar `.lang-content-en` e ocultar `.lang-content-pt` e `#news-pt-only-notice`; caso contrário, mostrar `.lang-content-pt` e, se `lang === 'en'`, revelar `#news-pt-only-notice`.

---

## 7. Segurança e Configurações

- **[`SecurityConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/SecurityConfig.java)**:
  - `/admin/noticias/**` permanece protegido por `.requestMatchers("/admin", "/admin/**").authenticated()`.
  - Garantir `.requestMatchers("/noticias", "/noticias/**", "/uploads/**").permitAll()`.
  - Manter `/uploads/` fora do `CsrfTokenInitializationFilter` (sem criação de sessão HTTP em requisições de imagens).
  - Manter proteção CSRF obrigatória em todos os `POST` de `/admin/noticias/**`.
- **Prevenção de XSS (`th:utext` + `HtmlSanitizerService`)**:
  - Todo conteúdo HTML salvo em `conteudo_html_pt` e `conteudo_html_en` passa obrigatoriamente pelo `HtmlSanitizerService` no backend antes da persistência.
  - Testes automatizados dedicados devem validar que payloads maliciosos (`<script>`, `<img onerror=...>`, `<a href="javascript:...">`, `<iframe>`) são completamente removidos, enquanto tags editoriais legítimas (`<p>`, `<strong>`, `<blockquote>`, `<span class="interview-question">`) são preservadas.
- **Asset do Editor Rich Text (Quill 2)**:
  - Recomenda-se hospedar os arquivos estáticos do Quill em `src/main/resources/static/vendor/quill/` (evitando dependência externa de CDN no painel administrativo e facilitando políticas futuras de CSP).

---

## 8. Ordem Sugerida de Implementação (Fluxo TDD: Red → Green → Refactor)

1. **Dependência, Banco & Models**: adicionar `org.jsoup:jsoup` no `pom.xml`, atualizar `schema.sql` com `noticia` e `noticia_tag` (com `enforceForeignKeys(true)` em `DatabaseConfig`) e criar `EstiloTag`, `Noticia` e `NoticiaTag` (`Persistable<UUID>`).
2. **`SlugUtils` & `HtmlSanitizerService` (TDD)**: escrever testes unitários primeiro cobrindo acentuação/normalização de slugs e sanitização anti-XSS (bloqueio de scripts/event handlers/URI `javascript:` + preservação de `<blockquote>`, `<strong>`, links seguros e classes `.interview-question` / `.interview-answer`).
3. **Repositories & Testes de Persistência**: implementar `NoticiaRepository` e `NoticiaTagRepository`, testando unicidade de `slug`, busca de `publicado = 1`, `findUltimasDuasPublicadas()`, filtro por tag, `ON DELETE CASCADE` em `noticia_tag` e `ON DELETE SET NULL` em `edicao_id`.
4. **`NoticiaService` (TDD)**: implementar validações (campos PT obrigatórios, coerência da tradução EN opcional, unicidade de slug), sanitização automática, upload/limpeza WebP via `FileUploadService` e resolução da edição vinculada (`PT` + irmã `EN`).
5. **Painel Admin (`/admin/noticias`)**: implementar `NoticiaFormDTO`, `AdminNoticiaController`, atualizar `admin/index.html` e criar `admin/noticias/lista.html` e `admin/noticias/form.html` (abas PT/EN + Quill 2 + tags + seletor de edição), validados com testes `MockMvc` (autenticação, CSRF e upload multipart).
6. **Páginas Públicas (`/noticias/{slug}`, `/noticias` e Home `/`)**: implementar `NoticiaPublicController`, atualizar `PublicPagesController`, refatorar `noticia.html`, `noticias.html` e `index.html` para Thymeleaf e estender `applyLanguage()` no `script.js`, cobrindo com testes `MockMvc` (notícia publicada, rascunho retornando `404`, filtro por tag no feed e 2 últimas notícias na Home).
