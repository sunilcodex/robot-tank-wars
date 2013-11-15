package org.game.ante;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.res.AssetManager;
import android.os.Handler;

public class SimpleWebServer extends Thread {

	public static final int MSG_WHAT_FIRE = 2;
	public static final int MSG_WHAT_HIT = 3;
	public static final int MSG_WHAT_MOTOR = 4;
	public static final String ACTION_FIRE = "fire";
	public static final String ACTION_MOTOR = "motor";

	private ServerSocket serverSocket; // socket that listens for clients
	private final Handler handler; // communicate with UI thread
	private String ip; // local IP
	private int port; // port to listen on
	private final AssetManager assetManager; // used to access app's internal
												// files
	private boolean isRunning = true; // not used
	private ConnectionThread connectionThread;
	private Socket clientSocket;

	public SimpleWebServer(final AssetManager assetManager, final Handler handler) throws Exception {
		this.assetManager = assetManager;
		this.handler = handler;

		setup();
	}

	private void setup() throws Exception {
		ip = getLocalIpAddress();

		if (ip == null) {
			logMessage("IP address not assigned");
			throw new Exception();
		}
		port = 8080;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			logMessage("Server listening on " + ip + ":" + port);

			// loop forever
			while (isRunning) {
				// block waiting for client connection
				clientSocket = serverSocket.accept();

				// create new thread to handle client connection
				connectionThread = new ConnectionThread(clientSocket, assetManager, handler);
				connectionThread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		try {
			if (clientSocket != null) {
				clientSocket.close();
			}
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		isRunning = false;

		if (connectionThread != null) {
			connectionThread.interrupt();
			connectionThread = null;
		}
	}

	/*
	 * Send a message to the UI thread
	 */
	private void logMessage(String message) {
		handler.sendMessage(handler.obtainMessage(1, message));
	}

	/*
	 * Try to find the IP address of the device's external interface
	 */
	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
}