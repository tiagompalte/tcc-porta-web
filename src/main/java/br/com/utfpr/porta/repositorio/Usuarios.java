package br.com.utfpr.porta.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.helper.usuario.UsuariosQueries;

@Repository
public interface Usuarios extends JpaRepository<Usuario, Long>, UsuariosQueries {
	
	public Optional<Usuario> findByEmail(String email);
	
	public Usuario findByRfid(String rfid);

	public List<Usuario> findByEstabelecimentoAndCodigoIn(Estabelecimento estabelecimento, Long[] codigos);
	
	public List<Usuario> findByEstabelecimentoAndAtivoTrue(Estabelecimento estabelecimento);
		
}
