package br.com.utfpr.porta.controle;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.filtro.PortaFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.PortaServico;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;

@Controller
@RequestMapping("/portas")
public class PortaControle {
	
	@Autowired
	private PortaServico portaServico;
	
	@Autowired
	private Portas portaRepositorio;
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Porta porta) {
		
		ModelAndView mv = new ModelAndView("porta/CadastroPorta");
		
		if(porta.isNovo() && !UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			porta.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		if(UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			mv.addObject("estabelecimentos", estabelecimentosRepositorio.findAll());
		}
		else {
			mv.addObject("estabelecimentos", UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Porta porta, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(porta);
		}
		
		portaServico.salvar(porta);
		
		attributes.addFlashAttribute("mensagem", "Porta salva com sucesso");
		return new ModelAndView("redirect:/portas/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(PortaFiltro portaFiltro, 
			@PageableDefault(size = 5) Pageable pageable, HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/porta/PesquisaPortas");
		
		if(!UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			portaFiltro.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
				
		PageWrapper<Porta> paginaWrapper = new PageWrapper<>(
				portaRepositorio.filtrar(portaFiltro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
		
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		
		Porta porta;
		if(UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			porta = portaRepositorio.findOne(codigo);
		}
		else {
			porta = portaRepositorio.findByCodigoAndEstabelecimento(codigo, UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
			
		ModelAndView mv = novo(porta);
		mv.addObject(porta);
		return mv;
	}
	
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable("codigo") Long codigo) {
		try {
			portaServico.excluir(codigo);
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok().build();
	}
	
}
