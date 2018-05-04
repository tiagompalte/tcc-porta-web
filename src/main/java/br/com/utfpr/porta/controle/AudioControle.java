package br.com.utfpr.porta.controle;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;

import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.repositorio.Parametros;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.seguranca.UsuarioSistema;

@RestController
@RequestMapping("/audios")
public class AudioControle {
	
	private static final String BUCKET = "awporta";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AudioControle.class);
	
	@Autowired
	private AmazonS3 amazonS3;
	
	@Autowired
	private Usuarios usuarioRepositorio;

	@Autowired
	private Parametros parametroRepositorio;
				
	@PostMapping
	public @ResponseBody ResponseEntity upload(@RequestParam String name, @RequestParam MultipartFile file) {	
		
		AccessControlList acl = new AccessControlList();
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
				
		try {
			
			if(file == null || file.isEmpty()) {
				throw new NullPointerException("Nenhum arquivo foi enviado");
			}
			
			if((file.getBytes().length / 1024) > 500) {
				throw new Exception("O arquivo enviado excede o tamanho máximo de 500 kilobytes");
			}
			
			Parametro parCodGrpUsr = parametroRepositorio.findOne("COD_GRP_USUARIO");
			if(parCodGrpUsr == null || Strings.isEmpty(parCodGrpUsr.getValor())) {
				throw new NullPointerException("Parâmetro COD_GRP_USUARIO não cadastrado");
			}
						
			if(!UsuarioSistema.getUsuarioLogado().isPertenceAoGrupo(parCodGrpUsr.getValorLong())) {
				throw new Exception("Usuário não possui o perfil para gravar senha falada");
			}
						
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());
			amazonS3.putObject(new PutObjectRequest(BUCKET, name, file.getInputStream(), metadata).withAccessControlList(acl));
						
			int i = usuarioRepositorio.gravarNomeAudio(UsuarioSistema.getUsuarioLogado().getCodigo(), name);
			
			if(i == 0) {
				LOGGER.error("O áudio {} não foi gravado no usuário {}", name, UsuarioSistema.getCodigoUsuario());
			}
			
			LOGGER.info("Áudio salvo no S3 com o nome {}", name);
		}
		catch(Exception e) {
			usuarioRepositorio.apagarNomeAudio(name);
			LOGGER.error("Erro salvando arquivo no S3: ".concat(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
			
		return ResponseEntity.ok("Áudio gravado");
	}
	
	@GetMapping("/{nome:.*}")
	public byte[] recuperar(@PathVariable String nome) {
		
		byte[] audio = null;
		
		if(Strings.isEmpty(nome)) {
			return audio;
		}
		
		try {
			InputStream is = amazonS3.getObject(BUCKET, nome).getObjectContent();
			audio = IOUtils.toByteArray(is);
		} catch (IOException e) {
			LOGGER.error("Não conseguiu recuperar o áudio {} do S3. ", nome, e);
		} catch (NullPointerException e) {
			usuarioRepositorio.apagarNomeAudio(nome);
			LOGGER.error("O áudio {} não existe. ", nome, e);
		} catch(Exception e) {
			LOGGER.error("Exceção ao recuperar áudio: ", e);
		}
		return audio;
	}
	
}
