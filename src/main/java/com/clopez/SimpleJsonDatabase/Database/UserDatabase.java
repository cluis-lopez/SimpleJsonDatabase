package com.clopez.SimpleJsonDatabase.Database;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;


public class UserDatabase extends SimpleJsonDatabase<User> {
    
	public UserDatabase(String filename, Logger log, Properties props) { 
    	super (filename, log, User.class);
    }

	public JsonObject getMethods(String kk) {
		JsonObject jo = new JsonObject();
		JsonArray ja = new JsonArray();
		Method[] methods =null;
			Class<UserDatabase> c = UserDatabase.class;
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
	
	public JsonObject saveDatabase (String kk) { //Dumb String parameter to follow invocation standards
		JsonObject jo = new JsonObject();
		if (saveDatabase())
			jo.addProperty("code", "OK");
		else
			jo.addProperty("code", "Failure when saving database");
		
		return jo;
	}
	
	
    public JsonObject createUser(String s) {
    	JsonObject jo = new JsonObject();
    	jo.addProperty("code", "Unkown Error at createUser");
    	try {
    		User u = gson.fromJson(s, User.class);
    		if (findByName(u.getName()) == null) {
    			createItem(u.getId(), u);
    			jo.addProperty("code",  "OK");
    			log.log(Level.INFO, "Successfully created user " + u.getName());
    		} else
    			jo.addProperty("code", "Duplicated User Name");
    	} catch (JsonSyntaxException e) {
    		log.log(Level.INFO, "Invalid parameter for createUser");
    		jo.addProperty("code", "Inavlid parameter for createUser");
    	} catch (IllegalArgumentException e) {
    		log.log(Level.INFO, "Duplicated User Id");
    		jo.addProperty("code", "Inavlid parameter for createUser");
    	}
    	return jo;
    }
    
    public JsonObject updateUser(String s) {
    	JsonObject jo = new JsonObject();
    	jo.addProperty("code", "Invalid user to update");
    	try {
    		User uNew = gson.fromJson(s, User.class);
    		User uOld = findById(uNew.getId());
    		if (uOld != null) { // The user already exists
    			updateItem(uOld.getId(), uNew);
    			jo.addProperty("code", "OK");
    			log.log(Level.INFO, "Successfully updated user " + uNew.getName());
    		}
    	} catch (JsonSyntaxException e) {
    		log.log(Level.INFO, "Invalid parameter for updateUser " + e.getMessage());
    		jo.addProperty("code", "Invalid parameter for updateUser " + e.getMessage());
    	} catch (IllegalArgumentException e) {
    		log.log(Level.INFO, "Duplicated User Id " + e.getMessage());
    		jo.addProperty("code", "Invalid parameter for updateUser " + e.getMessage());
    	}
    	return jo;
    }

    public JsonObject deleteUser(String s) {
    	JsonObject jo = new JsonObject();
    	try {
    		User u = gson.fromJson(s, User.class);
    		deleteItem(u.getId());
    		jo.addProperty("code", "OK");
    	} catch (JsonSyntaxException e) {
    		log.log(Level.INFO, "Invalid parameter for deleteUser");
    		jo.addProperty("code", "Inavlid parameter for deleteUser");
    	} catch (IllegalArgumentException e) {
    		log.log(Level.INFO, "Non-exixting User");
    		jo.addProperty("code", "Inavlid parameter for deleteUser");
    	}
    	return jo;
    }
    
    public JsonObject findUserById (String jsonId) {
    	JsonObject jo = new JsonObject();
    	String id = gson.fromJson(jsonId, String.class);
    	User u = findById(id);
    	if (u == null)
    		jo.addProperty("code", "User not found");
    	else {
    		jo.addProperty("code", "OK");
        	JsonElement je = gson.toJsonTree(u);
        	jo.add("user", je);
    	}
    	return jo;
    }

    public JsonObject findUserByName(String jsonName){
        User u;
        JsonObject jo = new JsonObject();
        String name = gson.fromJson(jsonName, String.class);
        jo.addProperty("code", "User not found");
        if ((u = findByName(name)) != null) {
            	jo.addProperty("code", "OK");
            	JsonElement je = gson.toJsonTree(u);
            	jo.add("user", je);
            }
        return jo;
    }
    
    
    private User findByName(String name) {
    	User u = null;
    	for (String id : data.keySet()){
            u = data.get(id);
            if (u.getName().equals(name)) {
            	break;
            } else
            	u = null;
        }
    	return u;
    }
    
    public JsonObject findUserByWildChar(String jsonWc) {
    	JsonObject jo = new JsonObject();
    	JsonArray ja = new JsonArray();
    	try {
    		String wc = gson.fromJson(jsonWc,  String.class);
    		User u;
    	
    		if (wc == null || wc.equals("")) {
    			jo.addProperty("code",  "Invalid search pattern");
    			return jo;
    		}
    	
    		for (String id : data.keySet()) {
    			u = data.get(id);
    			if (u.getName().toLowerCase().contains(wc)) {
    				JsonElement je = gson.toJsonTree(u);
    				ja.add(je);
    			}
    		}
    		jo.addProperty("code", "OK");
    		jo.add("users", ja);
    	} catch (JsonSyntaxException e) {
    		jo.addProperty("code", "Invalid payload. String expected");
    	}
    	return jo;
    }
}
