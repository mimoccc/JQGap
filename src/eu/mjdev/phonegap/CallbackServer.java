package eu.mjdev.phonegap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class CallbackServer implements Runnable {
	private LinkedList<String> javascript;
	private int port;
	private Thread serverThread;
	private boolean active;
	private boolean empty;
	private boolean usePolling = true;
	private String token;

	public CallbackServer() {
		this.active = false;
		this.empty = true;
		this.port = 0;
		this.javascript = new LinkedList<String>();
	}

	public void init(String url) {
		this.active = false;
		this.empty = true;
		this.port = 0;
		this.javascript = new LinkedList<String>();
		if ((url != null) && !url.startsWith("file://")) {
			this.usePolling = true;
			this.stopServer();
		} else if (android.net.Proxy.getDefaultHost() != null) {
			this.usePolling = true;
			this.stopServer();
		} else {
			this.usePolling = false;
			this.startServer();
		}
	}

	public void reinit(String url) {
		this.stopServer();
		this.init(url);
	}

	public boolean usePolling() {
		return this.usePolling;
	}

	public int getPort() {
		return this.port;
	}

	public String getToken() {
		return this.token;
	}

	public void startServer() {
		this.active = false;
		this.serverThread = new Thread(this);
		this.serverThread.start();
	}

	public void restartServer() {
		this.stopServer();
		this.startServer();
	}

	public void run() {
		try {
			this.active = true;
			String request;
			ServerSocket waitSocket = new ServerSocket(0);
			this.port = waitSocket.getLocalPort();
			this.token = java.util.UUID.randomUUID().toString();
			while (this.active) {
				Socket connection = waitSocket.accept();
				BufferedReader xhrReader = new BufferedReader(new InputStreamReader(connection.getInputStream()), 40);
				DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				request = xhrReader.readLine();
				String response = "";
				if (this.active && (request != null)) {
					if (request.contains("GET")) {
						String[] requestParts = request.split(" ");
						if ((requestParts.length == 3)&& (requestParts[1].substring(1).equals(this.token))) {
							synchronized (this) {
								while (this.empty) {
									try {
										this.wait(10000);
										break;
									} catch (Exception e) {}
								}
							}
							if (this.active) {
								if (this.empty) {
									response = "HTTP/1.1 404 NO DATA\r\n\r\n ";
								} else {
									response = "HTTP/1.1 200 OK\r\n\r\n";
									String js = this.getJavascript();
									if (js != null) response += encode(js, "UTF-8");
								}
							} else {
								response = "HTTP/1.1 503 Service Unavailable\r\n\r\n ";
							}
						} else {
							response = "HTTP/1.1 403 Forbidden\r\n\r\n ";
						}
					} else {
						response = "HTTP/1.1 400 Bad Request\r\n\r\n ";
					}
					output.writeBytes(response);
					output.flush();
				}
				output.close();
				xhrReader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.active = false;
	}

	public void stopServer() {
		if (this.active) {
			this.active = false;
			synchronized (this) {
				this.notify();
			}
		}
	}

	public void destroy() {
		this.stopServer();
	}

	public int getSize() {
		synchronized (this) {
			int size = this.javascript.size();
			return size;
		}
	}

	public String getJavascript() {
		synchronized (this) {
			if (this.javascript.size() == 0) return null;
			String statement = this.javascript.remove(0);
			if (this.javascript.size() == 0) this.empty = true;
			return statement;
		}
	}

	public void sendJavascript(String statement) {
		synchronized (this) {
			this.javascript.add(statement);
			this.empty = false;
			this.notify();
		}
	}

	static final String digits = "0123456789ABCDEF";

	public static String encode(String s, String enc)
			throws UnsupportedEncodingException {
		if (s == null || enc == null)
			throw new NullPointerException();
		"".getBytes(enc);
		StringBuilder buf = new StringBuilder(s.length() + 16);
		int start = -1;
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9')
					|| " .-*_'(),<>=?@[]{}:~\"\\/;!".indexOf(ch) > -1) {
				if (start >= 0) {
					convert(s.substring(start, i), buf, enc);
					start = -1;
				}
				if (ch != ' ')
					buf.append(ch);
				else
					buf.append(' ');
			} else if (start < 0)
				start = i;
		}
		if (start >= 0)
			convert(s.substring(start, s.length()), buf, enc);
		return buf.toString();
	}

	private static void convert(String s, StringBuilder buf, String enc)
			throws UnsupportedEncodingException {
		byte[] bytes = s.getBytes(enc);
		for (int j = 0; j < bytes.length; j++) {
			buf.append('%');
			buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
			buf.append(digits.charAt(bytes[j] & 0xf));
		}
	}
}