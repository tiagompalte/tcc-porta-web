package br.com.utfpr.porta.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.utfpr.porta.modelo.Pessoa;

@Repository
public interface Pessoas extends JpaRepository<Pessoa, Long> {
	
	public Pessoa findByCpfOuCnpj(String cpfOuCnpj);

}
