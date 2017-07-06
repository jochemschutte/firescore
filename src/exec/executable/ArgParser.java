package exec.executable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public abstract class ArgParser{

	protected abstract Stack<String> getArgStack();
	protected abstract Map<String, Integer> getArgList();
	protected abstract Map<Integer, String> getDescrList();
	protected abstract Map<Integer, Boolean> getManditoryList(); 
	protected abstract void checkArguments(Map<String, Option> arguments) throws IllegalArgumentException;
	
	@SuppressWarnings("unchecked")
	public String getDescription(){
		String header = "";
		List<String> result = new LinkedList<>();
		result.add("--help\t\tdisplays this help menu\n");
		Stack<String> copy = (Stack<String>)getArgStack().clone();
		while(copy.size() > 0){
			String key = copy.pop();
			int id = getArgList().get(key);
			String format = "%s ";
			if(!getManditoryList().get(id)){
				format = "[%s] ";
			}
			header += String.format(format, key);
			result.add(key +"\t\t"+getDescrList().get(id));
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
		List<String> args = split(input);
		if(args.size() > 0 && args.get(0).equals("--help")){
			System.out.println(this.getDescription());
			System.exit(0);
		}
		Map<String, Option> result = new TreeMap<>();
		for(String arg : args){
			String[] parts = arg.split("=");
			if(arg.startsWith("--")){
				String key = arg.substring(2);
				if(key.length() < 1){
					throw new IllegalArgumentException("'--' is not a valid argument");
				}
				result.put(key, new Option(key, "true"));
			}else if(arg.startsWith("-")){
				if(arg.length() < 2){
					throw new IllegalArgumentException("'-' is not a valid flag");
				}
				for(char c : arg.substring(1).toCharArray()){
					String key = Character.toString(c);
					result.put(key, new Option(key, "true"));
				}
			}else if(parts.length > 1){
				String[] valueParts = java.util.Arrays.copyOfRange(parts, 1, parts.length);
				result.put(parts[0], new Option(parts[0], String.join("=", valueParts)));
			}else{
				String key = getNextArgument();
				result.put(key, new Option(key, arg));
			}
		}
		this.checkArguments(result);
		return result;
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
	
	private Iterator<Character> toCharStream(String line){
		List<Character> result = new LinkedList<>();
		for(char c : line.toCharArray()){
			result.add(c);
		}
		return result.iterator();
	}
	
}