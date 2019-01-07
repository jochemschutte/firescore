package exec.executable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class ArgParser{

	protected abstract void checkArguments(Map<String, Option> arguments) throws IllegalArgumentException;

	private LinkedList<String> argStack = new LinkedList<>();
	private Map<String, Option> options = new TreeMap<>();
	
	private Map<String, Option> getOptions(){
		return this.options;
	}

	private LinkedList<String> getArgStack(){
		return this.argStack;
	}
	
	@SuppressWarnings("unchecked")
	public String getDescription(){
		String header = "";
		List<String> result = new LinkedList<>();
		result.add("--help\t\tdisplays this help menu\n");
		LinkedList<String> copy = (LinkedList<String>)getArgStack().clone();
		while(copy.size() > 0){
			String key = copy.pop();
			String format = "%s ";
			if(!getOptions().get(key).isManditory()){
				format = "[%s] ";
			}
			header += String.format(format, key);
			result.add(key +"\t\t"+getOptions().get(key).getDescription());
		}
		return String.format("%s\n\n%s", header, String.join("\n", result));
	}
	
	private String getNextArgument(){
		return getArgStack().pop();
	}
			
	public Map<String, Option> parse(String[] input){
		return parse(String.join(" ", input));
	}

	public Map<String, Option> parse(String input) throws IllegalArgumentException{
		Map<String, Option> result = getOptions();
		List<String> args = split(input);
		if(args.size() > 0 && args.get(0).equals("--help")){
			System.out.println(this.getDescription());
			System.exit(0);
		}
		for(String arg : args){
			String[] parts = arg.split("=");
			if(arg.startsWith("--")){
				if(arg.length() < 3 || !result.containsKey(arg.substring(2))){
					throwKeyNotFound(arg);
				}
				result.get(arg.substring(2)).setValue("true");
			}else if(arg.startsWith("-")){
				if(arg.length() < 2){
					throwKeyNotFound("-");
				}
				for(char c : arg.substring(1).toCharArray()){
					String key = Character.toString(c);
					if(!result.containsKey(key)){
						throwKeyNotFound(String.format("-%s", key));
					}
					getOptions().get(key).setValue("true");
				}
			}else if(parts.length > 1){
				String[] valueParts = java.util.Arrays.copyOfRange(parts, 1, parts.length);
				if(!result.containsKey(valueParts[0])){
					throwKeyNotFound(valueParts[0]);
				}
				getOptions().get(parts[0]).setValue(String.join("=", valueParts));
			}else{
				String key = getNextArgument();
				getOptions().get(key).setValue(arg);
			}
		}
		this.checkArguments(getOptions());
		return getOptions();
	}
	
	private void throwKeyNotFound(String key) throws IllegalArgumentException{
		throw new IllegalArgumentException(String.format("Option '%s' is not recognised. type '--help' for all options", key));
	}
	
	public static void throwArg(String format, String... args) throws IllegalArgumentException{
		throw new IllegalArgumentException(String.format(format, (Object[])args));
	}
	
	private List<String> split(String line){
		List<String> result = new LinkedList<>();
		boolean escape = false;
		Iterator<Character> stream = toCharStream(line);
		String current = "";
		while(stream.hasNext()){
			char next = stream.next();
			if(escape){
				current += next;
				escape = false;
			}else{
				switch(next){
				case '\\':
					escape = true;
					break;
				case '"':
					current += readString(stream);
					break;
				case ' ':
					if(current.length() > 0){
						result.add(current);
						current = "";
					}
					break;
				default:
					current += next;
				}
			}
		}
		if(current.length() > 0){
			result.add(current);
		}
		return result;
	}
	
	private String readString(Iterator<Character> stream){
		boolean escape = false;
		String result = "";
		for(char next = stream.next(); !escape || next != '"'; next = stream.next()){
			result += next;
		}
		return result;
	}
	
	protected void putOption(String key, String description, boolean manditory){
		argStack.add(key);
		getOptions().put(key, new Option(key, description, manditory));
	}
	
	private Iterator<Character> toCharStream(String line){
		List<Character> result = new LinkedList<>();
		for(char c : line.toCharArray()){
			result.add(c);
		}
		return result.iterator();
	}
	
}