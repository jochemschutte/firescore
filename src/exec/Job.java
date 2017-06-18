package exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import config.Config;
import draw.LineGraph;
import draw.generator.NumberGenerator;
import draw.generator.StringGenerator;
import io.DocInput;
import model.Coordinate;
import model.Shot;
import model.cards.Card;
import model.cards.Card.AVGMode;

public class Job{
	
	private String discipline;
	private File inputFile;
	private File outputFolder;
	private Config cardConfig;
	private Config globalConfig;
	
	public Job(File inputFile, File outputFolder, Config global){
		this.inputFile = inputFile;
		this.outputFolder = outputFolder;
		this.globalConfig = global;
	}
	
	private static List<String> getYScale(){
		List<String> result = new LinkedList<>();
		for(int i = 0; i <= 10; i++){
			result.add(Integer.toString(i));
		}
		return result;
	}
	
	public File run() throws IOException {
		generateCards();
		return writeHtml();
	}
	
	private void generateCards() throws IOException{
		DocInput input = io.ShotReader.read(inputFile);
		this.discipline = input.getDiscipline();
		this.cardConfig = Config.getConfig(String.format("cards/%s", input.getDiscipline()));
		int nrVisuals = cardConfig.getInt("nrVisuals");
		List<List<Shot>> read = input.getShots();
		File cardsFolder = new File(String.format("%s/cards", outputFolder.getAbsolutePath()));
		cardsFolder.mkdirs();
		int i = 1;
		for(List<Shot> shots : read){ 
			Card card = getCard(model.cards.Card.AVGMode.TOTAL, cardConfig);
			card.setShotsInSequence(shots);
			File outputFile = new File(String.format("%s/cards/%s_%d.png", outputFolder.getAbsolutePath(), input.getDiscipline(), i));
			card.draw(outputFile);
			i++;
		}
		List<List<Shot>> flipped = flipLists(read);
		List<List<Shot>> flattened = flatten(flipped, cardConfig.getInt("nrVisuals"));
		if(hasMultipleVisualsWithShots(flattened)){
			Card card = getCard(AVGMode.PERVISUAL, cardConfig);
			card.setShotList(flattened);
			card.draw(new File(String.format("%s/cards/%s_total.png", outputFolder.getAbsolutePath(), input.getDiscipline())));
		}else{
			System.out.println("Average card was omitted due to only having one visual");
		}
		Card card = getCard(AVGMode.TOTAL, cardConfig);
		card.setShotList(allInBag(cardConfig.getInt("avgVisual"), nrVisuals, flipped));
		card.draw(new File(String.format("%s/cards/%s_sum.png", outputFolder.getAbsolutePath(), input.getDiscipline())));
		
		new File(String.format("%s/graphs", outputFolder.getAbsoluteFile())).mkdir();
		drawGraph(read, new File(String.format("%s/cards/lapse.png", outputFolder.getAbsolutePath())));
	}
	
	public static boolean hasMultipleVisualsWithShots(List<List<Shot>> shots){
		int nrVisuals = 0;
		for(List<Shot> inner: shots){
			if(inner.size() > 0){
				nrVisuals++;
			}
		}
		return nrVisuals > 1;
	}
	
	private static List<List<Shot>> flatten(List<List<Shot>> shots, int nrVisuals){
		List<List<Shot>> result = new ArrayList<>();
		for(int i = 0; i < nrVisuals; i++){
			result.add(new LinkedList<>());
		}
		for(int i = 0; i < shots.size(); i++){
			int pointer = i%nrVisuals;
			result.get(pointer).addAll(shots.get(i));
		}
		return result;
	}
	
	private void drawGraph(List<List<Shot>> shots, File outputFile) throws IOException{
		
		List<Double> flatScores = new LinkedList<>();
		for(List<Shot> inner : shots){
			for(Shot s : inner){
				flatScores.add(s.getScore() * 1.0);
			}
		}
		int graphWidth = globalConfig.getInt("graphWidth");
		int graphHeight = globalConfig.getInt("graphHeight");
		StringGenerator xScale = new NumberGenerator(1,1);
		LineGraph graph = new LineGraph(graphWidth, graphHeight, xScale, getYScale(), 10, 1);
		graph.setPoints(flatScores);
		LineGraph.write(graph.draw(), outputFile);
	}
	
