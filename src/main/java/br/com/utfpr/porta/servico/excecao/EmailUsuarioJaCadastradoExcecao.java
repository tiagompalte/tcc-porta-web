package br.com.utfpr.porta.servico.excecao;

public class EmailUsuarioJaCadastradoExcecao extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public EmailUsuarioJaCadastradoExcecao(String message) {
		super(message);
	}

}
