package br.com.utfpr.porta.servico.excecao;

public class ValidacaoBancoDadosExcecao extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ValidacaoBancoDadosExcecao(String message) {
		super(message);
	}
	
}