	private File writeHtml() throws IOException{
		File outputFile = new File(String.format("%s/scores.html", outputFolder.getAbsolutePath()));
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		out.write("<html>\n");
		out.write(h(1, inputFile.getParentFile().getName()));
		out.write("<font size=5>");
		for(int i = 1; getCardFile(i).exists(); i++){
			File card = getCardFile(i);
			if(card.getName().matches(".+_[0-9]+.png")){
				out.write(card.getName().split("\\.")[0] + "<br />\n");
				out.write(imgHtml(card) + "<br />&nbsp <br />\n");
			}
		}
		out.write("<hr />\n");
		File totalCard = new File(String.format("%s/cards/%s_total.png", outputFolder.getAbsolutePath(), this.discipline));
		if(totalCard.exists()){
			out.write(totalCard.getName().split("\\.")[0] + "<br />\n");
			out.write(imgHtml(totalCard) + "<br />&nbsp <br />\n");
		}
		
		File sumCard = new File(String.format("%s/cards/%s_sum.png", outputFolder.getAbsolutePath(), this.discipline));
		if(sumCard.exists()){
			out.write(sumCard.getName().split("\\.")[0] + "<br />\n");
			out.write(imgHtml(sumCard) + "\n");
		}
		
		File lapseOutput = new File(String.format("%s/grahs/lapse.png", outputFolder.getAbsolutePath()));
		System.out.println(String.format("Generating \'%s\'.", lapseOutput.getName()));
		out.write("<hr />\n");
		out.write("Lapse: <br />\n");
		out.write(graphHtml(lapseOutput));
		
		out.write("</font>\n</html>");
		out.flush();
		out.close();
		return outputFile;
	}
		
	private File getCardFile(int suffix){
		return new File(String.format("%s/cards/%s_%d.png", outputFolder.getAbsolutePath(), this.discipline, suffix));
	}
	
	private static String h(int headerSize, String txt){
		return String.format("<h%d>%s</h%d>", headerSize, txt, headerSize);
	}
	
	private String imgHtml(File card){
		int imgSize = cardConfig.getInt("imgSize");
		return imgHtml(card, imgSize, imgSize);
	}
	
	private String imgHtml(File img, int width, int height){
		return String.format("<img src=\"cards/%s\" width=%d hight=%d>", img.getName(), width, height);
	}
	
	private String graphHtml(File graph){
		int width = globalConfig.getInt("graphWidth");
		int height = globalConfig.getInt("graphHeight");
		return imgHtml(graph, width, height);
	}
	
	private static <T> List<List<T>> allInBag(int bagId, int nrVisuals, List<List<T>> list){
		List<List<T>> result = new ArrayList<>();
		for(int i = 0; i < nrVisuals; i++){
			result.add(new LinkedList<>());
		}
		for(List<T> inner : list){
			result.get(bagId).addAll(inner);
		}
		return result;
	}
	
	private static <T> List<List<T>> flipLists(List<List<T>> lists){
		List<List<T>> result = new ArrayList<>();
		for(List<T> inner : lists){
			Iterator<T> iter = inner.iterator();
			for(int i = 0; iter.hasNext(); i++){
				while(result.size() <= i){
					result.add(new LinkedList<>());
				}
				result.get(i).add(iter.next());
			}
		}
		return result;
	}
	
	public static Card getCard(Card.AVGMode mode, Config prop){
		String discipline = getDiscipline(prop);
		File background = new File(String.format("cards/%s.png", discipline));
		Map<Card.Offset, Object> offsetMap = new TreeMap<>();
		offsetMap.put(Card.Offset.AVGSHOT, getCoordinate(prop.getString("offsets.AVGSHOT")));
		offsetMap.put(Card.Offset.SCORE, getCoordinate(prop.getString("offsets.SCORE")));
		offsetMap.put(Card.Offset.SHOTS, getCoordinates(prop.getList("offsets.SHOTS")));
		int deviation = prop.getInt("deviation");
		int bulletSize = prop.getInt("bulletSize");
		int textSize = prop.getInt("textSize");
		return new Card(background, offsetMap, deviation, bulletSize, textSize, mode);
	}
	
	private static String getDiscipline(Config prop){
		String bullet = prop.getString("bulletType");
		String weapon = prop.getString("weaponType");
		int nrVisuals = prop.getInt("nrVisuals");
		int distance = prop.getInt("distanceToCard");
		return String.format("%s_%s_%dv_%dm", bullet, weapon, nrVisuals, distance);
	}
	
	public static Map<Integer, Integer> aggegateScores(List<Shot> shots){
		Map<Integer, Integer> result = new TreeMap<>();
		for(int i = 1; i <= 10; i++){
			result.put(i, 0);
		}
		for(Shot shot : shots){
			result.put(shot.getPoints(), result.get(shot.getPoints()));
		}
		return result;
	}
	
	public static Coordinate[] getCoordinates(List<String> values){
		List<Coordinate> result = new LinkedList<>();
		for(String c : values){
			result.add(getCoordinate(c));
		}
		return result.toArray(new Coordinate[result.size()]);
	}
	
	private static Coordinate getCoordinate(String value){
		String[] parts = value.split(",");
		return Coordinate.instance(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
	}
}