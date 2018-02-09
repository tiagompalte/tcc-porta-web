package br.com.utfpr.porta.util;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.repositorio.Parametros;

public class EnvioRequisicao implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EnvioRequisicao.class);
			
	@Autowired
	private Parametros parametroRepositorio;
		
	@Override
	public void run() {
		
		Parametro parUrlAudio = parametroRepositorio.findOne("URL_AUDIO");
		
		if(parUrlAudio == null || Strings.isEmpty(parUrlAudio.getValor())) {
			LOGGER.error("Parâmetro URL_AUDIO não cadastrado");
			return;
		}
		
		try {
			URL obj = new URL(parUrlAudio.getValor());
			HttpURLConnection con;
			int responseCode = 0;
			int tentativas = 1;
			do {			
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");			
				responseCode = con.getResponseCode();	
				LOGGER.info("Tentativa: ".concat(String.valueOf(tentativas).concat(" Resposta: ").concat(String.valueOf(responseCode))));
				Thread.sleep(3000);
			}
			while(responseCode != HttpURLConnection.HTTP_OK || tentativas < 10);
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage());
		}
		
	}		
	
}
