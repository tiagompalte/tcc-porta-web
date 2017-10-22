package br.com.utfpr.porta.storage;

import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import br.com.utfpr.porta.dto.AudioDTO;
import br.com.utfpr.porta.storage.local.AudioStorageLocal;

public class AudioStorageRunnable implements Runnable {

	private MultipartFile[] files;
	private DeferredResult<AudioDTO> resultado;
	private AudioStorageLocal audioStorage;
	
	public AudioStorageRunnable(MultipartFile[] files, DeferredResult<AudioDTO> resultado, AudioStorage audioStorage) {
		this.files = files;
		this.resultado = resultado;
		this.audioStorage = (AudioStorageLocal) audioStorage;
	}

	@Override
	public void run() {
		String nomeFoto = this.audioStorage.salvarTemporariamente(files);
		String contentType = files[0].getContentType();
		resultado.setResult(new AudioDTO(nomeFoto, contentType));
	}	

}
