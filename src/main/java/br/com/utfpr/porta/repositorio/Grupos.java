package br.com.utfpr.porta.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.utfpr.porta.modelo.Grupo;

@Repository
public interface Grupos extends JpaRepository<Grupo, Long> {
	
	List<Grupo> findByVisivelPaginaTrue();

}
