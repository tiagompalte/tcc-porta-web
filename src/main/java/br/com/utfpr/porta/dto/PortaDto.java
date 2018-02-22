package br.com.utfpr.porta.dto;

import br.com.utfpr.porta.modelo.Porta;

public class PortaDto {
	
	private Long codigo;
	private String descricao;
	
	public PortaDto() {}
	
	public PortaDto(Porta porta) {
		if(porta == null) {
			return;
		}
		this.codigo = porta.getCodigo();
		this.descricao = porta.getDescricao();
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
