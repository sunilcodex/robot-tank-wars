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

	private Robot ioioRobotLooper = null;

	MediaPlayer mpExplosion = null;
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
				}
			}
		};

		// setup log screen
		status = (TextView) findViewById(R.id.status);

		logMessage("starting web server...");
		server = new SimpleWebServer(getResources().getAssets(), handler);
		server.start();

		mpExplosion = MediaPlayer.create(this, R.raw.explosion);
		mpHit = MediaPlayer.create(this, R.raw.explosion);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mpExplosion.release();
		mpHit.release();

		server.stopServer();
	}

	private void fire() {
		mpExplosion.seekTo(0);
		mpExplosion.start();

		ioioRobotLooper.fire();
	}

	private void hit() {
		mpHit.seekTo(0);
		mpHit.start();

		logMessage("hit");
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
		ioioRobotLooper = new Robot(handler);
		return ioioRobotLooper;
	}
}