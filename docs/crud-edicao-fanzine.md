# Análise e Especificação: CRUD de Edições do Fanzine (Português `R$` & Inglês `€`)

Este documento registra toda a especificação funcional e técnica necessária para implementar o CRUD completo de **Edições do Fanzine** no portal **Armamento Bélico**, tomando como base o template [`src/main/resources/templates/edicao.html`](../src/main/resources/templates/edicao.html), o comportamento atual em [`src/main/resources/static/js/script.js`](../src/main/resources/static/js/script.js) e a arquitetura já existente no projeto (`com.armamentobelico.portalarmamentobelico` — Spring Boot 4.2, Java 21, Spring Data JDBC, SQLite, Thymeleaf e Spring Security).

---

## 1. Diagnóstico do Estado Atual e Divisão de Etapas

### 1.1. Estado Atual do Repositório
Atualmente:
- O template [`edicao.html`](../src/main/resources/templates/edicao.html) possui a estrutura visual completa da página de detalhes de uma edição (com dados estáticos da edição `#58` no HTML e preenchimento dinâmico via `renderEditionDetailPage()` no [`script.js`](../src/main/resources/static/js/script.js) a partir do objeto mock `ZINE_CATALOG`).
- No mock atual `ZINE_CATALOG` em `script.js`, as edições em inglês (`ZINE_CATALOG.en`) utilizam provisoriamente `US$` — o que **será substituído pela regra oficial do negócio: apenas Real (`R$`) para edições em Português e Euro (`€`) para edições em Inglês**.
- O template usa links estáticos (`index.html`, `arquivo.html`, `assinatura.html`, `assets/images/...`) que precisam ser convertidos para rotas Thymeleaf/Spring MVC.
- O backend já possui:
  - Banco **SQLite** (`armamentobelico.db`) inicializado via [`src/main/resources/schema.sql`](../src/main/resources/schema.sql) e configurado em [`DatabaseConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/DatabaseConfig.java) e [`JdbcDialectConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/JdbcDialectConfig.java) (com conversores prontos `UUID <-> String` e `Boolean <-> Integer`).
  - Padrão de entidades com `UUID` (`Persistable<UUID>`) em [`model/Admin.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/model/Admin.java).
  - Segurança configurada em [`SecurityConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/SecurityConfig.java) protegendo `/admin` e `/admin/**` e inicializando token CSRF via `CsrfTokenInitializationFilter`.
  - Serviço de upload e conversão automática para **WebP** em [`service/FileUploadService.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/service/FileUploadService.java) (salvando em `${user.dir}/armamentobelico-uploads`, com limite de `10MB` por arquivo e `30MB` por requisição no [`application.properties`](../src/main/resources/application.properties)).
  - Utilitários de validação em [`utils/ValidationUtils.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/utils/ValidationUtils.java).
- Ainda **não existem**:
  - Tabelas de edição no `schema.sql` nem ativação explícita de `PRAGMA foreign_keys = ON` no `SQLiteDataSource`.
  - Modelos, enums (`IdiomaEdicao`, `MoedaEdicao`, `StatusEstoque`), repositórios, serviços e controllers para Edições.
  - Mapeamento HTTP para servir as imagens salvas no diretório `upload.dir` (`/uploads/**`).
  - Telas administrativas de listagem, criação e edição em `templates/admin/edicoes/`.

### 1.2. Recorte de Escopo entre Etapas
1. **Etapa 1 — CRUD do Fanzine + Página Pública da Edição (foco deste documento):**
   - Banco de dados (`schema.sql`), camada de domínio, repositórios, serviço com regras de idioma/moeda, exposição segura de `/uploads/**`, painel administrativo completo (`/admin/edicoes`) e renderização dinâmica da página pública de detalhes (`edicao.html`) em `/edicoes/{idioma}/{numero}` (e atalho `/edicoes/{numero}`).
2. **Etapa 2 — Catálogo Geral, Frete Internacional e Pagamento em Euro (etapa posterior):**
   - Alimentação dinâmica da Home (`/`) e do Catálogo (`/arquivo`) a partir do banco filtrando pelo idioma ativo (`PT` ou `EN`).
   - Regra definitiva de cálculo de frete internacional em Euro (`€`), adaptação do Necrocarrinho para trabalhar com `BRL` (`R$`) e `EUR` (`€`) sem misturar moedas no mesmo pedido, e definição da forma de pagamento/checkout para pedidos em inglês.

---

## 2. Regras de Negócio de Idioma (`PT` / `EN`) e Moeda (`R$` / `€`)

