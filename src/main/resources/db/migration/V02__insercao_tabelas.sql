INSERT INTO grupo (codigo, nome, visivel_pagina) VALUES (1, 'Super administrador', FALSE);
INSERT INTO grupo (codigo, nome, visivel_pagina) VALUES (2, 'Administrador', TRUE);
INSERT INTO grupo (codigo, nome, visivel_pagina) VALUES (3, 'Usuário', TRUE);

INSERT INTO permissao VALUES (1, 'ROLE_CADASTRAR_USUARIO');
INSERT INTO permissao VALUES (2, 'ROLE_CADASTRAR_AUTORIZACAO');
INSERT INTO permissao VALUES (3, 'ROLE_CADASTRAR_ESTABELECIMENTO');
INSERT INTO permissao VALUES (4, 'ROLE_CADASTRAR_PORTA');
INSERT INTO permissao VALUES (5, 'ROLE_VISUALIZAR_USUARIO');
INSERT INTO permissao VALUES (6, 'ROLE_VISUALIZAR_AUTORIZACAO');
INSERT INTO permissao VALUES (7, 'ROLE_VISUALIZAR_PORTA');

INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (1, 1);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (1, 2);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (1, 3);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (1, 4);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (1, 5);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (1, 6);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (1, 7);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (2, 1);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (2, 2);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (2, 4);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (3, 5);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (3, 6);
INSERT INTO grupo_permissao (codigo_grupo, codigo_permissao) VALUES (3, 7);

INSERT INTO endereco(logradouro, numero, bairro, cep, cidade, estado) VALUES ('Av. Sete de Setembro', '3165', 'Rebouças', '80.230-901', 'Curitiba', 'PR');

INSERT INTO pessoa(nome, tipo_pessoa, cpf_cnpj) VALUES ('Administrador', 'FISICA', '18294700002');

INSERT INTO estabelecimento(nome, codigo_endereco, codigo_responsavel) VALUES ('Sistema', (SELECT codigo FROM endereco WHERE cep = '80.230-901' and numero = '3165'), (SELECT codigo FROM pessoa WHERE cpf_cnpj = '18294700002'));

INSERT INTO usuario(codigo_estabelecimento, codigo_pessoa, email, senha) VALUES ((SELECT codigo FROM estabelecimento WHERE nome = 'Sistema'), (SELECT codigo FROM pessoa WHERE cpf_cnpj = '18294700002'), 'admin', '$2a$06$m80TFwmzpVpp9szafs9MtukurC3vdvhMWuAm8e.9l/37.28F0pwoK');

INSERT INTO usuario_grupo (codigo_usuario, codigo_grupo) VALUES ((SELECT codigo FROM usuario WHERE email = 'admin'), 1);
INSERT INTO usuario_grupo (codigo_usuario, codigo_grupo) VALUES ((SELECT codigo FROM usuario WHERE email = 'admin'), 2);