
var timer = new Timer();
timer.addEventListener('secondsUpdated', function (e) {
    $('#time-display').html(timer.getTimeValues().toString(['seconds', 'secondTenths']));
}); 

timer.addEventListener('targetAchieved', function (e) {
    $('#time-display').html('03:00');
});

function startRecording() {
	mic = new p5.AudioIn();
	mic.start();
	recorder = new p5.SoundRecorder();
	recorder.setInput(mic);
	soundFile = new p5.SoundFile();
	timer.start({precision: 'secondTenths', countdown: true, startValues: {seconds: 3}});
	recorder.record(soundFile);
	setTimeout(stopRecording, 3000);
	document.getElementById('record').setAttribute("disabled","disabled");
	document.getElementById('cancel').removeAttribute("disabled");	
}

function stopRecording() {
	timer.stop();
	recorder.stop();
	playRecord();
	document.getElementById('play').removeAttribute("disabled");
	document.getElementById('record').removeAttribute("disabled");
	document.getElementById('cancel').setAttribute("disabled","disabled");
}

function playRecord() {
	soundFile.play();
}

function adicionarCsrfToken(xhr) {
	var token = $('input[name=_csrf]').val();
	var header = $('input[name=_csrf_header').val();
	xhr.setRequestHeader(header, token);
}

//document.getElementsByClassName('js-container-audio')["0"].attributes[1].value