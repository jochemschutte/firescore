package generic.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import model.Action;
import model.Action.ActionType;
import model.NewCard;
import model.Pause;
import model.Shot;

public class ShotRepo{

	private File outputFile;
	private String discipline;
	private LinkedList<Action> actions = new LinkedList<>();
	
	public ShotRepo(String discipline, File outputFile) {
		this.discipline = discipline;
		this.outputFile = outputFile;
		actions.add(new NewCard());
	}
	
	
	public String getDiscipline() {
		return this.discipline;
	}

	
	public File getOutputFile() {
		return this.outputFile;
	}

	
	public void add(Shot s) {
		actions.add(s);
	}

	public void pause() {
		actions.add(new Pause());
	}
	
	public List<Action> getActionList(){
		return actions;
	}
	
	public Action getLastAction() {
		return actions.getLast();
	}

	public List<Action> getLastCard(){
		List<Action> result = new LinkedList<>();
		Iterator<Action> iter = actions.descendingIterator();
		Action current = null;
		while(iter.hasNext()) {
			current = iter.next();
			if(current.getActionType() == ActionType.NEWCARD)
				break;
			result.add(0, current);
		}
		return result;
	}
	
	public Action removeLastAction() {
		Action result = actions.removeLast();
		if(actions.isEmpty())
			actions.add(new NewCard());
		return result;
	}

	public List<Action> removeLastCard() {
		LinkedList<Action> result = new LinkedList<>();
		Action current = actions.removeLast();
		while(!(current instanceof NewCard)) {
			result.add(current);
			current = actions.removeLast();
		}
		actions.add(current);
		if(actions.isEmpty())
			actions.add(new NewCard());
		return result;
	}

	
	public void newLine() {
		actions.add(new NewCard());
	}
	
	public void addAction(Action a) {
		actions.add(a);
	}

	
	public boolean lastLineEmpty() {
		Iterator<Action> iter = actions.descendingIterator();
		while(iter.hasNext()) {
			Action current = iter.next();
			switch(current.getActionType()){
			case SHOT:
			case PAUSE:
				return false;
			case NEWCARD:
				return true;
			default:
			}
		}
		return false;
	}

	
	public boolean isEmpty() {
		return actions.stream().filter(a-> a.getActionType()==ActionType.SHOT||a.getActionType()==ActionType.PAUSE).count() == 0;
	}

	
	public int size() {
		return (int) actions.stream().filter(a-> a instanceof Shot).count();
	}

	
	public void persist() throws IOException {
		outputFile.getParentFile().mkdirs();
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		out.write(this.discipline+"\n");
		List<String> buffer = new LinkedList<>();
		for(Action action : actions) {
			switch(action.getActionType()) {
			case SHOT:
				Shot shot = (Shot)action;
				out.write(String.format(Locale.US, "s%f,%d;", shot.getScore(), shot.getAngle()));
				break;
			case NEWCARD:
				out.write("c;");
				buffer.clear();
				break;
			case PAUSE:
				out.write("p;");
				break;				
			}
		}
		if(!buffer.isEmpty()) {
			out.write("\n");
		}
		out.flush();
		out.close();
	}
	
	
	public void addPause() {
		if(actions.getLast().getActionType() != ActionType.PAUSE){
			actions.add(new Pause());
		}
	}
	
}