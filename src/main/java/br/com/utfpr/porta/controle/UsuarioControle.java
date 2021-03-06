package br.com.utfpr.porta.controle;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.utfpr.porta.controle.paginacao.PageWrapper;
import br.com.utfpr.porta.modelo.Genero;
import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.modelo.TipoPessoa;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Grupos;
import br.com.utfpr.porta.repositorio.Parametros;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(UsuarioControle.class);	
	private static final String URL_AUDIO = "URL_AUDIO";
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private Usuarios usuariosRepositorio;
	
	@Autowired
	private Grupos gruposRepositorio;
		
	@Autowired
	private AudioStorage audioStorage;
	
	@Autowired
	private Parametros parametroRepositorio;
		
	@RequestMapping("/novo")
	public ModelAndView novo(Usuario usuario) {	
		ModelAndView mv = new ModelAndView("usuario/CadastroUsuario");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		mv.addObject("grupos", gruposRepositorio.findAll());
		
		Parametro parUrlAudio = parametroRepositorio.findOne(URL_AUDIO);
		if(parUrlAudio != null && !Strings.isEmpty(parUrlAudio.getValor())) {
			mv.addObject("urlAudio", parUrlAudio.getValor());
		}
		else {
			LOG.error("Parâmetro {} não cadastrado", URL_AUDIO);
			mv = new ModelAndView("redirect:/500");
		}
		
		mv.addObject("podeGravar", usuario.isNovo() || UsuarioSistema.getUsuarioLogado().getCodigo().equals(usuario.getCodigo()));
		
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(usuario);
		}
		
		try {
			
			Parametro parCodGrpAnfitriao = parametroRepositorio.findOne("COD_GRP_ANFITRIAO");
			
			if(parCodGrpAnfitriao != null && parCodGrpAnfitriao.getValor() != null
					&& !usuario.isPertenceAoGrupo(parCodGrpAnfitriao.getValorLong())) {
				usuario.setEstabelecimento(null);
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
		} catch (Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(usuario);
		}
				
		attributes.addFlashAttribute("mensagem", "Usuário salvo com sucesso");
		return new ModelAndView("redirect:/usuarios/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(UsuarioFiltro usuarioFiltro, HttpServletRequest httpServletRequest,
			@PageableDefault(size = 5, direction = Direction.ASC, sort = "p.nome") Pageable pageable) {
		
		ModelAndView mv = new ModelAndView("/usuario/PesquisaUsuarios");		
		PageWrapper<Usuario> paginaWrapper = new PageWrapper<>(
				usuariosRepositorio.filtrar(usuarioFiltro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
		
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {	
		
		if(!UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_USUARIOS")) {
			return new ModelAndView("redirect:/403");
		}
		
		Usuario usuario = usuariosRepositorio.buscarComGrupos(codigo);		
		ModelAndView mv = novo(usuario);
		mv.addObject(usuario);
		return mv;
	}
	
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity excluir(@PathVariable("codigo") Long codigo) {
		
		try {
			
			if(!UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_USUARIOS")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sem autorização");
			}
			
			Usuario usuario = usuariosRepositorio.findOne(codigo);
			
			if(usuario == null || usuario.getCodigo() == null) {
				throw new NullPointerException("Usuário não encontrado");
			}
			
			usuarioServico.excluir(codigo);
			
			if(Strings.isNotEmpty(usuario.getNomeAudio())) {
				audioStorage.excluir(usuario.getNomeAudio());
			}			
			
		} catch (ImpossivelExcluirEntidadeException | NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		return ResponseEntity.ok().build();
	}
		
}
