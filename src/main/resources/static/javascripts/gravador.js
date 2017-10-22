var Brewer = Brewer || {};

Brewer.UploadAudio = (function() {
		
	function UploadAudio() {
						
	}
	
	UploadAudio.prototype.iniciar = function () {
		
	}
	
		
	function adicionarCsrfToken(xhr) {
		var token = $('input[name=_csrf]').val();
		var header = $('input[name=_csrf_header').val();
		xhr.setRequestHeader(header, token);
	}
	
	return UploadAudio;
	
})();

$(function() {
	var uploadAudio = new Brewer.UploadAudio();
	uploadAudio.iniciar();
});