package com.clopez.SimpleJsonDatabase.Database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class MessageDatabase {
	private int maxMessageFiles;
	private int maxMessagesPerFile;
	private String messageFilePrefix;
	
	private String dataDir;
	private Path currentDataFile;
	private int currentNumMessages;
	private Gson gson;
	private Map<Integer, List<Message>> mapById;
	Logger log;

	public MessageDatabase(String dataDir, Logger log, Properties props) {
		this.currentNumMessages = 0;
		this.dataDir = dataDir;
		gson = new Gson();
		mapById = new HashMap<>();
		
		try {
			maxMessageFiles = Integer.parseInt(props.getProperty("maxMessageFiles"));
			maxMessagesPerFile = Integer.parseInt(props.getProperty("maxMessagesPerFile"));
			messageFilePrefix = props.getProperty("messageFilePrefix");
			} catch (NumberFormatException e) {
				System.err.println("Error reading data from config file. Cannot instantiate class");
				log.log(Level.SEVERE, 
						"Error reading data from config file. " + Arrays.toString(e.getStackTrace()));
			}
		
		currentDataFile = getLastFile(dataDir);

		Message m;
		try (BufferedReader br = Files.newBufferedReader(currentDataFile)){
		    String line;
		    while ((line = br.readLine()) != null) {
		    	try {
		    		m = gson.fromJson(line, new TypeToken<Message>() {}.getType());
		    		mapMessageFromChatId(m);
		    		currentNumMessages = getNumMessagesInt();
		    	}catch (JsonIOException | JsonSyntaxException f) {
		   			System.err.println("Malformatted line");
		   		}
		   	}
		    log.log(Level.INFO, "Readed: " + getNumMessages("kk") + " messages in " + getNumChats("kk")+ " chats");
		} catch (IOException e) {
			System.err.println("No existe la BBDD. Creamos el primer fichero");
			log.log(Level.INFO, "Datafile not present. Will be created with the first message to be stored");
		}
	}
	
	public JsonObject getMethods(String kk) {
		JsonObject jo = new JsonObject();
		JsonArray ja = new JsonArray();
		Method[] methods =null;
			Class<MessageDatabase> c = MessageDatabase.class;
			methods = c.getDeclaredMethods();
		for (Method m : methods) {
			Type[] pars = m.getParameterTypes();
			JsonArray ja2 = new JsonArray();
			for (Type t : pars)
				ja2.add(t.getTypeName());
			JsonObject jme = new JsonObject();
			jme.addProperty("name", m.getName());
			jme.add("params", ja2);
			ja.add(jme);
		}
		jo.addProperty("code",  "OK");
		jo.add("methods", ja);
		return jo;
	}
	
	public JsonObject addMessage(String s) {
		JsonObject jo = new JsonObject();
		if (currentNumMessages > maxMessagesPerFile) {
			rotateFiles(dataDir);
			currentNumMessages = 0;
		}
		currentNumMessages += 1;
		
		Message m = gson.fromJson(s,  Message.class);
		
		try (BufferedWriter writer = Files.newBufferedWriter(currentDataFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
		    writer.write(m.getJson() + System.lineSeparator());
		    writer.flush();
		    jo.addProperty("code", "OK");
		} catch (IOException ioe) {
		    System.err.format("Cannot write: IOException: %s%n", ioe);
		    log.log(Level.SEVERE, "Cannot write: IOException: %s%n", ioe );
		    jo.addProperty("code", "I/O problem writing message");;
		}
		
		mapMessageFromChatId(m);
		return jo;
	}
	
	public JsonObject getChatById(String s) {
		JsonObject jo = new JsonObject();
		jo.addProperty("code", "OK");
		try {
			int id = gson.fromJson(s, Integer.class);
			List<Message> lm =  mapById.get(id);
			JsonArray ja = new JsonArray();
			for (Message m : lm)
				ja.add(gson.toJson(m));
			jo.add("messages", ja);
		} catch (JsonSyntaxException e) {
			jo.addProperty("code", "Invalid argument");
		}
		return jo;
	}
	
	public JsonObject getChatByIdNumber(String s){ //Return the last "number" of messages
		JsonObject jo = new JsonObject();
		jo.addProperty("code", "OK");
		
		try {
			JsonObject pars = JsonParser.parseString(s).getAsJsonObject();
			int id = pars.get("id").getAsInt();
			int number = pars.get("number").getAsInt();
			List<Message> lm =  mapById.get(id);
			int min = (number >= lm.size()? 0 : lm.size()-number);
			JsonArray ja = new JsonArray();
			for (int i = lm.size()-1; i < min; i--)
				ja.add(gson.toJson(lm.get(i)));
			jo.add("messages", ja);
		} catch (JsonSyntaxException e) {
			
		}
		return jo;
	}
		
			
	public JsonObject getPendingMessagesTo(String userTo){
		JsonObject jo = new JsonObject();
		JsonArray ja = new JsonArray();
		for (List<Message> temp : mapById.values())
			for (Message m : temp)
				if (m.getTo().equals(userTo) && !m.isDelivered())
					ja.add(gson.toJson(m));
		jo.addProperty("code", "OK");
		jo.add("messages", ja);
		return jo;
	}
	
	public JsonObject getNumMessages(String kk) { //Param not needed other than for clean invocation
		JsonObject jo = new JsonObject();
		jo.addProperty("code",  "OK");
		jo.addProperty("nummessages", getNumMessagesInt());
		return jo;
	}
	
	public JsonObject getNumChats(String kk) {//Param not needed other than for clean invocation
		JsonObject jo = new JsonObject();
		jo.addProperty("code",  "OK");
		jo.addProperty("numchats", mapById.size());
		return jo;
	}
	
	private int getNumMessagesInt() {
		int ret = 0;
		for(int i: mapById.keySet())
			ret += mapById.get(i).size();
		return ret;
	}
		
	private void mapMessageFromChatId (Message m) {
		if (mapById.containsKey(m.getChatId()))
			mapById.get(m.getChatId()).add(m);
		else {
			List<Message> l = new ArrayList<>();
			l.add(m);
			mapById.put(m.getChatId(), l);
		}
	}
	
	private Path getLastFile(String dataDir) {
		if (new File(dataDir).isDirectory()) {
			File[] ls = new File(dataDir).listFiles();
			return Paths.get(dataDir + File.separator + messageFilePrefix + getLastIndex(ls));
		} else {
			System.err.println("No existe el directorio " + dataDir + File.separator  + "No se podrán retener los mensajes");
			log.log(Level.SEVERE, "Create the appropiate directory for messsages");
			return Paths.get("");
		}
	}
	
	private int getLastIndex(File ls[]) {
		int subindex = messageFilePrefix.length();
		int j; 
		int maxIndex = 0;
		for (int i=0; i<ls.length; i++) {
			try {
				j = Integer.parseInt(ls[i].getName().substring(subindex));
				if (j > maxIndex)
					maxIndex = i;
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				continue;
			}
					
		}
		return maxIndex;
	}
		
	private void rotateFiles(String dataDir) throws IllegalArgumentException {
		File[] ls = new File(dataDir).listFiles();
		int lastIndex = getLastIndex(ls) + 1;
		if (lastIndex > maxMessageFiles)
			currentDataFile = Paths.get(messageFilePrefix + "0");
		else
			currentDataFile = Paths.get(messageFilePrefix + lastIndex);		
	}
	
}
