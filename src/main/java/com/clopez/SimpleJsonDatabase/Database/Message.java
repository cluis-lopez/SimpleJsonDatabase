package com.clopez.SimpleJsonDatabase.Database;

import java.time.Instant;
import java.util.Map;

import com.google.gson.Gson;

public class Message {
	
	private String type;
	private String mid;
	private String to;
	private String from;
	private String content;
	private Instant createdAt;
	private Instant deliveredAt;
	private int chatId;
	private boolean delivered;
	private boolean stored;
	
	public Message(String type, String mid, String to, String from, String content) {
		this.type = type;
		this.mid = mid;
		this.to = to;
		this.from = from;
		this.content = content;
		this.createdAt = Instant.now();
		this.deliveredAt = Instant.ofEpochSecond(0L);
		this.delivered = false;
		this.stored = true;
		this.chatId = generateChatId(from, to);
	}
	
	public Message (Map<String, String> jsonPayload) {
        this.type = "TEXT";
        this.mid = jsonPayload.get("id");
        this.to = jsonPayload.get("to");
        this.from = jsonPayload.get("from");
        this.content = jsonPayload.get("content");
        this.createdAt = Instant.parse(jsonPayload.get("createdAt"));
        this.deliveredAt = Instant.ofEpochSecond(0L);
        this.delivered = false;
        this.stored = true;
        this.chatId = generateChatId(from, to);
	}
	
	/*
	 * public boolean send(Session sid) { boolean ret; Gson gson = new Gson(); try {
	 * sid.getBasicRemote().sendText(gson.toJson(this)); this.delivered = true;
	 * this.deliveredAt = Instant.now(); ret = true; } catch (IOException e) {
	 * e.printStackTrace(); ret = false; } return ret; }
	 */
	
	public String getJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public String getTo() {
		return to;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getId() {
		return mid;
	}
	
	public int getChatId() {
		return chatId;
	}
	
	public Instant getCreated() {
		return createdAt;
	}
	
	public Instant getDelivered() {
		return deliveredAt;
	}
	
	public boolean isDelivered() {
		return this.delivered;
	}
	
	public void setDelivered() {
		this.delivered = true;
	}
	
	public void isStored(boolean stored) {
		this.stored = stored;
	}
	
	public int generateChatId(String from, String to) {
		String temp;
		if (from.compareTo(to) <= 0)
			temp = from + to;
		else
			temp = to + from;
		return temp.hashCode();
	}
	
}
