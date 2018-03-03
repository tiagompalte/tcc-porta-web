package br.com.utfpr.porta.controle;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;

import br.com.utfpr.porta.repositorio.Usuarios;

@RestController
@RequestMapping("/audios")
public class AudioControle {
	
	private static final String BUCKET = "awporta";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AudioControle.class);
	
	@Autowired
	private AmazonS3 amazonS3;
	
	@Autowired
	private Usuarios usuarioRepositorio;	
			
	@PostMapping
	public void upload(@RequestParam String name, @RequestParam MultipartFile file) {	
		
		AccessControlList acl = new AccessControlList();
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
				
		try {
			
			if(file == null || file.isEmpty()) {
				throw new NullPointerException("Nenhum arquivo foi enviado");
			}
			
			if((file.getBytes().length / 1024) > 500) {
				throw new Exception("O arquivo enviado excede o tamanho máximo de 500 kilobytes");
			}
						
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());
			amazonS3.putObject(new PutObjectRequest(BUCKET, name, file.getInputStream(), metadata).withAccessControlList(acl));
			LOGGER.info("Áudio salvo no S3 com o nome ".concat(name));
		}
		catch(Exception e) {
			usuarioRepositorio.apagarNomeAudio(name);
			LOGGER.error("Erro salvando arquivo no S3: ".concat(e.getMessage()));
		}
				
	}
	
	@GetMapping("/{nome:.*}")
	public byte[] recuperar(@PathVariable String nome) {
		
		if(Strings.isEmpty(nome)) {
			return null;
		}
		
		try {
			InputStream is = amazonS3.getObject(BUCKET, nome).getObjectContent();
			return IOUtils.toByteArray(is);
		} catch (IOException e) {
			LOGGER.error(String.format("Não conseguiu recuperar o áudio %s do S3", nome).concat(e.getMessage()));
		} catch (NullPointerException e) {
			LOGGER.error(String.format("O áudio %s não existe. ", nome).concat(e.getMessage()));
		} catch(Exception e) {
			LOGGER.error("Exceção ao recuperar áudio: ", e);
		}
		return null;
	}
	
}
