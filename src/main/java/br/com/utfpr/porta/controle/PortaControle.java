package br.com.utfpr.porta.controle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import br.com.utfpr.porta.dto.PortaDto;
import br.com.utfpr.porta.modelo.Estabelecimento;
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
		
	private static final String ROLE_SUPORTE = "ROLE_EDITAR_TODOS_ESTABELECIMENTOS";
	private static final String ESTABELECIMENTOS = "estabelecimentos";
	
	@RequestMapping("/novo")
	public ModelAndView novo(Porta porta) {
		
		ModelAndView mv = new ModelAndView("porta/CadastroPorta");
		
		if(porta.isNovo() && !UsuarioSistema.isPossuiPermissao(ROLE_SUPORTE)) {
			porta.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		if(UsuarioSistema.isPossuiPermissao(ROLE_SUPORTE)) {
			mv.addObject(ESTABELECIMENTOS, estabelecimentosRepositorio.findAll());
		}
		else {
			mv.addObject(ESTABELECIMENTOS, UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Porta porta, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(porta);
		}
		
		if(porta.isNovo() && !UsuarioSistema.isPossuiPermissao(ROLE_SUPORTE)) {
			result.reject("Usuário sem permissão para cadastrar nova porta", "Usuário sem permissão para cadastrar nova porta");
			return novo(porta);
		}
				
		try {
			portaServico.salvar(porta);
		}
		catch(Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(porta);
		}
				
		attributes.addFlashAttribute("mensagem", "Porta salva com sucesso");
		return new ModelAndView("redirect:/portas/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(PortaFiltro portaFiltro, HttpServletRequest httpServletRequest,
			@PageableDefault(size = 5, direction = Direction.ASC, sort = "codigo") Pageable pageable) {
		
		ModelAndView mv = new ModelAndView("/porta/PesquisaPortas");
		 
		if(!UsuarioSistema.isPossuiPermissao(ROLE_SUPORTE)) {
			portaFiltro.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		else {
			List<Estabelecimento> listaEstabelecimentos = estabelecimentosRepositorio.findAll();
			mv.addObject(ESTABELECIMENTOS, listaEstabelecimentos);
		}
				
		PageWrapper<Porta> paginaWrapper = new PageWrapper<>(portaRepositorio.filtrar(portaFiltro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
		
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		
		Porta porta;
		if(UsuarioSistema.isPossuiPermissao(ROLE_SUPORTE)) {
			porta = portaRepositorio.findOne(codigo);
		}
		else {
			porta = portaRepositorio.findByCodigoAndEstabelecimento(codigo, UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		//Para não aparecer na tela de cadastro/edição
		porta.setSenha(null);
			
		ModelAndView mv = novo(porta);
		mv.addObject(porta);
		return mv;
	}
	
	@GetMapping("/cadastramento/{codigo}")
	public Porta cadastramentoUsuario(@PathVariable Long codigo) {		
		return portaRepositorio.findOne(codigo);		
	}
	
	@GetMapping("/estabelecimento/{codigoEstabelecimento}")
	public @ResponseBody ResponseEntity obterListaPortasPorEstabelecimento(@PathVariable Long codigoEstabelecimento) {
		
		if(codigoEstabelecimento == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código do estabelecimento não informado");
		}
		
		if(!UsuarioSistema.isPossuiPermissao(ROLE_SUPORTE) &&
				Long.valueOf(UsuarioSistema.getCodigoEstabelecimento()).compareTo(codigoEstabelecimento) != 0) {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Usuário sem permissão");
		}
				
		List<Porta> listaPortas = portaRepositorio.findByEstabelecimento(new Estabelecimento(codigoEstabelecimento));
		
		List<PortaDto> listaPortaDto = new ArrayList<>();
		if(listaPortas != null) {
			listaPortaDto = listaPortas.stream().map(porta -> new PortaDto(porta)).collect(Collectors.toList());
		}
		
		return ResponseEntity.ok().body(listaPortaDto);
	}
	
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity excluir(@PathVariable("codigo") Long codigo) {
		
		if(!UsuarioSistema.isPossuiPermissao(ROLE_SUPORTE)) {
						
			try {
				
				Porta porta = portaRepositorio.findOne(codigo);
				
				if(porta == null) {
					throw new NullPointerException("Porta não encontrada na base de dados");
				}
				
				porta.setEstabelecimento(null);
				portaServico.salvar(porta);
				
			} catch (ImpossivelExcluirEntidadeException | NullPointerException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			} 			
		}
		else {
			try {
				portaServico.excluir(codigo);
			} catch (ImpossivelExcluirEntidadeException | NullPointerException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			} 
		}
				
		return ResponseEntity.ok().build();
	}
	
}
