package br.com.utfpr.porta.storage.local;

import static java.nio.file.FileSystems.getDefault;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import br.com.utfpr.porta.storage.AudioStorage;

public class AudioStorageLocal implements AudioStorage {

	private static final Logger LOGGER = LoggerFactory.getLogger(AudioStorageLocal.class);
	
	private Path local;
	private Path localTemporario;
	
	public AudioStorageLocal() {
		this(getDefault().getPath(System.getenv("HOME"), ".portaaudios"));
	}
	
	public AudioStorageLocal(Path path) {
		this.local = path;
		criarPastas();
	}

	@Override
	public void salvarTemporariamente(String name, MultipartFile file) {
		if (file != null && !StringUtils.isEmpty(name)) {
			try {
				file.transferTo(new File(this.localTemporario.toAbsolutePath().toString() + getDefault().getSeparator() + name));
			} catch (IOException e) {
				throw new RuntimeException("Erro salvando o audio na pasta temporário", e);
			}
		}
	}
	
	@Override
	public byte[] recuperarAudioTemporaria(String nome) {
		try {
			return Files.readAllBytes(this.localTemporario.resolve(nome));
		} catch (IOException e) {
			throw new RuntimeException("Erro lendo o audio temporário", e);
		}
	}
	
	@Override
	public void salvar(String audio) {
		try {
			Files.move(this.localTemporario.resolve(audio), this.local.resolve(audio));
		} catch (IOException e) {
			throw new RuntimeException("Erro movendo o audio para destino final", e);
		}		
	}
	
	@Override
	public byte[] recuperar(String nome) {
		try {
			return Files.readAllBytes(this.local.resolve(nome));
		} catch (IOException e) {
			throw new RuntimeException("Erro lendo o audio", e);
		}
	}
	
	private void criarPastas() {
		try {
			Files.createDirectories(this.local);
			this.localTemporario = getDefault().getPath(this.local.toString(), "temp");
			Files.createDirectories(this.localTemporario);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Pastas criadas para salvar audios.");
				LOGGER.debug("Pasta default: " + this.local.toAbsolutePath());
				LOGGER.debug("Pasta temporária: " + this.localTemporario.toAbsolutePath());
			}
		} catch (IOException e) {
			throw new RuntimeException("Erro criando pasta para salvar audio", e);
		}
	}
	
}
