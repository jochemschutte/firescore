package exec;

import java.io.File;
import java.io.IOException;

import config.Config;

public class FireScore{
	
	public static void main(String[] args){
		if(args.length == 0){
			throw new IllegalArgumentException("no input arguments given.");
		}
		System.out.println(args[0]);
		try{
			File inputFolder = new File(String.format("data/%s", args[0]));
			if(inputFolder.list().length == 0){
				throw new IllegalStateException(String.format("No input file found in \'%s\'", args[0]));
			}
			if(inputFolder.list().length > 1){
				throw new IllegalStateException(String.format("More than one input files found in \'%s\'", args[0]));
			}
			File inputFile = inputFolder.listFiles()[0];
			File outputFolder = new File(String.format("output/%s", args[0]));
			Config generalConfig = Config.getConfig("configs/global");
			Job job = new Job(inputFile, outputFolder, generalConfig);
			System.out.println("Starting...");
			job.run();
			System.out.println("_Finished.");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}