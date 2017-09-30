package br.com.utfpr.porta.modelo.validacao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import br.com.utfpr.porta.modelo.Pessoa;

public class PessoaGroupSequenceProvider implements DefaultGroupSequenceProvider<Pessoa> {
	
	@Override
	public List<Class<?>> getValidationGroups(Pessoa pessoa) {
		List<Class<?>> grupos = new ArrayList<>();
		grupos.add(Pessoa.class);
		
		if (isPessoaSelecionada(pessoa)) {
			grupos.add(pessoa.getTipoPessoa().getGrupo());
		}
		
		return grupos;
	}

	private boolean isPessoaSelecionada(Pessoa pessoa) {
		return pessoa != null && pessoa.getTipoPessoa() != null;
	}

}
