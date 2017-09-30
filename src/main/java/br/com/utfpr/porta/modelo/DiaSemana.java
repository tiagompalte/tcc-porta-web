package br.com.utfpr.porta.modelo;

public enum DiaSemana {
	
	SEGUNDA("Segunda-feira"),
	TERCA("Terça-feira"),
	QUARTA("Quarta-feira"),
	QUINTA("Quinta-feira"),
	SEXTA("Sexta-feira"),
	SABADO("Sábado"),
	DOMINGO("Domingo");
	
    private String descricao;
	
	DiaSemana(String descricao) {
		this.descricao = descricao;
	}
		
	public String getDescricao() {
		return descricao;
	}

}
