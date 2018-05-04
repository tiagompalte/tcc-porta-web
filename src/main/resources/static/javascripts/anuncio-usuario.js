$('#estado').change(function() {
	if($("#estado option:selected")["0"].value == null || $("#estado option:selected")["0"].value == "") {
		limparSelectCidade();
		return;
	}
	
	$.ajax({
		type: "GET",
		url: $('#cidade').data('url-cidades') + $("#estado option:selected")["0"].value,
		beforeSend: function() {
			limparSelectCidade();
		},
		success: function(data) {					
			if(data != null && data.length > 0) {
				$('#cidade').prop('disabled', false);
				data.forEach(function(item){
				    $('#cidade').append('<option value='+item+'>' + item + '</option>');
				});
			}
		}
	});
});	

function limparSelectCidade() {
	$('#cidade').prop('disabled', true);
	var select = document.getElementById("cidade");
	var length = $('#cidade')["0"].length - 1;
	for (var i = length; i >= 0; i--) {
		select.options[i] = null;
	}
	$('#cidade').append('<option value="">Todas as cidades</option>');
}

$('.js-marcar-btn').click(function() {
	marcarInteresse($(this).data('url'), $(this).data('objeto'));
});

$('.js-desmarcar-btn').click(function() {			
	desfazerInteresse($(this).data('url'));
});

$('.js-modal-btn').click(function(event) {
	var modal = $('#modalVisualizarAnuncioUsuario');
	var url = $(this).data('url');
	$.ajax({
		url: url,
		method: 'GET'				
	}).success(function(response){
		modal.find('[id="codigo"]').val(response.codigo).end()
			.find('[id="urlModal"]').val(url).end()
			.find('[id="descricaoResumida"]').val(response.descricaoResumida).end()
			.find('[id="preco"]').val(response.preco).end()
			.find('[id="descricao"]').val(response.descricao).end()
			.find('[id="endereco"]').val(response.endereco).end()
			.find('[id="nomeResponsavel"]').val(response.nomeResponsavel).end()
			.find('[id="emailResponsavel"]').val(response.emailResponsavel).end()
			.find('[id="telefoneResponsavel"]').val(response.telefoneResponsavel).end()
			.find('[id="dataPublicacao"]').val(response.dataPublicacao).end()
			.find('[id="dataExpiracao"]').val(response.dataExpiracao).end();
			
		if(response.usuarioJaInteressado) { 					
			$('.js-modal-marcar-btn').addClass('hidden');
			$('.js-modal-desmarcar-btn').removeClass('hidden');
		}
		else {
			$('.js-modal-marcar-btn').removeClass('hidden');
			$('.js-modal-desmarcar-btn').addClass('hidden');
		} 					
		modal.show();
	}).error(function(){
		modal.hide();
	});
});

$('.js-modal-marcar-btn').click(function() {
	let url = document.getElementById("urlModal").value.substring(0, document.getElementById("urlModal").value.lastIndexOf("/"));
	marcarInteresse(url, document.getElementById("codigo").value);			
});
		
$('.js-modal-desmarcar-btn').click(function(){
	desfazerInteresse(document.getElementById("urlModal").value);
});

function marcarInteresse(url, codigo) {
	$.ajax({
		url: url,
		method: 'POST',
		contentType: 'application/json',
		data: JSON.stringify({ codigoAnuncio: codigo }),
		success: function() {
			window.location.reload();
		},
		error: function(e) {
			swal('Oops!', e.responseText, 'error');
		}
	});
}

function desfazerInteresse(url){
	$.ajax({
		url: url,
		method: 'DELETE',
		success: function(){
			window.location.reload();
		},
		error: function(e) {
			swal('Oops!', e.responseText, 'error');
		}
	});
}