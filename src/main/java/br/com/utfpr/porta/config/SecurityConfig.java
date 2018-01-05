package br.com.utfpr.porta.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import br.com.utfpr.porta.seguranca.Seguranca;

@EnableWebSecurity
@ComponentScan(basePackageClasses = Seguranca.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsService userDetailsService;
			
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
			.antMatchers("/layout/**")
			.antMatchers("/images/**")
			.antMatchers("/javascripts/**")
			.antMatchers("/stylesheets/**")
			.antMatchers("/novoUsuario")
			.antMatchers("/salvarNovoUsuario")
			.antMatchers("/novoEstabelecimento")
			.antMatchers("/salvarNovoEstabelecimento")
			.antMatchers("/");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/usuarios").hasRole("VISUALIZAR_USUARIO")
				.antMatchers("/autorizacoes").hasRole("VISUALIZAR_AUTORIZACAO")	
				.antMatchers("/portas").hasRole("VISUALIZAR_PORTA")
				.antMatchers("/usuarios/**").hasRole("CADASTRAR_USUARIO")
				.antMatchers("/pessoas/**").hasRole("CADASTRAR_USUARIO")
				.antMatchers("/autorizacoes/**").hasRole("CADASTRAR_AUTORIZACAO")
				.antMatchers("/estabelecimentos/**").hasRole("CADASTRAR_ESTABELECIMENTO")
				.antMatchers("/portas/**").hasRole("CADASTRAR_PORTA")
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/login")
				.permitAll()
				.defaultSuccessUrl("/dashboard")
				.and()
			.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/")
				.and()
			.exceptionHandling()
				.accessDeniedPage("/403")
				.and()
			.sessionManagement()
				.invalidSessionUrl("/login")
				.maximumSessions(1).expiredUrl("/login");
	}	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
