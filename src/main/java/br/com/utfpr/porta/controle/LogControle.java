package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import br.com.utfpr.porta.controle.paginacao.PageWrapper;
import br.com.utfpr.porta.modelo.Estabelecimento;
import br.com.utfpr.porta.modelo.Log;
import br.com.utfpr.porta.repositorio.Logs;
import br.com.utfpr.porta.repositorio.filtro.LogFiltro;
import br.com.utfpr.porta.seguranca.UsuarioSistema;

@Controller
@RequestMapping("/logs")
public class LogControle {
	
	@Autowired
	private Logs logsRepositorio;

	@GetMapping
	public ModelAndView pesquisar(LogFiltro logFiltro, @PageableDefault(size = 5) Pageable pageable, HttpServletRequest httpServletRequest) {
		
		ModelAndView mv = new ModelAndView("/log/PesquisaLogs");
		
		LocalDateTime dataHoraInicio = null;
		LocalDateTime dataHoraFinal = null;
		
		if(logFiltro != null) {
			if(logFiltro.getDataInicio() != null) {
				if(logFiltro.getHoraInicio() != null) {
					dataHoraInicio = LocalDateTime.of(logFiltro.getDataInicio(), logFiltro.getHoraInicio());
				}
				else {
					dataHoraInicio = LocalDateTime.of(logFiltro.getDataInicio(), LocalTime.MIN);
				}				
			}
			
			if(logFiltro.getDataFinal() != null) {
				if(logFiltro.getHoraFinal() != null) {
					dataHoraFinal = LocalDateTime.of(logFiltro.getDataFinal(), logFiltro.getHoraFinal());
				}
				else {
					dataHoraFinal = LocalDateTime.of(logFiltro.getDataFinal(), LocalTime.MAX);
				}
			}
		}
		
		Estabelecimento estabelecimento = null;
		if(UsuarioSistema.getUsuarioLogado().getEstabelecimento() != null) {
			estabelecimento = UsuarioSistema.getUsuarioLogado().getEstabelecimento();			
		}
		else if(logFiltro != null && logFiltro.getEstabelecimento() != null){
			estabelecimento = logFiltro.getEstabelecimento();
		}
		
		PageWrapper<Log> paginaWrapper = new PageWrapper<>(logsRepositorio.filtrar(
				estabelecimento, dataHoraInicio, dataHoraFinal, pageable), httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		
		return mv;
	}	
	
}
