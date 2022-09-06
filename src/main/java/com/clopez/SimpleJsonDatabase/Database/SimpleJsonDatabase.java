package com.clopez.SimpleJsonDatabase.Database;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public abstract class SimpleJsonDatabase<T> {
	HashMap<String, T> data;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	String datafile;
	Logger log;

	public SimpleJsonDatabase(String filename, Logger log, Class c) {
		data = new HashMap<>();
		JsonReader reader;
		this.datafile = filename;
		this.log = log;
		
		java.lang.reflect.Type dataType = TypeToken.getParameterized(HashMap.class, String.class, c).getType();
		
		try {
			reader = new JsonReader(new FileReader(datafile));
			data = gson.fromJson(reader, dataType);
			reader.close();
			System.out.println("La base de datos contiene " + data.size() + " elementos");
			for (String s : data.keySet()) {
				System.out.println("Key :" + s);
				System.out.println("Value" + data.get(s).getClass());
			}
		} catch (FileNotFoundException e) {
			System.err.println("Warning: no existe el fichero de datos " + filename);
			log.log(Level.WARNING, "Datafile does not exist ... First time? " + datafile);
		} catch (IOException e) {
			System.err.println("Warning: fichero con problemas");
			log.log(Level.SEVERE, "Failure reading datafile " + datafile);
			log.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
		}
	}

	/**
	 * 
	 */
	public void saveDatabase() {
		try {
			File file = new File(datafile);
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			gson.toJson(data, fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Warning: problmas al escribir en el fichero de datos " + datafile);
			log.log(Level.SEVERE, "Failure writing datafile " + datafile);
			log.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
		}
	}

	public boolean deleteDatabase() {
		File file = new File(datafile);
		System.out.println("El fichero: " + file.getAbsolutePath() + " se va a borrar");
		return file.delete();
	}

	protected void createItem(String id, T t) throws IllegalArgumentException {
		if (findById(id) != null)
			throw new IllegalArgumentException("El elemento ya existe en la BD");
		else {
			data.put(id, t);
			saveDatabase();
		}

	}

	protected void deleteItem(String id) throws IllegalArgumentException {
		if (!data.containsKey(id)) // Invalid user
			throw new IllegalArgumentException("El elemento no existe");
		else {
			data.remove(id);
			saveDatabase();
		}
	}

	protected T findById(String id) {
		if (data.containsKey(id))
			return data.get(id);
		else
			return null;
	}
}