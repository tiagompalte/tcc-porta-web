var Brewer = Brewer || {};

Brewer.MascaraCpfCnpj = (function() {
	
	function MascaraCpfCnpj() {
		this.radioTipoPessoa = $('.js-radio-tipo-pessoa');
		this.labelCpfCnpj = $('[for=cpfOuCnpj]');
		this.inputCpfCnpj = $('#cpfOuCnpj');
		this.labelData = $('[for=dataNascimento]');
	}
	
	MascaraCpfCnpj.prototype.iniciar = function() {
		this.radioTipoPessoa.on('change', onTipoPessoaAlterado.bind(this));
		var tipoPessoaSelecionada = this.radioTipoPessoa.filter(':checked')[0];
		if (tipoPessoaSelecionada) {
			aplicarMascara.call(this, $(tipoPessoaSelecionada));
		}
	}
	
	function onTipoPessoaAlterado(evento) {
		var tipoPessoaSelecionada = $(evento.currentTarget);
		aplicarMascara.call(this, tipoPessoaSelecionada);
		this.inputCpfCnpj.val('');
	}
	
	function aplicarMascara(tipoPessoaSelecionada) {
		this.labelCpfCnpj.text(tipoPessoaSelecionada.data('documento'));
		this.inputCpfCnpj.mask(tipoPessoaSelecionada.data('mascara'));
		this.inputCpfCnpj.removeAttr('disabled');
		this.labelData.text(tipoPessoaSelecionada.data('data'));
		
		this.divGenero = document.getElementById('divGenero');
		
		if(tipoPessoaSelecionada.data('documento') == 'CPF') {
			this.divGenero.style.display = 'block';
		}
		else {
			this.divGenero.style.display = 'none';
		}
		
	}
	
	return MascaraCpfCnpj;
	
}());

$(function() {
	var mascaraCpfCnpj = new Brewer.MascaraCpfCnpj();
	mascaraCpfCnpj.iniciar();
});