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
import io.DocInput;
import model.Coordinate;
import model.Shot;
import model.cards.Card;
import model.cards.Card.AVGMode;

public class Job{
	
	private int imgSize;
	private String discipline;
	private File inputFile;
	private File outputFolder;
	private Config cardConfig;
	
	public Job(File inputFile, File outputFolder, Config general){
		this.imgSize = general.getInt("imgSize");
		this.inputFile = inputFile;
		this.outputFolder = outputFolder;
	}
	
	public File run() throws IOException {
		generateCards();
		return writeHtml();
	}
	
	private void generateCards() throws IOException{
		DocInput input = io.ShotReader.read(inputFile);
		this.discipline = input.getDiscipline();
		this.cardConfig = Config.getConfig(String.format("cards/%s", input.getDiscipline()));
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
		Card card = getCard(AVGMode.PERVISUAL, cardConfig);
		card.setShotList(flipLists(read));
		card.draw(new File(String.format("%s/cards/%s_total.png", outputFolder.getAbsolutePath(), input.getDiscipline())));
		
		card = getCard(AVGMode.TOTAL, cardConfig); 
		card.setShotList(allInBag(2, read));
		card.draw(new File(String.format("%s/cards/%s_sum.png", outputFolder.getAbsolutePath(), input.getDiscipline())));
	}
	
	private File writeHtml() throws IOException{
		File outputFile = new File(String.format("%s/scores.html", outputFolder.getAbsolutePath()));
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		out.write("<html>\n");
		out.write(h(1, inputFile.getParentFile().getName()));
		out.write("<font size=5>");
		for(int i = 1; getCardFile(i).exists(); i++){
			File card = getCardFile(i);
			if(card.getName().matches(".+_[0-9].png")){
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
		return String.format("<img src=\"cards/%s\" width=%d hight=%d>", card.getName(), imgSize, imgSize);
	}
	
	private static <T> List<List<T>> allInBag(int bagId, List<List<T>> list){
		List<List<T>> result = new ArrayList<>();
		for(int i = 0; i < list.size(); i++){
			result .add(new LinkedList<>());
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
	
	private static Card getCard(Card.AVGMode mode, Config prop){
		String discipline = getDiscipline(prop);
		File background = new File(String.format("cards/%s.png", discipline));
		Map<Card.Offset, Object> offsetMap = new TreeMap<>();
		offsetMap.put(Card.Offset.AVGSHOT, getCoordinate(prop.getString("offsets.AVGSHOT")));
		offsetMap.put(Card.Offset.SCORE, getCoordinate(prop.getString("offsets.SCORE")));
		offsetMap.put(Card.Offset.SHOTS, getCoordinates(prop.getList("offsets.SHOTS")));
		int deviation = prop.getInt("deviation");
		int bulletSize = prop.getInt("bulletSize");
		return new Card(background, offsetMap, deviation, bulletSize, mode);
	}
	
	private static String getDiscipline(Config prop){
		String bullet = prop.getString("bulletType");
		String weapon = prop.getString("weaponType");
		int nrVisuals = prop.getInt("nrVisuals");
		int distance = prop.getInt("distanceToCard");
		return String.format("%s_%s_%dv_%dm", bullet, weapon, nrVisuals, distance);
	}
	
	private static Coordinate[] getCoordinates(List<String> values){
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