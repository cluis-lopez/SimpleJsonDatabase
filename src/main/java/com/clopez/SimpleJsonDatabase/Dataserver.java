package com.clopez.SimpleJsonDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.clopez.SimpleJsonDatabase.Database.SimpleJsonDatabase;

public class Dataserver {

	private static final Logger log = Logger.getLogger("SimpleJsonDatabase");

	private static final AtomicBoolean keepRunning = new AtomicBoolean(true);
	
	private static Map<String, Object> dbs;

	public void kill() {
		keepRunning.set(false);
	}

	public static void main(String[] args) {
		new Dataserver();
	}

	public Dataserver() {

		// Initialize Properties

		String propsFile = System.getenv("ConfigFile");
		if (propsFile == null || propsFile.equals("")) {
			propsFile = "etc/config/Serverdata.cnf";
		}

		System.out.println("Using config file at " + propsFile);

		Properties props = new Properties();

        try (InputStream input = new FileInputStream(propsFile)) {
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Cannot execute Dataserver without properties file");
            return;
        }

		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tF %1$tT %4$s %5$s%6$s%n");
		FileHandler fd = null;

		try {
			fd = new FileHandler(props.getProperty("logFile"), true);
			System.out.println("Using log file : " + props.getProperty("logFile"));
		} catch (SecurityException | IOException e1) {
			System.err.println("No se puede abrir el fichero de log");
			e1.printStackTrace();
		}
		
		log.setUseParentHandlers(false); // To avoid console logging
		log.addHandler(fd);
		SimpleFormatter formatter = new SimpleFormatter();
		fd.setFormatter(formatter);


		//Initialize Databases here
		dbs = new HashMap<>();
		String[] ddbbs = props.getProperty("dataBases").split(",");
		String dataPath = props.getProperty("dataPath");
		for (String dbname : ddbbs) {
			String s = dbname.trim();
			String dp = dataPath + File.separator + s + "_data";
			
			System.out.println("Abriendo la base de datos " + s + " en el fichero " + dp);
			log.log(Level.INFO, "Opening database: " + s + " using " + dp + " as file");
			try {
				Class<?> cl = Class.forName("com.clopez.SimpleJsonDatabase.Database." + s + "Database");
				Constructor<?> cons = cl.getConstructor(String.class, Logger.class, Properties.class);
				Object ob = cons.newInstance(dp, log, props);
				dbs.put(s + "Database", ob);
			} catch (ClassNotFoundException e) {
				System.err.println("No se encuentra la clase para la BBDD " + s);
				log.log(Level.WARNING, "Cannot open Database : " + s);
				log.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
					| SecurityException e) {
				System.err.println("No se puede instanciar la BBDD " + s + ":" + e.getClass().getName());
				log.log(Level.WARNING, "Cannot instantiate Database main class " + s);
				log.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
			} catch (InvocationTargetException e) {
				System.err.println("Error al invocar el constructor de la BBDD " + s);
				log.log(Level.WARNING, "Fail when constructing Database class " + s);
				log.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
			}
		}
		
		System.out.println("Databases en uso");
		for (String s : dbs.keySet()) {
			if (dbs.get(s) != null)
				System.out.println(s);
		}
		
		
		// Start the HTTP Data Server

		ServerSocket server = null;
		int dataPort = 0;

		try {
			dataPort = Integer.parseInt(props.getProperty("dataPort"));
		} catch (NumberFormatException e) {
			log.log(Level.SEVERE, "Invalid port number: " + dataPort);
			log.log(Level.SEVERE, e.getMessage());
			log.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
			System.err.println("Puerto invalido" + dataPort);
			e.printStackTrace();
			return;
		}
		
		try {
			server = new ServerSocket(dataPort);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Cannot start Server Socket at port: " + dataPort);
			log.log(Level.SEVERE, e.getMessage());
			log.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
			System.err.println("No se puede arrancar el server en el puerto " + dataPort);
			e.printStackTrace();
			return;
		} 
		
		System.out.println("Arrancando el servidor en el puerto: " + dataPort);
		log.log(Level.INFO, "Server started");
		log.log(Level.INFO, "Listening at port: " + dataPort);

		Socket client = null;
		while (keepRunning.get()) {
			try {
				client = server.accept();
			} catch (IOException e) {
				log.log(Level.WARNING, "Cannot launch thread to accept client: " + client.toString());
				log.log(Level.WARNING, Arrays.toString(e.getStackTrace()));
			}
			final DataserverAPI request = new DataserverAPI(log, client, props.getProperty("adminRootPath"), dbs);
			Thread thread = new Thread(request);
			thread.setName("Data Request Dispatcher #" + thread.getId());
			thread.start();
		}
	}
}
