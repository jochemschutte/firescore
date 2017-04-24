package io;

import java.util.List;

import model.Shot;

public class DocInput{
	
	private String discipline;
	private List<List<Shot>> shots;
	
	public DocInput(String discipline, List<List<Shot>> shots) {
		this.discipline = discipline;
		this.shots = shots;
	}
	public String getDiscipline() { 
		return discipline;
	}
	public void setDiscipline(String dicipline) {
		this.discipline = dicipline;
	}
	public List<List<Shot>> getShots() {
		return shots;
	}
	public void setShots(List<List<Shot>> shots) {
		this.shots = shots;
	}
	
}