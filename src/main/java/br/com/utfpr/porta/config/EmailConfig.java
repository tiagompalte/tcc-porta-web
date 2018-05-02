package br.com.utfpr.porta.config;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

@Configuration
@PropertySource(value = { "file://${HOME}/email-account.properties", "file:\\${USERPROFILE}\\email-account.properties" }, ignoreResourceNotFound = true)
public class EmailConfig {
	
	@Autowired
	private Environment env;
		
	private static final String EMAIL_USERNAME = "EMAIL_USERNAME";
	private static final String EMAIL_PASSOWORD = "EMAIL_PASSOWORD";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailConfig.class);
		
	@Profile("local")
	@Bean
	public JavaMailSender envioEmailLocal() {
		if(StringUtils.isEmpty(env.getProperty(EMAIL_USERNAME)) || StringUtils.isEmpty(env.getProperty(EMAIL_PASSOWORD))) {
			return null;
		}
		return envioEmail(env.getProperty(EMAIL_USERNAME), env.getProperty(EMAIL_PASSOWORD));
	}
	
	@Profile("prod")
	@Bean
	public JavaMailSender envioEmailProd() {
		return envioEmail(System.getenv(EMAIL_USERNAME), System.getenv(EMAIL_PASSOWORD));
	}
	
	private JavaMailSender envioEmail(String username, String password) {		
		Properties prop = carregarArquivoConfiguracao();		
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setJavaMailProperties(prop);	     
	    mailSender.setUsername(username);
	    mailSender.setPassword(password);
	    return mailSender;
	}
	
	private Properties carregarArquivoConfiguracao() {
		Properties prop = new Properties();
		try {		
			prop.load(getClass().getResourceAsStream("/env/email.properties"));
		} catch (IOException e) {
			LOGGER.error("Erro ao carregar arquivo de configuração de email. ", e);
		} 
		return prop;
	}

}
