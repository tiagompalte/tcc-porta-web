package br.com.utfpr.porta.servico;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.utfpr.porta.modelo.Endereco;
import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Pessoa;
import br.com.utfpr.porta.repositorio.Enderecos;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Pessoas;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Service
public class EstabelecimentoServico {
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@Autowired
	private Enderecos enderecosRespositorio;
	
	@Autowired
	private Pessoas pessoasRepositorio;
	
	@Transactional
	public void salvar(Estabelecimento estabelecimento) {
		
		if(estabelecimento == null) {
			throw new NullPointerException("Estabelecimento não informado");
		}
		
		if(estabelecimento.getEndereco() == null) {
			throw new NullPointerException("Endereço não informado");
		}
		
		if(estabelecimento.getResponsavel() == null) {
			throw new NullPointerException("Responsável não informado");
		}
				
		Endereco enderecoSalvo = enderecosRespositorio.save(estabelecimento.getEndereco());
		
		if(enderecoSalvo == null || enderecoSalvo.getCodigo() == null) {
			throw new ValidacaoBancoDadosExcecao("Não foi possível salvar o endereço do estabelecimento"); 
		}
		
		estabelecimento.setEndereco(enderecoSalvo);
		
		Pessoa pessoaSalva = pessoasRepositorio.save(estabelecimento.getResponsavel());
		
		if(pessoaSalva == null || pessoaSalva.getCodigo() == null) {
			throw new ValidacaoBancoDadosExcecao("Não foi possível salvar o responsável do estabelecimento"); 
		}
		
		if(estabelecimento.getResponsavel().isNovo()) {			
			estabelecimento.setResponsavel(pessoaSalva);
		}
					
		estabelecimentosRepositorio.save(estabelecimento);
		
	}
	
	@Transactional
	public void excluir(Long codigo) {
		
		if(codigo == null) {
			throw new NullPointerException("Código do estabelecimento não informado");
		}
		
		try {
			estabelecimentosRepositorio.delete(codigo);
			estabelecimentosRepositorio.flush();
		}
		catch(PersistenceException e) {
			throw new ImpossivelExcluirEntidadeException("Impossível excluir estabelecimento. Ele está vinculado a usuários, portas e autorizações");
		}
		
	}

}
