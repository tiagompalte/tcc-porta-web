package br.com.utfpr.porta.thymeleaf;

import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import br.com.utfpr.porta.thymeleaf.processador.ClassForErrorAttributeTagProcessor;
import br.com.utfpr.porta.thymeleaf.processador.MessageElementTagProcessor;
import br.com.utfpr.porta.thymeleaf.processador.OrderElementTagProcessor;
import br.com.utfpr.porta.thymeleaf.processador.PaginationElementTagProcessor;


public class BrewerDialect extends AbstractProcessorDialect {
	
	public BrewerDialect() {
		super("AlgaWorks Brewer", "brewer", StandardDialect.PROCESSOR_PRECEDENCE);
	}
	
	@Override
	public Set<IProcessor> getProcessors(String dialectPrefix) {
		final Set<IProcessor> processadores = new HashSet<>();
		processadores.add(new ClassForErrorAttributeTagProcessor(dialectPrefix));
		processadores.add(new MessageElementTagProcessor(dialectPrefix));
		processadores.add(new OrderElementTagProcessor(dialectPrefix));
		processadores.add(new PaginationElementTagProcessor(dialectPrefix));
		return processadores;
	}

}
