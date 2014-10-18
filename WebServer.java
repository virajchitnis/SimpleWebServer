/* 
 * Viraj Chitnis
 * TUid: 912984033
 * CIS 3329 Assignment 8
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class WebServer {
	public static void main(String argv[]) {
		/*
		 * Show the graphical user interface for the application if GUI is supported, or
		 * else display info on the console.
		 */
		if (!GraphicsEnvironment.isHeadless()) {
			showGUI();
		}
		else {
			showConsole();
		}
		/*
		 * Start the web server which will listen on localhost:9999
		 */
		startServer();
	}
	
	public static void startServer() {
		/* 
		 * This is the server socket part of the program.
		 * The code below will start a socket server which will listen on localhost:9999
		 */
		try {
			ServerSocket welcomeSocket = new ServerSocket(9999);
			
			while(true) {
				/*
				 * Pass a newly created connection to a new thread. See 'ServerClientThread'
				 * class below to see how the connection is handled and data is returned.
				 */
				Socket connectionSocket = welcomeSocket.accept();
				ServerClientThread newRunnable = new ServerClientThread(connectionSocket);
				new Thread(newRunnable).start();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void showConsole() {
		System.out.println(" ");
		System.out.println("\u001B[34mCIS 3329 Assignment 8");
		System.out.println("by Viraj Chitnis");
		System.out.println(" ");
		System.out.println("The server is ready...");
		System.out.println("Listening on localhost (127.0.0.1) port 9999\u001B[0m");
		System.out.println(" ");
		System.out.println("\u001B[31mWARNING: This program may not work in Windows, please use Mac OS X or Linux to run it.\u001B[0m");
		System.out.println(" ");
		System.out.println(" ");
		System.out.println("Copyright © 2014 Viraj Chitnis. All Rights Reserved.");
		System.out.println(" ");
	}
	
	public static void showGUI() {
		/* 
		 * This is the graphical user interface part of the program, the server socket part is below.
		 */
		JFrame frame = new JFrame("WebServer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		JPanel leftBlankPanel = new JPanel();
		leftBlankPanel.setPreferredSize(new Dimension(25, 0));
		frame.getContentPane().add(leftBlankPanel, BorderLayout.WEST);
		
		JPanel rightBlankPanel = new JPanel();
		rightBlankPanel.setPreferredSize(new Dimension(25, 0));
		frame.getContentPane().add(rightBlankPanel, BorderLayout.EAST);
		
		JLabel textServerStatus = new JLabel("<html>CIS 3329 Assignment 8<br>"
			+ "by Viraj Chitnis<br>"
			+ "&nbsp;<br>"
			+ "<font color=blue>The server is ready...<br>"
			+ "Listening on localhost (127.0.0.1) port 9999</font><br><br>"
			+ "<font color=red>WARNING: This program may not work in Windows,<br>please use Mac OS X or Linux to run it.</font></html>");
		textServerStatus.setPreferredSize(new Dimension(350, 200));
		textServerStatus.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(textServerStatus, BorderLayout.NORTH);
		
		JButton openLinkButton = new JButton("Open http://localhost:9999 in a web browser");
		openLinkButton.setLayout(new BorderLayout());
		openLinkButton.setToolTipText("Click here to open http://localhost:9999");
		openLinkButton.addActionListener(new openLinkButtonAction());
		frame.getContentPane().add(openLinkButton, BorderLayout.CENTER);
		
		JLabel textInfo = new JLabel("<html>Copyright &copy; 2014 Viraj Chitnis. All Rights Reserved.</html>");
		textInfo.setFont(textInfo.getFont().deriveFont(9.0f));
		textInfo.setPreferredSize(new Dimension(350, 25));
		textInfo.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(textInfo, BorderLayout.SOUTH);
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

class ServerClientThread implements Runnable {
	protected Socket connectionSocket;
	protected String serverName;
	protected BufferedReader inFromClient;
	protected DataOutputStream outToClient;

    public ServerClientThread(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
		this.serverName = "CHITNIS/1.0";
    }

    public void run() {
		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			ArrayList<String> httpRequestHeaders = new ArrayList<String>();
			while (true) {
				String tempClientText = inFromClient.readLine();
				if (tempClientText == null || tempClientText.length() == 0) {
					if (httpRequestHeaders.size() == 0) {
						continue;
					}
					else {
						break;
					}
				}
				else {
					httpRequestHeaders.add(tempClientText);
				}
			}
		
			if (httpRequestHeaders.size() >= 2) {
				String clientGETRequest = httpRequestHeaders.get(0);
				if ((clientGETRequest.length() < 14) || (!clientGETRequest.startsWith("GET")) || 
				((!clientGETRequest.endsWith("HTTP/1.1")) && (!clientGETRequest.endsWith("HTTP/1.0")))) {
					returnBadRequest();
					closeOpenConnections();
					return;
				}
			
				String clientHOSTRequest = httpRequestHeaders.get(1);
				if (!clientHOSTRequest.startsWith("Host: ")) {
					returnBadRequest();
					closeOpenConnections();
					return;
				}
				
				String[] clientRequestObjects = clientGETRequest.split(" ");
				String[] httpVersionObjects = clientRequestObjects[2].split("/");
				String[] hostObjects = httpRequestHeaders.get(1).split(" ");
				
				String clientIP = connectionSocket.getInetAddress().getHostAddress();
				Date currentDate = new Date();

				String responseBody = "<html><head><title>Viraj's Web Server</title></head><body>"
						+ "<p>Your IP address is " + clientIP + "<br>"
						+ "It is now " + currentDate.toString() + "</p>"
						+ "<p><b>method</b> " + clientRequestObjects[0] + "<br>"
						+ "<b>URL</b> " + clientRequestObjects[1] + "<br>"
						+ "<b>HTTP version</b> " + httpVersionObjects[1] + "<br>"
						+ "<b>host</b> " + hostObjects[1] + "<br>";
				
				if (headerFieldExists(httpRequestHeaders, "Connection:")) {
					responseBody = responseBody + "<b>Connection</b> " + getValueOfHeaderField(httpRequestHeaders, "Connection:") + "<br>";
				}
				
				if (headerFieldExists(httpRequestHeaders, "Accept:")) {
					responseBody = responseBody + "<b>Accept</b> " + getValueOfHeaderField(httpRequestHeaders, "Accept:") + "<br>";
				}
				
				if (headerFieldExists(httpRequestHeaders, "User-Agent:")) {
					responseBody = responseBody + "<b>User-Agent</b> " + getValueOfHeaderField(httpRequestHeaders, "User-Agent:") + "<br>";
				}
				
				if (headerFieldExists(httpRequestHeaders, "Accept-Encoding:")) {
					responseBody = responseBody + "<b>Accept-Encoding</b> " + getValueOfHeaderField(httpRequestHeaders, "Accept-Encoding:") + "<br>";
				}
				
				if (headerFieldExists(httpRequestHeaders, "Accept-Language:")) {
					responseBody = responseBody + "<b>Accept-Language</b> " + getValueOfHeaderField(httpRequestHeaders, "Accept-Language:") + "<br>";
				}
				
				if (headerFieldExists(httpRequestHeaders, "Accept-Charset:")) {
					responseBody = responseBody + "<b>Accept-Charset</b> " + getValueOfHeaderField(httpRequestHeaders, "Accept-Charset:") + "<br>";
				}
				
				if (headerFieldExists(httpRequestHeaders, "Cookie:")) {
					responseBody = responseBody + "<b>Cookie</b> " + getValueOfHeaderField(httpRequestHeaders, "Cookie:") + "<br>";
				}
				
				if (headerFieldExists(httpRequestHeaders, "Cache-Control:")) {
					responseBody = responseBody + "<b>Cache-Control</b> " + getValueOfHeaderField(httpRequestHeaders, "Cache-Control:") + "<br>";
				}
				
				responseBody = responseBody + "</p><br><p><b>Your request headers:</b><br>";
				
				for (int i = 0; i < httpRequestHeaders.size(); i++) {
					responseBody = responseBody + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + httpRequestHeaders.get(i) + "<br>";
				}
				responseBody = responseBody + "</p></body></html>";
				int responseLength = (int)responseBody.length();

				String serverResponse = "HTTP/1.1 200 OK\r\n"
					+ "Date: " + getServerHTTPTime() + "\r\n"
					+ "Connection: close\r\n"
					+ "Server: " + serverName + "\r\n"
					+ "Content-Length: " + responseLength + "\r\n"
					+ "Content-Type: text/html; charset=UTF-8\r\n\r\n\r\n"
					+ responseBody + "\n";
				writeBytesToClient(serverResponse);
				closeOpenConnections();
			}
			else {
				returnBadRequest();
				closeOpenConnections();
				return;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	Boolean headerFieldExists(ArrayList<String> headers, String header) {
		for (int i = 0; i < headers.size(); i++) {
			String currHeader = headers.get(i);
			String[] currHeaderObjects = currHeader.split(" ");
			if (currHeaderObjects[0].equals(header)) {
				return true;
			}
		}
		return false;
	}
	
	String getValueOfHeaderField(ArrayList<String> headers, String header) {
		for (int i = 0; i < headers.size(); i++) {
			String currHeader = headers.get(i);
			String[] currHeaderObjects = currHeader.split(" ");
			if (currHeaderObjects[0].equals(header)) {
				String ret = "";
				for (int j = 1; j < currHeaderObjects.length; j++) {
					ret = ret + " " + currHeaderObjects[j];
				}
				return ret;
			}
		}
		return "";
	}
	
	String getServerHTTPTime() {
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
	
	Boolean returnNotFoundRequest() {
		String responseBody = "<html><head><title>Error - Viraj's Web Server</title></head><body>"
				+ "<h1>Page not found</h1></body></html>";
		int responseLength = (int)responseBody.length();

		String serverResponse = "HTTP/1.1 404 Not Found\r\n"
			+ "Date: " + getServerHTTPTime() + "\r\n"
			+ "Connection: close\r\n"
			+ "Server: " + serverName + "\r\n"
			+ "Content-Length: " + responseLength + "\r\n"
			+ "Content-Type: text/html; charset=UTF-8\r\n\r\n\r\n"
			+ responseBody + "\n";
		return writeBytesToClient(serverResponse);
	}
	
	Boolean returnBadRequest() {
		String responseBody = "<html><head><title>Error - Viraj's Web Server</title></head><body>"
				+ "<h1>Bad Request</h1></body></html>";
		int responseLength = (int)responseBody.length();

		String serverResponse = "HTTP/1.1 400 Bad Request\r\n"
			+ "Date: " + getServerHTTPTime() + "\r\n"
			+ "Connection: close\r\n"
			+ "Server: " + serverName + "\r\n"
			+ "Content-Length: " + responseLength + "\r\n"
			+ "Content-Type: text/html; charset=UTF-8\r\n\r\n\r\n"
			+ responseBody + "\n";
		return writeBytesToClient(serverResponse);
	}
	
	Boolean writeBytesToClient(String bytesToWrite) {
		try {
			outToClient.writeBytes(bytesToWrite);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	void closeOpenConnections() {
		try {
			outToClient.flush();
			connectionSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/* 
 * This class is required for the button in the GUI to work as expected.
 */
class openLinkButtonAction implements ActionListener {
	public void actionPerformed(ActionEvent evt) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				URI uri = new URI("http://localhost:9999");
				desktop.browse(uri);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}