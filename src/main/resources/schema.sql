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
