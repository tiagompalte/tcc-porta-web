package br.com.utfpr.porta.storage;

import org.springframework.web.multipart.MultipartFile;

public interface AudioStorage {
	
	public void salvarTemporariamente(String name, MultipartFile file);

	public byte[] recuperarAudioTemporaria(String nome);

	public void salvar(String audio);

	public byte[] recuperar(String audio);

}
