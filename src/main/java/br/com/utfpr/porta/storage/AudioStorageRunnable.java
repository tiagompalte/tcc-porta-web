package br.com.utfpr.porta.storage;

import org.springframework.web.multipart.MultipartFile;

import br.com.utfpr.porta.storage.local.AudioStorageLocal;

public class AudioStorageRunnable implements Runnable {

	private MultipartFile file;
	private String name;
	private AudioStorageLocal audioStorage;
	
	public AudioStorageRunnable(MultipartFile file, String name, AudioStorage audioStorage) {
		this.file = file;
		this.name = name;
		this.audioStorage = (AudioStorageLocal) audioStorage;
	}

	@Override
	public void run() {
		this.audioStorage.salvarTemporariamente(name, file);
	}	

}
