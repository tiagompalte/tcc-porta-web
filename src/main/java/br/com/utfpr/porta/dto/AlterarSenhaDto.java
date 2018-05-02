package br.com.utfpr.porta.dto;

import br.com.utfpr.porta.validacao.AtributoConfirmacao;

@AtributoConfirmacao(atributo="senhaSite", atributoConfirmacao="confirmacaoSenhaSite", message="Confirmação da senha não confere")
public class AlterarSenhaDto {
	
	private String token;
	private String senhaSite;
	private String confirmacaoSenhaSite;
		
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getSenhaSite() {
		return senhaSite;
	}
	public void setSenhaSite(String senhaSite) {
		this.senhaSite = senhaSite;
	}
	public String getConfirmacaoSenhaSite() {
		return confirmacaoSenhaSite;
	}
	public void setConfirmacaoSenhaSite(String confirmacaoSenhaSite) {
		this.confirmacaoSenhaSite = confirmacaoSenhaSite;
	}	
}
