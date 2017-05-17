package generic.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import model.Shot;

public class ShotRepo{
	
	private LinkedList<List<Shot>> shotList = new LinkedList<>();
	String discipline;
	File outputFile;
	
	public ShotRepo(String discipline, File outputFile){
		this.discipline = discipline;
		this.outputFile = outputFile;
		this.newLine();
	}
	
	public void add(Shot s){
		shotList.getLast().add(s);
	}
	
	public void newLine(){
		if(shotList.size() == 0 || !lastLineEmpty()){
			shotList.add(new LinkedList<>());
		}
	}
	
	public boolean lastLineEmpty(){
		return shotList.getLast().size() == 0;
	}
	
	public boolean isEmpty(){
		return shotList.size() == 0 || (shotList.size() == 1 && lastLineEmpty());
	}
	
	public void persist() throws IOException{
		outputFile.getParentFile().mkdirs();
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		out.write(this.discipline + "\n");
		for(List<Shot> shots : shotList){
			List<String> ser = new LinkedList<>();
			for(Shot shot : shots){
				double[] polar = shot.getXY().toPolar();
				ser.add(String.format("%f,%d", polar[0], (int)polar[1]));
			}
			out.write(String.join(";", ser) + "\n");
		}
		out.flush();
		out.close();
	}
	
}