package generic.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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

	public String getDiscipline() {
		return discipline;
	}


	public File getOutputFile() {
		return outputFile;
	}


	public void add(Shot s){
		shotList.getLast().add(s);
	}
	
	public List<Shot> getLastLine(){
		return this.shotList.getLast();
	}
	
	public Shot removeLastShot(){
		Shot result = null;
		if(!this.lastLineEmpty()){
			List<Shot> inner = this.shotList.getLast();
			result = inner.remove(inner.size() -1);
		}
		return result;
	}
	
	public List<Shot> removeLastCard(){
		List<Shot> result = new LinkedList<>();
		if(!this.lastLineEmpty()){
			result = this.shotList.removeLast();
			this.shotList.add(new LinkedList<>());
		}
		return result;
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
	
	public int size(){
		int size = 0;
		for(List<Shot> inner: shotList){
			size += inner.size();
		}
		return size;
	}
	
	public void persist() throws IOException{
		outputFile.getParentFile().mkdirs();
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		out.write(this.discipline + "\n");
		for(List<Shot> shots : shotList){
			List<String> ser = new LinkedList<>();
			for(Shot shot : shots){
				ser.add(String.format(Locale.US,"%f,%d", shot.getScore(), shot.getAngle()));
			}
			out.write(String.join(";", ser) + "\n");
		}
		out.flush();
		out.close();
	}
	
}