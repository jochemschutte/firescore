package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LineIO{
	
	public static List<String> read(File inputFile) throws IOException{
		List<String> result = new LinkedList<>();
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		for(String line = in.readLine(); line != null; line = in.readLine()){
			result.add(line);
		}
		in.close();
		return result;
	}
	
	public static void write(Iterable<String> data, File outputFile) throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		for(String line : data){
			out.write(line + "\n");
		}
		out.flush();
		out.close();
	}
}