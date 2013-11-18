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
		case 'forward_L_20' : motor('forward_L_20'); break;
		case 'forward_L_50' : motor('forward_L_50'); break;
		case 'forward_L_Full' : motor('forward_L_Full'); break;
		case 'stop_L' : motor('stop_L'); break;
		case 'forward_R_20' : motor('forward_R_20'); break;
		case 'forward_R_50' : motor('forward_R_50'); break;
		case 'forward_R_Full' : motor('forward_R_Full'); break;
		case 'stop_R' : motor('stop_R'); break;
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