package br.com.utfpr.porta.seguranca;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import br.com.utfpr.porta.modelo.Usuario;

public class UsuarioSistema extends User {
	
	private static final long serialVersionUID = 1L;
	
	private Usuario usuario;
	
	public UsuarioSistema(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
		super(usuario.getEmail(), usuario.getSenhaSite(), authorities);	
		this.usuario = usuario;
	}

	public Usuario getUsuario() {
		return usuario;
	}
	
	public static UsuarioSistema getUsuarioSistema() {
		return (UsuarioSistema) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	public static Usuario getUsuarioLogado() {
		return getUsuarioSistema().getUsuario();
	}
		
	public static boolean isPossuiPermissao(String role) {		
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority(role));
	}
	
	public static String getCodigoEstabelecimento() {		
		if(getUsuarioLogado().getEstabelecimento() != null) {			
			return getUsuarioLogado().getEstabelecimento().getCodigo().toString();
		}
		return "";
	}
	
	public static String getCodigoUsuario() {		
		return getUsuarioLogado().getCodigo().toString();
	}
	
}
