package br.com.utfpr.porta.controle;

import java.time.LocalDate;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import br.com.utfpr.porta.modelo.Anuncio;
import br.com.utfpr.porta.repositorio.AnuncioUsuario;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.filtro.AnuncioFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.AnuncioServico;
import br.com.utfpr.porta.servico.excecao.CampoNaoInformadoExcecao;
import br.com.utfpr.porta.servico.excecao.InformacaoInvalidaException;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Controller
@RequestMapping("/anuncios")
public class AnuncioControle {
	
	@Autowired
	private AnuncioServico anuncioServico;
	
	@Autowired
	private br.com.utfpr.porta.repositorio.Anuncio anuncioRepositorio;
	
	@Autowired
	private Estabelecimentos estabelecimentoRepositorio;
	
	@Autowired
	private AnuncioUsuario anuncioUsuarioRepositorio;
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Anuncio anuncio) {	
		ModelAndView mv = new ModelAndView("anuncio/CadastroAnuncio");
		
		if(!anuncio.isNovo()) {
			mv.addObject("usuarios", anuncioUsuarioRepositorio.obterListaUsuariosPorAnuncio(anuncio.getCodigo()));
		}
		
		if(UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {
			mv.addObject("estabelecimentos", estabelecimentosRepositorio.findAll());
		}
		
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Anuncio anuncio, BindingResult result, 
			RedirectAttributes attributes, HttpServletRequest request) {
		
		if (result.hasErrors()) {
			return novo(anuncio);
		}
		
		if(anuncio.isNovo() && !UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {	
			anuncio.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}		
				
		if(anuncio.isNovo() && anuncio.getDataPublicacao() == null) {			
			LocalDate dataUsr = LocalDate.now(Calendar.getInstance(request.getLocale()).getTimeZone().toZoneId());	
			anuncio.setDataPublicacao(dataUsr);
		}	
		
		try {
			anuncioServico.salvar(anuncio);
		} catch (CampoNaoInformadoExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novo(anuncio);
		} catch (InformacaoInvalidaException e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novo(anuncio);
		} catch (Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(anuncio);
		}
		
		attributes.addFlashAttribute("mensagem", "Anúncio salvo com sucesso");
		return new ModelAndView("redirect:/anuncios/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(AnuncioFiltro filtro, @PageableDefault(size = 5) Pageable pageable, 
			HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/anuncio/PesquisaAnuncios");
		
		if(filtro.getEstabelecimento() == null || !UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {
			filtro.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		if(UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {		
			mv.addObject("estabelecimentos", estabelecimentoRepositorio.findAll());
		}
		
		PageWrapper<Anuncio> paginaWrapper = new PageWrapper<>(
				anuncioRepositorio.filtrar(filtro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
		
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		
		if(codigo == null) {
			return new ModelAndView("redirect:/500");
		}
		
		Anuncio anuncio = null;
		if(UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {
			anuncio = anuncioRepositorio.findOne(codigo);
		}
		else {			
			anuncio = anuncioRepositorio.findByCodigoAndEstabelecimento(
					codigo, UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		if(anuncio == null) {
			return new ModelAndView("redirect:/403");
		}
			
		ModelAndView mv = novo(anuncio);
		mv.addObject(anuncio);
		return mv;
	}
	
	@GetMapping("/expirar/{codigo}")
	public ModelAndView expirar(@PathVariable Long codigo, HttpServletRequest request) {
		
		if(codigo == null) {
			return new ModelAndView("redirect:/500");
		}
		
		Anuncio anuncio = anuncioRepositorio.findByCodigoAndEstabelecimento(
				codigo, UsuarioSistema.getUsuarioLogado().getEstabelecimento());
				
		try {
			anuncioServico.expirarAnuncio(anuncio, LocalDate.now(Calendar.getInstance(request.getLocale()).getTimeZone().toZoneId()));
		} catch(Exception e) {
			return new ModelAndView("redirect:/404");
		}
				
		return new ModelAndView("redirect:/anuncios");
	}
	
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable Long codigo) {
		
		try {
			
			Anuncio anuncio = anuncioRepositorio.findByCodigoAndEstabelecimento(
					codigo, UsuarioSistema.getUsuarioLogado().getEstabelecimento());
			
			if(anuncio == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Anúncio não encontrado");
			}
						
			anuncioServico.excluir(anuncio);
						
		} catch (ValidacaoBancoDadosExcecao | NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		return ResponseEntity.ok().build();
	}

}
