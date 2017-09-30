package br.com.utfpr.porta.repositorio.helper.autorizacao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.utfpr.porta.modelo.Autorizacao;
import br.com.utfpr.porta.repositorio.filtro.AutorizacaoFiltro;

public interface AutorizacoesQueries {
	
	public List<Autorizacao> findByCodigoUsuarioAndCodigoPorta(Long codigoUsuario, Long codigoPorta);
	
	public Page<Autorizacao> filtrar(AutorizacaoFiltro filtro, Pageable pageable);

}
