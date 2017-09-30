package br.com.utfpr.porta.modelo;

public enum TipoAutorizacao {
	
	PERMANENTE("Permanete"),
	TEMPORARIO("Temporário"),
	PROGRAMADO("Programado");
	
	private String descricao;
	
	TipoAutorizacao(String descricao) {
		this.descricao = descricao;
	}
	
	public String getDescricao() {
		return descricao;
	}

}
