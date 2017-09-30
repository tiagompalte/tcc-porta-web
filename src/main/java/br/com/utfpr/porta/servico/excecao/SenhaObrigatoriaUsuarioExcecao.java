package br.com.utfpr.porta.servico.excecao;

public class SenhaObrigatoriaUsuarioExcecao extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public SenhaObrigatoriaUsuarioExcecao(String message) {
		super(message);
	}
}
