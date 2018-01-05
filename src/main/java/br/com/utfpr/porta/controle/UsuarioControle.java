package br.com.utfpr.porta.controle;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.utfpr.porta.controle.paginacao.PageWrapper;
import br.com.utfpr.porta.modelo.Genero;
import br.com.utfpr.porta.modelo.TipoPessoa;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Grupos;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.repositorio.filtro.UsuarioFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.servico.excecao.CampoNaoInformadoExcecao;
import br.com.utfpr.porta.servico.excecao.EmailUsuarioJaCadastradoExcecao;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;
import br.com.utfpr.porta.servico.excecao.RfidUsuarioJaCadastradoExcecao;
import br.com.utfpr.porta.storage.AudioStorage;

@Controller
@RequestMapping("/usuarios")
public class UsuarioControle {
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private Usuarios usuariosRepositorio;
	
	@Autowired
	private Grupos gruposRepositorio;
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AudioStorage audioStorage;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Usuario usuario) {
		
		if(usuario.isNovo() && !UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			usuario.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		ModelAndView mv = new ModelAndView("usuario/CadastroUsuario");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		
		if(UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			mv.addObject("estabelecimentos", estabelecimentosRepositorio.findAll());
		}
		else {
			mv.addObject("estabelecimentos", UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(usuario);
		}
		
		try {
			
			if(usuario.isNovo() || (!usuario.isNovo() && !StringUtils.isEmpty(usuario.getSenhaSite()))) {
				usuario.setSenhaSite(this.passwordEncoder.encode(usuario.getSenhaSite()));
				usuario.setConfirmacaoSenhaSite(usuario.getSenhaSite());
			}
			else if(StringUtils.isEmpty(usuario.getSenhaSite())) {
				Usuario usuarioBase = usuariosRepositorio.findOne(usuario.getCodigo());
				if(usuarioBase != null) {
					usuario.setSenhaSite(usuarioBase.getSenhaSite());
				}
			}
			
			usuarioServico.salvar(usuario);
									
		} catch (EmailUsuarioJaCadastradoExcecao e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return novo(usuario);
		} catch (RfidUsuarioJaCadastradoExcecao e) {
			result.rejectValue("rfid", e.getMessage(), e.getMessage());
			return novo(usuario);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novo(usuario);
		}  catch (NullPointerException e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(usuario);
		} 
		
		attributes.addFlashAttribute("mensagem", "Usuário salvo com sucesso");
		return new ModelAndView("redirect:/usuarios/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(UsuarioFiltro usuarioFiltro, @PageableDefault(size = 5) Pageable pageable, 
			HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/usuario/PesquisaUsuarios");
		
		if(UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			mv.addObject("estabelecimentos", estabelecimentosRepositorio.findAll());
		}
		else {
			usuarioFiltro.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		PageWrapper<Usuario> paginaWrapper = new PageWrapper<>(
				usuariosRepositorio.filtrar(usuarioFiltro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
		
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		
		Usuario usuario;
		if(UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			usuario = usuariosRepositorio.buscarComGrupos(codigo);
		}
		else {
			usuario = usuariosRepositorio.buscarComGruposEstabelecimento(codigo, UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		ModelAndView mv = novo(usuario);
		mv.addObject(usuario);
		return mv;
	}
	
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable("codigo") Long codigo) {
		
		try {
			
			Usuario usuario = usuariosRepositorio.findOne(codigo);
			
			if(usuario == null || usuario.getCodigo() == null) {
				throw new NullPointerException("Usuário não encontrado");
			}
			
			usuarioServico.excluir(codigo);
			
			if(StringUtils.isEmpty(usuario.getNomeAudio()) == false) {
				audioStorage.excluir(usuario.getNomeAudio());
			}			
			
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		return ResponseEntity.ok().build();
	}
		
}
