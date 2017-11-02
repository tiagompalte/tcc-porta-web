package br.com.utfpr.porta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import br.com.utfpr.porta.servico.Servico;
import br.com.utfpr.porta.storage.AudioStorage;
import br.com.utfpr.porta.storage.local.AudioStorageLocal;

@Configuration
@ComponentScan(basePackageClasses = Servico.class)
public class ServiceConfig {
	
	@Bean
	public AudioStorage audioStorage() {
		return new AudioStorageLocal();
	}

}
