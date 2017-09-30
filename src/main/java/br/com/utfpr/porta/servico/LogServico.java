package br.com.utfpr.porta.servico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.utfpr.porta.modelo.Acao;
import br.com.utfpr.porta.modelo.Log;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Logs;

@Service
public class LogServico {
	
	@Autowired
	private Logs logRepositorio;
	
	@Transactional
	public void entrarPorta(Usuario usuario, Porta porta) {
		Log log = new Log();
		log.setAcao(String.format(Acao.ENTRAR_PORTA.getDescricao(), usuario.getCodigoNome(), porta.getCodigoDescricao()));
		log.setEstabelecimento(usuario.getEstabelecimento());
		logRepositorio.save(log);
	}
}
