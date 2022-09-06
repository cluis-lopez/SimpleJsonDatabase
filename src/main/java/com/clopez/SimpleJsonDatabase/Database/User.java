package com.clopez.SimpleJsonDatabase.Database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class User {
    private String name;
    private String id;
    private String password;
    private String token;
    private Date token_valid_upTo;
    private Chat[] recentChats;
    private boolean isConnected;
    private static int numChats = 5;
    private int updates;
    private static int maxUpdates = 5;

    public User(String name, String password) {
        this.name = name;
        this.id = UUID.randomUUID().toString();
        this.recentChats = new Chat[numChats];
        //Arrays.fill(recentChats, "");
        for(int i=0; i<recentChats.length; i++)
        	recentChats[i] = new Chat("", false);
        if (! isPasswordValid(password))
            throw new IllegalArgumentException("Invalid Password");
        else 
            this.password = encryptPassword(password);;

        generateToken(30);
        this.isConnected = false;
        this.updates = 0;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return name;
    }

    public String getToken(){
        return token;
    }
    
    public Chat[] getRecentChats() {
    	return recentChats;
    }

    public Date getTokenValidUpTo(){
        return token_valid_upTo;
    }
    
    public int getUpdates() {
    	return updates;
    }
    
    public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public boolean isTokenValid(){
        if (token_valid_upTo.compareTo(new Date()) > 0)
            return true;
        else
            return false;
    }

    public void generateToken(int days){
        Date d = new Date();
        long milliseconds = (long)days * 24 * 3600 * 1000;
        d.setTime(d.getTime()+ milliseconds);
        this.token_valid_upTo = d;
        this.token = UUID.randomUUID().toString();
    }

    public boolean passwordMatch(String password){
        if (encryptPassword(password).equals(this.password))
            return true;
        else
            return false;
    }
    
    public void updateRecent(String chatName, boolean isUser) {
    	if (! chatName.equals(recentChats[0].name)) {
    		Chat[] temp = new Chat[numChats];
    		//Arrays.fill(temp, "");
            for(int i=0; i<recentChats.length; i++)
            	temp[i] = new Chat("", false);
    		temp[0].name = chatName;
    		temp[0].isUser = isUser;
    		for (int i=0; i<recentChats.length-1; i++) {
    			if (recentChats[i].name.equals(chatName))
    				continue;
    			temp[i+1] = recentChats[i];
    		}
    		recentChats = temp;
    		updates += 1;
    	}
    	return;
    }
    
    public boolean needsUpdate() {
    	if (updates >= maxUpdates) {
    		updates = 0;
    		return true;
    	}
    	else
    		return false;
    }

    private boolean isPasswordValid(String password){
        if (password != null && password.length()>=4)
            return true;
        else 
            return false;
    }

    private String encryptPassword(String clearPassword) {
        StringBuilder hexString = new StringBuilder();
        byte[] temp = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            temp = digest.digest(clearPassword.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < temp.length; i++) {
            hexString.append(Integer.toHexString(0xff & temp[i]));
        }
        return hexString.toString();
    }
    
    public class Chat{
    	String name;
    	boolean isUser;
    	
    	protected Chat(String name, boolean isUser) {
    		this.name = name;
    		this.isUser = isUser;
    	}
    	
    	public String getName() {
    		return this.name;
    	}
    	
    	public boolean getIsUser() {
    		return this.isUser;
    	}
    }
}
