package br.com.utfpr.porta.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.utfpr.porta.modelo.Endereco;

@Repository
public interface Enderecos extends JpaRepository<Endereco, Long> {

}
