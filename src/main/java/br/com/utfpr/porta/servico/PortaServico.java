package br.com.utfpr.porta.servico;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.servico.excecao.ErroValidacaoSenha;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;
import br.com.utfpr.porta.util.GeradorSenha;

@Service
public class PortaServico {
	
	@Autowired
	private Portas portasRepositorio;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
		
	@Transactional
	public String salvar(Porta porta) {
		
		if(porta == null) {
			throw new NullPointerException("Porta não informada");
		}
		
		String senha = null;
		
		if(porta.isNovo()) {
			
			if(StringUtils.isEmpty(porta.getSenha())) {
				senha = GeradorSenha.gerarSenha(6);
			}
			else {
				senha = porta.getSenha();
			}
			
			porta.setSenha(this.passwordEncoder.encode(porta.getSenha()));
		}
		else {
			
			Porta portaDB = portasRepositorio.findOne(porta.getCodigo());
			
			if(portaDB == null) {
				throw new ValidacaoBancoDadosExcecao("Não foi encontrado na base de dados essa porta");
			}
			else if(passwordEncoder.matches(porta.getSenha(), portaDB.getSenha()) == false) {
				throw new ErroValidacaoSenha("Senha não confere");
			}
		}
		
		portasRepositorio.save(porta);
		
		return senha;
	}	
	
	@Transactional
	public void excluir(Long codigo) {
		
		if(codigo == null) {
			throw new NullPointerException("Código da porta não informado");
		}
		
		try {
			portasRepositorio.delete(codigo);
			portasRepositorio.flush();
		}
		catch(PersistenceException e) {
			throw new ImpossivelExcluirEntidadeException("Impossível excluir porta. Ele possui autorizações vinculadas.");
		}
		
	}
	
}
