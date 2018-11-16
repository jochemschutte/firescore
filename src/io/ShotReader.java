package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import config.Config;
import generic.gui.ShotRepo;
import model.Shot;

public class ShotReader{
	
	public static final String NULL = "null";
	
	public static DocInput read(File inputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		String discipline = in.readLine();
		ShotRepo repo = new ShotRepo(discipline, null);
		Config cardConfig = Config.getConfig(String.format("cards/%s", discipline));
		double shotSize = cardConfig.getDouble("bulletSize");
		double scoreDelta = cardConfig.getDouble("shotDelta");
		int minScore = cardConfig.getInt("minScore");
		String line = in.readLine();
		String[] parts = line.split(";");
		for(String part : parts) {
			if(!part.equals(NULL)) {
				String prefix = part.substring(0,1);
				String data = part.substring(1);
				switch(prefix) {
				case "s":
					String[] dataArr = data.split(",");
					repo.add(new Shot(Double.parseDouble(dataArr[0]), Integer.parseInt(dataArr[1]), shotSize, scoreDelta, minScore));
					break;
				case "p":
					repo.addPause();
					break;
				case "c":
					repo.newLine();
					break;
				}
			}else {
				repo.addAction(null);
			}
		}
		in.close();
		return new DocInput(discipline, repo.getActionList());	
	}
	
}