package br.com.utfpr.porta.servico.excecao;

public class ErroValidacaoSenha extends RuntimeException {

	private static final long serialVersionUID = 8809195487300363372L;
	
	public ErroValidacaoSenha(String message) {
		super(message);
	}

}
