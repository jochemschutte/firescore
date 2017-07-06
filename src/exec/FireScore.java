package exec;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

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
			if(arguments.get(ALLOPTION).isSet()){
				File dataFolder = new File("data");
				for(File inputFolder : dataFolder.listFiles()){
					generate(inputFolder.getName(), generalConfig);
				}
				System.out.println("_Finished generating all.");
				System.out.println("Will not open due to mass generation");
			}else if(!arguments.get(OPENONLYOPTION).isSet()){
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
		
		public FireScoreParser(){
			this.putOption("date", "The date the series was shot", true);
			this.putOption("o", "Only open the previously rendered document", false);
			this.putOption("all", "Generate all data sources", false);
		}

		@Override
		protected void checkArguments(Map<String, Option> args) throws IllegalArgumentException {
			if(args.get("date").isSet()){
				String date = args.get("date").getValue();
				if(date.matches(".*[.\\/].*")){
					throw new IllegalArgumentException("date cannot contain special characters like \"./\\\". Stop trying to hack my software!");
				}
				File inputFolder = new File(String.format("data/%s", date));
				if(!inputFolder.exists()){
					throw new IllegalArgumentException(String.format("no data folder found with date '%s'", date));
				}
			}else if(!args.get(ALLOPTION).isSet()){
				throw new IllegalArgumentException("no 'date' argument given");
			}
		}
	}
}