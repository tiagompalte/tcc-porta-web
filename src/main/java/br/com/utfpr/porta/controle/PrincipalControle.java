package br.com.utfpr.porta.controle;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import br.com.utfpr.porta.email.EmailServico;
import br.com.utfpr.porta.email.EnvioEmailRunnable;
import br.com.utfpr.porta.modelo.Autorizacao;
import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Genero;
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

@Controller
public class PrincipalControle {
	
	private static final Logger LOG = LoggerFactory.getLogger(PrincipalControle.class);
	
	private static final String REDIRECT_FORBIDDEN = "redirect:/403";
	private static final String REDIRECT_DASHBOARD = "redirect:/dashboard";
	private static final String COD_GRP_USUARIO = "COD_GRP_USUARIO";
	private static final String COD_GRP_ANFITRIAO = "COD_GRP_ANFITRIAO";
	private static final String URL_AUDIO = "URL_AUDIO";
	private static final String GENEROS = "generos";
	
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

	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	private EmailServico emailServico;
				
	@GetMapping("/login")
	public String login(@AuthenticationPrincipal User user) {
		
		if(user != null) {
			return REDIRECT_DASHBOARD;			
		}
		
		return "Login";
	}
	
	@GetMapping("/dashboard")
	public ModelAndView dashboard(HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("Dashboard");
		
		Usuario usuarioLogado = usuariosRepositorio.buscarComGrupos(UsuarioSistema.getUsuarioLogado().getCodigo());
				
		if(usuarioLogado == null) {
			return mv;
		}
		
		String fraseWelcome = fraseBoasVindas(usuarioLogado.getPessoa() != null ? usuarioLogado.getPessoa().getGenero() : null);
		
		mv.addObject("welcome", fraseWelcome.concat(", "));
		
		Parametro parCodGrpUsr = parametroRepositorio.findOne(COD_GRP_USUARIO);
		
		boolean mostrarGravador = false;
		if(parCodGrpUsr != null && parCodGrpUsr.getValorLong() != null) {			
			mostrarGravador = UsuarioSistema.getUsuarioLogado().isPertenceAoGrupo(parCodGrpUsr.getValorLong()) && Strings.isEmpty(usuarioLogado.getNomeAudio());						
		}
				
		mv.addObject("mostrarGravador", mostrarGravador);
		mv.addObject("urlAudio", parametroUrlAudio());
		mv.addObject("nomeAudio", usuarioLogado.getNomeAudio());
		
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
		mv.addObject(GENEROS, Genero.values());				
		return mv;
	}
	
	private String fraseBoasVindas(Genero genero) {
		
		StringBuilder fraseBoasVindas = new StringBuilder("Seja bem vind");
		if(genero != null) {
			if(genero.compareTo(Genero.MASCULINO) == 0) {
				fraseBoasVindas.append("o");
			}
			else if (genero.compareTo(Genero.FEMININO) == 0){
				fraseBoasVindas.append("a");
			}
			else {
				fraseBoasVindas.append("o(a)");
			}
		}
		else {
			fraseBoasVindas.append("o(a)");
		}
				
		return fraseBoasVindas.toString();
	}
	
	private String parametroUrlAudio() {
		Parametro parUrlAudio = parametroRepositorio.findOne(URL_AUDIO);
		
		if(parUrlAudio == null || Strings.isEmpty(parUrlAudio.getValor())) {
			return null;
		}
		
		return parUrlAudio.getValor().concat(!parUrlAudio.getValor().endsWith("/") ? "/" : "");
	}
		
	private void enviarEmailBoasVindas(Usuario usuario) {
		
		try {
			
			if(usuario == null || usuario.getPessoa() == null || Strings.isEmpty(usuario.getPessoa().getNome())
					|| Strings.isEmpty(usuario.getEmail())) {
				throw new NullPointerException("Informações faltantes");
			}
			
			Parametro parUrl = parametroRepositorio.findOne("URL_SITE");
			if(parUrl == null || Strings.isEmpty(parUrl.getValor())) {
				throw new NullPointerException("Parâmetro URL_SITE não cadastrado");
			}
			
			String fraseBoasVindas = fraseBoasVindas(usuario.getPessoa().getGenero());
			
			Context context = new Context();						
			context.setVariable("nome", usuario.getPessoa().getNome());
			context.setVariable("url", parUrl.getValor());
			String mensagem = templateEngine.process("email/mensagemBoasVindas", context);
			
			EnvioEmailRunnable thread = new EnvioEmailRunnable(usuario.getEmail(), fraseBoasVindas, 
											new String(mensagem.getBytes("UTF-8"), "ISO-8859-1"), emailServico);
			thread.run();
			
		} catch(Exception e) {
			LOG.error("Erro ao enviar email de boas vindas. ", e);
		}		
	}
	
