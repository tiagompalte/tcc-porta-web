package br.com.utfpr.porta.seguranca;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Usuarios;

@Service
public class AppUserDetailsService implements UserDetailsService {

	@Autowired
	private Usuarios usuarioRepositorio;
	
	@Override
	public UserDetails loadUserByUsername(String email) {
		Optional<Usuario> usuarioOptional = usuarioRepositorio.porEmailEAtivoComGrupos(email);
		Usuario usuario = usuarioOptional.orElseThrow(() -> new UsernameNotFoundException("Usuário e/ou senha incorretos"));
		return new UsuarioSistema(usuario, getPermissoes(usuario));
	}
	
	private Collection<? extends GrantedAuthority> getPermissoes(Usuario usuario) {		
		
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		
		//Lista de permissões do usuário
		List<String> permissoes = usuarioRepositorio.permissoes(usuario);
		permissoes.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.toUpperCase())));
		
		return authorities;		
	}

}
