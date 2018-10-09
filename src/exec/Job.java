package exec;

import java.awt.Color;
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
import draw.BarGraph;
import draw.BufferedLineGraph;
import draw.generator.NumberGenerator;
import draw.generator.StringGenerator;
import io.DocInput;
import model.Coordinate;
import model.DoubleAverager;
import model.Shot;
import model.cards.Card;
import model.cards.Card.AVGMode;

public class Job{
	
	private static final String doubleBreak = "<br />&nbsp<br />";
	
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
		List<Shot> shotList = listShots(read);
		List<Double> scoreList = listScores(shotList);
		System.out.println("generating lapse");
		BufferedLineGraph lg = initLineGraph(scoreList.size());
		lg.drawHrule(Shot.avgPoints(shotList), Color.RED, 1);
		
		lg.draw(scoreList, Color.BLACK, 1);
		
		List<Double> avgScoreList = new DoubleAverager(cardConfig.getInt("graphAvgReach")).runDoubles(scoreList);
		if(avgScoreList.size() > 1){
			lg.draw(avgScoreList, Color.BLUE, 2);
		}else{
			System.out.println(String.format("Average lapse omitted due to only having %d average shot", avgScoreList.size()));
		}
		
		lg.write(new File(String.format("%s/graphs/lapse.png", outputFolder.getAbsolutePath())));
		
		System.out.println("Generating score count graph");
		BarGraph bg = initBarGraph();
		bg.setValues(getIntList(10), countScores(shotList));
		bg.draw(Color.BLUE);
		bg.write(new File(String.format("%s/graphs/count.png", outputFolder.getAbsolutePath())));
		
	} 
	
	private static List<String> getIntList(int maxValue){
		List<String> result = new LinkedList<>();
		for(int i = 0; i <= maxValue; i++){
			result.add(Integer.toString(i));
		}
		return result;
	}
	
	private static Map<String, Double> countScores(Iterable<Shot> shots){
		Map<String, Double> result = new TreeMap<>();
		for(int i = 0; i <= 10; i++){
			result.put(Integer.toString(i), 0.0);
		}
		for(Shot shot : shots){
			String points = Integer.toString(shot.getPoints());
			result.put(points, result.get(points)+1);
		}
		return result;
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
	
	private List<Shot> listShots(List<List<Shot>> shots){
		List<Shot> result = new LinkedList<>();
		for(List<Shot> inner: shots){
			result.addAll(inner);
		}
		return result;
	}
	
	private List<Double> listScores(List<Shot> shots){
		List<Double> flatScores = new LinkedList<>();
		for(Shot s : shots){
			flatScores.add(s.getPoints() * 1.0);
		}
		return flatScores;
	}
	
	private BufferedLineGraph initLineGraph(int nrXValues) throws IOException{
		int graphWidth = globalConfig.getInt("graphWidth");
		int graphHeight = globalConfig.getInt("graphHeight");
		StringGenerator xScale = new NumberGenerator(1,1);
		BufferedLineGraph graph = new BufferedLineGraph(graphWidth, graphHeight, xScale, nrXValues, getYScale(), 10, 1);
		return graph;
	}
	
	private BarGraph initBarGraph(){
		return new BarGraph(globalConfig.getInt("graphWidth"), globalConfig.getInt("graphHeight"), globalConfig.getInt("barGraphStep"));
	}
	
	private File writeHtml() throws IOException{
		File outputFile = new File(String.format("%s/scores.html", outputFolder.getAbsolutePath()));
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		out.write("<html>\n");
		out.write("<font size=5>\n");
		for(int i = 1; getCardFile(i).exists(); i++){
			out.write("<p>\n");
			if(i == 1){
				out.write(h(1, inputFile.getParentFile().getName()));
			}
			File card = getCardFile(i);
			if(card.getName().matches(".+_[0-9]+.png")){
				out.write(card.getName().split("\\.")[0] + "<br />\n");
				out.write(cardHtml(card) + "<br />&nbsp <br />\n");
			}
//			out.write("<hr />\n");
			out.write("</p>\n");
		}
		File totalCard = new File(String.format("%s/cards/%s_total.png", outputFolder.getAbsolutePath(), this.discipline));
		if(totalCard.exists()){
			out.write("<p>\n");
			out.write(totalCard.getName().split("\\.")[0] + "<br />\n");
			out.write(cardHtml(totalCard) + doubleBreak +"\n");
			out.write("</p>\n");
		}
		
		File sumCard = new File(String.format("%s/cards/%s_sum.png", outputFolder.getAbsolutePath(), this.discipline));
		if(sumCard.exists()){
			out.write("<p>\n");
			out.write(sumCard.getName().split("\\.")[0] + "<br />\n");
			out.write(cardHtml(sumCard) + "\n");
			out.write("</p>\n");
		}
		File lapseOutput = new File(String.format("%s/graphs/lapse.png", outputFolder.getAbsolutePath()));
//		out.write("<hr />\n");
		out.write("<p>\n");
		out.write(String.format("Lapse (avg=%d): <br />\n", cardConfig.getInt("graphAvgReach")));
		out.write(graphHtml(lapseOutput));
		out.write(doubleBreak + "\n");
		
		
		File avgOutput = new File(String.format("%s/graphs/avglapse.png", outputFolder.getAbsolutePath()));
		if(avgOutput.exists()){
			out.write(String.format("Averages (%d shots)<br>\n", cardConfig.getInt("graphAvgReach")*2+1));
			out.write(graphHtml(avgOutput) + "\n");
		}
		
		File countGraph = new File(String.format("%s/graphs/count.png", outputFolder.getAbsolutePath()));
//		out.write("<hr />\n");
		out.write("Shots counted: <br />\n");
		out.write(graphHtml(countGraph) + "\n");
		out.write("</p>\n");
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
	
	private String cardHtml(File card){
		int imgSize = cardConfig.getInt("imgSize");
		return cardHtml(card, imgSize, imgSize);
	}
	
	private String cardHtml(File img, int width, int height){
		return String.format("<img src=\"cards/%s\" width=%d hight=%d border=1>", img.getName(), width, height);
	}
	
	private String graphHtml(File graph){
		int width = globalConfig.getInt("graphWidth");
		int height = globalConfig.getInt("graphHeight");
		return String.format("<img src=\"graphs/%s\" width=%d hight=%d border=1>", graph.getName(), width, height);
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
		double bulletSize = prop.getDouble("bulletSize");
		int textSize = prop.getInt("textSize");
		int minScore = prop.getInt("minScore");
		double shotDelta = prop.getDouble("shotDelta");
		return new Card(background, offsetMap, deviation, bulletSize, shotDelta, textSize, minScore, mode);
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