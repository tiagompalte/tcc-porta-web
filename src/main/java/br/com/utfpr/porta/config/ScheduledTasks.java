package br.com.utfpr.porta.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.utfpr.porta.repositorio.Autorizacoes;
import br.com.utfpr.porta.repositorio.TokenResetSenhas;

@Component
public class ScheduledTasks {
			
	@Autowired
	private Autorizacoes autorizacaoRepositorio;
	
	@Autowired
	private TokenResetSenhas tokenResetSenhasRepositorio;
	
    /**
     * Tarefa que realiza a limpeza da base de dados de autorizações temporárias vencidas
     */
	//@Scheduled(cron="0 0 0 * * SUN-SAT") //segundo, minuto, hora, dia, mês, dia da semana
    @Scheduled(initialDelay = 0, fixedDelay = 3600000) // 1 hora
	public void limpezaBaseDadosAutorizacoesTemporarias() {		
    		autorizacaoRepositorio.apagarAutorizacoesTemporariasVencidas(new Date());
    }
    
    @Scheduled(initialDelay = 0, fixedDelay = 3600000) // 1 hora
	public void limpezaTokensResetSenhas() {		
    		tokenResetSenhasRepositorio.apagarTokensVencidos(new Date());
    }

}
