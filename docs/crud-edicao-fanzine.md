# Análise e Especificação: CRUD de Edições do Fanzine

Este documento registra tudo o que é necessário para implementar o CRUD completo de **Edições do Fanzine** no portal **Armamento Bélico**, tomando como base o template [`src/main/resources/templates/edicao.html`](../src/main/resources/templates/edicao.html), o comportamento atual em [`src/main/resources/static/js/script.js`](../src/main/resources/static/js/script.js) e a arquitetura já existente no projeto (Spring Boot, Spring Data JDBC, SQLite, Thymeleaf e Spring Security).

---

## 1. Diagnóstico do Estado Atual

Atualmente:
- O template `edicao.html` possui a estrutura visual completa da página de detalhes de uma edição (com dados estáticos da edição `#58` no HTML e preenchimento dinâmico via `renderEditionDetailPage()` no `script.js` a partir do objeto mock `ZINE_CATALOG`).
- O template usa links estáticos (`index.html`, `arquivo.html`, `assinatura.html`, `assets/images/...`) que precisam ser convertidos para rotas Thymeleaf/Spring MVC.
- O backend já possui:
  - Banco **SQLite** (`armamentobelico.db`) inicializado via `src/main/resources/schema.sql`.
  - Padrão de entidades com `UUID` (`Persistable<UUID>`) em `model/Admin.java`.
  - Segurança configurada em `SecurityConfig.java` protegendo `/admin/**` e inicializando token CSRF.
  - Serviço de upload e conversão automática para **WebP** em `service/FileUploadService.java` (salvando em `${user.dir}/armamentobelico-uploads`, com limite de `10MB` por arquivo e `30MB` por requisição no `application.properties`).
  - Utilitários de validação em `utils/ValidationUtils.java`.
- Ainda **não existem**:
  - Tabelas de edição no `schema.sql`.
  - Modelos, repositórios, serviços e controllers para Edições.
  - Mapeamento HTTP para servir as imagens salvas no diretório `upload.dir`.
  - Telas administrativas de listagem, criação e edição em `templates/admin/`.

---

## 2. Mapeamento Completo de Dados (`edicao.html` + `script.js`)

Analisando todos os blocos de `edicao.html` e o método `renderEditionDetailPage()` em `script.js`, uma edição do fanzine exige os seguintes dados:

### 2.1. Dados Principais da Edição
| Campo | Tipo sugerido | Obrigatório | Origem / Uso no `edicao.html` |
| :--- | :--- | :---: | :--- |
| `id` | `UUID` (`TEXT`) | Sim | Identificador único (padrão do projeto, como em `Admin`) |
| `numeroEdicao` | `Integer` | Sim (único) | `#edition-number-tag` (`EDIÇÃO #58`), ordenação e URL `/edicoes/{numero}` |
| `titulo` | `String` | Sim | `#edition-title`, `<title>`, breadcrumb (`Armamento Bélico #58`) |
| `subtitulo` | `String` | Não | `#edition-subtitle` |
| `mesLancamento` | `String` | Sim | `#edition-meta-month` (ex.: `Agosto`) |
| `anoLancamento` | `Integer` | Sim | `#edition-meta-year` (ex.: `2026`) |
| `anoRevista` | `String` | Sim | `#edition-year-tag` e `#edition-meta-existence` (ex.: `Ano 18`) |
| `paginas` | `Integer` | Sim | `#edition-meta-pages` e linha na ficha técnica (ex.: `68`) |
| `preco` | `BigDecimal` | Sim | `#edition-price-value` (ex.: `30.00`) |
| `moeda` | `String` | Sim | Padrão `R$` (compatível com regra de frete em `script.js`) |
| `statusEstoque` | `Enum` (`AVAILABLE`, `LOW`, `SOLDOUT`) | Sim | `#edition-stock-pill`, `#edition-stock-inline`, `#edition-soldout-alert` e estado do botão `#edition-btn-buy` |
| `quantidadeEstoque` | `Integer` | Não | Controle interno opcional para baixa automática futura |
| `prazoPostagem` | `String` | Não | `#edition-shipping-deadline-text` (com valor padrão se vazio) |
| `capaPath` | `String` | Sim | `#edition-main-photo`, primeira miniatura e lightbox |
| `descricao` | `String` (TEXT) | Sim | `#edition-description-text` |
| `youtubeUrl` | `String` | Não | `#edition-video-section` e `#edition-video-external-link` (se vazio, ocultar seção) |
| `ativo` / `publicado` | `Boolean` | Sim | Permite salvar rascunho antes de publicar no catálogo público |

