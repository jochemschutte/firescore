package generic.gui;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import config.Config;
import exec.executable.Option;

public class ShotInput{
	
	public static void main(String[] args){
		try{
			Map<String, Option> arguments = new ShotInputParser().parse(args);
			String cardType = arguments.get("card").asText();
			String date = arguments.get("date").asText();
			Config cardConfig = Config.getConfig(String.format("cards/%s", cardType));
			int cardSize = cardConfig.getInt("imgSize");
			System.out.println("Starting...");
			InputFrame f = new InputFrame(cardSize, cardSize, cardType, cardConfig, new ShotRepo(cardType, new File(String.format("data/%s/input.csv", date))));
			if(arguments.get("D").isSet()){
				f.setDebugMode(true);
				System.out.println("Debug mode enabled");
			}
		}catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
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
			Option card = arguments.get("card");
			if(!card.isSet()){
				throw new IllegalArgumentException("No card was supplied");
			}
			if(!new File(String.format("cards/%s.png", card.asText())).exists()) {
				throwArg("No card file found for '%s'", card.asText());
			}
			if(!new File(String.format("cards/%s.prop", card.asText())).exists()){
				throwArg("No property file found for '%s'", card.asText());
			}
			if(!arguments.get("date").isSet() && !arguments.get("D").isSet()){
				throwArg("Supply a date or open in debug mode (-D)");
			}
			String date = arguments.get("date").asText();
			if(date.matches(".*[.\\/].*") || card.asText().matches(".*[.\\/].*")){
				throwArg("date or card cannot contain special characters like \"./\\\". Stop trying to hack my software!");
			}
			File outputFolder = new File(String.format("data/%s", date));
			if(outputFolder.exists() && !(arguments.get("F").isSet() || arguments.get("D").isSet())){
				throwArg("A series already exists with date '%s'. Choose another date or add force flag (-F) or debug flag (-D).", date);
			}
			
		}
		
	}
	
}