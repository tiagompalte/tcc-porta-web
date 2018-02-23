package br.com.utfpr.porta.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import br.com.utfpr.porta.modelo.Anuncio;
import br.com.utfpr.porta.modelo.serializacao.LocalDateSerializador;

public class AnuncioDto {
	
	private Long codigo;
	private String descricaoResumida;
	private String descricao;
	private String preco;
	private String endereco;
	private String nomeResponsavel;
	private String telefoneResponsavel;
	private String emailResponsavel;
	private boolean usuarioJaInteressado;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	@JsonSerialize(using = LocalDateSerializador.class)
	private LocalDate dataPublicacao;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	@JsonSerialize(using = LocalDateSerializador.class)
	private LocalDate dataExpiracao;
	
	public AnuncioDto() {}
	
	public AnuncioDto(Anuncio anuncio) {
		this();
		this.codigo = anuncio.getCodigo();
		this.descricaoResumida = anuncio.getDescricaoResumida();
		this.descricao = anuncio.getDescricao();
		this.preco = anuncio.getPrecoString();
		this.dataPublicacao = anuncio.getDataPublicacao();
		this.dataExpiracao = anuncio.getDataExpiracao();
		this.usuarioJaInteressado = anuncio.isUsuarioJaInteressado();
		
		if(anuncio.getEstabelecimento() != null) {
			if(anuncio.getEstabelecimento().getResponsavel() != null 
					&& anuncio.getEstabelecimento().getResponsavel().getPessoa() != null) {
				this.nomeResponsavel = anuncio.getEstabelecimento().getResponsavel().getPessoa().getNome();
				this.telefoneResponsavel = anuncio.getEstabelecimento().getResponsavel().getPessoa().getTelefone();
				this.emailResponsavel = anuncio.getEstabelecimento().getResponsavel().getEmail();
			}
			if(anuncio.getEstabelecimento().getEndereco() != null) {
				this.endereco = anuncio.getEstabelecimento().getEndereco().toString();
			}
		}
	}
	
	public Long getCodigo() {
		return codigo;
	}
	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}
	public String getDescricaoResumida() {
		return descricaoResumida;
	}
	public void setDescricaoResumida(String descricaoResumida) {
		this.descricaoResumida = descricaoResumida;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public String getPreco() {
		return preco;
	}
	public void setPreco(String preco) {
		this.preco = preco;
	}
	public LocalDate getDataPublicacao() {
		return dataPublicacao;
	}
	public void setDataPublicacao(LocalDate dataPublicacao) {
		this.dataPublicacao = dataPublicacao;
	}
	public LocalDate getDataExpiracao() {
		return dataExpiracao;
	}
	public void setDataExpiracao(LocalDate dataExpiracao) {
		this.dataExpiracao = dataExpiracao;
	}
	public String getEndereco() {
		return endereco;
	}
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
	public String getNomeResponsavel() {
		return nomeResponsavel;
	}
	public void setNomeResponsavel(String nomeResponsavel) {
		this.nomeResponsavel = nomeResponsavel;
	}	
	public String getTelefoneResponsavel() {
		return telefoneResponsavel;
	}
	public void setTelefoneResponsavel(String telefoneResponsavel) {
		this.telefoneResponsavel = telefoneResponsavel;
	}
	public String getEmailResponsavel() {
		return emailResponsavel;
	}
	public void setEmailResponsavel(String emailResponsavel) {
		this.emailResponsavel = emailResponsavel;
	}
	public boolean isUsuarioJaInteressado() {
		return usuarioJaInteressado;
	}
	public void setUsuarioJaInteressado(boolean usuarioJaInteressado) {
		this.usuarioJaInteressado = usuarioJaInteressado;
	}	
}