### 2.2. Ficha Técnica (`#edition-specs-list`)
Pode ficar na própria tabela `edicao` (relação 1:1):
- `formato` (`String`) — ex.: `A5 (14,8 × 21 cm)`
- `papelImpressao` (`String`) — ex.: `Miolo offset rústico 90g • Capa papel cartão 250g com verniz fosco`
- `idioma` (`String`) — ex.: `Português (Brasil)`
- `tiragem` (`String`) — ex.: `300 cópias físicas numeradas`

*(Nota: "Páginas" e "Lançamento" exibidos na ficha técnica são derivados dos campos principais `paginas`, `mesLancamento`, `anoLancamento` e `anoRevista`.)*

### 2.3. Relacionamentos 1:N (Listas Dinâmicas)
1. **Fotos Extras da Galeria (`edicao_foto`)** — `#edition-thumbs-grid` e `#edition-lightbox-dialog`
   - O template suporta **capa + até 5 fotos extras** (máximo de 6 imagens no total).
   - Campos: `id`, `edicao_id`, `arquivo_path`, `legenda`, `ordem` (1 a 5).
2. **Hordas & Artistas Entrevistados (`edicao_entrevista`)** — `#edition-interviews-list`
   - Campos: `id`, `edicao_id`, `banda`, `pais`, `estilo`, `ordem`.
3. **Demais Conteúdos Presentes (`edicao_conteudo_extra`)** — `#edition-extras-list`
   - Campos: `id`, `edicao_id`, `descricao`, `ordem`.

### 2.4. Campos Derivados / Calculados no Backend
- **Navegação Contextual (`#edition-nav-prev` e `#edition-nav-next`)**: não precisa ser cadastrada manualmente; deve ser consultada automaticamente no banco buscando a edição publicada imediatamente anterior (`numero_edicao < atual ORDER BY numero_edicao DESC LIMIT 1`) e a próxima (`numero_edicao > atual ORDER BY numero_edicao ASC LIMIT 1`).
- **Links de Compartilhamento (`#edition-share-*`)**: gerados a partir da URL canônica da edição (`/edicoes/{numero}`) e do título.
- **Frete (`#edition-shipping-fee-text`)**: atualmente usa `SHIPPING_CONFIG` no `script.js` (`R$ 13,00` + `R$ 2,00` adicional); pode ser mantido centralizado ou parametrizado futuramente.

> **Observação sobre Internacionalização (PT/EN):** o `script.js` atual possui `ZINE_CATALOG.pt` e `ZINE_CATALOG.en`. Para o MVP do CRUD, recomenda-se cadastrar os textos principais em português e decidir em seguida se haverá colunas/tabela de tradução para inglês (`en`) ou se apenas os rótulos fixos da interface permanecerão bilíngues via `data-i18n`.

---

## 3. Estrutura de Banco de Dados (`schema.sql`)

Adicionar ao [`src/main/resources/schema.sql`](../src/main/resources/schema.sql) mantendo a convenção SQLite + UUID em `TEXT`:

```sql
CREATE TABLE IF NOT EXISTS edicao (
    id TEXT PRIMARY KEY,
    numero_edicao INTEGER NOT NULL UNIQUE,
    titulo TEXT NOT NULL,
    subtitulo TEXT,
    mes_lancamento TEXT NOT NULL,
    ano_lancamento INTEGER NOT NULL,
    ano_revista TEXT NOT NULL,
    paginas INTEGER NOT NULL,
    preco REAL NOT NULL,
    moeda TEXT NOT NULL DEFAULT 'R$',
    status_estoque TEXT NOT NULL DEFAULT 'AVAILABLE',
    quantidade_estoque INTEGER DEFAULT 0,
    prazo_postagem TEXT,
    capa_path TEXT NOT NULL,
    descricao TEXT NOT NULL,
    formato TEXT,
    papel_impressao TEXT,
    idioma TEXT,
    tiragem TEXT,
    youtube_url TEXT,
    publicado INTEGER NOT NULL DEFAULT 1,
    criado_em TEXT NOT NULL,
    atualizado_em TEXT NOT NULL
);

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
    FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS edicao_conteudo_extra (
    id TEXT PRIMARY KEY,
    edicao_id TEXT NOT NULL,
    descricao TEXT NOT NULL,
    ordem INTEGER NOT NULL,
    FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE CASCADE
);
```

> **Atenção ao SQLite:** garantir que `PRAGMA foreign_keys = ON;` esteja ativo na conexão (ou gerenciar a remoção explícita das tabelas filhas no service ao atualizar/excluir) para que `ON DELETE CASCADE` funcione corretamente.

