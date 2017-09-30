package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.utfpr.porta.controle.paginacao.PageWrapper;
import br.com.utfpr.porta.modelo.Genero;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.TipoPessoa;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Grupos;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.repositorio.filtro.UsuarioFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.AutorizacaoServico;
import br.com.utfpr.porta.servico.LogServico;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.servico.excecao.EmailUsuarioJaCadastradoExcecao;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;
import br.com.utfpr.porta.servico.excecao.SenhaObrigatoriaUsuarioExcecao;

@Controller
@RequestMapping("/usuarios")
public class UsuarioControle {
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private AutorizacaoServico autorizacaoServico;
	
	@Autowired
	private Usuarios usuariosRepositorio;
	
	@Autowired
	private Grupos gruposRepositorio;
	
	@Autowired
	private Portas portasRepositorio;
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@Autowired
	private LogServico logServico;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Usuario usuario) {
		
		if(usuario.isNovo() && !UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			usuario.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		ModelAndView mv = new ModelAndView("usuario/CadastroUsuario");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		mv.addObject("grupos", gruposRepositorio.findByVisivelPaginaTrue());
		
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
			usuarioServico.salvar(usuario);
		} catch (EmailUsuarioJaCadastradoExcecao e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return novo(usuario);
		} catch (SenhaObrigatoriaUsuarioExcecao e) {
			result.rejectValue("senha", e.getMessage(), e.getMessage());
			return novo(usuario);
		} catch(NullPointerException e) {
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
			usuarioServico.excluir(codigo);
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok().build();
	}
		
	@RequestMapping(value="/rfid/{rfid}/{codigo_porta}", method=RequestMethod.GET)
	public ResponseEntity<?> obterUsuarioPorRFID(@PathVariable String rfid, @PathVariable Long codigo_porta) {
		
		if(StringUtils.isEmpty(rfid) || codigo_porta == null) {
			return null;
		}
		
		LocalDateTime dataHora = LocalDateTime.now();
		
		Usuario usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			return ResponseEntity.notFound().build();
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario, dataHora)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário sem autorização para acesso a porta desejada");
		}
		
		logServico.entrarPorta(usuario, porta);
		
		return ResponseEntity.status(HttpStatus.OK).body(usuario);			
	}
	
	
}
