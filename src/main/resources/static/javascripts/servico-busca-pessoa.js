
function callbackPessoa(conteudo) {
	
	if(conteudo != null && conteudo != '') {
		document.getElementById('codigoPessoa').value=(conteudo.codigo);
		document.getElementById('nomePessoa').value=(conteudo.nome);
		document.getElementById('telefone').value=(conteudo.telefone);
		document.getElementById('dataNascimento').value=(conteudo.dataNascimento);
		document.getElementById('genero').value=(conteudo.genero);
	}		
}
        
function pesquisapessoa(valor) {

	//CNPJ/CPF somente com dígitos.
    var cpf_cnpj = valor.replace(/\D/g, '');

    //Verifica se campo possui valor informado.
    if (cpf_cnpj != "") {
    	
    	this.input = $('#cpfOuCnpj');
    	
    	var resposta = $.ajax({
			url: this.input.data('url') + '/' + cpf_cnpj,
			method: 'GET',
			contentType: 'application/json'
		});
    	resposta.done(callbackPessoa.bind(this));
    } 
};
