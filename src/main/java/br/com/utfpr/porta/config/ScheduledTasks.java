package br.com.utfpr.porta.config;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.repositorio.Autorizacoes;
import br.com.utfpr.porta.repositorio.Parametros;
import br.com.utfpr.porta.repositorio.TokenResetSenhas;

@Component
public class ScheduledTasks {
	
	private static final String PARAMETRO_AUDIO = "URL_AUDIO";
	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
	
	@Autowired
	private Parametros parametroRepositorio;
	
	@Autowired
	private Autorizacoes autorizacaoRepositorio;
	
	@Autowired
	private TokenResetSenhas tokenResetSenhasRepositorio;
	
	/**
	 * Tarefa que executa a cada 25 minutos para verificar a estabilidade do micro serviço de áudio
	 */
    @Scheduled(initialDelay = 0, fixedDelay = 1500000) //25 minutos
    public void pingServicoAudio() {
		
		Parametro parUrlAudio = parametroRepositorio.findOne(PARAMETRO_AUDIO);
		
		if(parUrlAudio == null || Strings.isEmpty(parUrlAudio.getValor())) {
			LOGGER.error("Erro no ping no serviço áudio. Parâmetro {} não cadastrado", PARAMETRO_AUDIO);
			return;
		}
		
		try {
			URL obj = new URL(parUrlAudio.getValor());
			HttpURLConnection con;
			int responseCode = 0;
			int tentativas = 0;
			do {			
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");			
				responseCode = con.getResponseCode();
				tentativas++;
				LOGGER.info("Ping no serviço de API: Tentativa: {} Resposta: {}", tentativas, responseCode);				
				Thread.sleep(3000);
			}
			while(!(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) && tentativas < 10);
		}
		catch(Exception e) {
			LOGGER.error("Erro no ping no serviço de áudio: ", e);
		}
    }
    
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
