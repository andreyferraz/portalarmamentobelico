CREATE TABLE IF NOT EXISTS admin (
	id TEXT PRIMARY KEY,
	username TEXT NOT NULL UNIQUE,
	password TEXT NOT NULL
);

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
	FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS edicao_conteudo_extra (
	id TEXT PRIMARY KEY,
	edicao_id TEXT NOT NULL,
	descricao TEXT NOT NULL,
	ordem INTEGER NOT NULL,
	FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE CASCADE
);

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

CREATE TABLE IF NOT EXISTS evento (
	id TEXT PRIMARY KEY,
	titulo TEXT NOT NULL,
	flyer_path TEXT NOT NULL,
	cidade TEXT NOT NULL,
	estado TEXT NOT NULL CHECK (estado IN (
		'AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG',
		'PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'
	)),
	data_evento TEXT NOT NULL,
	horario TEXT NOT NULL,
	contato_organizador TEXT NOT NULL,
	whatsapp_publicador TEXT NOT NULL,
	papel_publicador TEXT NOT NULL CHECK (papel_publicador IN ('ORGANIZADOR', 'AJUDANTE')),
	aceitou_regras INTEGER NOT NULL DEFAULT 1 CHECK (aceitou_regras IN (0, 1)),
	status_moderacao TEXT NOT NULL DEFAULT 'PENDENTE'
		CHECK (status_moderacao IN ('PENDENTE', 'APROVADO', 'RECUSADO')),
	motivo_recusa TEXT,
	moderado_em TEXT,
	moderado_por TEXT,
	criado_em TEXT NOT NULL,
	atualizado_em TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_evento_status_data
	ON evento (status_moderacao, data_evento);

CREATE INDEX IF NOT EXISTS idx_evento_estado_status_data
	ON evento (estado, status_moderacao, data_evento);

CREATE TABLE IF NOT EXISTS indicacao_banda (
	id TEXT PRIMARY KEY,
	nome_banda TEXT NOT NULL,
	nome_banda_normalizado TEXT NOT NULL,
	pais TEXT NOT NULL,
	pais_normalizado TEXT NOT NULL,
	regiao TEXT,
	titulo_material_fisico TEXT NOT NULL,
	link_audicao TEXT NOT NULL,
	status TEXT NOT NULL DEFAULT 'PENDENTE' CHECK (
		status IN (
			'PENDENTE',
			'RECUSADA',
			'APROVADA',
			'EM_ENTREVISTA',
			'ENTREVISTA_CONCLUIDA',
			'PUBLICADA'
		)
	),
	homonimo INTEGER NOT NULL DEFAULT 0 CHECK (homonimo IN (0, 1)),
	esclarecimento_homonimo TEXT,
	edicao_homonimo_referencia TEXT,
	motivo_recusa TEXT,
	edicao_publicada_id TEXT,
	numero_edicao_publicada INTEGER,
	moderado_em TEXT,
	publicada_em TEXT,
	criado_em TEXT NOT NULL,
	atualizado_em TEXT NOT NULL,
	FOREIGN KEY (edicao_publicada_id) REFERENCES edicao(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_indicacao_banda_chave_status
	ON indicacao_banda (nome_banda_normalizado, pais_normalizado, status);

CREATE INDEX IF NOT EXISTS idx_indicacao_banda_status_criado
	ON indicacao_banda (status, criado_em DESC);
