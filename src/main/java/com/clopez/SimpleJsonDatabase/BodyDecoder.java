package com.clopez.SimpleJsonDatabase;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class BodyDecoder {
	
	private Map<String, String> params = new HashMap<>();
	
	public BodyDecoder(String body, String contentType) throws UnsupportedEncodingException {
		if (contentType.equals("application/x-www-form-urlencoded")) {
			for (String s : body.split("&")) {
				int idx = s.indexOf("=");
				params.put(URLDecoder.decode(s.substring(0, idx), "UTF-8"),
						URLDecoder.decode(s.substring(idx + 1), "UTF-8"));
			}
				
		} else if (contentType.equals("application/json")){
			return;
		} else {
			throw new UnsupportedEncodingException("Not supported encoding protocol");
		}
	}
	
	public Map<String, String> getParams() {
		return params;
	}

}
