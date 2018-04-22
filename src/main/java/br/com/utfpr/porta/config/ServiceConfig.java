package br.com.utfpr.porta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.thymeleaf.TemplateEngine;

import br.com.utfpr.porta.email.EmailServico;
import br.com.utfpr.porta.email.EmailServicoImpl;
import br.com.utfpr.porta.servico.Servico;
import br.com.utfpr.porta.storage.AudioStorage;
import br.com.utfpr.porta.storage.local.AudioStorageLocal;
import br.com.utfpr.porta.storage.s3.AudioStorageS3;

@Configuration
@ComponentScan(basePackageClasses = Servico.class)
@Import({WebConfig.class})
public class ServiceConfig {
	
	@Profile("local")
	@Bean
	public AudioStorage audioStorageLocal() {
		return new AudioStorageLocal();
	}
	
	@Profile("prod")
	@Bean
	public AudioStorage audioStorageS3() {
		return new AudioStorageS3();
	}
	
	@Bean
	public EmailServico emailServico(TemplateEngine templateEngine) {
		return new EmailServicoImpl(templateEngine);
	}
		
}
