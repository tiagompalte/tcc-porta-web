package br.com.utfpr.porta.config;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.repositorio.Parametros;

@Component
public class ScheduledTasks {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
	
	@Autowired
	private Parametros parametroRepositorio;
	
    @Scheduled(initialDelay = 0, fixedDelay = 1500000) //25 minutos
    public void pingServicoAudio() {
		
		Parametro parUrlAudio = parametroRepositorio.findOne("URL_AUDIO");
		
		if(parUrlAudio == null || Strings.isEmpty(parUrlAudio.getValor())) {
			LOGGER.error("Erro no ping no serviço de áudio: Parâmetro URL_AUDIO não cadastrado");
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
				LOGGER.info("Ping no serviço de áudio: Tentativa: ".concat(String.valueOf(tentativas).concat(" Resposta: ").concat(String.valueOf(responseCode))));
				Thread.sleep(3000);
			}
			while(responseCode != HttpURLConnection.HTTP_OK && tentativas < 10);
		}
		catch(Exception e) {
			LOGGER.error("Erro no ping no serviço de áudio: ", e);
		}
    }
    
	//@Scheduled(cron="0 0 0 * * SUN-SAT") //segundo, minuto, hora, dia, mês, dia da semana 
	//public void limpezaBaseDadosAutorizacoesTemporarias() {
		//thread para limpeza da base de dados de autorizações temporárias já realizadas
    
	//}

}
