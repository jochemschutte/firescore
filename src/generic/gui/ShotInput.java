package generic.gui;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

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
			if(arguments.containsKey("D")){
				f.setDebugMode(true);
				System.out.println("Debug mode enabled");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static class ShotInputParser extends exec.executable.ArgParser{

		private Stack<String> argStack;
		private Map<String, Integer> argList;
		private Map<Integer, String> descrList;
		private Map<Integer, Boolean> manditoryList;
		
		public ShotInputParser(){
			argStack = new Stack<>();
			argStack.push("-D");
			argStack.push("-F");
			argStack.push("date");
			argStack.push("card");
			argList = new TreeMap<>();
			argList.put("card", 1);
			argList.put("date", 2);
			argList.put("-F", 3);
			argList.put("-D", 4);
			descrList = new TreeMap<>();
			descrList.put(1, "Card type (discipline)");
			descrList.put(2, "Date the series was shot");
			descrList.put(3, "Force overwrite of existing files");
			descrList.put(4, "Debug mode, no write");
			manditoryList = new TreeMap<>();
			manditoryList.put(1, true);
			manditoryList.put(2, true);
			manditoryList.put(3, false);
			manditoryList.put(4, false);
		}
		
		@Override
		protected Stack<String> getArgStack() {
			return argStack;
		}

		@Override
		protected Map<String, Integer> getArgList() {
			return this.argList;
		}

		@Override
		protected Map<Integer, String> getDescrList() {
			return this.descrList;
		}

		@Override
		protected Map<Integer, Boolean> getManditoryList() {
			return this.manditoryList;
		}

		@Override
		protected void checkArguments(Map<String, Option> arguments) throws IllegalArgumentException {
			if(!arguments.containsKey("card")){
				throw new IllegalArgumentException("No card was supplied");
			}
			if(!arguments.containsKey("date") && !arguments.containsKey("D")){
				throw new IllegalArgumentException("Supply a date or open in debug mode (-D)");
			}
			File outputFolder = new File(String.format("data/%s", arguments.get("date").getValue()));
			if(outputFolder.exists() && !(arguments.containsKey("F") || arguments.containsKey("D"))){
				throw new IllegalArgumentException(String.format("A series already exists with date '%s'. Choose an other date or add force flag (-F) or debug flag (-D).", arguments.get("date").getValue()));
			}
		}
		
	}
	
}