package org.game.ante;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import android.os.Handler;
import android.util.Log;

/**
 * This is the thread on which all the IOIO activity happens. It will be run
 * every time the application is resumed and aborted when it is paused. The
 * method setup() will be called right after a connection with the IOIO has been
 * established (which might happen several times!). Then, loop() will be called
 * repetitively until the IOIO gets disconnected.
 */
public class IOIORobot extends BaseIOIOLooper {
	final public static int PIN_GUN = 1;
	final public static int PIN_MOTOR_LEFT = 46;
	final public static int PIN_MOTOR_RIGHT = 45;
	final public static int PIN_SENZOR = 42;

	/** The on-board LED. */
	// private DigitalOutput led_;

	private PwmOutput gunPwmOutput;
	private PwmOutput motorLeftPwmOutput;
	private PwmOutput motorRightPwmOutput;
	private ReceiverThread receiverThread;
	private final Handler handler;
	private boolean fire = false;
	private float motorLeftSpeed = 0.0f;
	private float motorRightSpeed = 0.0f;

	public IOIORobot(Handler handler) {
		super();
		this.handler = handler;
	}

	/**
	 * Called every time a connection with IOIO has been established. Typically
	 * used to open pins.
	 * 
	 * @throws ConnectionLostException
	 *             When IOIO connection is lost.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
	 */
	@Override
	protected void setup() throws ConnectionLostException {
		Log.i("tag", "setup");
		handler.sendMessage(handler.obtainMessage(1, "IOIO setup"));
		// led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
		gunPwmOutput = ioio_.openPwmOutput(PIN_GUN, 36000);
		motorLeftPwmOutput = ioio_.openPwmOutput(PIN_MOTOR_LEFT, 400);
		motorRightPwmOutput = ioio_.openPwmOutput(PIN_MOTOR_RIGHT, 400);

		receiverThread = new ReceiverThread(handler, ioio_);
		receiverThread.start();
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
		if (fire) {
			// led_.write(false);
			gunPwmOutput.setDutyCycle(0.5f);
			fire = false;
		} else {
			// led_.write(true);
			gunPwmOutput.setDutyCycle(0.0f);
		}

		motorLeftPwmOutput.setDutyCycle(motorLeftSpeed);
		motorRightPwmOutput.setDutyCycle(motorRightSpeed);

		try {
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disconnected() {
		if (receiverThread != null) {
			receiverThread.stopThread();
			receiverThread.interrupt();
			receiverThread = null;
		}
	}

	public void fire() {
		fire = true;
	}

	public void motor(int directive) {
		if (directive == 10) {
			motorLeftSpeed = 0.0f;
		} else if (directive == 11) {
			motorLeftSpeed = 1.0f;
		} else if (directive == 12) {
			motorLeftSpeed = 0.4f;
		} else if (directive == 13) {
			motorLeftSpeed = 0.1f;
		} else if (directive == 20) {
			motorRightSpeed = 0.0f;
		} else if (directive == 21) {
			motorRightSpeed = 1.0f;
		} else if (directive == 22) {
			motorRightSpeed = 0.4f;
		} else if (directive == 23) {
			motorRightSpeed = 0.1f;
		}
	}
}
