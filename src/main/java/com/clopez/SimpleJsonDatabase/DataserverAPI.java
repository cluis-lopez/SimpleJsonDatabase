package com.clopez.SimpleJsonDatabase;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.clopez.SimpleJsonDatabase.Database.SimpleJsonDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DataserverAPI implements Runnable {

	private static final String newLine = "\r\n";
	private Logger log;
	private String adminRootPath;
	private Map<String, Object> dbs;
	private Socket socket;

	private Map<String, String> headerFields;
	private String body;

	public DataserverAPI(Logger log, Socket s, String adminRootPath, Map<String, Object> dbs) {
		this.log = log;
		this.adminRootPath = adminRootPath;
		this.dbs = dbs;
		this.socket = s;
		headerFields = new HashMap<>();
		body = null;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream out = new BufferedOutputStream(socket.getOutputStream());
			PrintStream pout = new PrintStream(out);

			// read first line of request
			String request = in.readLine();
			if (request == null || request.length() == 0)
				return;

			// Rest of Header
			while (true) {
				String header = in.readLine();
				if (header == null || header.length() == 0)
					break;
				headerFields.put(header.split(":", 2)[0].trim(), header.split(":", 2)[1].trim());
			}

			// Body, if any
			if (headerFields.get("Content-Length") != null
					&& Integer.parseInt(headerFields.get("Content-Length")) > 0) {
				StringBuilder sb = new StringBuilder();
				char[] cb = new char[Integer.parseInt(headerFields.get("Content-Length"))];
				int bytesread = in.read(cb, 0, Integer.parseInt(headerFields.get("Content-Length")));
				sb.append(cb, 0, bytesread);
				body = sb.toString();
			}
			
			Headerdecoder reqLine = new Headerdecoder(request);

			// Logging

			log.log(Level.INFO, "Serving {0}",
					reqLine.command + " " + reqLine.resource + " from " + socket.getInetAddress().toString());

			String response = "";

			boolean reqValid = (reqLine.command.equals("GET") || reqLine.command.equals("POST")
					|| reqLine.command.equals("DELETE"))
					&& (reqLine.protocol.equals("HTTP/1.0") || reqLine.protocol.equals("HTTP/1.1"));

			if (!reqValid) // Bad Request
				response = "HTTP/1.1 400 Bad Request" + newLine + newLine;
			// Only POST with json payload is supported
			else if (reqLine.command.equals("POST") && headerFields.get("Content-Type") != null
					&& !headerFields.get("Content-Type").equals("Application/json") && body != null)
				response = processPost(reqLine);
			else
				response = "HTTP/1.1 400 Bad Request" + newLine + newLine;

			pout.print(response);
			pout.flush();
			pout.close();
			out.close();
			in.close();
			socket.close();

		} catch (IOException e) {
			log.log(Level.WARNING, "Cannot read from socket");
			log.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
		}
	}

	private String processPost(Headerdecoder req) {
		String resp = "";
		Object ob = null;
		String[] res = req.resource.split("/"); // First element should be DataBase name, Second must be the command
		System.out.println("DDBB: " + res[1] + " , Comando: " + res[2]);
		Object db = dbs.get(res[1]);
		
		if (res == null || res.length != 3 || db == null) {
			resp = "HTTP/1.1 400 Bad Request (Expected databaseName/Operation)" + newLine + newLine;
		} else {
			Method method;
			try {
				method = db.getClass().getMethod(res[2], String.class);
				if (method != null) {
					System.out.println("Invocando : " + method.getName());
					System.out.println("Con json payload " + body);
					Gson gson = new Gson();
					if (body.length() > 0) {
						try {
							JsonObject jo = (JsonObject) method.invoke(db, body);
							String payload = gson.toJson(jo);
							resp = "HTTP/1.1 200 OK" + newLine + "Content-Type: application/json" + newLine + "Date: " + new Date() + newLine
									+ "Content-length: " + payload.length() + newLine + newLine + payload;
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
							System.out.println(e.getMessage());
						}
					}
				} else {
					resp = "HTTP/1.1 400 Bad Request (Ivalid Operation Requested)" + newLine + newLine;
				}
			} catch (NoSuchMethodException | SecurityException e1) {
				resp = "HTTP/1.1 400 Bad Request (Ivalid Operation)" + newLine + newLine;
			}
		}
		return resp;
	}
			
	// HTTP Get should be reserved for static files
	private String processGet(Headerdecoder req) {
		String[] ret = new String[2];
		String resp = "";
		/*
		 * ret = new doFile(log, adminRootPath).doGet(req.resource.substring(1)); if
		 * (ret[0].equals("")) { // No file found resp = "HTTP/1.1 404 " + ret[1] +
		 * newLine + newLine; } else { resp = "HTTP/1.1 200 OK" + newLine +
		 * "Content-Type: " + ret[0] + newLine + "Date: " + new Date() + newLine +
		 * "Content-length: " + ret[1].length() + newLine + newLine + ret[1]; }
		 */
		return resp;
	}

}
