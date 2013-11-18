package org.game.ante;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;

public class ConnectionThread extends Thread {
	private final Socket client;
	private PrintStream outStream;
	private final Pattern commandPattern = Pattern.compile("(GET|POST) (/.*?)(\\?(.*))? HTTP/1");
	private int requestCounter; // increments with every request received
	private final Handler handler; // communicate with UI thread
	private final AssetManager assetManager; // used to access app's internal

	ConnectionThread(final Socket client, final AssetManager assetManager, final Handler handler) {
		this.client = client;
		this.assetManager = assetManager;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			outStream = new PrintStream(client.getOutputStream(), true);

			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			String line = in.readLine(); // read first line of request

			handler.sendMessage(handler.obtainMessage(1, line));

			Matcher commandMatcher = commandPattern.matcher(line);
			if (commandMatcher.find()) {
				// String method = commandMatcher.group(1);
				String path = commandMatcher.group(2);

				// parse query string
				Map<String, String> queryMap = new HashMap<String, String>();
				if (commandMatcher.group(4) != null) {
					String[] params = commandMatcher.group(4).split("&");
					for (String param : params) {
						String[] splat = param.split("=");
						queryMap.put(splat[0], splat.length > 1 ? splat[1] : "");
					}
				}

				if (queryMap.get("counter") != null) {
					int newCount = Integer.parseInt(queryMap.get("counter"));
					if (newCount > requestCounter) {
						requestCounter = newCount;
					} else {
						client.close();
						return; // older request so ignore it
					}
				}

				if (queryMap.get("action") != null) {
					String action = queryMap.get("action");

					// switch case to java 7
					if (action.equals(SimpleWebServer.ACTION_FIRE)) {
						fire();
					} else if (action.equals(SimpleWebServer.ACTION_MOTOR)) {
						String directive = queryMap.get("directive");

						motor(directive);
					}
				} else {
					// server a file
					if (path.equals("/") || path.equals("/index.html")) {
						path = "/index.html";
						requestCounter = 0;
					}

					// determine content type from file extension
					String ext = path.substring(path.lastIndexOf(".") + 1);
					String mimeType = "text/plain";
					if (ext.equals("html"))
						mimeType = "text/html";
					if (ext.equals("js"))
						mimeType = "text/javascript";
					if (ext.equals("css"))
						mimeType = "text/css";
					if (ext.equals("ico"))
						mimeType = "image/x-icon";
					// if (ext.equals("png")) mimeType = "image/png";

					// read file into a string
					try {
						BufferedReader fileReader = new BufferedReader(new InputStreamReader(assetManager.open(path.substring(1))));
						String body = "";
						String fileLine;
						while ((fileLine = fileReader.readLine()) != null)
							body = body.concat(fileLine + "\n");
						sendResponse("200 OK", body, mimeType);
					} catch (FileNotFoundException e) {
						sendResponse("404 File Not Found", "File not found: " + path);
					}

				}
			} else {
				sendResponse("400 Bad Request", "Malformed request: " + line);
			}

			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void fire() {
		Message message = new Message();

		message.what = SimpleWebServer.MSG_WHAT_FIRE;

		handler.sendMessage(message);
	}

	private void motor(String directive) throws Exception {
		Integer directiveInt = null;

		if (directive.equals("forward_L_Full")) {
			directiveInt = 11;
		} else if (directive.equals("forward_L_50")) {
			directiveInt = 12;
		} else if (directive.equals("forward_L_20")) {
			directiveInt = 13;
		} else if (directive.equals("stop_L")) {
			directiveInt = 10;
		} else if (directive.equals("forward_R_Full")) {
			directiveInt = 21;
		} else if (directive.equals("forward_R_50")) {
			directiveInt = 22;
		} else if (directive.equals("forward_R_20")) {
			directiveInt = 23;
		} else if (directive.equals("stop_R")) {
			directiveInt = 20;
		} else {
			throw new Exception("Wrong directive");
		}

		Message message = new Message();

		message.what = SimpleWebServer.MSG_WHAT_MOTOR;
		message.arg1 = directiveInt;
		handler.sendMessage(message);
	}

	/*
	 * Print HTTP response with various kinds of inpiut
	 */
	private void sendResponse(String code, String body, String type) {
		sendHeaders(code, type, body.length());
		outStream.print(body);
	}

	private void sendResponse(String code, String body) {
		sendResponse(code, body, "text/plain");
	}

	private void sendHeaders(String code, String type, int contentLength) {
		outStream.println("HTTP/1.1 " + code);
		outStream.println("Content-Type: " + type);
		outStream.println("Content-Length: " + contentLength);
		outStream.println("Access-Control-Allow-Origin: *");
		outStream.println();
	}
}
