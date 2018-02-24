package br.com.utfpr.porta.controle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.utfpr.porta.modelo.Autorizacao;
import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Genero;
import br.com.utfpr.porta.modelo.Grupo;
import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.modelo.TipoPessoa;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Autorizacoes;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Grupos;
import br.com.utfpr.porta.repositorio.Parametros;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.EstabelecimentoServico;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.servico.excecao.CampoNaoInformadoExcecao;
import br.com.utfpr.porta.servico.excecao.EmailUsuarioJaCadastradoExcecao;
import br.com.utfpr.porta.servico.excecao.EnderecoJaCadastradoExcecao;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Controller
public class PrincipalControle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalControle.class);
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private Usuarios usuariosRepositorio;
	
	@Autowired
	private EstabelecimentoServico estabelecimentoServico;
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@Autowired
	private Grupos gruposRepositorio;
	
	@Autowired
	private Parametros parametroRepositorio;
	
	@Autowired
	private Autorizacoes autorizacoesRepositorio;
		
	@GetMapping("/login")
	public String login(@AuthenticationPrincipal User user) {
		
		if(user != null) {
			return "redirect:/dashboard";			
		}
		
		return "Login";
	}
	
	@GetMapping("/403")
	public String acessoNegado() {
		return "403";
	}
	
	@GetMapping("/dashboard")
	public ModelAndView dashboard(HttpServletRequest httpServletRequest) {
		ModelAndView mv = new ModelAndView("Dashboard");
		mv.addObject("welcome", "Seja bem vindo, ");
		return mv;
	}
	
	@GetMapping("/")
	public String home() {
		return "index";
	}
	
	@GetMapping("/novoUsuario")
	public ModelAndView novoUsuario(Usuario usuario) {
		ModelAndView mv = new ModelAndView("usuario/NovoUsuario");
		mv.addObject("csrfTokenFake", UUID.randomUUID());
		mv.addObject("csrfHeaderNameFake", UUID.randomUUID());
		mv.addObject("generos", Genero.values());
		
		Parametro parUrlAudio = parametroRepositorio.findOne("URL_AUDIO");
		if(parUrlAudio != null && !Strings.isEmpty(parUrlAudio.getValor())) {
			mv.addObject("url_audio", parUrlAudio.getValor());
		}
		else {
			mv = new ModelAndView("redirect:/500");
		}
		
		return mv;
	}
	
	@PostMapping("/novoUsuario")
	public ModelAndView salvarNovoUsuario(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novoUsuario(usuario);
		}
		
		try {	
			
			Parametro par_cod_grp_usuario = parametroRepositorio.findOne("COD_GRP_USUARIO");
			
			if(par_cod_grp_usuario == null) {
				throw new NullPointerException("COD_GRP_USUARIO não parametrizado");
			}
			
			Grupo grupo_usuario = gruposRepositorio.findByCodigo(par_cod_grp_usuario.getValorLong());
			List<Grupo> lista_grupo = new ArrayList<>();
			lista_grupo.add(grupo_usuario);			
			usuario.setGrupos(lista_grupo);
									
			usuarioServico.salvar(usuario);
									
		} catch (EmailUsuarioJaCadastradoExcecao e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} catch (NullPointerException e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} catch(Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		}
		
		attributes.addFlashAttribute("mensagem", "Usuário salvo com sucesso");
		
		//Thread para "segurar" o redirect da página com o intuito de garantir a transmissão completa do áudio
		try {			
			Thread.sleep(5000); // 5 segundos
		}
		catch(Exception e) {
			LOGGER.error("Erro ao iniciar thread de sleep");
		}
		
		return new ModelAndView("redirect:/login");
	}
	
	private ModelAndView carregarLayoutEdicaoUsuario(Usuario usuario) {
		ModelAndView mv = new ModelAndView("usuario/CadastroUsuario");
		mv.addObject("generos", Genero.values());
		mv.addObject(usuario);	
		
		Parametro parUrlAudio = parametroRepositorio.findOne("URL_AUDIO");
		if(parUrlAudio != null && !Strings.isEmpty(parUrlAudio.getValor())) {
			mv.addObject("url_audio", parUrlAudio.getValor());
		}
		else {
			mv = new ModelAndView("redirect:/500");
		}
		
		return mv;
	}
	
	@GetMapping("/usuarioCadastro/{codigo}")
	public ModelAndView editarUsuario(@PathVariable Long codigo) {
		
		if(UsuarioSistema.getUsuarioLogado().getCodigo().compareTo(codigo) != 0) {
			return new ModelAndView("redirect:/403");
		}
				
		Usuario usuario = usuariosRepositorio.findOne(codigo);
		ModelAndView mv = carregarLayoutEdicaoUsuario(usuario);		
		return mv;
	}
	
	@PostMapping("/usuarioCadastro/{codigo}")
	public ModelAndView salvarEdicaoUsuario(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return carregarLayoutEdicaoUsuario(usuario);
		}
		
		try {	
			
			Parametro par_cod_grp_usuario = parametroRepositorio.findOne("COD_GRP_USUARIO");
			
			if(par_cod_grp_usuario == null) {
				throw new NullPointerException("COD_GRP_USUARIO não parametrizado");
			}
			
			Grupo grupo_usuario = gruposRepositorio.findByCodigo(par_cod_grp_usuario.getValorLong());
			List<Grupo> lista_grupo = new ArrayList<>();
			lista_grupo.add(grupo_usuario);			
			usuario.setGrupos(lista_grupo);
						
			usuarioServico.salvar(usuario);
									
		} catch (EmailUsuarioJaCadastradoExcecao e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoUsuario(usuario);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoUsuario(usuario);
		} catch (NullPointerException e) {
			result.reject(e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoUsuario(usuario);
		} catch(Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoUsuario(usuario);
		}
		
		//Thread para "segurar" o redirect da página com o intuito de garantir a transmissão completa do áudio
		try {			
			Thread.sleep(5000); // 5 segundos
		}
		catch(Exception e) {
			LOGGER.error("Erro ao iniciar thread de sleep");
		}
		
		attributes.addFlashAttribute("mensagem", "Usuário salvo com sucesso");
		return new ModelAndView("redirect:/usuarioCadastro/".concat(usuario.getCodigo().toString()));
	}
	
	@GetMapping("/novoEstabelecimento")
	public ModelAndView novoEstabelecimento(Estabelecimento estabelecimento) {
		ModelAndView mv = new ModelAndView("estabelecimento/NovoEstabelecimento");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		return mv;
	}
	
	@PostMapping("/novoEstabelecimento")
	public ModelAndView salvarNovoEstabelecimento(@Valid Estabelecimento estabelecimento, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novoEstabelecimento(estabelecimento);
		}
		
		try {
			
			Parametro par_cod_grp_anfitriao = parametroRepositorio.findOne("COD_GRP_ANFITRIAO");
			
			if(par_cod_grp_anfitriao == null) {
				throw new NullPointerException("COD_GRP_ANFITRIAO não parametrizado");
			}
			
			Grupo grupo_anfitriao = gruposRepositorio.findByCodigo(par_cod_grp_anfitriao.getValorLong());
			List<Grupo> lista_grupo = new ArrayList<>();
			lista_grupo.add(grupo_anfitriao);			
			estabelecimento.getResponsavel().setGrupos(lista_grupo);
						
			estabelecimentoServico.salvar(estabelecimento);
			
		} catch (NullPointerException e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		} catch (EnderecoJaCadastradoExcecao e) {
			result.rejectValue("endereco", e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		} catch (ValidacaoBancoDadosExcecao e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		} catch (Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		}
		
		attributes.addFlashAttribute("mensagem", "Estabelecimento salvo com sucesso");
		return new ModelAndView("redirect:/login");
	}
	
	@GetMapping("/estabelecimentoCadastro/{codigo}")
	public ModelAndView editarEstabelecimento(@PathVariable Long codigo) {
		
		if(UsuarioSistema.getUsuarioLogado().getEstabelecimento() != null
				&& UsuarioSistema.getUsuarioLogado().getEstabelecimento().getCodigo().compareTo(codigo) != 0) {
			return new ModelAndView("redirect:/403");
		}
		
		Estabelecimento estabelecimento = estabelecimentosRepositorio.findOne(codigo);
		ModelAndView mv = carregarLayoutEdicaoEstabelecimento(estabelecimento);	
		return mv;
	}
	
	private ModelAndView carregarLayoutEdicaoEstabelecimento(Estabelecimento estabelecimento) {
		ModelAndView mv = new ModelAndView("estabelecimento/CadastroEstabelecimento");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		mv.addObject(estabelecimento);		
		return mv;
	}
	
	@PostMapping("/estabelecimentoCadastro/{codigo}")
	public ModelAndView salvarEdicaoEstabelecimento(@Valid Estabelecimento estabelecimento, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			carregarLayoutEdicaoEstabelecimento(estabelecimento);
		}
		
		if(UsuarioSistema.getUsuarioLogado().getEstabelecimento() != null
				&& UsuarioSistema.getUsuarioLogado().getEstabelecimento().getCodigo().compareTo(estabelecimento.getCodigo()) != 0) {
			return new ModelAndView("redirect:/403");
		}
		
		try {
			
			Parametro par_cod_grp_anfitriao = parametroRepositorio.findOne("COD_GRP_ANFITRIAO");
			
			if(par_cod_grp_anfitriao == null) {
				throw new NullPointerException("COD_GRP_ANFITRIAO não parametrizado");
			}
			
			Grupo grupo_anfitriao = gruposRepositorio.findByCodigo(par_cod_grp_anfitriao.getValorLong());
			List<Grupo> lista_grupo = new ArrayList<>();
			lista_grupo.add(grupo_anfitriao);			
			estabelecimento.getResponsavel().setGrupos(lista_grupo);
			
			estabelecimentoServico.salvar(estabelecimento);
			
		} catch (NullPointerException e) {
			result.reject(e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoEstabelecimento(estabelecimento);
		} catch (EnderecoJaCadastradoExcecao e) {
			result.rejectValue("endereco", e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoEstabelecimento(estabelecimento);
		} catch (ValidacaoBancoDadosExcecao e) {
			result.reject(e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoEstabelecimento(estabelecimento);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoEstabelecimento(estabelecimento);
		} catch (Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoEstabelecimento(estabelecimento);
		}
		
		attributes.addFlashAttribute("mensagem", "Estabelecimento salvo com sucesso");
		return new ModelAndView("redirect:/estabelecimentoCadastro/".concat(estabelecimento.getCodigo().toString()));		
	}
	
	@GetMapping("/autorizacoesUsuario/{codigo}")
	public ModelAndView visualizarAutorizacoes(@PathVariable Long codigo) {
		
		if(UsuarioSistema.getUsuarioLogado().getCodigo().compareTo(codigo) != 0) {
			return new ModelAndView("redirect:/403");
		}
		
		List<Autorizacao> listaAutorizacoes = autorizacoesRepositorio.findByCodigoUsuario(codigo);
		ModelAndView mv = new ModelAndView("autorizacao/UsuarioAutorizacoes");
		mv.addObject("lista", listaAutorizacoes);
		
		return mv;
	}
	
		
}
