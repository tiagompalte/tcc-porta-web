package br.com.utfpr.porta.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.repositorio.helper.estabelecimento.EstabelecimentosQueries;

@Repository
public interface Estabelecimentos extends JpaRepository<Estabelecimento, Long>, EstabelecimentosQueries {
	
	public Estabelecimento findByCodigo(Long codigo);

}
