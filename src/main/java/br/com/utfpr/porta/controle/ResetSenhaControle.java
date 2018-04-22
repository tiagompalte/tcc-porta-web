package br.com.utfpr.porta.controle;

import java.util.Optional;

import javax.persistence.PersistenceException;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.utfpr.porta.dto.AlterarSenhaDto;
import br.com.utfpr.porta.dto.EmailDto;
import br.com.utfpr.porta.email.EmailServico;
import br.com.utfpr.porta.email.EnvioEmailRunnable;
import br.com.utfpr.porta.modelo.TokenResetSenha;
import br.com.utfpr.porta.repositorio.TokenResetSenhas;
import br.com.utfpr.porta.servico.TokenResetSenhaServico;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.servico.excecao.CampoNaoInformadoExcecao;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Controller
public class ResetSenhaControle {
	
	private static final String REDIRECT_FORBIDDEN = "redirect:/403";
	private static final String REDIRECT_INTERNAL_SERVER_ERROR = "redirect:/500";
	private static final String URL_RESETAR_SENHA = "/resetSenha/ResetarSenha";
	
	private static final Logger LOG = LoggerFactory.getLogger(ResetSenhaControle.class);
		
	@Autowired
	private TokenResetSenhaServico tokenResetSenhaServico;
	
	@Autowired
	private TokenResetSenhas tokenResetSenhaRepositorio;
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private EmailServico emailServico;
	
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
		
		EnvioEmailRunnable envioEmail = new EnvioEmailRunnable(emailDto.getEmail(), emailServico);
		envioEmail.run();
						
		return new ModelAndView("/resetSenha/EmailEnviado");		
	}
	
	@RequestMapping("/resetarSenha/{token}")
	public ModelAndView resetarSenha(@PathVariable String token) {
		
		if(Strings.isEmpty(token)) {
			return new ModelAndView(REDIRECT_FORBIDDEN);
		}
		
		Optional<TokenResetSenha> tokenResetSenha = tokenResetSenhaRepositorio.findByToken(token);
		
		if(!tokenResetSenha.isPresent()) {
			return new ModelAndView(REDIRECT_FORBIDDEN);
		}
		
		AlterarSenhaDto alterarSenhaDto = new AlterarSenhaDto();
		alterarSenhaDto.setToken(tokenResetSenha.get().getToken());
		
		ModelAndView mv = new ModelAndView(URL_RESETAR_SENHA);
		mv.addObject(alterarSenhaDto);		
		return mv;
	}
	
	@PostMapping({"/resetarSenha", "{\\+w}"})
	public ModelAndView alterarSenha(@Valid AlterarSenhaDto alterarSenhaDto, BindingResult result, RedirectAttributes attributes) {
		
		if(Strings.isEmpty(alterarSenhaDto.getToken())) {
			return new ModelAndView(REDIRECT_FORBIDDEN);
		}
		
		if(result.hasErrors()) {
			return new ModelAndView(URL_RESETAR_SENHA);
		}	
		
		Optional<TokenResetSenha> tokenResetSenha = tokenResetSenhaRepositorio.findByToken(alterarSenhaDto.getToken());
		
		if(!tokenResetSenha.isPresent() || tokenResetSenha.get().getUsuario() == null) {
			return new ModelAndView(REDIRECT_INTERNAL_SERVER_ERROR);
		}
		
		try {			
			usuarioServico.alterarSenhaSite(tokenResetSenha.get().getUsuario(), alterarSenhaDto.getSenhaSite());
			tokenResetSenhaServico.excluir(tokenResetSenha.get());
		}
		catch(CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return new ModelAndView(URL_RESETAR_SENHA);
		}
		catch(ValidacaoBancoDadosExcecao | NullPointerException | PersistenceException e) {
			LOG.error("Erro ao alterar senha do site", e);
			return new ModelAndView(REDIRECT_INTERNAL_SERVER_ERROR);
		}
		
		return new ModelAndView("redirect:/login");		
	}

}
