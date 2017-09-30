package br.com.utfpr.porta.modelo;

import br.com.utfpr.porta.modelo.validacao.grupo.CnpjGroup;
import br.com.utfpr.porta.modelo.validacao.grupo.CpfGroup;

public enum TipoPessoa {
	
	FISICA("Física", "CPF", "000.000.000-00", "Data de nascimento", CpfGroup.class) {
		@Override
		public String formatar(String cpfOuCnpj) {
			return cpfOuCnpj.replaceAll("(\\d{3})(\\d{3})(\\d{3})", "$1.$2.$3-");
		}
	}, 
	
	JURIDICA("Jurídica", "CNPJ", "00.000.000/0000-00", "Data da fundação", CnpjGroup.class) {
		@Override
		public String formatar(String cpfOuCnpj) {
			return cpfOuCnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})", "$1.$2.$3/$4-");
		}
	};
	
	private String descricao;
	private String documento;
	private String mascara;
	private String data;
	private Class<?> grupo;
	
	TipoPessoa(String descricao, String documento, String mascara, String data, Class<?> grupo) {
		this.descricao = descricao;
		this.documento = documento;
		this.mascara = mascara;
		this.data = data;
		this.grupo = grupo;
	}
	
	public abstract String formatar(String cpfOuCnpj);

	public String getDescricao() {
		return descricao;
	}

	public String getDocumento() {
		return documento;
	}

	public String getMascara() {
		return mascara;
	}
	
	public String getData() {
		return data;
	}
	
	public Class<?> getGrupo() {
		return grupo;
	}
	
	public static String removerFormatacao(String cpfOuCnpj) {
		return cpfOuCnpj.replaceAll("\\.|-|/", "");
	}
}
