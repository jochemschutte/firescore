package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import config.Config;
import model.Shot;

public class ShotReader{
	
	public static final String NULL = "null";
	
	public static DocInput read(File inputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		List<List<Shot>> shots = new LinkedList<>();
		String discipline = in.readLine();
		Config cardConfig = Config.getConfig(String.format("cards/%s", discipline));
		double shotSize = cardConfig.getDouble("bulletSize");
		for(String line = in.readLine(); line != null; line = in.readLine()){
			if(!line.trim().equals("")){
				List<Shot> inner = new LinkedList<>();
				for(String shot : line.split(";")){
					if(!shot.equals(NULL)){
						String[] parts = shot.split(",");
						inner.add(new Shot(Double.parseDouble(parts[0]), Integer.parseInt(parts[1]), shotSize));
					}else{
						inner.add(null);
					}
				}
				shots.add(inner);
			}
		}
		in.close();
		return new DocInput(discipline, shots);	
	}
	
}