package br.com.utfpr.porta.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.utfpr.porta.modelo.Autorizacao;
import br.com.utfpr.porta.modelo.AutorizacaoId;
import br.com.utfpr.porta.repositorio.helper.autorizacao.AutorizacoesQueries;

@Repository
public interface Autorizacoes extends JpaRepository<Autorizacao, AutorizacaoId>, AutorizacoesQueries {
	
}
