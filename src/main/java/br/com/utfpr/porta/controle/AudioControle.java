package br.com.utfpr.porta.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import br.com.utfpr.porta.dto.AudioDTO;
import br.com.utfpr.porta.storage.AudioStorage;
import br.com.utfpr.porta.storage.AudioStorageRunnable;

//@RestController
//@RequestMapping("/audios")
public class AudioControle {

//	@Autowired
//	private AudioStorage audioStorage;
//	
//	@PostMapping
//	public DeferredResult<AudioDTO> upload(@RequestParam("files[]") MultipartFile[] files) {
//		DeferredResult<AudioDTO> resultado = new DeferredResult<>();
//
//		Thread thread = new Thread(new AudioStorageRunnable(files, resultado, audioStorage));
//		thread.start();
//		
//		return resultado;
//	}
//	
//	@GetMapping("/temp/{nome:.*}")
//	public byte[] recuperarAudioTemporaria(@PathVariable String nome) {
//		return audioStorage.recuperarAudioTemporaria(nome);
//	}
//	
//	@GetMapping("/{nome:.*}")
//	public byte[] recuperar(@PathVariable String nome) {
//		return audioStorage.recuperar(nome);
//	}
	
}
