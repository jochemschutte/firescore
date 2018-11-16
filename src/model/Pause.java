package model;

public class Pause implements Action{

	@Override
	public ActionType getActionType() {
		return Action.ActionType.PAUSE;
	}
	
}