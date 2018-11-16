package model;

public class NewCard implements Action{

	@Override
	public ActionType getActionType() {
		return Action.ActionType.NEWCARD;
	}
	
}