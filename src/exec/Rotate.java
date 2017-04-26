package exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import config.Config;

public class Rotate{
	
	public static final String ROTATE = "rotate";
	
	public static void main(String[] args){
		if(args.length == 0){
			throw new IllegalArgumentException("no input arguments given.");
		}
		System.out.println(args[0]);
		try{
			run(new File(String.format("data/%s/input.csv", args[0])));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void run(File input)throws IOException{
		System.out.println("Started rotating...");
		BufferedReader in = new BufferedReader(new FileReader(input));
		String rotate = in.readLine();
		if(!rotate.trim().toLowerCase().equals(ROTATE)){
			in.close();
			throw new IllegalStateException(String.format("File did not start with %s.", ROTATE));
		}
		String discipline = in.readLine();
		List<String> lines = new LinkedList<>();
		for(String line = in.readLine(); line != null; line = in.readLine()){
			lines.add(line);
		}
		in.close();
		List<String> rotated = rotate(lines, getMods(discipline));
		BufferedWriter out = new BufferedWriter(new FileWriter(input));
		out.write(discipline + "\n");
		for(String line : rotated){
			out.write(line + "\n");
		}
		out.flush();
		out.close();
		System.out.println("_Finished");
	}
	
	private static List<Integer> getMods(String discipline) throws IOException{
		Config config = Config.getConfig(String.format("%s/%s", model.cards.Card.cardPath, discipline));
		List<Integer> result = new LinkedList<>();
		for(String mod : config.getList("rotations")){
			result.add(Integer.parseInt(mod));
		}
		return result;
	}
	
	public static List<String> rotate(List<String> lines, List<Integer> mods) throws IOException{
		List<String> result = new LinkedList<>();
		for(String line : lines){
			String[] parts = line.split(";");
			String[] resultParts = new String[parts.length];
			for(int i = 0; i < parts.length; i++){
				if(!parts[i].equals("")){
					String[] elems = parts[i].split(",");
					elems[1] = Integer.toString(Integer.parseInt(elems[1]) + mods.get(i));
					resultParts[i] = String.join(",", elems);
				}
			}
			result.add(String.join(";", resultParts));
		}
		return result;
	}
}