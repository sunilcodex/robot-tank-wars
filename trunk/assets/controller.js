var interval, stopCount = 0, requestCounter = 0, 
	leftSpeed = 0, rightSpeed = 0;
			
var inits = {
	buttons: function initButtons() {
		$('.panel.buttons button')
			.bind('mousedown', buttonPress)
			.bind('touchstart', buttonPress)
			.bind('mouseup', stop)
			.bind('touchend', stop);
		$(window).bind('keydown', keyDown).bind('keyup', stop);
	},
	joystick: function() {
		$('#stickInput').click(startStick);
	},
	tilt: function() {
		window.addEventListener('devicemotion', motionListener, true);
	},
	clean: function() {
		$('.panel.buttons button')
			.unbind('mousedown', buttonPress)
			.unbind('touchstart', buttonPress)
			.unbind('mouseup', stop)
			.unbind('touchend', stop);
		$(window).unbind('keydown', keyDown).unbind('keyup', stop);
		window.removeEventListener('devicemotion', motionListener, true);
	}
};

init(); //go!

function init() {
	$('#nav a').click(function(e) {
		e.preventDefault();
		
		var type = $(this).closest('li').addClass('active')
			.siblings().removeClass('active').end()
			.attr('class').split(' ')[0];
		$('.panel.'+type).show().siblings('.panel').hide();
		
		inits['clean']();
		inits[type]();
	}).eq(0).click();
	
	initCamera();
	
	if (navigator.platform.indexOf("iPhone") == -1 && navigator.platform.indexOf("iPod") == -1) {
		$('.panel.tilt p').text('Tilt control only works with iPhones & iPads');
	}
}

function buttonPress() {
	switch ($(this).val()) {
		case 'forward' : leftSpeed =  0.6; rightSpeed =  0.6; break;
		case 'left' :    leftSpeed = -0.3; rightSpeed =  0.5; break;
		case 'right' :   leftSpeed =  0.5; rightSpeed = -0.3; break;
		case 'back' :    leftSpeed = -0.5; rightSpeed = -0.5; break;
	}
	startLoop();
}

function keyDown(e) {
	switch (e.keyCode) {
		case 87 : leftSpeed =  0.6; rightSpeed =  0.6; break; //W
		case 65 : leftSpeed = -0.3; rightSpeed =  0.5; break; //A
		case 68 : leftSpeed =  0.5; rightSpeed = -0.3; break; //D
		case 83 : leftSpeed = -0.5; rightSpeed = -0.5; break; //S
		default : return;
	}
	startLoop();
}
	

function stop() {
	leftSpeed = 0;
	rightSpeed = 0;
}

function startLoop() {
	clearInterval(interval);
	interval = setInterval(function(){
		var url = '/control?'+$.param({
			counter:requestCounter,
			left:leftSpeed,
			right:rightSpeed
		});
		$.post(url);
		
		requestCounter++;
		if (!leftSpeed && !rightSpeed) stopCount++;
		else stopCount = 0;
		if (stopCount > 5) clearInterval(interval);
	}, 80);
}

function initCamera() {
	var originalSrc = $('#view img').attr('src');
	var cameraInterval = setInterval(function(){
		$('#view img').attr('src', originalSrc + '?' + new Date().getTime());
	}, 700);
	$('#view img').click(function(){
		clearInterval(cameraInterval);
	});
}

function startStick(e) {
	e.stopPropagation();
	var $container = $(this);
	var width = $container.width();
	var height = $container.height();
	var top = $container.position().top;
	var left = $container.position().left;
	
	$('body').bind('mousemove', function(e) {
		var x = e.pageX - left;
		var y = e.pageY - top;
		if (x<=0 || y<=0 || x>=width || y>=height) {
			x = width/2;
			y = height/2;
		}
		$container.find('span').css({
			left: x-5,
			top: y-5
		});
		
		xyToSpeed(x*2/width-1, 1-y*2/height);
	}).one('click', function(){
		$('#stickInput').unbind('mousemove');
	});
}

function motionListener(e) {
	var x = e.accelerationIncludingGravity.x / 10;
	var y = e.accelerationIncludingGravity.y / 10;
	
	$('#tiltDisplay span').css({
		left: 50+x*50+'%',
		top: 50-y*50+'%'
	});
	
	xyToSpeed(x, y)
}

function xyToSpeed(x, y) {
	var angle = Math.atan2(x, y);
	var speed = Math.min(1, Math.sqrt(x*x+y*y));

	if (speed < 0.2) speed = 0;
	if (Math.abs(angle) < 0.3) angle = 0;
	if (Math.abs(angle) > 2.8) angle = Math.PI;
	
	if (speed && !leftSpeed && !rightSpeed) {
		startLoop();
	}
	 
	leftSpeed = Math.round(speed * angleToSpeed(angle-Math.PI/2) * 100) / 100;
	rightSpeed = Math.round(speed * angleToSpeed(angle) * 100) / 100;
}


function angleToSpeed(angle) {
	//normalise angle to between 0 and 2PI
	angle %= Math.PI*2;
	if (angle < 0) angle += Math.PI*2;
	
	//create mustache function
	angle *= 2;
	if (angle < Math.PI) return Math.cos(angle);
	if (angle < Math.PI*2) return -1;
	if (angle < Math.PI*3) return Math.cos(angle+Math.PI);
	return 1;
	
}