var Brewer = Brewer || {};

Brewer.MaskMoney = (function() {
	
	function MaskMoney() {
		this.decimal = $('.js-decimal');
		this.plain = $('.js-plain');
	}
	
	MaskMoney.prototype.enable = function() {
		this.decimal.maskNumber({ decimal: ',', thousands: '.' });
		this.plain.maskNumber({ integer: true, thousands: '.' });
	}
	
	return MaskMoney;
	
}());

Brewer.MaskPhoneNumber = (function() {
	
	function MaskPhoneNumber() {
		this.inputPhoneNumber = $('.js-phone-number');
	}
	
	MaskPhoneNumber.prototype.enable = function() {
		var maskBehavior = function (val) {
		  return val.replace(/\D/g, '').length === 11 ? '(00) 00000-0000' : '(00) 0000-00009';
		};
		
		var options = {
		  onKeyPress: function(val, e, field, options) {
		      field.mask(maskBehavior.apply({}, arguments), options);
		    }
		};
		
		this.inputPhoneNumber.mask(maskBehavior, options);
	}
	
	return MaskPhoneNumber;
	
}());

Brewer.MaskCep = (function() {
	
	function MaskCep() {
		this.inputCep = $('.js-cep');
	}
	
	MaskCep.prototype.enable = function() {
		this.inputCep.mask('00.000-000');
	}
	
	return MaskCep;
	
}());

Brewer.MaskDate = (function() {
	
	function MaskDate() {
		this.inputDate = $('.js-date');
	}
	
	MaskDate.prototype.enable = function() {
		this.inputDate.mask('00/00/0000');
		this.inputDate.datepicker({
			orientation: 'bottom',
			language: 'pt-BR',
			autoclose: true
		});
	}
	
	return MaskDate;
	
}());

Brewer.Security = (function() {
	
	function Security() {
		this.token = $('input[name=_csrf]').val();
		this.header = $('input[name=_csrf_header]').val();
	}
	
	Security.prototype.enable = function() {
		$(document).ajaxSend(function(event, jqxhr, settings) {
			jqxhr.setRequestHeader(this.header, this.token);
		}.bind(this));
	}
	
	return Security;
	
}());

Brewer.MaskSenhaTeclado = (function() {
	
	function MaskSenhaTeclado() {
		this.inputSenhaTeclado = $('.js-senha-teclado');
	}
	
	MaskSenhaTeclado.prototype.enable = function() {
		this.inputSenhaTeclado.mask('0000');
	}
	
	return MaskSenhaTeclado;
	
}());

Brewer.MaskCpf = (function() {
	
	function MaskCpf() {
		this.inputCpf = $('.js-cpf');
	}
	
	MaskCpf.prototype.enable = function() {
		this.inputCpf.mask('000.000.000-00');
	}
	
	return MaskCpf;
	
}());

Brewer.MaskHora = (function() {
	
	function MaskHora() {
		this.inputHora = $('.js-hora');
	}
	
	MaskHora.prototype.enable = function() {				
		this.inputHora.mask('00:00');
	}
	
	return MaskHora;
	
}());

Brewer.MaskDataHora = (function() {
	
	function MaskDataHora() {
		this.inputDataHora = $('.js-datahora');
	}
	
	MaskDataHora.prototype.enable = function() {				
		this.inputDataHora.mask('00/00/0000 00:00');
	}
	
	return MaskDataHora;
	
}());

numeral.language('pt-br');

Brewer.formatarMoeda = function(valor) {
	return numeral(valor).format('0,0.00');
}

Brewer.recuperarValor = function(valorFormatado) {
	return numeral().unformat(valorFormatado);
}

$(function() {
	var maskMoney = new Brewer.MaskMoney();
	maskMoney.enable();
	
	var maskPhoneNumber = new Brewer.MaskPhoneNumber();
	maskPhoneNumber.enable();
	
	var maskCep = new Brewer.MaskCep();
	maskCep.enable();
	
	var maskDate = new Brewer.MaskDate();
	maskDate.enable();
	
	var security = new Brewer.Security();
	security.enable();
	
	var senhaTeclado = new Brewer.MaskSenhaTeclado();
	senhaTeclado.enable();
	
	var cpf = new Brewer.MaskCpf();
	cpf.enable();
	
	var hora = new Brewer.MaskHora();
	hora.enable();
	
	var dataHora = new Brewer.MaskDataHora();
	dataHora.enable();
	
});