	@PostMapping("/novoUsuario")
	public ModelAndView salvarNovoUsuario(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes, HttpServletRequest request) {
		
		if (result.hasErrors()) {
			return novoUsuario(usuario);
		}
		
		try {	
			
			Parametro parCodGrpUsr = parametroRepositorio.findOne(COD_GRP_USUARIO);
			
			if(parCodGrpUsr == null) {
				throw new NullPointerException("COD_GRP_USUARIO não parametrizado");
			}
			
			usuario.addGrupo(gruposRepositorio.findByCodigo(parCodGrpUsr.getValorLong()));
			
			Parametro parCodGrpAnfitriao = parametroRepositorio.findOne(COD_GRP_ANFITRIAO);
			
			if(parCodGrpAnfitriao != null && parCodGrpAnfitriao.getValor() != null
					&& !usuario.isPertenceAoGrupo(parCodGrpAnfitriao.getValorLong())) {
				usuario.setEstabelecimento(null);
			}
									
			usuarioServico.salvar(usuario);
									
		} catch (EmailUsuarioJaCadastradoExcecao e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} catch (Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} 
		
		enviarEmailBoasVindas(usuario);
				
		authenticateUserAndSetSession(usuario, request);
		
		return new ModelAndView(REDIRECT_DASHBOARD);
	}
	
	private void authenticateUserAndSetSession(Usuario usuario, HttpServletRequest request) {
		
		UserDetails userDetails = new UsuarioSistema(usuario, getPermissoes(usuario));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }
	
