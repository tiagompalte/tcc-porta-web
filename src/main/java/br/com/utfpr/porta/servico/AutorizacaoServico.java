package br.com.utfpr.porta.servico;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.utfpr.porta.modelo.Autorizacao;
import br.com.utfpr.porta.modelo.AutorizacaoId;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.TipoAutorizacao;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Autorizacoes;
import br.com.utfpr.porta.servico.excecao.CampoNuloExcecao;
import br.com.utfpr.porta.servico.excecao.HoraInicialPosteriorHoraFinalExcecao;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Service
public class AutorizacaoServico {
	
	@Autowired
	private Autorizacoes autorizacoesRepositorio;
		
	@Transactional
	public void salvar(Autorizacao autorizacao) {
		
		if(autorizacao.getId() == null) {
			throw new ValidacaoBancoDadosExcecao("Usuário e porta não informados");
		}
		else if(autorizacao.getId().getUsuario() == null || autorizacao.getId().getUsuario().getCodigo() == null) {
			throw new CampoNuloExcecao("id.usuario", "Usuário é obrigatório");
		}
		else if(autorizacao.getId().getPorta() == null || autorizacao.getId().getPorta().getCodigo() == null) {
			throw new CampoNuloExcecao("id.porta", "Porta é obrigatório");
		}
		
		if(autorizacao.getTipoAutorizacao().compareTo(TipoAutorizacao.PROGRAMADO) == 0) {
			if(autorizacao.getDiaSemana() == null) {
				throw new CampoNuloExcecao("diaSemana", "Campo dia da semana é obrigatório");
			}
			if(autorizacao.getHoraInicio() == null) {
				throw new CampoNuloExcecao("horaInicio", "Campo hora de início é obrigatório");
			}
			if(autorizacao.getHoraFim() == null) {
				throw new CampoNuloExcecao("horaFim", "Campo hora final é obrigatório");
			}
			if(autorizacao.getHoraInicio() != null && autorizacao.getHoraFim() != null
					&& autorizacao.getHoraInicio().isAfter(autorizacao.getHoraFim())) {
				throw new HoraInicialPosteriorHoraFinalExcecao("horaInicio", "horaFim", "Hora inicial não pode ser posterior a hora final");
			}
			
			autorizacao.setDataHoraInicio(null);
			autorizacao.setDataHoraFim(null);
		}
		else if(autorizacao.getTipoAutorizacao().compareTo(TipoAutorizacao.TEMPORARIO) == 0) {
			if(autorizacao.getDataTemporaria() == null) {
				throw new CampoNuloExcecao("dataTemporaria", "Campo data é obrigatório");
			}
			if(autorizacao.getDataHoraInicio() == null) {
				throw new CampoNuloExcecao("horaInicioTemporaria", "Campo hora de início é obrigatório");
			}
			if(autorizacao.getDataHoraFim() == null) {
				throw new CampoNuloExcecao("horaFimTemporaria", "Campo hora final é obrigatório");
			}
			if(autorizacao.getDataHoraInicio() != null && autorizacao.getDataHoraFim() != null
					&& autorizacao.getDataHoraInicio().isAfter(autorizacao.getDataHoraFim())) {
				throw new HoraInicialPosteriorHoraFinalExcecao("horaInicioTemporaria", "horaFimTemporaria", "Hora inicial não pode ser posterior a hora final");
			}
			
			autorizacao.setDiaSemana(null);
			autorizacao.setHoraInicio(null);
			autorizacao.setHoraFim(null);
		}
		else {
			autorizacao.setDataHoraInicio(null);
			autorizacao.setDataHoraFim(null);
			autorizacao.setDiaSemana(null);
			autorizacao.setHoraInicio(null);
			autorizacao.setHoraFim(null);
		}
		
		if(autorizacao.isNovo()) {
			autorizacao.getId().setSequencia(obterSequencia(autorizacao));
		}
		
		autorizacoesRepositorio.save(autorizacao);
		
	}
	
	private Long obterSequencia(Autorizacao autorizacao) {
				
		List<Autorizacao> lista = autorizacoesRepositorio.findByCodigoUsuarioAndCodigoPorta(
				autorizacao.getId().getUsuario().getCodigo(), autorizacao.getId().getPorta().getCodigo());
		
		Long sequencia = Long.valueOf(1);
			
		
		if(lista != null && !lista.isEmpty()) {
			
			for(Autorizacao aut : lista) {
				if(aut.getTipoAutorizacao().compareTo(TipoAutorizacao.PERMANENTE) == 0) {
					throw new ValidacaoBancoDadosExcecao(
							String.format("O usuário %s já possui uma autorização permanente para a porta %s", 
									aut.getId().getUsuario().getCodigoNome(), 
									aut.getId().getPorta().getCodigoDescricao()));
				}
				
				if(aut.getId().getSequencia().compareTo(sequencia) > 0) {
					sequencia = aut.getId().getSequencia();
				}
			}
			sequencia++;
		}
		
		return sequencia;
	}
	
	public boolean validarAcessoUsuario(Porta porta, Usuario usuario, LocalDateTime dataHora) {
				
		if(dataHora == null) {
			dataHora = LocalDateTime.now();
		}
		
		List<Autorizacao> lista = autorizacoesRepositorio.findByCodigoUsuarioAndCodigoPorta(usuario.getCodigo(), porta.getCodigo());
		
		if(lista == null || lista.isEmpty()) {
			return false;
		}
		
		for(Autorizacao aut : lista) {
						
			if(TipoAutorizacao.PERMANENTE.compareTo(aut.getTipoAutorizacao()) == 0) {
				return true;
			}
			else if(TipoAutorizacao.PROGRAMADO.compareTo(aut.getTipoAutorizacao()) == 0) {
				if(aut.getDiaSemana() != null && aut.getHoraInicio() != null && aut.getHoraFim() != null 
						&& aut.getDiaSemana().ordinal() == dataHora.getDayOfWeek().getValue()
						&& aut.getHoraInicio().isAfter(dataHora.toLocalTime())
						&& aut.getHoraFim().isBefore(dataHora.toLocalTime())) {
					return true;
				}
			}
			else if(TipoAutorizacao.PERMANENTE.compareTo(aut.getTipoAutorizacao()) == 0) {
				if(aut.getDataHoraInicio() != null && aut.getDataHoraFim() != null
						&& aut.getDataHoraInicio().isAfter(dataHora)
						&& aut.getDataHoraFim().isBefore(dataHora)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Transactional
	public void excluir(AutorizacaoId id) {
				
		if(id == null || id.getSequencia() == null 
				|| id.getEstabelecimento() == null || id.getEstabelecimento().getCodigo() == null
				|| id.getPorta() == null || id.getPorta().getCodigo() == null
				|| id.getUsuario() == null || id.getUsuario().getCodigo() == null) {
			throw new NullPointerException("Identificador da autorização não informado");
		}
		
		try {
			autorizacoesRepositorio.delete(id);
			autorizacoesRepositorio.flush();
		}
		catch(PersistenceException e) {
			throw new ImpossivelExcluirEntidadeException("Erro ao excluir autorização");
		}
		
	}
	
}
