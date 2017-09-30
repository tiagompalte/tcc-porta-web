package br.com.utfpr.porta.repositorio.helper.porta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.repositorio.filtro.PortaFiltro;

public interface PortasQueries {
	
	public Page<Porta> filtrar(PortaFiltro filtro, Pageable pageable);

}
