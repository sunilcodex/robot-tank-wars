package org.game.ante;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class RobotActivity extends IOIOActivity {

	private TextView status;
	private SimpleWebServer server;

	private boolean fire = false;

	private Handler handler;

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

		fire = true;
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
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;
		private PwmOutput gunPwmOutput;
		private DigitalInput irReceiver;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			Log.i("tag", "setup");
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			gunPwmOutput = ioio_.openPwmOutput(1, 36000);

			irReceiver = ioio_.openDigitalInput(8);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			boolean hit = false;

			try {
				hit = !irReceiver.read();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			if (fire) {
				led_.write(false);
				gunPwmOutput.setDutyCycle(0.9f);
				fire = false;
			} else {
				led_.write(true);
				gunPwmOutput.setDutyCycle(0.0f);
			}

			if (hit) {
				sendHit("Hit!");
			}

			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * Send a message to the UI thread
		 */
		private void sendHit(String message) {
			handler.sendMessage(handler.obtainMessage(SimpleWebServer.MSG_WHAT_HIT, message));
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
}