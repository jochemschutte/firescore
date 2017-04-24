package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import model.Shot;

public class ShotReader{
	
	public static DocInput read(File inputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		List<List<Shot>> shots = new LinkedList<>();
		String dicipline = in.readLine();
		for(String line = in.readLine(); line != null; line = in.readLine()){
			List<Shot> inner = new LinkedList<>();
			for(String shot : line.split(";")){
				String[] parts = shot.split(",");
				inner.add(new Shot(Double.parseDouble(parts[0]), Integer.parseInt(parts[1])));
			}
			shots.add(inner);
		}
		in.close();
		return new DocInput(dicipline, shots);	
	}
	
}