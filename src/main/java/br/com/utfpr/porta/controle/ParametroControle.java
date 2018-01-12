package br.com.utfpr.porta.controle;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.repositorio.Parametros;
import br.com.utfpr.porta.servico.ParametroServico;

@Controller
@RequestMapping("/parametros")
public class ParametroControle {
	
	@Autowired
	private Parametros parametroRepositorio;
	
	@Autowired
	private ParametroServico parametroServico;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Parametro parametro) {		
		ModelAndView mv = new ModelAndView("parametro/CadastroParametro");		
		return mv;
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Parametro parametro, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(parametro);
		}
						
		try {			
			parametroServico.salvar(parametro);
		}
		catch(Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(parametro);
		}		
		
		attributes.addFlashAttribute("mensagem", "Parâmetro salvo com sucesso");
		return new ModelAndView("redirect:/parametros/novo");
	}
	
	@GetMapping
	public ModelAndView carregarLista(HttpServletRequest httpServletRequest) {		
		ModelAndView mv = new ModelAndView("/parametro/PesquisaParametros");
		List<Parametro> lista_parametros = parametroRepositorio.findAll(); 
		mv.addObject("parametros", lista_parametros);
		return mv;
	}
		
	@GetMapping("/{codigo}")
	public ModelAndView editar(@PathVariable String codigo) {		
		Parametro parametro = parametroRepositorio.findOne(codigo);			
		ModelAndView mv = novo(parametro);
		mv.addObject(parametro);
		return mv;
	}
		
	@DeleteMapping("/{codigo}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable("codigo") String codigo) {
		
		try {
			parametroServico.excluir(codigo);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
				
		return ResponseEntity.ok().build();
	}

}
