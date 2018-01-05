package br.com.utfpr.porta.controle;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.utfpr.porta.modelo.Genero;
import br.com.utfpr.porta.modelo.TipoPessoa;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.servico.excecao.EmailUsuarioJaCadastradoExcecao;
import br.com.utfpr.porta.servico.excecao.SenhaObrigatoriaUsuarioExcecao;

@Controller
public class SeguracaoControle {
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
		
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
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		return mv;
	}
	
	@PostMapping({ "/salvarNovoUsuario", "{\\+d}" })
	public ModelAndView salvarNovoUsuario(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novoUsuario(usuario);
		}
		
		try {
			
			usuario.setSenhaSite(this.passwordEncoder.encode(usuario.getSenhaSite()));
			usuario.setConfirmacaoSenhaSite(usuario.getSenhaSite());
			
			usuarioServico.salvar(usuario);
									
		} catch (EmailUsuarioJaCadastradoExcecao e) {
			result.rejectValue("email", e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} catch (SenhaObrigatoriaUsuarioExcecao e) {
			result.rejectValue("senha_site", e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} catch (NullPointerException e) {
			result.reject(e.getMessage(), e.getMessage());
			return novoUsuario(usuario);
		} 
		
		return new ModelAndView("redirect:/login");
	}
	

}
