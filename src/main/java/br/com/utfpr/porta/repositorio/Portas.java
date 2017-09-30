package br.com.utfpr.porta.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.repositorio.helper.porta.PortasQueries;

@Repository
public interface Portas extends JpaRepository<Porta, Long>, PortasQueries {
		
	public List<Porta> findByEstabelecimento(Estabelecimento estabelecimento);
	
	public Porta findByCodigoAndEstabelecimento(Long codigo, Estabelecimento estabelecimento);

}
