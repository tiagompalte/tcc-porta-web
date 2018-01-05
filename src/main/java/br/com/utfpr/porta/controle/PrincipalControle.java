package br.com.utfpr.porta.controle;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Genero;
import br.com.utfpr.porta.modelo.TipoPessoa;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Grupos;
import br.com.utfpr.porta.servico.EstabelecimentoServico;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.servico.excecao.CampoNaoInformadoExcecao;
import br.com.utfpr.porta.servico.excecao.EmailUsuarioJaCadastradoExcecao;

@Controller
public class PrincipalControle {
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private EstabelecimentoServico estabelecimentoServico;
	
	@Autowired
	private Grupos gruposRepositorio;
		
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
		mv.addObject("generos", Genero.values());
		return mv;
	}
	
	@PostMapping({ "/salvarNovoUsuario", "{\\+d}" })
	public ModelAndView salvarNovoUsuario(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novoUsuario(usuario);
		}
		
		try {	
			
			usuario.setGrupos(Arrays.asList(gruposRepositorio.findByCodigo(Long.getLong("3")))); //usuário
						
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
		}
		
		return new ModelAndView("redirect:/login");
	}
	
	@GetMapping("/novoEstabelecimento")
	public ModelAndView novoEstabelecimento(Estabelecimento estabelecimento) {
		ModelAndView mv = new ModelAndView("estabelecimento/NovoEstabelecimento");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		return mv;
	}
	
	@PostMapping({ "/salvarNovoEstabelecimento", "{\\+d}" })
	public ModelAndView salvarNovoEstabelecimento(@Valid Estabelecimento estabelecimento, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novoEstabelecimento(estabelecimento);
		}
		
		try {
			
			estabelecimento.getResponsavel().setGrupos(Arrays.asList(gruposRepositorio.findByCodigo(Long.parseLong("2")))); //anfitrião
						
			estabelecimentoServico.salvar(estabelecimento);
			
		} catch(Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoEstabelecimento(estabelecimento);
		} 
				
		return new ModelAndView("redirect:/login");
	}
	
}