1. **Duas versões independentes e opcionais por número de edição:**
   - Uma edição (ex.: `#58`) pode ter até **duas versões cadastradas**: uma versão impressa em **Português (`PT`)** e uma versão impressa em **Inglês (`EN`)**.
   - **Nem todas as edições terão ambas as versões** (algumas edições existem apenas em Português; outras podem existir nos dois idiomas ou apenas em Inglês).
   - A unicidade no banco de dados é composta pelo par **`(numero_edicao, idioma)`**.
   - Como se tratam de tiragens físicas separadas, cada versão possui seu próprio registro, estoque (`status_estoque`, `quantidade_estoque`), textos editoriais, capa e fotos extras.

2. **Vínculo automático e obrigatório entre Idioma, Moeda e Ficha Técnica:**
   - O sistema trabalha exclusivamente com **duas moedas**: **Real (`BRL` / `R$`)** e **Euro (`EUR` / `€`)**.
   - O idioma selecionado define automaticamente a moeda e o texto exibido na Ficha Técnica (substituindo qualquer preenchimento manual de idioma ou moeda):
     | Código (`IdiomaEdicao`) | Opção no Admin | Moeda (`MoedaEdicao`) | Símbolo | Exibição de Preço | Rótulo na Ficha Técnica (UI PT / UI EN) |
     | :--- | :--- | :--- | :---: | :--- | :--- |
     | `PT` | Português | `BRL` | `R$` | `R$ 30,00` | `Português (Brasil)` / `Brazilian Portuguese` |
     | `EN` | Inglês | `EUR` | `€` | `€ 12,00` (UI PT) / `€ 12.00` (UI EN) | `Inglês (Internacional)` / `English (International)` |
   - O **valor numérico (`preco`)** é informado manualmente pelo administrador no formulário de cadastro/edição. Ao alternar entre Português e Inglês no formulário, o indicador/prefixo monetário muda imediatamente entre `R$` e `€`, e o backend força `moeda = idioma.getMoeda()` antes de persistir.

3. **Cadastro assistido da segunda versão no Admin ("Criar versão EN / PT"):**
   - Na listagem administrativa (`/admin/edicoes`), quando um `numero_edicao` possuir apenas uma das versões cadastradas (ex.: existe `#58 PT`, mas ainda não existe `#58 EN`), o sistema exibe um botão de atalho **"Criar versão EN"** (ou **"Criar versão PT"**).
   - Esse atalho abre o formulário de nova edição (`GET /admin/edicoes/nova?origemId={uuid}`) já com o idioma complementar selecionado (e moeda ajustada para `€` ou `R$`), pré-preenchendo campos estruturais comuns (`numeroEdicao`, `anoLancamento`, `paginas`, `formato`, `youtubeUrl` e a estrutura da lista de `entrevistas`), deixando `preco` em branco para preenchimento manual na nova moeda e permitindo ao admin:
     - Enviar capa e fotos próprias da tiragem traduzida; **ou**
     - Optar por copiar a capa/fotos da edição de origem, caso em que o backend **duplica fisicamente os arquivos `.webp` com novos UUIDs** no disco (evitando compartilhamento de arquivo físico, para que excluir ou trocar imagem de uma versão nunca quebre a outra).

---

## 3. Mapeamento Completo de Dados (`edicao.html` + `script.js`)