---

## 4. Camada Backend (Spring Boot)

### 4.1. Models (`model/`)
- `StatusEstoque` (enum): `AVAILABLE`, `LOW`, `SOLDOUT` (com métodos auxiliares para texto/classe CSS ou mapeamento no template).
- `Edicao` (`@Table("edicao")`, implementando `Persistable<UUID>` como `Admin`).
- `EdicaoFoto` (`@Table("edicao_foto")`).
- `EdicaoEntrevista` (`@Table("edicao_entrevista")`).
- `EdicaoConteudoExtra` (`@Table("edicao_conteudo_extra")`).
- DTO/Form Object (`EdicaoFormDTO`) para receber os dados do formulário multipart (campos da edição, `MultipartFile capa`, `List<MultipartFile> fotosExtras`, legendas, listas de entrevistas e conteúdos extras) sem acoplar diretamente as entidades de persistência.

### 4.2. Repositories (`repository/`)
- `EdicaoRepository extends ListCrudRepository<Edicao, UUID>`:
  - `Optional<Edicao> findByNumeroEdicao(Integer numeroEdicao)`
  - `boolean existsByNumeroEdicaoAndIdNot(Integer numeroEdicao, UUID id)`
  - `List<Edicao> findAllByOrderByNumeroEdicaoDesc()`
  - `List<Edicao> findByPublicadoTrueOrderByNumeroEdicaoDesc()`
  - Consultas para edição anterior e próxima publicadas.
- `EdicaoFotoRepository`, `EdicaoEntrevistaRepository`, `EdicaoConteudoExtraRepository` (ordenados por `ordem ASC`, com método `deleteByEdicaoId(UUID edicaoId)`).

### 4.3. Services (`service/`)
- **`EdicaoService`**:
  - **Validações** (reutilizando `ValidationUtils`): campos obrigatórios, `numeroEdicao > 0` e único, `paginas > 0`, `preco >= 0`, máximo de **5 fotos extras** por edição, validação de URL do YouTube (quando preenchida).
  - **Upload e limpeza de imagens** (integrando com `FileUploadService`):
    - No cadastro: salvar capa (`salvarImagem`) e até 5 fotos extras.
    - Na edição: se uma nova capa for enviada, salvar a nova e remover a antiga (`removerImagem`); permitir remover fotos extras existentes e adicionar novas respeitando o teto de 5.
    - Na exclusão: remover a capa e todas as fotos extras do disco antes/após deletar do banco.
  - **Atualização de listas 1:N**: substituir ou sincronizar `edicao_entrevista` e `edicao_conteudo_extra` mantendo a ordem definida no formulário.
- **Exposição pública dos uploads (`config/WebMvcConfig.java`)**:
  - Hoje o `FileUploadService` grava em `${upload.dir}` (`${user.dir}/armamentobelico-uploads`), mas não há mapeamento HTTP para servir esses arquivos.
  - Criar um `WebMvcConfigurer` mapeando `/uploads/**` para `file:${upload.dir}/` (ou um `MediaController` dedicado) e garantir em `SecurityConfig.java` que `/uploads/**` seja `permitAll()`.

### 4.4. Controllers (`controller/`)
- **`AdminEdicaoController` (`/admin/edicoes`)**:
  - `GET /admin/edicoes` — Lista todas as edições (com busca por número/título, filtro por status e ação rápida de publicar/esgotar).
  - `GET /admin/edicoes/nova` — Exibe formulário de cadastro.
  - `POST /admin/edicoes` — Valida, salva edição, imagens e listas; redireciona com `RedirectAttributes` (flash message de sucesso/erro).
  - `GET /admin/edicoes/{id}/editar` — Carrega edição, fotos extras, entrevistas e conteúdos extras no formulário.
  - `POST /admin/edicoes/{id}` — Atualiza edição e arquivos.
  - `POST /admin/edicoes/{id}/excluir` — Exclui edição e remove imagens do disco.
- **`PublicPagesController` (ou `EdicaoController` público)**:
  - `GET /edicoes/{numero}` — Busca edição publicada pelo `numeroEdicao` (retornando 404 amigável se não existir), busca anterior/próxima e renderiza `templates/edicao.html`.
  - Atualizar também `GET /arquivo` e `GET /` para futuramente consumirem a lista de edições do banco em vez do mock JS.

---

## 5. Camada Frontend & Templates

