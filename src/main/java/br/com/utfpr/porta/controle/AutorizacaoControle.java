package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.TipoAutorizacao;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Autorizacoes;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Parametros;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.repositorio.filtro.AutorizacaoFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;
import br.com.utfpr.porta.servico.AutorizacaoServico;
import br.com.utfpr.porta.servico.excecao.CampoNaoInformadoExcecao;
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
	
	@Autowired
	private Estabelecimentos estabelecimentosRepositorio;
	
	@Autowired
	private Parametros parametroRepositorio;
	
	@RequestMapping("/novo")
	public ModelAndView novo(Autorizacao autorizacao) {				
		ModelAndView mv = new ModelAndView("autorizacao/CadastroAutorizacao");		
		carregarDependencias(mv, autorizacao);		
		return mv;
	}

	private void carregarDependencias(ModelAndView mv, Autorizacao autorizacao) {
		
		mv.addObject("tipos", TipoAutorizacao.values());
		mv.addObject("dias", DiaSemana.values());
		
		List<Estabelecimento> estabelecimentos = null;
		if(UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {
			estabelecimentos = estabelecimentosRepositorio.findAll();
			mv.addObject("estabelecimentos", estabelecimentos);
		}	
		
		Estabelecimento estabelecimento = null;
		if(estabelecimentos == null) {
			estabelecimento = estabelecimentosRepositorio.findByResponsavel(UsuarioSistema.getUsuarioLogado());
		}
		else if(autorizacao != null && autorizacao.getId() != null && autorizacao.getId().getEstabelecimento() != null) {
			estabelecimento = autorizacao.getId().getEstabelecimento();
		}
		
		Parametro par_cod_grp_usuario = parametroRepositorio.findOne("COD_GRP_USUARIO");		
		if(par_cod_grp_usuario == null || StringUtils.isEmpty(par_cod_grp_usuario.getValor())) {
			throw new NullPointerException("COD_GRP_USUARIO não foi parametrizado");
		}
		
		if(estabelecimento != null) {
			List<Porta> portas = portasRepositorio.findByEstabelecimento(estabelecimento);
			if(portas != null && portas.isEmpty() == false) {
				mv.addObject("portas", portas);
			}
		}	
		
		List<Usuario> usuarios = usuariosRepositorio.buscarPorGrupoCodigoAndAtivo(par_cod_grp_usuario.getValorLong());
		mv.addObject("usuarios", usuarios);
				
	}
	
	@PostMapping({ "/novo", "{\\+d}" })
	public ModelAndView salvar(@Valid Autorizacao autorizacao, BindingResult result, RedirectAttributes attributes) {
		
		if (result.hasErrors()) {
			return novo(autorizacao);
		}
		
		try {
						
			if(autorizacao.isNovo() && !UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {				
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
		catch(CampoNaoInformadoExcecao e) {
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
		catch(Exception e) {
			result.reject(e.getMessage(), e.getMessage());
			return novo(autorizacao);
		}
		
		attributes.addFlashAttribute("mensagem", "Autorização salva com sucesso");
		return new ModelAndView("redirect:/autorizacoes/novo");
	}
	
	@RequestMapping("/novo/usuario/{codigo}")
	public ModelAndView autorizarUsuario(@PathVariable Long codigo, Autorizacao autorizacao) {	
		
		ModelAndView mv = new ModelAndView("autorizacao/CadastroAutorizacao");		
		Usuario usuario = usuariosRepositorio.findOne(codigo);		
		if(usuario != null) {
			AutorizacaoId id = new AutorizacaoId();
			id.setUsuario(usuario);
			autorizacao.setId(id);
		}
		
		carregarDependencias(mv, autorizacao);		
		
		return mv;
	}
		
	@GetMapping
	public ModelAndView pesquisar(AutorizacaoFiltro autorizacaoFiltro, @PageableDefault(size = 5) Pageable pageable,
			HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/autorizacao/PesquisaAutorizacoes");
		
		List<Usuario> listaUsuarios = null;
		List<Porta> listaPortas = null;
		
		if(!UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {
			autorizacaoFiltro.setEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());			
			listaPortas = portasRepositorio.findByEstabelecimento(UsuarioSistema.getUsuarioLogado().getEstabelecimento());
		}
		else {
			listaPortas = portasRepositorio.findAll();
		}
		
		Parametro par_cod_grp_usuario = parametroRepositorio.findOne("COD_GRP_USUARIO");		
		if(par_cod_grp_usuario == null || StringUtils.isEmpty(par_cod_grp_usuario.getValor())) {
			throw new NullPointerException("COD_GRP_USUARIO não foi parametrizado");
		}
		
		listaUsuarios = usuariosRepositorio.buscarPorGrupoCodigoAndAtivo(par_cod_grp_usuario.getValorLong());
		
		mv.addObject("usuarios", listaUsuarios);
		mv.addObject("portas", listaPortas);
		mv.addObject("tipos", TipoAutorizacao.values());
		
		PageWrapper<Autorizacao> paginaWrapper = new PageWrapper<>(
				autorizacoesRepositorio.filtrar(autorizacaoFiltro, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}
	
	@GetMapping("/{codigoUsuario}/{codigoPorta}/{sequencia}")
	public ModelAndView editar(@PathVariable Long codigoUsuario, @PathVariable Long codigoPorta, @PathVariable Long sequencia) {
		
		ModelAndView mv = new ModelAndView("autorizacao/CadastroAutorizacao");
		
		Usuario usuario = usuariosRepositorio.findOne(codigoUsuario);		
		Porta porta = portasRepositorio.findOne(codigoPorta);
		if(porta == null || usuario == null) {
			return new ModelAndView("redirect:/500");
		}
		
		List<Usuario> usuarios = Arrays.asList(usuario);
		List<Porta> portas = Arrays.asList(porta);
		List<Estabelecimento> estabelecimentos = Arrays.asList(porta.getEstabelecimento());
				
		AutorizacaoId id = new AutorizacaoId();
		id.setSequencia(sequencia);
		id.setUsuario(usuario);
		id.setPorta(porta);
		id.setEstabelecimento(porta.getEstabelecimento());
						
		Autorizacao autorizacao = autorizacoesRepositorio.findOne(id);
		
		if(autorizacao == null) {
			return new ModelAndView("redirect:/500");
		}
		
		mv.addObject(autorizacao);
		mv.addObject("tipos", TipoAutorizacao.values());
		mv.addObject("dias", DiaSemana.values());
		mv.addObject("usuarios", usuarios);
		mv.addObject("portas", portas);
		mv.addObject("estabelecimentos", estabelecimentos);
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
		id.setEstabelecimento(porta.getEstabelecimento());				
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
	
	@GetMapping("/estabelecimento/{codigoEstabelecimento}")
	public ModelAndView modificarListaUsuariosPortasPorEstabelecimento(@PathVariable Long codigoEstabelecimento) {
		
		Autorizacao autorizacao = new Autorizacao();
		if(!UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {
			return novo(autorizacao);
		}
		
		ModelAndView mv = new ModelAndView("autorizacao/CadastroAutorizacao");	
		
		mv.addObject("tipos", TipoAutorizacao.values());
		mv.addObject("dias", DiaSemana.values());
		
		Estabelecimento estabelecimento = estabelecimentosRepositorio.findByCodigo(codigoEstabelecimento);
		
		List<Estabelecimento> listaEstabelecimentos = estabelecimentosRepositorio.findAll();
		mv.addObject("estabelecimentos", listaEstabelecimentos);
		
		if(estabelecimento == null) {
			mv.addObject("autorizacao", autorizacao);
			return mv;
		}
		
		AutorizacaoId id = new AutorizacaoId();
		id.setEstabelecimento(estabelecimento);
		autorizacao.setId(id);
		mv.addObject("autorizacao", autorizacao);
		
		List<Porta> listaPortas = portasRepositorio.findByEstabelecimento(estabelecimento);
		if(listaPortas != null) {			
			mv.addObject("portas", listaPortas);
		}
		
		Parametro par_cod_grp_usuario = parametroRepositorio.findOne("COD_GRP_USUARIO");		
		if(par_cod_grp_usuario == null || StringUtils.isEmpty(par_cod_grp_usuario.getValor())) {
			throw new NullPointerException("COD_GRP_USUARIO não foi parametrizado");
		}
		
		List<Usuario> listaUsuarios = usuariosRepositorio.buscarPorGrupoCodigoAndAtivo(par_cod_grp_usuario.getValorLong());		
		mv.addObject("usuarios", listaUsuarios);
		
		return mv;
	}
	
}
