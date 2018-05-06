package br.com.utfpr.porta.controle;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import br.com.utfpr.porta.controle.paginacao.PageWrapper;
import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Log;
import br.com.utfpr.porta.repositorio.Estabelecimentos;
import br.com.utfpr.porta.repositorio.Logs;
import br.com.utfpr.porta.repositorio.filtro.LogFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;

@Controller
@RequestMapping("/logs")
public class LogControle {
	
	@Autowired
	private Logs logsRepositorio;
	
	@Autowired
	private Estabelecimentos estabelecimentoRepositorio;

	@GetMapping
	public ModelAndView pesquisar(LogFiltro logFiltro, HttpServletRequest httpServletRequest,
			@PageableDefault(size = 5, direction = Direction.DESC, sort = "dataHora") Pageable pageable) {
		
		ModelAndView mv = new ModelAndView("/log/PesquisaLogs");
		
		List<Estabelecimento> estabelecimentos = null;
		if(UsuarioSistema.isPossuiPermissao("ROLE_EDITAR_TODOS_ESTABELECIMENTOS")) {
			estabelecimentos = estabelecimentoRepositorio.findAll();
			mv.addObject("estabelecimentos", estabelecimentos);
		}
				
		Estabelecimento estabelecimento = null;
		
		if(logFiltro != null) {
			if(logFiltro.getDataHoraInicio() != null && logFiltro.getDataHoraFim() != null
					&& logFiltro.getDataHoraInicio().isAfter(logFiltro.getDataHoraFim())) {
				logFiltro.setDataHoraInicio(null);
				logFiltro.setDataHoraFim(null);
			}
			
			if(logFiltro.getEstabelecimento() != null && estabelecimentos != null) {
				estabelecimento = logFiltro.getEstabelecimento();
			}
			else {
				estabelecimento = UsuarioSistema.getUsuarioLogado().getEstabelecimento();		
			}
		}
		else {
			logFiltro = new LogFiltro();			
		}
				
		PageWrapper<Log> paginaWrapper = new PageWrapper<>(logsRepositorio.filtrar(
				estabelecimento, logFiltro.getDataHoraInicio(), logFiltro.getDataHoraFim(), pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		
		return mv;
	}	
	
}
