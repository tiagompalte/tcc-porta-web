var initial = 3000;
var count = initial;
var counter; //10 will  run it every 100th of a second
var initialMillis;
var soundFile;
var mic;

function succes(stream) {}

function fail(error) {	
	alert("Não foi encontrado nenhum microfone disponível");
	this.divGroupAudio = document.getElementById('divGroupAudio');
	this.divGroupAudio.style.display = 'none';
}

navigator.getUserMedia({audio : true}, succes, fail);

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
    document.getElementById("timer").innerHTML = res.toPrecision(count.toString().length) + " segundos";
}

function carregarAudio() {
	
	if(document.getElementById('audio').value == null || document.getElementById('audio').value === '') {
		return;
	}
	
	document.getElementById('tocadorAudio').src = $('.js-container-audio').data('url-audios') + "/" + document.getElementById('audio').value;
	document.getElementById('play').removeAttribute("disabled");
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
	catch(err) {
		alert(err);
	}
}

function stopRecording() {	
	recorder.stop();
	stopTimer();
	playRecord();
	document.getElementById('play').removeAttribute("disabled");	
	document.getElementById('record').removeAttribute("disabled");
	document.getElementById('cancel').setAttribute("disabled","disabled");
	
	if(document.getElementById('audio').value == null || document.getElementById('audio').value === '') {
		document.getElementById('audio').value = generateUUID() + ".wav";
	}
	
}

function playRecord() {
	if((soundFile === undefined || soundFile == null) && document.getElementById('tocadorAudio').src != null 
			&& document.getElementById('tocadorAudio').src != '') {
		document.getElementById('tocadorAudio').play();
	}
	else {
		soundFile.play();
	}	
}

function adicionarCsrfToken(xhr) {
	var token = $('input[name=_csrf]').val();
	var header = $('input[name=_csrf_header').val();
	xhr.setRequestHeader(header, token);
}

function salvarAudio() {
	
	if(soundFile === undefined || soundFile == null || soundFile.buffer == null) {
		return;
	}
		
	var dataview = encodeWAV(soundFile.buffer.getChannelData(0), soundFile.buffer.sampleRate);
	var audioBlob = new Blob([dataview], { type: 'audio/wav' });
	
	var formData = new FormData();	
	formData.append("name", document.getElementById('audio').value);
	formData.append("file", audioBlob);
		
	$.ajax({
		url: $('.js-container-audio').data('url-audios'),	
		type: "POST",
        enctype: 'multipart/form-data',
        processData: false,
        contentType: false,
        cache: false,
        data: formData
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
