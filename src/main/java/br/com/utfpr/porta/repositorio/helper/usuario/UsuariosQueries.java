package br.com.utfpr.porta.repositorio.helper.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.filtro.UsuarioFiltro;

public interface UsuariosQueries {
	
	public Optional<Usuario> porEmailEAtivo(String email);
		
	public Page<Usuario> filtrar(UsuarioFiltro filtro, Pageable pageable);
	
	public List<String> permissoes(Usuario usuario);
	
	public Usuario buscarComGrupos(Long codigo);
	
	public Usuario buscarComGruposEstabelecimento(Long codigo, Estabelecimento estabelecimento);
	
}
