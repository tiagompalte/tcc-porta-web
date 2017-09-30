package br.com.utfpr.porta.repositorio.filtro;

import br.com.utfpr.porta.modelo.Estabelecimento;

public class PortaFiltro {
	
	private String descricao;	
	private Estabelecimento estabelecimento;
	
	public Estabelecimento getEstabelecimento() {
		return estabelecimento;
	}

	public void setEstabelecimento(Estabelecimento estabelecimento) {
		this.estabelecimento = estabelecimento;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	

}
