package move;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import config.Config;
import exec.executable.ArgParser;
import exec.executable.Option;
import generic.gui.ShotRepo;
import io.DocInput;
import io.ShotReader;
import model.Coordinate;
import model.Shot;

public class Move{
	
	public static void main(String[] argStrings){
		MoveParser parser = new MoveParser();
		Map<String, Option> args = parser.parse(argStrings);
		System.out.println("Started moving...");
		try {
			DocInput input = ShotReader.read(new File(String.format("data/%s/input.csv", args.get("date").getValue())));
			Config cardConfig = Config.getConfig(String.format("cards/%s", input.getDiscipline()));
			double bulletSize = cardConfig.getDouble("bulletSize");
			File outputFile = new File("data/tmp/input.csv");
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
			ShotRepo repo = new ShotRepo(input.getDiscipline(), outputFile);
			String moveString = args.get("move").getValue();
			String[] moveParts = moveString.split("/");
//			Coordinate move = Coordinate.instance(Double.parseDouble(moveParts[0]), Double.parseDouble(moveParts[1]));
			for(List<Shot> line : input.getShots()){
				for(Shot shot : line){
					Coordinate move = Coordinate.instance(Double.parseDouble(moveParts[0]), Double.parseDouble(moveParts[1]));
//					System.out.println(shot.getXY().toString() + " - " + shot.getXY().add(move));
					Coordinate c = shot.getXY();
					
					double[] polar = c.add(move).toPolar();
					Shot movedShot = new Shot(10-polar[0], (int)polar[1], bulletSize);
					repo.add(movedShot);
				}
				repo.newLine();
			}
			repo.persist();
			System.out.println("_Finished!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static class MoveParser extends ArgParser{

		public MoveParser(){
			this.putOption("date", "the date the series was shot", true);
			this.putOption("move", "Modification to move the shots (x/y)", true);
		}
		
		@Override
		protected void checkArguments(Map<String, Option> arguments) throws IllegalArgumentException {
			String date = arguments.get("date").getValue();
			if(!new File(String.format("data/%s/input.csv", date)).exists()){
				throw new IllegalArgumentException(String.format("Data folder '%s' does not exist.", date));
			}
			String[] moveParts = arguments.get("move").getValue().split("/");
			
			if(moveParts.length != 2){
				throw new IllegalArgumentException("incorrect move format. Should be 'x/y'");
			}
			Double.parseDouble(moveParts[0]);
			Double.parseDouble(moveParts[1]);	
		}
		
	}
}