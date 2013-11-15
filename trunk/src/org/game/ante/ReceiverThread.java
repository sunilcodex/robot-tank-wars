package org.game.ante;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.os.Handler;

public class ReceiverThread extends Thread {
	private final Handler handler; // communicate with UI thread
	private boolean isRunning = true;
	private DigitalInput irReceiver;
	private final IOIO ioio_;

	public ReceiverThread(final Handler handler, final IOIO ioio_) {
		this.handler = handler;
		this.ioio_ = ioio_;
	}

	@Override
	public void run() {
		logMessage("Receiver thread started.");

		try {
			irReceiver = ioio_.openDigitalInput(8);
		} catch (ConnectionLostException e) {
			logMessage("Could not open input pin for receiver!");
			e.printStackTrace();
		}

		while (isRunning) {
			try {
				irReceiver.waitForValue(false);
				sendHit("Hit!");

				try {
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				logMessage("IOIO InterruptedException.");
				e.printStackTrace();
			} catch (ConnectionLostException e) {
				logMessage("IOIO ConnectionLostException!");
				e.printStackTrace();
			}
		}
	}

	/*
	 * Send a message to the UI thread
	 */
	private void logMessage(String message) {
		handler.sendMessage(handler.obtainMessage(1, message));
	}

	public void stopThread() {
		isRunning = false;
	}

	private void sendHit(String message) {
		handler.sendMessage(handler.obtainMessage(SimpleWebServer.MSG_WHAT_HIT, message));
	}
}
