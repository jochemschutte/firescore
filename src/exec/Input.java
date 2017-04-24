package exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Input{
	
	public static final String EXITCMD = "exit";
	
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("Error:");
			System.out.println("No output file supplied. Shutting down");
			System.exit(0);
		}
		try{
			InputStream in = System.in;
			File outputFile = new File(String.format("data/%s/input.csv", args[0]));
			if(outputFile.exists()){
				in = new FileInputStream(outputFile);
				Scanner scan = new Scanner(in);
				String firstLine = scan.nextLine();
				scan.close();
				if(!firstLine.trim().toLowerCase().equals("DRAFT")){
					System.out.println("Error:");
					System.out.println("When reading from file the first line should be \"DRAFT\"");
					System.exit(0);
				}
			}
			List<String> lines = read(in, outputFile);
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			for(String line : lines){
				out.write(line + "\n");
			}
			out.flush();
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("_Finished");
	}
	
	private static List<String> read(InputStream inputStream, File outputFile){
		Scanner in = new Scanner(inputStream);
		List<String> outputLines = new LinkedList<>();
		outputLines.add(in.nextLine());
		System.out.println("Title added.");
		while(in.hasNext()){
			String line = in.nextLine();
			try{
				String[] parts = line.split(" ");
				if(parts.length % 2 != 0){
					System.out.println("Odd nr of arguments. Line not added.");
				}else{
					outputLines.add(format(parts));
					System.out.println("Line added");
				}
			}catch(NumberFormatException e){
				System.out.println("Input contains non-numerals. line not added");
			}
		}
		in.close();
		return outputLines;
	}
	
	private static String format(String[] parts) throws NumberFormatException{
		String result = "";
		for(int i = 0; i < parts.length; i++){
			if(i % 2 == 0){
				result += Double.parseDouble(parts[i]) + ",";
			}else{
				result += Integer.parseInt(parts[i]) + ";";
			}
		}
		return result;
	}
}