	private Collection<? extends GrantedAuthority> getPermissoes(Usuario usuario) {		
		
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		
		//Lista de permissões do usuário
		List<String> permissoes = usuariosRepositorio.permissoes(usuario);
		permissoes.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.toUpperCase())));
		
		return authorities;		
	}
	
	private ModelAndView carregarLayoutEdicaoUsuario(Usuario usuario) {
		ModelAndView mv = new ModelAndView("usuario/CadastroUsuario");
		mv.addObject(GENEROS, Genero.values());
		mv.addObject(usuario);	
		
		String urlAudio = parametroUrlAudio();
		if(Strings.isNotEmpty(urlAudio)) {
			mv.addObject("urlAudio", urlAudio);
		}
		else {
			LOG.error("Parâmetro {} não cadastrado", URL_AUDIO);
			mv = new ModelAndView("redirect:/500");
		}
		
		mv.addObject("podeGravar", usuario.isNovo() || UsuarioSistema.getUsuarioLogado().getCodigo().equals(usuario.getCodigo()));
		
		return mv;
	}
	
	@GetMapping("/usuarioCadastro/{codigo}")
	public ModelAndView editarUsuario(@PathVariable Long codigo) {
		
		if(UsuarioSistema.getUsuarioLogado().getCodigo().compareTo(codigo) != 0) {
			return new ModelAndView(REDIRECT_FORBIDDEN);
		}
				
		Usuario usuario = usuariosRepositorio.findOne(codigo);
		return carregarLayoutEdicaoUsuario(usuario);	
	}
	
	@PostMapping("/usuarioCadastro/{codigo}")
	public ModelAndView salvarEdicaoUsuario(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return carregarLayoutEdicaoUsuario(usuario);
		}
		
		try {	
			
			Parametro parCodGrpUsr = parametroRepositorio.findOne(COD_GRP_USUARIO);
			
			if(parCodGrpUsr == null) {
				throw new NullPointerException("COD_GRP_USUARIO não parametrizado");
			}
			
			usuario.addGrupo(gruposRepositorio.findByCodigo(parCodGrpUsr.getValorLong()));
			usuario.setEstabelecimento(null);
						
			usuarioServico.salvar(usuario);
									
		} catch (EmailUsuarioJaCadastradoExcecao e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoUsuario(usuario);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoUsuario(usuario);
		} catch (Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return carregarLayoutEdicaoUsuario(usuario);
		} 
		
		attributes.addFlashAttribute("mensagem", "Usuário salvo com sucesso");
		return new ModelAndView("redirect:/usuarioCadastro/".concat(usuario.getCodigo().toString()));
	}
	
	@GetMapping("/novoEstabelecimento")
	public ModelAndView novoEstabelecimento(Estabelecimento estabelecimento) {
		ModelAndView mv = new ModelAndView("estabelecimento/NovoEstabelecimento");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject(GENEROS, Genero.values());
		return mv;
	}
	
	@PostMapping("/novoEstabelecimento")
	public ModelAndView salvarNovoEstabelecimento(@Valid Estabelecimento estabelecimento, BindingResult result, RedirectAttributes attributes, HttpServletRequest request) {
		
		if (result.hasErrors()) {
			return novoEstabelecimento(estabelecimento);
		}
		
		try {
			
			Parametro parCodGrpAnfitriao = parametroRepositorio.findOne(COD_GRP_ANFITRIAO);
			
			if(parCodGrpAnfitriao == null || parCodGrpAnfitriao.getValor() == null) {
				throw new NullPointerException("COD_GRP_ANFITRIAO não parametrizado");
			}
			
			estabelecimento.getResponsavel().addGrupo(gruposRepositorio.findByCodigo(parCodGrpAnfitriao.getValorLong()));
						
			estabelecimentoServico.salvar(estabelecimento);
			
		} catch (EnderecoJaCadastradoExcecao e) {
			result.rejectValue("endereco", e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		} catch (Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		}
		
		enviarEmailBoasVindas(estabelecimento.getResponsavel());
		
		authenticateUserAndSetSession(estabelecimento.getResponsavel(), request);
		
		return new ModelAndView(REDIRECT_DASHBOARD);
	}
	
	@GetMapping("/estabelecimentoCadastro/{codigo}")
	public ModelAndView editarEstabelecimento(@PathVariable Long codigo) {
		
		if(UsuarioSistema.getUsuarioLogado().getEstabelecimento() != null
				&& UsuarioSistema.getUsuarioLogado().getEstabelecimento().getCodigo().compareTo(codigo) != 0) {
			return new ModelAndView(REDIRECT_FORBIDDEN);
		}
		
		Estabelecimento estabelecimento = estabelecimentosRepositorio.findOne(codigo);
		return carregarLayoutEdicaoEstabelecimento(estabelecimento);	
	}
	
	private ModelAndView carregarLayoutEdicaoEstabelecimento(Estabelecimento estabelecimento) {
		ModelAndView mv = new ModelAndView("estabelecimento/CadastroEstabelecimento");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject(GENEROS, Genero.values());
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
			return new ModelAndView(REDIRECT_FORBIDDEN);
		}
		
		try {
			
			Parametro parCodGrpAnfitriao = parametroRepositorio.findOne(COD_GRP_ANFITRIAO);
			
			if(parCodGrpAnfitriao == null) {
				throw new NullPointerException("COD_GRP_ANFITRIAO não parametrizado");
			}
			
			estabelecimento.getResponsavel().addGrupo(gruposRepositorio.findByCodigo(parCodGrpAnfitriao.getValorLong()));
			
			estabelecimentoServico.salvar(estabelecimento);
			
		} catch (EnderecoJaCadastradoExcecao e) {
			result.rejectValue("endereco", e.getMessage(), e.getMessage());
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
			return new ModelAndView(REDIRECT_FORBIDDEN);
		}
		
		List<Autorizacao> listaAutorizacoes = autorizacoesRepositorio.findByCodigoUsuario(codigo);
		ModelAndView mv = new ModelAndView("autorizacao/UsuarioAutorizacoes");
		mv.addObject("lista", listaAutorizacoes);
		
		return mv;
	}
				
}
