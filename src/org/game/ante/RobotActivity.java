package org.game.ante;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class RobotActivity extends IOIOActivity {
	private TextView status;
	private SimpleWebServer server;

	private Handler handler;

	private IOIORobot robot = null;

	MediaPlayer mpShot = null;
	MediaPlayer mpHit = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// setup web server
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// logMessage("message: " + (String) msg.obj);

				switch (msg.what) {
				case 1:
					logMessage((String) msg.obj);
					break;
				case SimpleWebServer.MSG_WHAT_FIRE:
					fire();

					break;
				case SimpleWebServer.MSG_WHAT_HIT:
					hit();

					break;
				case SimpleWebServer.MSG_WHAT_MOTOR:

					int directive = msg.arg1;

					motor(directive);

					break;
				}
			}
		};

		// setup log screen
		status = (TextView) findViewById(R.id.status);

		logMessage("starting web server...");
		try {
			server = new SimpleWebServer(getResources().getAssets(), handler);
			server.start();
		} catch (Exception e) {
			logMessage("Exception, Server not started!");

			e.printStackTrace();
		}

		mpShot = MediaPlayer.create(this, R.raw.shot);
		mpShot.seekTo(0);
		mpHit = MediaPlayer.create(this, R.raw.hit);
		mpHit.seekTo(0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mpShot.release();
		mpHit.release();

		if (server != null) {
			server.stopServer();
		}
	}

	private void fire() {
		mpShot.seekTo(0);
		mpShot.start();

		robot.fire();
	}

	private void hit() {
		mpHit.seekTo(0);
		mpHit.start();

		logMessage("hit");
	}

	private void motor(int directive) {
		robot.motor(directive);

		logMessage("move, directive: " + directive);
	}

	private void logMessage(String message) {
		status.setText(message + "\n" + status.getText());
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		robot = new IOIORobot(handler);
		return robot;
	}
}