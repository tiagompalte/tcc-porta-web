CREATE TABLE endereco (
    codigo BIGINT(20) AUTO_INCREMENT,
    logradouro VARCHAR(80),
    numero VARCHAR(8),
    complemento VARCHAR(20),
    bairro VARCHAR(30),
    cep VARCHAR(10),
    cidade VARCHAR(30),
    estado VARCHAR(2),
    CONSTRAINT PK_endereco PRIMARY KEY (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE pessoa (
    codigo BIGINT(20) AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL,
    tipo_pessoa VARCHAR(8) NOT NULL,
    cpf_cnpj VARCHAR(14) NOT NULL,
    genero VARCHAR(10),
    telefone VARCHAR(15),
    data_nascimento DATE,
    CONSTRAINT PK_pessoa PRIMARY KEY (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE estabelecimento (
    codigo BIGINT(20) AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL,
    codigo_endereco BIGINT(20) NOT NULL,
    codigo_responsavel BIGINT(20) NOT NULL,
    CONSTRAINT PK_estabelecimento PRIMARY KEY (codigo),
    CONSTRAINT FK_estabelecimento_endereco FOREIGN KEY (codigo_endereco) REFERENCES endereco(codigo),
    CONSTRAINT FK_estabelecimento_pessoa FOREIGN KEY (codigo_responsavel) REFERENCES pessoa(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE porta (
    codigo BIGINT(20) AUTO_INCREMENT,
    codigo_estabelecimento BIGINT(20) NOT NULL,
    descricao VARCHAR(50) NOT NULL,
    senha VARCHAR(120) NOT NULL,
    CONSTRAINT PK_porta PRIMARY KEY (codigo),
    CONSTRAINT FK_porta_estabelecimento FOREIGN KEY (codigo_estabelecimento) REFERENCES estabelecimento(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE usuario (
    codigo BIGINT(20) AUTO_INCREMENT,
    codigo_estabelecimento BIGINT(20) NOT NULL,
	codigo_pessoa BIGINT(20) NOT NULL,
    email VARCHAR(50) NOT NULL,
    senha VARCHAR(120) NOT NULL,
    rfid VARCHAR(25),
    ativo BOOLEAN DEFAULT true NOT NULL,
    CONSTRAINT PK_usuario PRIMARY KEY (codigo),
    CONSTRAINT FK_usuario_estabelecimento FOREIGN KEY (codigo_estabelecimento) REFERENCES estabelecimento(codigo),
    CONSTRAINT FK_usuario_pessoa FOREIGN KEY (codigo_pessoa) REFERENCES pessoa(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE autorizacao (
    codigo_usuario BIGINT(20) NOT NULL,
    codigo_porta BIGINT(20) NOT NULL,
    codigo_estabelecimento BIGINT(20) NOT NULL,
    sequencia BIGINT(20) NOT NULL,
    tipo_autorizacao VARCHAR(10) NOT NULL,
    dia_semana INT(1),
    hora_inicio TIME,
    hora_fim TIME,
    data_hora_inicio DATETIME,
    data_hora_fim DATETIME,
    CONSTRAINT PK_autorizacao PRIMARY KEY (codigo_usuario, codigo_porta, codigo_estabelecimento, sequencia),
    CONSTRAINT FK_autorizacao_usuario FOREIGN KEY (codigo_usuario) REFERENCES usuario(codigo),
    CONSTRAINT FK_autorizacao_porta FOREIGN KEY (codigo_porta) REFERENCES porta(codigo),
    CONSTRAINT FK_autorizacao_estabelecimento FOREIGN KEY (codigo_estabelecimento) REFERENCES estabelecimento(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE grupo (
    codigo BIGINT(20),
    nome VARCHAR(50) NOT NULL,
    visivel_pagina BOOLEAN DEFAULT true NOT NULL,
    CONSTRAINT PK_grupo PRIMARY KEY (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE permissao (
    codigo BIGINT(20),
    nome VARCHAR(50) NOT NULL,
    CONSTRAINT PK_permissao PRIMARY KEY (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE usuario_grupo (
    codigo_usuario BIGINT(20) NOT NULL,
    codigo_grupo BIGINT(20) NOT NULL,
    CONSTRAINT PK_usuario_grupo PRIMARY KEY (codigo_usuario, codigo_grupo),
    CONSTRAINT FK_usuario_grupo_usuario FOREIGN KEY (codigo_usuario) REFERENCES usuario(codigo),
    CONSTRAINT FK_usuario_grupo_grupo FOREIGN KEY (codigo_grupo) REFERENCES grupo(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE grupo_permissao (
    codigo_grupo BIGINT(20) NOT NULL,
    codigo_permissao BIGINT(20) NOT NULL,
    CONSTRAINT PK_grupo_permissao PRIMARY KEY (codigo_grupo, codigo_permissao),
    CONSTRAINT FK_grupo_permissao_grupo FOREIGN KEY (codigo_grupo) REFERENCES grupo(codigo),
    CONSTRAINT FK_grupo_permissao_permissao FOREIGN KEY (codigo_permissao) REFERENCES permissao(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE log (
    id BIGINT(20) AUTO_INCREMENT,
    codigo_estabelecimento BIGINT(20) NOT NULL,
    acao VARCHAR(500) NOT NULL,
    data_hora DATETIME NOT NULL,
    CONSTRAINT PK_log PRIMARY KEY (id),
    CONSTRAINT FK_log_estabelecimento FOREIGN KEY (codigo_estabelecimento) REFERENCES estabelecimento(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;