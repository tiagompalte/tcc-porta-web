package br.com.utfpr.porta.controle;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
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
import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Genero;
import br.com.utfpr.porta.modelo.Grupo;
import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.modelo.TipoPessoa;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Grupos;
import br.com.utfpr.porta.repositorio.Parametros;
import br.com.utfpr.porta.repositorio.filtro.EstabelecimentoFiltro;
import br.com.utfpr.porta.servico.EstabelecimentoServico;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;

@Controller
@RequestMapping("/estabelecimentos")
public class EstabelecimentoControle {
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@Autowired
	private EstabelecimentoServico estabelecimentoServico;
	
	@Autowired
	private Grupos gruposRepositorio;
	
	@Autowired
	private Parametros parametroRepositorio;
		
	@RequestMapping("/novo")
	public ModelAndView novo(Estabelecimento estabelecimento) {
		ModelAndView mv = new ModelAndView("estabelecimento/CadastroEstabelecimento");
		mv.addObject("tiposPessoa", TipoPessoa.values());
		mv.addObject("generos", Genero.values());
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Estabelecimento estabelecimento, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(estabelecimento);
		}
		
		try {
			
			Parametro par_grp_anfitriao = parametroRepositorio.findOne("COD_GRP_ANFITRIAO");
			
			if(par_grp_anfitriao == null || Strings.isEmpty(par_grp_anfitriao.getValor())) {
				throw new NullPointerException("Parâmetro do grupo de anfitrião não cadastrado");
			}
			
			Grupo grupo_anfitriao = gruposRepositorio.findByCodigo(par_grp_anfitriao.getValorLong());
			List<Grupo> lista_grupo = new ArrayList<>();
			lista_grupo.add(grupo_anfitriao);			
			estabelecimento.getResponsavel().setGrupos(lista_grupo);
			
			estabelecimentoServico.salvar(estabelecimento);
		}
		catch(Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(estabelecimento);
		}
				
		attributes.addFlashAttribute("mensagem", "Estabelecimento salvo com sucesso");
		return new ModelAndView("redirect:/estabelecimentos/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(EstabelecimentoFiltro estabelecimentoFiltro,
			@PageableDefault(size = 5) Pageable pageable, HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/estabelecimento/PesquisaEstabelecimentos");		
		PageWrapper<Estabelecimento> paginaWrapper = new PageWrapper<>(
				estabelecimentosRepositorio.filtrar(estabelecimentoFiltro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
	
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable Long codigo) {
		Estabelecimento estabelecimento = estabelecimentosRepositorio.findOne(codigo);
		ModelAndView mv = novo(estabelecimento);
		mv.addObject(estabelecimento);		
		return mv;
	}
		
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable("codigo") Long codigo) {
		try {
			estabelecimentoServico.excluir(codigo);
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok().build();
	}
	
}
