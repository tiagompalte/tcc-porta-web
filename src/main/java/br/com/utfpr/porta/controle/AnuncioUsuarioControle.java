package br.com.utfpr.porta.controle;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import br.com.utfpr.porta.controle.paginacao.PageWrapper;
import br.com.utfpr.porta.dto.AnuncioUsuarioDto;
import br.com.utfpr.porta.modelo.Anuncio;
import br.com.utfpr.porta.repositorio.AnuncioUsuario;
import br.com.utfpr.porta.repositorio.Enderecos;
import br.com.utfpr.porta.repositorio.filtro.AnuncioUsuarioFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.AnuncioUsuarioServico;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Controller
@RequestMapping("/anunciosUsuario")
public class AnuncioUsuarioControle {
	
	@Autowired
	private Enderecos enderecoRepositorio;
	
	@Autowired
	private AnuncioUsuario anuncioUsuarioRepositorio;
	
	@Autowired
	private AnuncioUsuarioServico anuncioUsuarioServico;
	
	@GetMapping
	public ModelAndView pesquisarAnuncioParaUsuario(AnuncioUsuarioFiltro filtro, @PageableDefault(size = 5) Pageable pageable, 
			HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/anuncio/AnunciosUsuario");
		
		mv.addObject("estados", enderecoRepositorio.obterEstados());
		
		if(Strings.isNotEmpty(filtro.getEstado())) {
			mv.addObject("cidades", enderecoRepositorio.obterCidadesPorEstado(filtro.getEstado()));
		}
		
		filtro.setCodigoUsuario(UsuarioSistema.getUsuarioLogado().getCodigo());
						
		PageWrapper<Anuncio> paginaWrapper = new PageWrapper<>(
				anuncioUsuarioRepositorio.filtrar(filtro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		
		return mv;
	}

	@GetMapping("/estado/{sigla}")
	public @ResponseBody ResponseEntity<?> obterListaCidadePorEstado(@PathVariable String sigla) {
		
		if(Strings.isEmpty(sigla)) {
			return null;
		}
		
		List<String> listaCidades = enderecoRepositorio.obterCidadesPorEstado(sigla);
		
		return ResponseEntity.ok().body(listaCidades);
	}
	
	@PostMapping
	public @ResponseBody ResponseEntity<?> marcarInteresseUsuario(@RequestBody AnuncioUsuarioDto anuncioUsuario) {
		
		if(anuncioUsuario == null || anuncioUsuario.getCodigoAnuncio() == null) {
			return ResponseEntity.badRequest().body("Código do anúncio não informado");
		}
		
		try {			
			anuncioUsuarioServico.adicionarUsuarioInteressado(anuncioUsuario.getCodigoAnuncio(), 
					UsuarioSistema.getUsuarioLogado().getCodigo());
		}
		catch(Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
		return ResponseEntity.ok().build();
	}
	
	@DeleteMapping("/{codigo_anuncio}")
	public @ResponseBody ResponseEntity<?> desmarcarInteresse(@PathVariable Long codigo_anuncio) {
		
		if(codigo_anuncio == null) {
			return ResponseEntity.badRequest().body("Código do anúncio não informado");
		}
		
		try {
			
			anuncioUsuarioServico.retirarInteresseUsuario(codigo_anuncio, UsuarioSistema.getUsuarioLogado().getCodigo());
									
		} catch (ValidacaoBancoDadosExcecao e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
		return ResponseEntity.ok().build();
	}
	
}
