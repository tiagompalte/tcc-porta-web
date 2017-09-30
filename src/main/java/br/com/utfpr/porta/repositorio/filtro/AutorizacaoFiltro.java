package br.com.utfpr.porta.repositorio.filtro;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.TipoAutorizacao;
import br.com.utfpr.porta.modelo.Usuario;

public class AutorizacaoFiltro {
	
	private Usuario usuario;
	private Porta porta;
	private TipoAutorizacao tipoAutorizacao;
	private Estabelecimento estabelecimento;
	
	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	public Porta getPorta() {
		return porta;
	}
	public void setPorta(Porta porta) {
		this.porta = porta;
	}
	public TipoAutorizacao getTipoAutorizacao() {
		return tipoAutorizacao;
	}
	public void setTipoAutorizacao(TipoAutorizacao tipoAutorizacao) {
		this.tipoAutorizacao = tipoAutorizacao;
	}
	public Estabelecimento getEstabelecimento() {
		return estabelecimento;
	}
	public void setEstabelecimento(Estabelecimento estabelecimento) {
		this.estabelecimento = estabelecimento;
	}
		
}