### 3.1. Dados Principais da Edição (Tabela `edicao`)
| Campo | Tipo Java / SQLite | Obrigatório | Origem / Uso no Admin e em `edicao.html` |
| :--- | :--- | :---: | :--- |
| `id` | `UUID` (`TEXT`) | Sim | Identificador único (padrão `Persistable<UUID>`, como em `Admin`) |
| `numeroEdicao` | `Integer` (`INTEGER`) | Sim* | `#edition-number-tag` (`EDIÇÃO #58` / `ISSUE #58`), ordenação e URL `/edicoes/{idioma}/{numero}` (*único em conjunto com `idioma`) |
| `idioma` | `IdiomaEdicao` (`TEXT`: `PT` \| `EN`) | Sim* | Seletor principal no Admin; define `moeda`, rota `/edicoes/{pt\|en}/{numero}` e linha "Idioma" na `#edition-specs-list` |
| `moeda` | `MoedaEdicao` (`TEXT`: `BRL` \| `EUR`) | Sim (auto) | Derivado automaticamente de `idioma` (`PT -> BRL/R$`, `EN -> EUR/€`) |
| `preco` | `BigDecimal` (`REAL`) | Sim | Valor manual informado no Admin; renderizado em `#edition-price-value` (`R$ 30,00` ou `€ 12,00` / `€ 12.00`) |
| `titulo` | `String` (`TEXT`) | Sim | `#edition-title`, `<title>`, `#edition-breadcrumb-current` e `#edition-lightbox-title` |
| `subtitulo` | `String` (`TEXT`) | Não | `#edition-subtitle` |
| `mesLancamento` | `String` (`TEXT`) | Sim | `#edition-meta-month` (ex.: `Agosto` em PT ou `August` em EN) |
| `anoLancamento` | `Integer` (`INTEGER`) | Sim | `#edition-meta-year` (ex.: `2026`) |
| `anoRevista` | `String` (`TEXT`) | Sim | `#edition-year-tag` e `#edition-meta-existence` (ex.: `Ano 18` / `Year 18`) |
| `paginas` | `Integer` (`INTEGER`) | Sim | `#edition-meta-pages` e linha derivada na ficha técnica (ex.: `68`) |
| `statusEstoque` | `StatusEstoque` (`TEXT`: `AVAILABLE`, `LOW`, `SOLDOUT`) | Sim | `#edition-stock-pill`, `#edition-stock-inline`, `#edition-soldout-alert` e estado do botão `#edition-btn-buy` |
| `quantidadeEstoque` | `Integer` (`INTEGER`) | Não | Controle interno opcional para baixa automática futura |
| `prazoPostagem` | `String` (`TEXT`) | Não | `#edition-shipping-deadline-text` (usa texto padrão do idioma caso vazio) |
| `capaPath` | `String` (`TEXT`) | Sim | `#edition-main-photo`, primeira miniatura e `#edition-lightbox-img` |
| `descricao` | `String` (`TEXT`) | Sim | `#edition-description-text` |
| `youtubeUrl` | `String` (`TEXT`) | Não | `#edition-video-section` e `#edition-video-external-link` (se vazio, ocultar seção) |
| `publicado` | `Boolean` (`INTEGER` `0`/`1`) | Sim | Permite salvar rascunho antes de publicar no catálogo público |
| `criadoEm` | `Instant` / `String` (`TEXT` ISO-8601) | Sim (auto) | Auditoria interna de criação |
| `atualizadoEm` | `Instant` / `String` (`TEXT` ISO-8601) | Sim (auto) | Auditoria interna de última alteração |

### 3.2. Ficha Técnica (`#edition-specs-list`)
Armazenada na própria tabela `edicao` (relação 1:1):
- `formato` (`String`) — ex.: `A5 (14,8 × 21 cm)` ou `A5 (14.8 × 21 cm)`
- `papelImpressao` (`String`) — ex.: `Miolo offset rústico 90g • Capa papel cartão 250g com verniz fosco`
- `tiragem` (`String`) — ex.: `300 cópias físicas numeradas`

*(Nota: "Idioma", "Páginas" e "Lançamento" exibidos na ficha técnica não são digitados separadamente; são derivados automaticamente de `idioma`, `paginas`, `mesLancamento`, `anoLancamento` e `anoRevista`.)*

### 3.3. Relacionamentos 1:N (Listas Dinâmicas por Versão)
1. **Fotos Extras da Galeria (`edicao_foto`)** — `#edition-thumbs-grid` e `#edition-lightbox-dialog`
   - O template suporta **capa + até 5 fotos extras** (máximo de 6 imagens no total).
   - Campos: `id`, `edicao_id`, `arquivo_path`, `legenda`, `ordem` (`1` a `5`).
2. **Hordas & Artistas Entrevistados (`edicao_entrevista`)** — `#edition-interviews-list`
   - Campos: `id`, `edicao_id`, `banda`, `pais`, `estilo`, `ordem`, `indicacao_banda_id` *(opcional — FK para `indicacao_banda(id) ON DELETE SET NULL`, permitindo vincular entrevistas oriundas de `/indique`; ver [`docs/crud-indicacao-banda.md`](./crud-indicacao-banda.md))*.
3. **Demais Conteúdos Presentes (`edicao_conteudo_extra`)** — `#edition-extras-list`
   - Campos: `id`, `edicao_id`, `descricao`, `ordem`.

### 3.4. Campos Derivados / Calculados no Backend
- **Versão Irmã no Outro Idioma (`versaoAlternativaUrl`)**: consulta se existe outra edição com `publicado = 1`, mesmo `numero_edicao` e `idioma != atual`. Se existir, disponibiliza sua URL (`/edicoes/{outroIdioma}/{numero}`) para redirecionamento automático quando o visitante clicar nas bandeiras `PT` / `EN` no cabeçalho.
- **Navegação Contextual (`#edition-nav-prev` e `#edition-nav-next`)**: consultada automaticamente no banco filtrando pelo **mesmo idioma** da edição exibida:
  - Anterior: `WHERE publicado = 1 AND idioma = :idioma AND numero_edicao < :atual ORDER BY numero_edicao DESC LIMIT 1`
  - Próxima: `WHERE publicado = 1 AND idioma = :idioma AND numero_edicao > :atual ORDER BY numero_edicao ASC LIMIT 1`
