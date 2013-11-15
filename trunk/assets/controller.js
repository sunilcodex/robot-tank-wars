var interval;
var requestCounter = 0;	

var inits = {
	buttons: function initButtons() {
		$('.buttons button')
			.bind('mousedown', buttonPress);
	},
	clean: function() {
		$('.buttons button')
			.unbind('mousedown', buttonPress);
	}
};

init(); //go!

function init() {
	inits['clean']();
	inits['buttons']();
}

function buttonPress() {
	switch ($(this).val()) {
		case 'fire' : fire(); break;
		case 'go' : motor('go'); break;
		case 'stop' : motor('stop'); break;
	}
}

function fire() {
	var url = '/control?'+$.param({
		counter:requestCounter,
		action:'fire'
	});
	$.post(url);

	requestCounter++;
}

function motor(state) {
	var url = '/control?'+$.param({
		counter:requestCounter,
		action:'motor',
		directive:state
	});
	$.post(url);

	requestCounter++;
}