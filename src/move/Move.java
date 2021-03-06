package move;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import config.Config;
import exec.executable.ArgParser;
import exec.executable.Option;
import generic.gui.ShotRepo;
import io.DocInput;
import io.ShotReader;
import model.Action;
import model.Coordinate;
import model.Shot;

public class Move{
	
	public static void main(String[] argStrings){
		MoveParser parser = new MoveParser();
		Map<String, Option> args = parser.parse(argStrings);
		System.out.println("Started moving...");
		try {
			DocInput input = ShotReader.read(new File(String.format("data/%s/input.csv", args.get("date").asText())));
			Config cardConfig = Config.getConfig(String.format("cards/%s", input.getDiscipline()));
			double bulletSize = cardConfig.getDouble("bulletSize");
			double scoreDelta = cardConfig.getDouble("shotDelta");
			File outputFile = new File("data/tmp/input.csv");
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
			ShotRepo repo = new ShotRepo(input.getDiscipline(), outputFile);
			String moveString = args.get("move").asText();
			String[] moveParts = moveString.substring(1, moveString.length()-1).split("/");
			
			for(List<Action> line : split(input.getActions())){
				for(Action action : line){
					Action movedShot = action;
					if(action instanceof Shot) {
						Shot shot = (Shot)action;
						Coordinate move = Coordinate.instance(Double.parseDouble(moveParts[0]), Double.parseDouble(moveParts[1]));
						Coordinate c = shot.getXY();
						
						double[] polar = c.add(move).toPolar();
						movedShot = new Shot(10-polar[0], (int)polar[1], bulletSize, scoreDelta);
					}
					repo.addAction(movedShot);
				}
				repo.newLine();
			}
			repo.persist();
			System.out.println("_Finished!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<List<Action>> split(List<Action> orig){
		List<List<Action>> result = new LinkedList<>();
		List<Action> inner = new LinkedList<>();
		for(Action a : orig) {
			switch(a.getActionType()) {
			case NEWCARD:
				if(!inner.isEmpty()) {
					result.add(inner);
				}
				inner = new LinkedList<>();
				break;
			default:
				inner.add(a);
			}
		}
		if(!inner.isEmpty()) {
			result.add(inner);
		}
		return result;
	}
	
	private static class MoveParser extends ArgParser{

		public MoveParser(){
			this.putOption("date", "the date the series was shot", true);
			this.putOption("move", "Modification to move the shots (x/y)", true);
		}
		
		@Override
		protected void checkArguments(Map<String, Option> arguments) throws IllegalArgumentException {
			String date = arguments.get("date").asText();
			if(!new File(String.format("data/%s/input.csv", date)).exists()){
				throw new IllegalArgumentException(String.format("Data folder '%s' does not exist.", date));
			}
			String moveString = arguments.get("move").asText();
			if(Pattern.matches("(-?[0-9]+//-[0-9].+)", moveString)){
				throw new IllegalArgumentException("incorrect move format. Should be '(x/y)'");
			}	
		}
		
	}
}