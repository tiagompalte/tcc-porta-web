package br.com.utfpr.porta.controle;

import java.util.Optional;

import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.utfpr.porta.dto.AlterarSenhaDto;
import br.com.utfpr.porta.dto.EmailDto;
import br.com.utfpr.porta.modelo.TokenResetSenha;
import br.com.utfpr.porta.repositorio.TokenResetSenhas;
import br.com.utfpr.porta.servico.TokenResetSenhaServico;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Controller
public class ResetSenhaControle {
	
	@Autowired
	private TokenResetSenhaServico tokenResetSenhaServico;
	
	@Autowired
	private TokenResetSenhas tokenResetSenhaRepositorio;
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@RequestMapping("/informarEmail")
	public ModelAndView informarEmail(EmailDto emailDto) {
		ModelAndView mv = new ModelAndView("/resetSenha/InformeEmail");
		mv.addObject(emailDto);
		return mv;		
	}
			
	@PostMapping("/informarEmail")
	public ModelAndView enviarEmail(@Valid EmailDto emailDto, BindingResult result, RedirectAttributes attributes) {
		
		if(result.hasErrors()) {
			return informarEmail(emailDto);
		}
				
		try {			
			tokenResetSenhaServico.gravarToken(emailDto.getEmail());
		}
		catch(Exception e) {
			return new ModelAndView("redirect:/500");
		}
		
		return new ModelAndView("/resetSenha/EmailEnviado");		
	}
	
	@RequestMapping("/resetarSenha/{token}")
	public ModelAndView resetarSenha(@RequestParam String token) {
		
		if(Strings.isEmpty(token)) {
			return new ModelAndView("redirect:/403");
		}
		
		Optional<TokenResetSenha> tokenResetSenha = tokenResetSenhaRepositorio.findByToken(token);
		
		if(!tokenResetSenha.isPresent()) {
			return new ModelAndView("redirect:/403");
		}
		
		ModelAndView mv = new ModelAndView("/resetSenha/ResetarSenha");
		mv.addObject("token", token);		
		return mv;
	}
	
	@PostMapping("/resetarSenha")
	public ModelAndView alterarSenha(@RequestBody AlterarSenhaDto alterarSenhaDto) {
		
		if(Strings.isEmpty(alterarSenhaDto.getToken())) {
			
		}
		
		if(!alterarSenhaDto.validarSenhas()) {
			
		}
		
		Optional<TokenResetSenha> tokenResetSenha = tokenResetSenhaRepositorio.findByToken(alterarSenhaDto.getToken());
		
		if(!tokenResetSenha.isPresent() || tokenResetSenha.get().getUsuario() == null) {
			
		}
		
		try {			
			usuarioServico.alterarSenhaSite(tokenResetSenha.get().getUsuario(), alterarSenhaDto.getSenha());
			tokenResetSenhaRepositorio.delete(tokenResetSenha.get());
		}
		catch(ValidacaoBancoDadosExcecao | NullPointerException e) {
			
		}
		
		return new ModelAndView("redirect:/login");
		
	}

}