- **Links de Compartilhamento (`#edition-share-*`)**: gerados a partir da URL canônica da edição (`/edicoes/{pt|en}/{numero}`) e do título.
- **Resumo de Frete (`#edition-shipping-fee-text`)**: para edições `PT` (`R$`), mantém o resumo atual dos Correios (`R$ 13,00` para 1 edição + `R$ 2,00` por adicional). Para edições `EN` (`€`), exibe temporariamente nota neutra de envio internacional registrado (*Registered Worldwide Airmail — shipping calculated at checkout*), deixando a tabela definitiva de frete e pagamento em Euro para a **Etapa 2**.

---

## 4. Estrutura de Banco de Dados (`schema.sql` e `DatabaseConfig.java`)

### 4.1. Atualização em `src/main/resources/schema.sql`
Mantendo a convenção SQLite + UUID em `TEXT`:

```sql
CREATE TABLE IF NOT EXISTS edicao (
    id TEXT PRIMARY KEY,
    numero_edicao INTEGER NOT NULL,
    idioma TEXT NOT NULL CHECK (idioma IN ('PT', 'EN')),
    titulo TEXT NOT NULL,
    subtitulo TEXT,
    mes_lancamento TEXT NOT NULL,
    ano_lancamento INTEGER NOT NULL,
    ano_revista TEXT NOT NULL,
    paginas INTEGER NOT NULL,
    preco REAL NOT NULL,
    moeda TEXT NOT NULL CHECK (moeda IN ('BRL', 'EUR')),
    status_estoque TEXT NOT NULL DEFAULT 'AVAILABLE' CHECK (status_estoque IN ('AVAILABLE', 'LOW', 'SOLDOUT')),
    quantidade_estoque INTEGER DEFAULT 0,
    prazo_postagem TEXT,
    capa_path TEXT NOT NULL,
    descricao TEXT NOT NULL,
    formato TEXT,
    papel_impressao TEXT,
    tiragem TEXT,
    youtube_url TEXT,
    publicado INTEGER NOT NULL DEFAULT 1,
    criado_em TEXT NOT NULL,
    atualizado_em TEXT NOT NULL,
    UNIQUE (numero_edicao, idioma)
);

CREATE INDEX IF NOT EXISTS idx_edicao_idioma_publicado_numero
    ON edicao (idioma, publicado, numero_edicao DESC);

CREATE TABLE IF NOT EXISTS edicao_foto (
    id TEXT PRIMARY KEY,
    edicao_id TEXT NOT NULL,
    arquivo_path TEXT NOT NULL,
    legenda TEXT,
    ordem INTEGER NOT NULL,
    FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS edicao_entrevista (
    id TEXT PRIMARY KEY,
    edicao_id TEXT NOT NULL,
    banda TEXT NOT NULL,
    pais TEXT NOT NULL,
    estilo TEXT NOT NULL,
    ordem INTEGER NOT NULL,
    indicacao_banda_id TEXT,
    FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE CASCADE,
    FOREIGN KEY (indicacao_banda_id) REFERENCES indicacao_banda(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS edicao_conteudo_extra (
    id TEXT PRIMARY KEY,
    edicao_id TEXT NOT NULL,
    descricao TEXT NOT NULL,
    ordem INTEGER NOT NULL,
    FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE CASCADE
);
```

