package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import br.com.utfpr.porta.modelo.Autorizacao;
import br.com.utfpr.porta.modelo.AutorizacaoId;
import br.com.utfpr.porta.modelo.DiaSemana;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.TipoAutorizacao;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Autorizacoes;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.repositorio.filtro.AutorizacaoFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.AutorizacaoServico;
import br.com.utfpr.porta.servico.excecao.CampoNuloExcecao;
import br.com.utfpr.porta.servico.excecao.HoraInicialPosteriorHoraFinalExcecao;
import br.com.utfpr.porta.servico.excecao.ImpossivelExcluirEntidadeException;
import br.com.utfpr.porta.servico.excecao.ValidacaoBancoDadosExcecao;

@Controller
@RequestMapping("/autorizacoes")
public class AutorizacaoControle {
	
	@Autowired
	private Autorizacoes autorizacoesRepositorio;
	
	@Autowired
	private AutorizacaoServico autorizacaoServico;
	
	@Autowired
	private Usuarios usuariosRepositorio;
	
	@Autowired
	private Portas portasRepositorio;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Autorizacao autorizacao) {				
		ModelAndView mv = new ModelAndView("autorizacao/CadastroAutorizacao");		
		carregarDependencias(mv);		
		return mv;
	}

	private void carregarDependencias(ModelAndView mv) {
		
		mv.addObject("tipos", TipoAutorizacao.values());
		mv.addObject("dias", DiaSemana.values());
		
		List<Usuario> usuarios = usuariosRepositorio.findByEstabelecimentoAndAtivoTrue(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		if(usuarios == null) {
			usuarios = new ArrayList<>();
		}
		
		List<Porta> portas = portasRepositorio.findByEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		if(portas == null) {
			portas = new ArrayList<>();
		}
				
		mv.addObject("usuarios", usuarios);
		mv.addObject("portas", portas);
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Autorizacao autorizacao, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(autorizacao);
		}
		
		try {
			
			if(autorizacao.isNovo()) {
				autorizacao.getId().setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
			}
						
			if(autorizacao.getTipoAutorizacao() != null && 
					autorizacao.getTipoAutorizacao().compareTo(TipoAutorizacao.TEMPORARIO) == 0) {
				if(autorizacao.getDataTemporaria() != null) {
					if(autorizacao.getHoraInicioTemporaria() != null) {
						autorizacao.setDataHoraInicio(LocalDateTime.of(autorizacao.getDataTemporaria(), autorizacao.getHoraInicioTemporaria()));
					}
					else {
						autorizacao.setDataHoraInicio(null);
					}
					if(autorizacao.getHoraFimTemporaria() != null) {
						autorizacao.setDataHoraFim(LocalDateTime.of(autorizacao.getDataTemporaria(), autorizacao.getHoraFimTemporaria()));
					}	
					else {
						autorizacao.setDataHoraFim(null);
					}
				}
				else {
					autorizacao.setDataHoraInicio(null);
					autorizacao.setDataHoraFim(null);
				}
			}
			
			autorizacaoServico.salvar(autorizacao);
		}
		catch(CampoNuloExcecao e) {
			result.rejectValue(e.getCampo(), e.getMessage(), e.getMessage());
			return novo(autorizacao);
		}
		catch(HoraInicialPosteriorHoraFinalExcecao e) {
			result.rejectValue(e.getCampoHoraInicial(), e.getMessage(), e.getMessage());
			result.rejectValue(e.getCampoHoraFinal(), e.getMessage());
			return novo(autorizacao);
		}
		catch(ValidacaoBancoDadosExcecao e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(autorizacao);
		}
		
		attributes.addFlashAttribute("mensagem", "Autorização salva com sucesso");
		return new ModelAndView("redirect:/autorizacoes/novo");
	}
	
	@GetMapping
	public ModelAndView pesquisar(AutorizacaoFiltro autorizacaoFiltro, @PageableDefault(size = 5) Pageable pageable,
			HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/autorizacao/PesquisaAutorizacoes");		
		carregarDependencias(mv);
		
		if(!UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			autorizacaoFiltro.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		PageWrapper<Autorizacao> paginaWrapper = new PageWrapper<>(autorizacoesRepositorio.filtrar(autorizacaoFiltro, pageable)
				, httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
	
	@GetMapping("/{codigoUsuario}/{codigoPorta}/{sequencia}")
	public ModelAndView editar(@PathVariable Long codigoUsuario, @PathVariable Long codigoPorta, 
			@PathVariable Long sequencia) {
		
		Usuario usr = new Usuario();
		usr.setCodigo(codigoUsuario);		
		Porta porta = new Porta();
		porta.setCodigo(codigoPorta);
		
		AutorizacaoId id = new AutorizacaoId();		
		id.setUsuario(usr);
		id.setPorta(porta);
		
		if(UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			usr = usuariosRepositorio.findOne(codigoUsuario);
			id.setEstabelecimento(usr.getEstabelecimento());
		}
		else {
			id.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		id.setSequencia(sequencia);
		
		Autorizacao autorizacao = autorizacoesRepositorio.findOne(id);
		ModelAndView mv = novo(autorizacao);
		mv.addObject(autorizacao);		
		return mv;
	}
	
	@DeleteMapping("/{codigoUsuario}/{codigoPorta}/{sequencia}")
	public @ResponseBody ResponseEntity<?> excluir(@PathVariable Long codigoUsuario, 
			@PathVariable Long codigoPorta, @PathVariable Long sequencia) {
		
		Usuario usr = usuariosRepositorio.findOne(codigoUsuario);	
		Porta porta = portasRepositorio.findOne(codigoPorta);
		
		AutorizacaoId id = new AutorizacaoId();		
		id.setUsuario(usr);
		id.setPorta(porta);
		
		if(UsuarioSistema.isPossuiPermissao("ROLE_CADASTRAR_ESTABELECIMENTO")) {
			id.setEstabelecimento(usr.getEstabelecimento());
		}
		else {
			id.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		
		id.setSequencia(sequencia);
		
		try {
			autorizacaoServico.excluir(id);
		} catch (ImpossivelExcluirEntidadeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch(NullPointerException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok().build();
	}
	
}
