package br.com.utfpr.porta.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.utfpr.porta.modelo.Pessoa;
import br.com.utfpr.porta.repositorio.Pessoas;

@Controller
@RequestMapping("/pessoas")
public class PessoaControle {
	
	@Autowired
	private Pessoas pessoasRepositorio;
	
	@GetMapping("/{cpfOuCnpj}")
	public ResponseEntity<?> obterCpfCnpj(@PathVariable String cpfOuCnpj) {
		
		if(StringUtils.isEmpty(cpfOuCnpj)) {
			return ResponseEntity.notFound().build();
		}
				
		Pessoa pessoa = pessoasRepositorio.findByCpfOuCnpj(cpfOuCnpj);
		
		return ResponseEntity.status(HttpStatus.OK).body(pessoa);
	}

}
