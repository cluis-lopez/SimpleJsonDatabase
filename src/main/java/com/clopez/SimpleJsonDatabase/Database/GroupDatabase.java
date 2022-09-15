package com.clopez.SimpleJsonDatabase.Database;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class GroupDatabase extends SimpleJsonDatabase<Group> {

	public GroupDatabase(String filename, Logger log, Properties props) {
		super(filename, log, Group.class);
	}

	public JsonObject getMethods(String kk) {
		JsonObject jo = new JsonObject();
		JsonArray ja = new JsonArray();
		Method[] methods = null;
		Class<GroupDatabase> c = GroupDatabase.class;
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
		jo.addProperty("code", "OK");
		jo.add("methods", ja);
		return jo;
	}

	/**
	 * Inserts a new Group into the Group's database
	 * 
	 * @param s String containing Json object with jsonized "Group" object
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 */
	public JsonObject createGroup(String s) throws IllegalArgumentException {
		JsonObject jo = new JsonObject();
		jo.addProperty("code", "Unkown Error at createGroup");
		try {
			Group g = gson.fromJson(s, Group.class);
			createItem(g.getId(), g);
			jo.addProperty("code", "OK");
		} catch (JsonSyntaxException e) {
			log.log(Level.INFO, "Invalid parameter for createGroup");
			jo.addProperty("code", "Inavlid parameter for createGroup");
		} catch (IllegalArgumentException e) {
			log.log(Level.INFO, "Duplicated Group");
			jo.addProperty("code", "Inavlid parameter for createGroup");
		}
		return jo;
	}

	/**
	 * Removes a Group from the Group's database
	 * 
	 * @param s String containing Json object with a jsonized "Group" object
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 */
	public JsonObject deleteGroup(String s) {
		JsonObject jo = new JsonObject();
		jo.addProperty("code", "Unkown Error at deleteteGroup");
		try {
			Group g = gson.fromJson(s, Group.class);
			deleteItem(g.getId());
			jo.addProperty("code", "OK");
		} catch (JsonSyntaxException e) {
			log.log(Level.INFO, "Invalid parameter for deleteGroup");
			jo.addProperty("code", "Inavlid parameter for deleteGroup");
		} catch (IllegalArgumentException e) {
			log.log(Level.INFO, "Non-existing User");
			jo.addProperty("code", "Inavlid parameter for deleteGroup");
		}
		return jo;
	}

	/**
	 * Adds a new member into the Group's list
	 * 
	 * @param s String containing Json object with "id" string key and "user" object
	 *          representing the user to add in the list
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 */
	public JsonObject addMember(String s) {
		JsonObject janswer = new JsonObject();
		janswer.addProperty("code", "Invalid Member");
		JsonObject jo = new JsonObject();

		try {
			jo = JsonParser.parseString(s).getAsJsonObject();
			String id = jo.get("id").getAsString();
			JsonElement userJson = jo.get("user");
			User u = gson.fromJson(userJson, User.class);
			Group g = data.get(id);
			g.addMember(u);
			saveDatabase();
			janswer.addProperty("code", "OK");
		} catch (JsonSyntaxException e) {
			log.log(Level.INFO, "Invalid parameter for addMember");
			jo.addProperty("code", "Inavlid parameter for addMembet");
		} catch (IllegalArgumentException e) {
			log.log(Level.INFO, "Invalid username for this group");
			jo.addProperty("code", "Invalid username for this group");
		}
		return janswer;
	}

	/**
	 * Removes a User from the Group's list
	 * 
	 * @param s String containing Json object with "id" of the group and "User"
	 *          object representing the user to be removed from the list
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 */
	public JsonObject removeMember(String s) {
		JsonObject janswer = new JsonObject();
		janswer.addProperty("code", "Invalid Member");
		JsonObject jo = new JsonObject();

		try {
			jo = JsonParser.parseString(s).getAsJsonObject();
			String id = jo.get("id").getAsString();
			JsonElement userJson = jo.get("user");
			User u = gson.fromJson(userJson, User.class);
			Group g = data.get(id);
			g.removeMember(u);
			saveDatabase();
			janswer.addProperty("code", "OK");
		} catch (JsonSyntaxException e) {
			log.log(Level.INFO, "Invalid parameter for addMember");
			jo.addProperty("code", "Inavlid parameter for addMembet");
		} catch (IllegalArgumentException e) {
			log.log(Level.INFO, "Invalid user name for this group");
			jo.addProperty("code", "Invalid user name for this group");
		}
		return janswer;
	}

	/**
	 * Finds a Group using its Id field
	 * 
	 * @param s String containing the "id"
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 *         and a Json object representation of the Group
	 */

	public JsonObject findGroupById(String jsonId) {
		JsonObject jo = new JsonObject();
		Group g = findById(jsonId);
		if (g == null)
			jo.addProperty("code", "Group not found");
		else {
			jo.addProperty("code", "OK");
			JsonElement je = gson.toJsonTree(g);
			jo.add("group", je);
		}
		return jo;
	}

	/**
	 * Finds a Group using its Name field
	 * 
	 * @param s String containing the name to look for
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 *         and a Json object representation of the Group
	 */
	public JsonObject findGroupByName(String jsonName) {
		Group g;
		JsonObject jo = new JsonObject();
		jo.addProperty("code", "Group not found");
		for (String id : data.keySet()) {
			g = data.get(id);
			if (g.getName().equals(jsonName)) {
				jo.addProperty("code", "OK");
				JsonElement je = gson.toJsonTree(g);
				jo.add("group", je);
				break;
			}
		}
		return jo;
	}

	/**
	 * Finds a list of Groups using a wildchar into the name field
	 * 
	 * @param s String containing the name to look for
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 *         and a Json array with the list of groups whose names math (contains)
	 *         the wildchar
	 */
	public JsonObject findGroupByWildChar(String jsonWc) {
		JsonObject jo = new JsonObject();
		JsonArray ja = new JsonArray();
		jo.addProperty("code", "Unkown error at findGroupByWildChar");
		Group g;

		if (jsonWc == null || jsonWc.equals("")) {
			jo.addProperty("code", "Invalid search pattern");
			return jo;
		}

		for (String id : data.keySet()) {
			g = data.get(id);
			if (g.getName().toLowerCase().contains(jsonWc)) {
				JsonElement je = gson.toJsonTree(g);
				ja.add(je);
			}
		}
		jo.addProperty("code", "OK");
		jo.add("groups", ja);
		return jo;
	}

	/**
	 * Finds the list of groups owned by a single user
	 * 
	 * @param s String containing the name of the owner
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 *         and a Json array with the list of groups owned by this user or empty
	 *         if the user does not own any group
	 */
	public JsonObject findByOwner(String s) {
		JsonObject jo = new JsonObject();
		JsonArray ja = new JsonArray();
		jo.addProperty("code", "Unkown error at findByOwner");
		try {
			User u = gson.fromJson(s, User.class);
			Group g;
			for (String id : data.keySet()) {
				g = data.get(id);
				if (g.getOwner().equals(u.getName())) {
					JsonElement je = gson.toJsonTree(g);
					ja.add(je);
				}
			}
			jo.addProperty("code", "OK");
			jo.add("groups", ja);
		} catch (JsonSyntaxException e) {
			jo.addProperty("code", "Invalid payload. User json expected");
		}
		return jo;
	}

	/**
	 * Finds the list of groups where one user belongs to
	 * 
	 * @param s String containing the name of the owner
	 * @return Returns a JsonObject with key "code" and the result of the operation
	 *         and a Json array with the list of groups that this user belongs to or
	 *         empty if the user does not own any group
	 */
	public JsonObject findByUser(String s) {
		JsonObject jo = new JsonObject();
		JsonArray ja = new JsonArray();
		jo.addProperty("code", "Unkown error at findByUser");
		try {
			User u = gson.fromJson(s, User.class);
			Group g;
			for (String id : data.keySet()) {
				g = data.get(id);
				if (g.isMember(u)) {
					JsonElement je = gson.toJsonTree(g);
					ja.add(je);
				}
			}
			jo.addProperty("code", "OK");
			jo.add("groups", ja);
		} catch (JsonSyntaxException e) {
			jo.addProperty("code", "Invalid payload. User json expected");
		}
		return jo;
	}
}
