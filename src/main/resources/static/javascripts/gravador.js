var initial = 3000;
var count = initial;
var counter; //10 will  run it every 100th of a second
var initialMillis;

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
	mic = new p5.AudioIn();
	mic.start();
	recorder = new p5.SoundRecorder();
	recorder.setInput(mic);
	soundFile = new p5.SoundFile();
	recorder.record(soundFile);
	//setTimeout(3000, stopRecording());
	resetTimer();
	startTimer();
	document.getElementById('record').setAttribute("disabled","disabled");
	document.getElementById('cancel').removeAttribute("disabled");	
}

function stopRecording() {	
	recorder.stop();
	stopTimer();
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