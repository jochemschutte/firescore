package model;

public interface Action{
	
	public enum ActionType {SHOT, PAUSE, NEWCARD};
	
	public ActionType getActionType();
}