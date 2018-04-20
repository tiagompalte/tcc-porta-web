package br.com.utfpr.porta.dto;

import org.apache.logging.log4j.util.Strings;

public class AlterarSenhaDto {
	
	private String token;
	private String senha;
	private String confirmacaoSenha;
		
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	public String getConfirmacaoSenha() {
		return confirmacaoSenha;
	}
	public void setConfirmacaoSenha(String confirmacaoSenha) {
		this.confirmacaoSenha = confirmacaoSenha;
	}
	
	public boolean validarSenhas() {
		
		if(Strings.isEmpty(senha) || Strings.isEmpty(confirmacaoSenha)) {
			return false;
		}
		
		return senha.equals(confirmacaoSenha);
	}
}