### 4.2. Integridade Referencial no SQLite (`DatabaseConfig.java`)
Por padrão, o driver `sqlite-jdbc` inicia conexões com `foreign_keys = OFF`. Em [`DatabaseConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/DatabaseConfig.java), ativar `SQLiteConfig`:

```java
SQLiteConfig config = new SQLiteConfig();
config.enforceForeignKeys(true);
SQLiteDataSource ds = new SQLiteDataSource(config);
ds.setUrl(datasourceUrl);
```

---

## 5. Camada Backend (Spring Boot)

### 5.1. Models, Enums e DTOs (`model/` e `dto/`)
- **`IdiomaEdicao` (enum)**:
  - `PT("pt", "Português", "Português (Brasil)", "Brazilian Portuguese", MoedaEdicao.BRL)`
  - `EN("en", "Inglês", "Inglês (Internacional)", "English (International)", MoedaEdicao.EUR)`
  - Método utilitário `Optional<IdiomaEdicao> fromSlug(String slug)` aceitando `"pt"` / `"en"` (case-insensitive).
- **`MoedaEdicao` (enum)**:
  - `BRL("R$")`, `EUR("€")`
  - Método `formatar(BigDecimal valor, IdiomaEdicao idiomaUi)`:
    - `BRL` → `R$ 30,00`
    - `EUR` → `€ 12,00` (quando UI em `PT`) ou `€ 12.00` (quando UI em `EN`).
- **`StatusEstoque` (enum)**:
  - `AVAILABLE` (`is-available`, `fa-solid fa-circle-check`, `"Disponível"` / `"Available"`),
  - `LOW` (`is-low`, `fa-solid fa-fire`, `"Últimas unidades"` / `"Last copies"`),
  - `SOLDOUT` (`is-soldout`, `fa-solid fa-skull`, `"Esgotada"` / `"Sold Out"`).
- **Entidades Spring Data JDBC** (implementando `Persistable<UUID>` como em `Admin`):
  - `Edicao` (`@Table("edicao")`)
  - `EdicaoFoto` (`@Table("edicao_foto")`)
  - `EdicaoEntrevista` (`@Table("edicao_entrevista")`)
  - `EdicaoConteudoExtra` (`@Table("edicao_conteudo_extra")`)
- **`EdicaoFormDTO`**:
  - Recebe os campos do formulário multipart (`numeroEdicao`, `idioma`, `preco`, textos, status, `MultipartFile capa`, `UUID origemId` opcional + flag `copiarImagensOrigem`, lista de fotos extras novas/existentes com legendas e IDs marcados para remoção, listas ordenadas de entrevistas e conteúdos extras).

### 5.2. Repositories (`repository/`)
- **`EdicaoRepository extends ListCrudRepository<Edicao, UUID>`**:
  - `List<Edicao> findAllByOrderByNumeroEdicaoDescIdiomaAsc()`
  - `List<Edicao> findByIdiomaOrderByNumeroEdicaoDesc(IdiomaEdicao idioma)`
  - `Optional<Edicao> findByNumeroEdicaoAndIdioma(Integer numeroEdicao, IdiomaEdicao idioma)`
  - `Optional<Edicao> findByNumeroEdicaoAndIdiomaAndPublicadoTrue(Integer numeroEdicao, IdiomaEdicao idioma)`
  - `List<Edicao> findByNumeroEdicaoAndPublicadoTrue(Integer numeroEdicao)`
  - `boolean existsByNumeroEdicaoAndIdioma(Integer numeroEdicao, IdiomaEdicao idioma)`
  - `boolean existsByNumeroEdicaoAndIdiomaAndIdNot(Integer numeroEdicao, IdiomaEdicao idioma, UUID id)`
  - `@Query("SELECT * FROM edicao WHERE publicado = 1 AND idioma = :idioma AND numero_edicao < :numero ORDER BY numero_edicao DESC LIMIT 1")` → `Optional<Edicao> findEdicaoAnteriorPublicada(String idioma, Integer numero)`
  - `@Query("SELECT * FROM edicao WHERE publicado = 1 AND idioma = :idioma AND numero_edicao > :numero ORDER BY numero_edicao ASC LIMIT 1")` → `Optional<Edicao> findProximaEdicaoPublicada(String idioma, Integer numero)`
- **`EdicaoFotoRepository`**, **`EdicaoEntrevistaRepository`**, **`EdicaoConteudoExtraRepository`**:
  - `List<...> findByEdicaoIdOrderByOrdemAsc(UUID edicaoId)`
  - `@Modifying @Query("DELETE FROM ... WHERE edicao_id = :edicaoId") void deleteByEdicaoId(UUID edicaoId)`

### 5.3. Services (`service/`)
- **Extensão pontual em `FileUploadService`**:
  - Adicionar `String duplicarImagem(String nomeArquivoOrigem)`: valida `arquivoExiste(nomeArquivoOrigem)`, gera novo `UUID.randomUUID() + ".webp"` e executa `Files.copy(...)` no `uploadDir`. Isso garante independência total de arquivos quando o admin clona uma edição entre `PT` e `EN`.
- **`EdicaoService`**:
  - **Regra Idioma → Moeda**: sempre define `edicao.setMoeda(form.getIdioma().getMoeda())` no servidor, impedindo qualquer inconsistência entre idioma e moeda.
  - **Validações** (reutilizando `ValidationUtils`):
    - Campos obrigatórios (`numeroEdicao`, `idioma`, `titulo`, `mesLancamento`, `anoLancamento`, `anoRevista`, `paginas`, `preco`, `statusEstoque`, `descricao`).
    - `numeroEdicao > 0`, `paginas > 0`, `preco >= 0` (com até 2 casas decimais).
    - Unicidade do par `(numeroEdicao, idioma)` tanto na criação (`existsByNumeroEdicaoAndIdioma`) quanto na edição (`existsByNumeroEdicaoAndIdiomaAndIdNot`), retornando mensagem amigável (ex.: *"Já existe a Edição #58 cadastrada em Inglês."*).
    - Teto máximo de **5 fotos extras** por edição e validação de URL do YouTube quando informada.
  - **Clonagem assistida (`prepararFormularioVersaoComplementar(UUID origemId)`)**:
    - Carrega a edição de origem, verifica qual idioma complementar (`PT` ↔ `EN`) está disponível (lançando erro amigável caso ambos os idiomas já existam para aquele `numeroEdicao`), copia `numeroEdicao`, `anoLancamento`, `paginas`, `formato`, `youtubeUrl` e linhas de `entrevistas`, define `idioma` para o idioma complementar, limpa `preco` e sinaliza `origemId` para permitir cópia opcional de imagens.
  - **Ciclo de vida de imagens**:
    - Cadastro normal: exige `capa` e salva até 5 fotos extras via `FileUploadService.salvarImagem`.
    - Cadastro via `origemId` com `copiarImagensOrigem = true` e sem novo upload de capa: duplica fisicamente os arquivos `.webp` com `FileUploadService.duplicarImagem`.
    - Edição: se nova capa for enviada, salva a nova e remove a antiga após commit/atualização; remove do disco apenas fotos extras desmarcadas/excluídas.
    - Exclusão: remove `capaPath` e todos os `arquivoPath` de `edicao_foto` do disco ao excluir a edição.
  - **Sincronização com Indicações de Bandas (`IndicacaoBandaService`)**:
    - Ao salvar/atualizar uma edição com `publicado = true`, aciona `IndicacaoBandaService.marcarComoPublicadasPorEdicao(edicao, entrevistas)` para promover automaticamente as indicações correspondentes (por `indicacao_banda_id` explícito ou por correspondência inequívoca de `(banda, pais)` normalizados) para `PUBLICADA`, removendo-as da vitrine pública de `/indique` (detalhes em [`docs/crud-indicacao-banda.md`](./crud-indicacao-banda.md)). Se a edição for despublicada ou excluída (sem versão irmã publicada), aciona `reverterPublicadasDaEdicao`.

### 5.4. Exposição Pública de Imagens (`config/WebMvcConfig.java`)
- Configurar `WebMvcConfigurer.addResourceHandlers` mapeando `/uploads/**` para `Paths.get(uploadDir).toUri().toString()` (garantindo barra final e cache HTTP adequado).

### 5.5. Controllers (`controller/`)
- **`AdminPagesController` (`/admin`)**:
  - Evoluir `templates/admin/index.html` de placeholder para painel inicial com acesso direto ao gerenciamento de Edições (`/admin/edicoes`) e contadores básicos (total em PT, total em EN, esgotadas/rascunhos).
- **`AdminEdicaoController` (`/admin/edicoes`)**:
  - `GET /admin/edicoes` — Lista edições com busca por número/título e filtros por **idioma (`TODOS`, `PT`, `EN`)** e **status de estoque**, indicando visualmente quais edições já possuem par `PT`/`EN` e exibindo atalho **"+ Versão EN"** / **"+ Versão PT"** quando faltar a outra versão.
  - `GET /admin/edicoes/nova` (com `@RequestParam(required = false) UUID origemId`) — Exibe formulário limpo (padrão `PT` / `R$`) ou pré-preenchido a partir de `origemId`.
  - `POST /admin/edicoes` — Valida, persiste edição, imagens e listas 1:N; redireciona com `RedirectAttributes`.
  - `GET /admin/edicoes/{id}/editar` — Carrega edição e relacionamentos no formulário.
  - `POST /admin/edicoes/{id}` — Atualiza edição, listas e arquivos.
  - `POST /admin/edicoes/{id}/excluir` — Exclui edição e limpa arquivos `.webp` correspondentes.
- **`EdicaoPublicController` (ou rotas em `PublicPagesController`)**:
  - `GET /edicoes/{idioma}/{numero}`:
    - Converte `{idioma}` (`pt` ou `en`, retornando `404` se inválido).
    - Busca `findByNumeroEdicaoAndIdiomaAndPublicadoTrue(numero, idioma)` (retornando `404` se não existir).
    - Consulta edição anterior e próxima publicadas **no mesmo `idioma`**.
    - Verifica se a versão irmã publicada no outro idioma existe (`versaoAlternativaUrl`).
    - Renderiza `templates/edicao.html`.
  - `GET /edicoes/{numero}` (atalho retrocompatível):
    - Busca versões publicadas de `{numero}`: se existir `PT`, redireciona (`redirect:/edicoes/pt/{numero}`); senão, se existir `EN`, redireciona (`redirect:/edicoes/en/{numero}`); caso contrário, retorna `404`.

---

## 6. Camada Frontend & Templates

### 6.1. Painel Administrativo (`templates/admin/`)
1. **Dashboard (`templates/admin/index.html`)**: navegação administrativa com link para `/admin/edicoes`, link para voltar ao site público e botão de logout (`POST /logout` com CSRF).
2. **Listagem (`templates/admin/edicoes/lista.html`)**:
   - Filtros por busca textual, idioma (`Todos`, `Português (R$)`, `Inglês (€)`) e estoque.
   - Tabela com miniatura da capa (`/uploads/...`), `#` da edição, badge de idioma/moeda (`🇧🇷 PT • R$` ou `🇺🇸 EN • €`), título, lançamento, valor formatado, status de estoque, publicado/rascunho e ações: **Ver no site**, **Editar**, **Criar versão EN/PT** (quando aplicável) e **Excluir** (com confirmação).
3. **Formulário (`templates/admin/edicoes/form.html`)**:
   - `enctype="multipart/form-data"` e `th:action` (injetando `_csrf` automaticamente).
   - Seções organizadas:
     1. **Idioma, Identificação & Lançamento**: seletor em destaque **Idioma da Edição (`Português — R$` | `Inglês — €`)**, número da edição, título, subtítulo, mês, ano de lançamento, ano da revista, páginas e flag publicado.
     2. **Venda & Estoque**: campo **Preço** com prefixo dinâmico (`R$` quando `PT`, `€` quando `EN`) atualizado imediatamente via JS ao mudar o idioma, exibição somente leitura da moeda (`BRL` / `EUR`), status de estoque (`AVAILABLE`, `LOW`, `SOLDOUT`), quantidade opcional e prazo de postagem.
     3. **Galeria**: upload da capa + preview (com opção de reaproveitar/duplicar capa da edição de origem quando aberto via `origemId`) e gerenciamento de até 5 fotos extras com legenda e remoção individual.
     4. **Conteúdo Editorial**: descrição principal + listas dinâmicas no formulário (adicionar/remover/reordenar linhas) para **Entrevistas** (`[banda, país, estilo]`) e **Demais Conteúdos**.
     5. **Ficha Técnica & Vídeo**: formato, papel/impressão, tiragem (exibindo lembrete de que o campo "Idioma" na ficha técnica será preenchido automaticamente conforme o idioma da edição) e URL do YouTube.

### 6.2. Página Pública (`templates/edicao.html` e `static/js/script.js`)
- **`templates/edicao.html`**:
  - Adicionar `class="page-edicao"` ao `<body>` (ou marcador equivalente no fragmento/layout) e converter links estáticos (`index.html`, `arquivo.html`, `assinatura.html`) para `th:href="@{/}"`, `th:href="@{/arquivo}"`, `th:href="@{/assinatura}"`.
  - Renderizar via **Thymeleaf** todos os dados da edição: tags (`#edition-number-tag`, `#edition-year-tag`), título, subtítulo, metadados, valor formatado em `R$` ou `€` (`#edition-price-value`), badges/alerta de estoque, entrevistas, conteúdos extras, ficha técnica (com idioma derivado de `IdiomaEdicao`), seção de vídeo condicional e cards de navegação anterior/próxima apontando para `/edicoes/{pt|en}/{num}`.
  - Incluir um banner de aviso inicialmente oculto (`#edition-single-lang-notice`, `hidden`) logo acima do bloco principal para informar quando o visitante alternar a UI para um idioma no qual aquela edição física não foi lançada.
  - Embutir `<script type="application/json" id="edition-page-data">` contendo `id`, `editionNumber`, `editionLang` (`"pt"` ou `"en"`), `alternateLangUrl` (ex.: `"/edicoes/en/58"` ou `null`), `title`, `price`, `currency` (`"R$"` ou `"€"`), `inStock`, `stockStatus`, `cover`, `extraPhotos` e `youtubeUrl`.
- **`static/js/script.js`**:
  - Atualizar `formatCurrency(value, currency)` para suportar `'R$'` (`R$ 30,00`) e `'€'` (`€ 12,00` quando `STATE.lang === 'pt'` e `€ 12.00` quando `STATE.lang === 'en'`).
  - Na página de edição (`#edition-page-data` presente):
    - Ao carregar diretamente `/edicoes/pt/{n}` ou `/edicoes/en/{n}`, sincronizar `STATE.lang` com `editionLang` da URL para que os rótulos da interface correspondam ao idioma da edição aberta.
    - Quando o visitante clicar manualmente nas bandeiras `.lang-btn` (`PT` / `EN`) no cabeçalho estando na página de edição:
      1. Se o idioma clicado for diferente de `editionLang` e `alternateLangUrl` existir, redirecionar via `window.location.href = alternateLangUrl`.
      2. Se `alternateLangUrl` for `null` (edição existente em apenas um idioma), manter na página atual, aplicar `applyLanguage(selectedLang)` nos rótulos fixos da interface e exibir `#edition-single-lang-notice` explicando que aquela edição impressa específica foi publicada apenas em Português (ou apenas em Inglês).
    - Manter no JS apenas as interações ricas de cliente (troca de foto principal pelas miniaturas, `<dialog id="edition-lightbox-dialog">`, facade de vídeo do YouTube, copiar link e compartilhamento social), sem sobrescrever o HTML renderizado pelo servidor com `ZINE_CATALOG`.

---

## 7. Segurança e Configurações

- **[`SecurityConfig.java`](../src/main/java/com/armamentobelico/portalarmamentobelico/config/SecurityConfig.java)**:
  - Rotas `/admin/edicoes/**` permanecem protegidas por `.requestMatchers("/admin", "/admin/**").authenticated()`.
  - Garantir `.requestMatchers("/uploads/**", "/edicoes/**").permitAll()`.
  - Adicionar `path.startsWith("/uploads/")` em `CsrfTokenInitializationFilter.shouldNotFilter(...)` para que requisições de imagens de capas e fotos extras não inicializem token CSRF nem criem sessão HTTP desnecessariamente (preservando o comportamento validado em [`AdminLoginSecurityTests.java`](../src/test/java/com/armamentobelico/portalarmamentobelico/AdminLoginSecurityTests.java)).
  - Manter proteção CSRF ativa em todos os `POST` administrativos (`criar`, `editar`, `excluir`).
- **[`application.properties`](../src/main/resources/application.properties)**:
  - `spring.servlet.multipart.max-file-size=10MB` e `spring.servlet.multipart.max-request-size=30MB` já suportam o envio simultâneo de capa + 5 fotos extras.

---

## 8. Ordem Sugerida de Implementação (Fluxo TDD: Red → Green → Refactor)

1. **Banco & Configuração SQLite**: ativar `enforceForeignKeys(true)` em `DatabaseConfig`, atualizar `schema.sql` com constraint `UNIQUE (numero_edicao, idioma)` e criar enums `IdiomaEdicao`, `MoedaEdicao`, `StatusEstoque` + entidades `Edicao`, `EdicaoFoto`, `EdicaoEntrevista`, `EdicaoConteudoExtra`.
2. **Repositories & Testes de Persistência**: implementar `EdicaoRepository` e repositórios filhos, cobrindo com testes a unicidade composta `(numero_edicao, idioma)` (permitindo `#58 PT` + `#58 EN` e bloqueando duplicata no mesmo idioma) e as consultas de anterior/próxima filtradas por idioma.
3. **Upload & `EdicaoService`**: adicionar `duplicarImagem` em `FileUploadService`, configurar `WebMvcConfig` (`/uploads/**` + `SecurityConfig`) e implementar `EdicaoService` com testes unitários/integração validando vínculo automático `PT -> BRL` e `EN -> EUR`, clonagem independente de arquivos `.webp` e limpeza de disco ao editar/excluir.
4. **Painel Admin (`/admin/edicoes`)**: implementar `EdicaoFormDTO`, `AdminEdicaoController`, templates `admin/index.html`, `admin/edicoes/lista.html` e `admin/edicoes/form.html` (com troca reativa `R$`/`€` e atalho "Criar versão EN/PT"), validados com testes `MockMvc` (autenticação, CSRF e upload multipart).
5. **Página Pública (`/edicoes/{idioma}/{numero}`)**: implementar controller público, refatorar `templates/edicao.html` para Thymeleaf, ajustar `script.js` para ler `#edition-page-data`, formatar Euro (`€`) e tratar a alternância das bandeiras `PT`/`EN` (redirect quando houver versão irmã ou aviso quando for edição de idioma único), cobrindo com testes `MockMvc`.

---

## 9. Escopo Reservado para a Etapa 2 (Pós-CRUD)

Itens intencionalmente desacoplados desta primeira entrega para serem especificados e implementados na sequência:
- Substituição completa de `ZINE_CATALOG` na **Home (`/`)** e em **Edições e Loja (`/arquivo`)**, carregando do banco as edições publicadas conforme o idioma selecionado (`PT` ou `EN`).
- Definição das regras e valores de **frete internacional em Euro (`€`)** (substituindo o antigo `SHIPPING_CONFIG['US$']`).
- Adaptação do **Necrocarrinho** para suportar `BRL` (`R$`) e `EUR` (`€`) com validação de moeda única por pedido.
- Atualização da **forma de pagamento / checkout internacional** para pedidos de edições em inglês (`EUR`).
