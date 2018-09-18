package httpserver;

import java.util.HashMap;
import java.util.Locale;

public class HttpHeader {
	
	private HashMap<String, String> headerMap;
	
	public HttpHeader() {
		this.headerMap = new HashMap<String, String>();
	}
	
	
	public String getHeaderField(String key) {
		if(key == null) {
			throw new NullPointerException("null Header Key");
		}
		return headerMap.get(key.toLowerCase(Locale.ENGLISH));
	}
	
	
	public void setHeaderField(String key, String value) {
		if((key == null) || (value == null)) {
			throw new NullPointerException("null Header Key/Value");
		}
		headerMap.put(key.trim().toLowerCase(Locale.ENGLISH), value.trim());
	}
	
	
	public String[] dumpHeader(String lineBreak) {
		
		String[] header = new String[headerMap.size()];
		
		int index = 0;
		for(String key: headerMap.keySet()) {
			String value = headerMap.get(key);
			header[index++] = key + ": " + value + lineBreak;
		}
		return header;
	}
}
