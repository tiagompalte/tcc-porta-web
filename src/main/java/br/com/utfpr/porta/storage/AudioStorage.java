package br.com.utfpr.porta.storage;

import org.springframework.web.multipart.MultipartFile;

public interface AudioStorage {
	
	public String salvarTemporariamente(MultipartFile[] files);

	public byte[] recuperarAudioTemporaria(String nome);

	public void salvar(String audio);

	public byte[] recuperar(String audio);

}
