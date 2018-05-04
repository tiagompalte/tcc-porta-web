
var initial = 2000;
var count = initial;
var counter; //10 will  run it every 100th of a second
var initialMillis;
var soundFile;
var mic;
var urlAudio;
var url;
var recorder;

function timer() {
    if (count <= 0) {
        clearInterval(counter);
        stopRecording();
        displayCount(0);
        return;
    }
    var current = Date.now();
    
    count = count - (current - initialMillis);
    initialMillis = current;
    displayCount(count);
}

function displayCount(count) {
    var res = count / 1000;
    document.getElementById("timer").innerHTML = res.toPrecision(count.toString().length) + " segs";
}

function carregarAudio(podeGravar, audio, url) {
	
	if(!audio && !url) {
		window.location.reload();
		return;
	}
	
	this.url = url;
		
	if(audio) {
		urlAudio = url + audio;		
		document.getElementById('audio').value = audio;
	}
	
	if(podeGravar) {		
		navigator.getMedia = ( navigator.getUserMedia ||
                navigator.webkitGetUserMedia ||
                navigator.mozGetUserMedia ||
                navigator.msGetUserMedia);

		navigator.getMedia(
				{audio : true}, 
				// callbackSucesso
				() => {
					document.getElementById('record').removeAttribute("disabled");
					verificarExistenciaAudio();
				}, 
				// callbackErro
				() => {
					document.getElementById('record').setAttribute("disabled","disabled");
					verificarExistenciaAudio();
				});
	}
	else {
		verificarExistenciaAudio();
	}
}

function verificarExistenciaAudio() {
	
	if(urlAudio) {
		
		waitingDialog.show('Carregando Áudio', {
			  dialogSize: 'm',
			  progressType: 'info'
			});
		
		$.ajax({
			url: urlAudio,	
			type: "GET"
		}).success((response) => {			
			if(response) {			
				document.getElementById('tocadorAudio').src = urlAudio;
				document.getElementById('play').removeAttribute("disabled");
			}
		}).done(function() {
			waitingDialog.hide();
			liberarJanelaGravador();
		});
	}
	else {
		liberarJanelaGravador();
	}
}

function liberarJanelaGravador() {
	if(document.getElementById('play').disabled && document.getElementById('record').disabled) {
		swal({
			title: 'Oops', 
			text: 'Não foi encontrado nenhum microfone disponível', 
			type: 'error'
		}, function() {
			window.location.reload();			
		});
	}
}

function startTimer() {
    clearInterval(counter);
    initialMillis = Date.now();
    counter = setInterval(timer, 1);
}

function stopTimer() {
    clearInterval(counter);
}

function resetTimer() {
    clearInterval(counter);
    count = initial;
    displayCount(count);
}

displayCount(initial);

function startRecording() {	
		
	try {		
		mic = new p5.AudioIn();
		mic.start();	
		recorder = new p5.SoundRecorder();
		recorder.setInput(mic);
		soundFile = new p5.SoundFile();
		resetTimer();
		startTimer();
		recorder.record(soundFile);	
		document.getElementById('record').setAttribute("disabled","disabled");
		document.getElementById('cancel').removeAttribute("disabled");
	}
	catch(error) {
		swal('Oops!', error, 'error');		
	}
}

function stopRecording() {	
	recorder.stop();
	stopTimer();
	playRecord();
	document.getElementById('play').removeAttribute("disabled");	
	document.getElementById('record').removeAttribute("disabled");
	document.getElementById('salvarAudio').removeAttribute("disabled");
	document.getElementById('cancel').setAttribute("disabled","disabled");
	
	if(!document.getElementById('audio').value) {
		document.getElementById('audio').value = generateUUID() + ".wav";		
	}
	
}

function playRecord() {
	if(!soundFile && document.getElementById('tocadorAudio').src) {
		document.getElementById('tocadorAudio').play();
	}
	else {
		soundFile.play();
	}	
}

function salvarAudio() {
	
	if(!soundFile || !soundFile.buffer || !url) {
		return;
	}
			
	var dataview = encodeWAV(soundFile.buffer.getChannelData(0), soundFile.buffer.sampleRate);
	var audioBlob = new Blob([dataview], { type: 'audio/wav' });
	
	var formData = new FormData();	
	formData.append("name", document.getElementById('audio').value);
	formData.append("file", audioBlob);
	
	waitingDialog.show('Transmitindo áudio', {
		  dialogSize: 'm',
		  progressType: 'info'
		});
			
	$.ajax({
		url: url,	
		type: "POST",
        enctype: 'multipart/form-data',
        processData: false,
        contentType: false,
        cache: false,
        data: formData
	})
	.success((response) => {		
		swal({
			title: 'Salvo!', 
			text: 'Áudio salvo com sucesso', 
			type: 'success'
		}, function() {
			window.location.reload();			
		});
	})
	.error((error) => {
		swal('Oops!', error.responseText, 'error');
	})
	.done(function() {
		waitingDialog.hide();
	});
}

function generateUUID() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
}

function encodeWAV(buf, sr){
    var buffer = new ArrayBuffer(44 + buf.length * 2);
    var view = new DataView(buffer);

    /* RIFF identifier */
    writeString(view, 0, 'RIFF');
    /* chunk size (= file length - 8) */
    view.setUint32(4, 36 + buf.length * 2, true);
    /* RIFF type */
    writeString(view, 8, 'WAVE');
    /* format chunk identifier */
    writeString(view, 12, 'fmt ');
    /* format chunk length */
    view.setUint32(16, 16, true);
    /* sample format (raw) */
    view.setUint16(20, 1, true);
    /* channel count */
    view.setUint16(22, 1, true);
    /* sample rate */
    view.setUint32(24, sr, true);
    /* byte rate (sample rate * block align) */
    view.setUint32(28, sr *2 , true);
    /* block align (channel count * bytes per sample) */
    view.setUint16(32, 2, true);
    /* bits per sample */
    view.setUint16(34, 16, true);
    /* data chunk identifier */
    writeString(view, 36, 'data');
    /* data chunk length */
    view.setUint32(40, buf.length * 2, true);

    floatTo16BitPCM(view, 44, buf);

    return view;
  }    

  function floatTo16BitPCM(output, offset, input){
    for (var i = 0; i < input.length; i++, offset+=2){
      var s = Math.max(-1, Math.min(1, input[i]));
      output.setInt16(offset, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
    }
  }

  function writeString(view, offset, string){
    for (var i = 0; i < string.length; i++){
      view.setUint8(offset + i, string.charCodeAt(i));
    }
  }
