package generic.gui;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import config.Config;
import exec.executable.Option;

public class ShotInput{
	
	public static void main(String[] args){
		Map<String, Option> arguments = new ShotInputParser().parse(args);
		String cardType = arguments.get("card").getValue();
		String date = arguments.get("date").getValue();
		Config cardConfig = Config.getConfig(String.format("cards/%s", cardType));
		int cardSize = cardConfig.getInt("imgSize");
		try{
			System.out.println("Starting...");
			InputFrame f = new InputFrame(cardSize, cardSize, cardType, cardConfig, new ShotRepo(cardType, new File(String.format("data/%s/input.csv", date))));
			if(arguments.get("D").isSet()){
				f.setDebugMode(true);
				System.out.println("Debug mode enabled");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static class ShotInputParser extends exec.executable.ArgParser{
		
		public ShotInputParser(){
			this.putOption("card", "Card type (discipline)", true);
			this.putOption("date", "Date the series was shot", true);
			this.putOption("F", "Force overwrite of existing files", false);
			this.putOption("D", "Debug mode, no write", false);
		}

		@Override
		protected void checkArguments(Map<String, Option> arguments) throws IllegalArgumentException {
			if(!arguments.get("card").isSet()){
				throw new IllegalArgumentException("No card was supplied");
			}
			if(!arguments.get("date").isSet() && !arguments.get("D").isSet()){
				throw new IllegalArgumentException("Supply a date or open in debug mode (-D)");
			}
			String date = arguments.get("date").getValue();
			String card = arguments.get("card").getValue();
			if(date.matches(".*[.\\/].*") || card.matches(".*[.\\/].*")){
				throw new IllegalArgumentException("date or card cannot contain special characters like \"./\\\". Stop trying to hack my software!");
			}
			File outputFolder = new File(String.format("data/%s", arguments.get("date").getValue()));
			if(outputFolder.exists() && !(arguments.get("F").isSet() || arguments.get("D").isSet())){
				throw new IllegalArgumentException(String.format("A series already exists with date '%s'. Choose an other date or add force flag (-F) or debug flag (-D).", arguments.get("date").getValue()));
			}
			
		}
		
	}
	
}