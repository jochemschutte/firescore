package exec;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import config.Config;
import exec.executable.Option;

public class FireScore{
	
	private static final String ALLOPTION = "all";
	private static final String OPENONLYOPTION = "o";
	
	public static void main(String[] args){
		FireScoreParser parser = new FireScoreParser();
		Map<String, Option> arguments = parser.parse(args);
		try{
			File htmlFile = null;
			Config generalConfig = Config.getConfig("configs/global");
			if(arguments.containsKey(ALLOPTION)){
				File dataFolder = new File("data");
				for(File inputFolder : dataFolder.listFiles()){
					generate(inputFolder.getName(), generalConfig);
				}
				System.out.println("_Finished generating all.");
				System.out.println("Will not open due to mass generation");
			}else if(!arguments.containsKey(OPENONLYOPTION) || !arguments.get(OPENONLYOPTION).asBoolean()){
				htmlFile = generate(arguments.get("date").getValue(), generalConfig);
				openHTML(htmlFile);
			}else{
				System.out.println("Skipped generating");
				openHTML(new File(String.format("output/%s/scores.html", arguments.get("date").getValue())));
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static void openHTML(File htmlFile) throws IOException{
		if(!htmlFile.exists()){
			throw new FileNotFoundException(String.format("File \'%s\' not found", htmlFile.getName()));
		}
		System.out.println("Opening output in browser");
		Desktop.getDesktop().browse(htmlFile.toURI());
		try{
			Thread.sleep(2000);
		}catch(InterruptedException e){}
	}
	
	private static File generate(String dataId, Config generalConfig) throws IOException{
		File inputFolder = new File(String.format("data/%s", dataId));
		File outputFolder = new File(String.format("output/%s", dataId));
		if(inputFolder.list().length == 0){
			throw new IllegalStateException(String.format("No input file found in \'%s\'", inputFolder.getName()));
		}
		if(inputFolder.list().length > 1){
			throw new IllegalStateException(String.format("More than one input files found in \'%s\'", inputFolder.getName()));
		}
		deleteFile(outputFolder);
		outputFolder.mkdir();
		Job job = new Job(inputFolder.listFiles()[0], outputFolder, generalConfig);
		System.out.println(String.format("Starting \'%s\'...", outputFolder.getName()));
		File htmlFile = job.run();
		System.out.println();
		return htmlFile;
	}
	
	public static void deleteFile(File file) throws IOException{
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				deleteFile(f);
			}
		}
		file.delete();
	}
	
	private static class FireScoreParser extends exec.executable.ArgParser{

		private Stack<String> argStack;
		private Map<String, Integer> argList;
		private Map<Integer, String> descrList;
		private Map<Integer, Boolean> manditoryList;
		
		public FireScoreParser(){
			argStack = new Stack<>();
			argList = new TreeMap<>();
			descrList = new TreeMap<>();
			manditoryList = new TreeMap<>();
			argStack.push("--all");
			argStack.push("-o");
			argStack.push("date");
			argList.put("date", 0);
			argList.put("-o", 1);
			argList.put("--all", 2);
			descrList.put(0, "The date the series was shot");
			descrList.put(1, "Only open the previously rendered document");
			descrList.put(2, "Generate all data sources");
			manditoryList.put(0, true);
			manditoryList.put(1, false);
			manditoryList.put(2, false);
		}
		
		@Override
		protected Stack<String> getArgStack() { 
			return this.argStack;
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
		protected void checkArguments(Map<String, Option> args) throws IllegalArgumentException {
			if(args.containsKey("date")){
				String date = args.get("date").getValue();
				File inputFolder = new File(String.format("data/%s", date));
				if(!inputFolder.exists()){
					throw new IllegalArgumentException(String.format("no data folder found with date '%s'", date));
				}
			}else if(!args.containsKey(ALLOPTION)){
				throw new IllegalArgumentException("no 'date' argument given");
			}
		}
	}
}