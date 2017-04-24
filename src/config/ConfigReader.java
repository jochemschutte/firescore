package config;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.LineIO;

public class ConfigReader{
	
	private static final char COMMENTCHAR = '%';
	private static final char ESCAPECHAR = '\\';
	private static final char LISTDELIM = ',';
	
	public static Map<String, Object> read(File inputFile) throws IOException{
		Map<String, Object> result = new TreeMap<>();
		List<String> lines = LineIO.read(inputFile);
		for(String line : lines){
			Map.Entry<String, Object> rule = interpretRule(line);
			if(rule != null){
				if(result.containsKey(rule.getKey())){
					throw new DuplicateKeyException(rule.getKey(), result.get(rule.getKey()), rule.getValue());
				}
				result.put(rule.getKey(), rule.getValue());
			}
		}
		return result;
	}
	
	private static Map.Entry<String, Object> interpretRule(String line){
		Map.Entry<String, Object> result = null;
		String trimmed = line.trim();
		if(!trimmed.matches(COMMENTCHAR + ".*")){
			String[] parts = trimmed.split("=");
			String key = parts[0].trim();
			String[] valueParts = new String[parts.length-1];
			System.arraycopy(parts, 1, valueParts, 0, valueParts.length);
			Object value = String.join("=", valueParts).trim();
			if(((String)value).matches("\\[.*\\]")){
				value = interpretList((String)value);
			}else{
				value = removeTrailingComment((String)value);
			}
			result = new java.util.AbstractMap.SimpleEntry<>(key, value);
		}
		return result;
	}
	
	private static String removeTrailingComment(String value){
		String result = "";
		boolean skip = false;
		for(char c : value.toCharArray()){
			if(skip){
				result += c;
				skip = false;
			}else{
				if(c == ESCAPECHAR){
					skip = true;
				}else if(c == COMMENTCHAR){
					break;
				}else{
					result += c;
				}
			}
		}
		return result.trim();
	}
	
	
	private static List<String> interpretList(String value){
		List<String> result = new LinkedList<>();
		String current = "";
		boolean skip = false;
		for(char c : value.substring(1, value.length()-1).toCharArray()){
			if(skip){
				current += c;
				skip = false;
			}else{
				if(c == ESCAPECHAR){
					skip = true;
				}else if(c == LISTDELIM){
					result.add(current.trim());
					current = "";
				}else if(c == COMMENTCHAR){
					break;
				}else{
					current += c;
				}
			}
		}
		result.add(current);
		return result;
	}
	
	public static void main(String[] args){
		try{
			System.out.println(read(new File("cards/air_rifle_5v_10m.prop")));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static class DuplicateKeyException extends RuntimeException{
		private static final long serialVersionUID = 3878566430851374552L;
		
		public DuplicateKeyException(String msg){
			super(msg);
		}
		
		public DuplicateKeyException(String key, Object... values){
			super(String.format("key = %s, values = %s", key, formatMap(values)));
		}
		
		private static Map<Integer, String> formatMap(Object... values){
			Map<Integer, String> result = new TreeMap<>();
			for(int i = 0; i < values.length; i++){
				if(values[i] instanceof String){
					result.put(i, String.format("\"%s\"", values[i].toString()));
				}else{
					result.put(i, values[i].toString());
				}
			}
			return result;
		}
		
	}
	
	
}