### 5.1. Painel Administrativo (`templates/admin/`)
1. **Menu / Dashboard (`templates/admin/index.html`)**: substituir o placeholder atual por navegação para o módulo de Edições (`/admin/edicoes`).
2. **Listagem (`templates/admin/edicoes/lista.html`)**:
   - Tabela com miniatura da capa, `#` da edição, título, data de lançamento, preço, badge de status (`Disponível`, `Últimas unidades`, `Esgotada`), publicado/rascunho e botões **Visualizar**, **Editar** e **Excluir** (com confirmação).
3. **Formulário (`templates/admin/edicoes/form.html`)**:
   - `enctype="multipart/form-data"` e inclusão automática do token CSRF via Thymeleaf (`th:action`).
   - Seções organizadas:
     1. **Identificação & Lançamento** (número, título, subtítulo, mês, ano, ano da revista, páginas, publicado).
     2. **Venda & Estoque** (preço, moeda, status de estoque, quantidade, prazo de postagem).
     3. **Galeria** (upload da capa + preview; slots/upload para até 5 fotos extras com legenda e opção de remoção na edição).
     4. **Conteúdo Editorial** (descrição; repeater JS simples para adicionar/remover linhas de **Entrevistas** `[banda, país, estilo]` e **Demais Conteúdos**).
     5. **Ficha Técnica & Mídia** (formato, papel/impressão, idioma, tiragem, URL do YouTube).

### 5.2. Refatoração de `templates/edicao.html` e `static/js/script.js`
- **`edicao.html`**:
  - Substituir links estáticos (`index.html`, `arquivo.html`, `assinatura.html`) por `th:href="@{/}"`, `th:href="@{/arquivo}"`, `th:href="@{/assinatura}"`.
  - Renderizar todos os textos, metadados, preço, alerta de esgotado, lista de entrevistas, lista de extras, ficha técnica, seção de vídeo (com `th:if="${edicao.youtubeUrl != null and !edicao.youtubeUrl.isBlank()}"`) e cards de edição anterior/próxima via **Thymeleaf** diretamente do servidor.
  - Renderizar a capa e as miniaturas com `th:src="@{/uploads/{img}(img=...)}"`.
- **`script.js`**:
  - Manter apenas o comportamento interativo do cliente na página de edição: troca de foto principal ao clicar nas miniaturas, abertura/navegação do `<dialog id="edition-lightbox-dialog">`, botão "Copiar link", links de compartilhamento, carregamento sob demanda do iframe do YouTube e adição da edição ao carrinho.
  - Remover a dependência de `ZINE_CATALOG` na página de detalhes, lendo os dados necessários (ex.: `id`, `title`, `price`, `cover`, `photos`) de atributos `data-*` ou de um `<script type="application/json">` renderizado pelo Thymeleaf.

---

## 6. Segurança e Configurações

- **`SecurityConfig.java`**:
  - Rotas `/admin/edicoes/**` já ficam protegidas por `.requestMatchers("/admin", "/admin/**").authenticated()`.
  - Garantir `.requestMatchers("/uploads/**", "/edicoes/**").permitAll()` para acesso público às páginas e imagens.
  - Manter proteção CSRF ativa nos formulários POST de criação, edição e exclusão no admin.
- **`application.properties`**:
  - `spring.servlet.multipart.max-file-size=10MB` e `max-request-size=30MB` já estão configurados e suportam capa + 5 fotos extras.

---

## 7. Ordem Sugerida de Implementação

1. **Banco & Models**: atualizar `schema.sql` e criar `StatusEstoque`, `Edicao`, `EdicaoFoto`, `EdicaoEntrevista`, `EdicaoConteudoExtra`.
2. **Repositories & Upload**: criar repositories e configurar `WebMvcConfigurer` para servir `/uploads/**` (+ liberar em `SecurityConfig.java`).
3. **Service**: implementar `EdicaoService` com validações, integração com `FileUploadService` e manipulação das listas 1:N.
4. **Admin CRUD**: criar `EdicaoFormDTO`, `AdminEdicaoController` e templates `admin/edicoes/lista.html` e `admin/edicoes/form.html`.
5. **Página Pública**: criar rota `GET /edicoes/{numero}`, conectar `edicao.html` ao Thymeleaf e desacoplar `renderEditionDetailPage()` do mock `ZINE_CATALOG` em `script.js`.
6. **Testes**: adicionar testes de serviço/repositório e testes `MockMvc` (seguindo `AdminLoginSecurityTests.java`) cobrindo autenticação em `/admin/edicoes/**`, upload de imagens e renderização pública de `/edicoes/{numero}`.
