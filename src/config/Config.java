package config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Config{
	private static Map<String, Config> configs = new TreeMap<>();
	
	private String id;
	private Map<String, Object> properties;
	
	private Config(File input) throws IOException{
		this.id = input.getName();
		this.properties = ConfigReader.read(input);
	}

	public String getName() {
		return id;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public String getString(String id){
		return (String)this.properties.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getList(String id){
		return (List<String>)this.properties.get(id); 
	}
	
	public int getInt(String id){
		try{
			return Integer.parseInt(getString(id));
		}catch(NumberFormatException e){
			throw new IllegalArgumentException(
					String.format("NaN: key = \'%s\', value = \'%s\'", id, getString(id))
				);
		}
	}
	
	public double getDouble(String id){
		try{
			return Double.parseDouble(getString(id));
		}catch(NumberFormatException e){
			throw new IllegalArgumentException(
					String.format("NaN: key = \'%s\', value = \'%s\'", id, getString(id))
				);
		}
	}
	
	public static Config getConfig(String id){
		Config result = null;
		String configPath = String.format("%s.prop", id);
		try{
			if(!configs.containsKey(id)){
				configs.put(id, new Config(new File(configPath)));
			}
			result = configs.get(id);
		}catch(IOException e){
			throw new NullPointerException(String.format("Configuration with ID \'%s\' not found. (No such file \'%s\'", id, configPath));
		}
		return result;
	}
	
	
}