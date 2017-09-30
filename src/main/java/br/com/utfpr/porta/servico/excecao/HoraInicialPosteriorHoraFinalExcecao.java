package br.com.utfpr.porta.servico.excecao;

public class HoraInicialPosteriorHoraFinalExcecao extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private String campoHoraInicial;	
	private String campoHoraFinal;
	
	public HoraInicialPosteriorHoraFinalExcecao(String campoHoraInicial, String campoHoraFinal, String mensagem) {
		super(mensagem);
		this.campoHoraInicial = campoHoraInicial;
		this.campoHoraFinal = campoHoraFinal;
	}

	public String getCampoHoraInicial() {
		return campoHoraInicial;
	}

	public String getCampoHoraFinal() {
		return campoHoraFinal;
	}
	
}
