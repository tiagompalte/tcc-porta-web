package br.com.utfpr.porta.repositorio.helper.estabelecimento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.repositorio.filtro.EstabelecimentoFiltro;

public interface EstabelecimentosQueries {
	
	public Page<Estabelecimento> filtrar(EstabelecimentoFiltro filtro, Pageable pageable);

}
