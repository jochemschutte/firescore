package io;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.Action;
import model.Shot;

public class DocInput{
	
	private String discipline;
	private List<Action> actions;
	
	public DocInput(String discipline, List<Action> actions) {
		this.discipline = discipline;
		this.actions = actions;
	}
	public String getDiscipline() { 
		return discipline;
	}
	public void setDiscipline(String dicipline) {
		this.discipline = dicipline;
	}
	public List<Action> getActions() {
		return actions;
	}
	public void setActions(List<Action> actions) {
		this.actions = actions;
	}
	
	public List<List<Shot>> getShots(){
		List<List<Shot>> result = new LinkedList<>();
		List<Shot> inner = new LinkedList<>();
		for(Action a : actions) {
			switch(a.getActionType()) {
			case SHOT:
				inner.add((Shot)a);
				break;
			case NEWCARD:
				if(!inner.isEmpty()) {
					result.add(inner);
				}
				inner = new LinkedList<>();
				break;
			}
		}
		if(!inner.isEmpty()) {
			result.add(inner);
		}
		return result;
	}
	
	public List<Integer> getPauses(){
		int i = 0;
		Iterator<Action> iter = actions.iterator();
		List<Integer> pauses = new LinkedList<>();
		while(iter.hasNext()) {
			switch(iter.next().getActionType()) {
			case SHOT:
				i++;
				break;
			case PAUSE:
				pauses.add(i);
				break;
			}
		}
		return pauses;
	}
}