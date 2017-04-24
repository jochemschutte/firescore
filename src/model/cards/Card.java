package model.cards;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import model.Coordinate;
import model.Shot;

public class Card{
	
	public enum Offset {SHOTS, AVGSHOT, SCORE};
	public enum Color {BLUE, RED};
	public enum AVGMode {PERVISUAL, TOTAL};
	protected static final String dotPath = "cards/dots";
	protected static final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
//	public int factor = 18;
//	public int bulletSize = 30;
//	protected Coordinate[] offsets = {
//			Coordinate.instance(237.000000, 237.000000), 
//			Coordinate.instance(765.000000, 237.000000), 
//			Coordinate.instance(501.000000, 501.000000), 
//			Coordinate.instance(237.000000, 765.000000), 
//			Coordinate.instance(765.000000, 765.000000)
//			};

//	protected Coordinate avgOffset = Coordinate.instance(501.000000, 501.000000);
//	protected Coordinate scoreOffset = Coordinate.instance(910, 40);

	
//	protected int nrVisuals = 5;
	private int nrVisuals;
	private int factor;
	private int bulletSize;
	private Coordinate[] offsets;
	private Coordinate avgOffset;
	private Coordinate scoreOffset;
	
	private AVGMode avgMode;
	private BufferedImage background;
	private List<List<Shot>> shots = new LinkedList<>();
	
	public Card(File backgroundLocation, Map<Offset, Object> offsetMap, int deviation, int bulletSize, AVGMode mode){
		this.avgMode = mode;
		this.offsets = (Coordinate[])(offsetMap.get(Offset.SHOTS));
		this.nrVisuals = this.offsets.length;
		this.factor = deviation;
		this.bulletSize = bulletSize;
		this.avgOffset = (Coordinate)offsetMap.get(Offset.AVGSHOT);
		this.scoreOffset = (Coordinate)offsetMap.get(Offset.SCORE);
		this.setShotsInSequence(new LinkedList<>());
		try{
			this.background = ImageIO.read(backgroundLocation);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public List<Shot> getShots() {
		return flatten(this.shots);
	}
	
	public List<List<Shot>> getShotList(){
		return this.shots;
	}
	
	public int getNrShots(){
		int result = 0;
		for(List<Shot> inner : this.shots){
			result += inner.size();
		}
		return result;
	}

	public void setShotsInSequence(List<Shot> shots) {
		this.shots = new ArrayList<>();
		for(int i = 0; i < nrVisuals; i++){
			this.shots.add(new LinkedList<>());
		}
		Iterator<Shot> iter = shots.iterator();
		for(int i = 0; iter.hasNext(); i++){
			this.shots.get(i%nrVisuals).add(iter.next());
		}
	}
	
	public void setShotList(List<List<Shot>> shots){
		this.shots = shots;
	}
	
	public void draw(File output) throws IOException{
		System.out.println(String.format("Generating '%s'.", output.getName()));
		Graphics g = this.background.getGraphics();
		g.setFont(font);
		g.setColor(java.awt.Color.RED);
		for(int i = 0; i < this.shots.size(); i++){
			List<Shot> shotList = shots.get(i);
			for(Shot shot : shotList){
				drawDot(g, shot, offsets[i], Color.RED);
			}
			if(this.shots.get(i).size() == 1){
				Shot shot = shotList.get(0);
				g.drawString(Integer.toString(shot.getPoints()), (int)(offsets[i].x()+9.5*factor), (int)offsets[i].y());
			}else if(this.shots.get(i).size() > 1){
				double avgScore = Shot.avgPoints(shotList);
				g.drawString(Double.toString(avgScore), (int)(offsets[i].x()+9.5*factor), (int)offsets[i].y());
			}
		}
		List<Shot> flattened = flatten(this.shots);
		switch(this.avgMode){
		case PERVISUAL:
			for(int i = 0; i < this.shots.size(); i++){
				drawDot(g, Shot.avgXY(this.shots.get(i)), offsets[i], Color.BLUE);
			}
			break;
		case TOTAL:
			drawDot(g, Shot.avgXY(flattened), avgOffset, Color.BLUE);
			break;
		}
		int totalScore = Shot.sumPoints(flattened);
		int nrShots = getNrShots();
		String totalScoreString = String.format("%d/%d", totalScore, nrShots);
		g.drawString(totalScoreString, (int)scoreOffset.x()-moveLeft(totalScoreString), (int)scoreOffset.y());
		double avgScore = Shot.avgPoints(flattened);
		
		g.drawString(Double.toString(avgScore), (int)scoreOffset.x()-moveLeft(avgScore)-1, (int)scoreOffset.y()+30);
		
		ImageIO.write(this.background, "PNG", output);
	}
	
	public int moveLeft(String in){
		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true); 
		return (int)font.getStringBounds(in, frc).getWidth();
	}
	
	public int moveLeft(int num){
		return moveLeft(Integer.toString(num));
	}
	
	public int moveLeft(double num){
		return moveLeft(Double.toString(num));
	}
	
	public void drawDot(Graphics g, Coordinate shot, Coordinate offset, Card.Color color) throws IOException{
		BufferedImage dot = ImageIO.read(new File(String.format("%s/dot_%s.png", dotPath, color.toString())));
		Coordinate c = shot.schale(factor).add(offset);
		int x = (int)c.x()-bulletSize/2;
		int y = (int)c.y()-bulletSize/2;
		g.drawImage(dot, x, y, bulletSize, bulletSize, null);
	}
	
	public void drawDot(Graphics g, Shot shot, Coordinate offset, Card.Color color) throws IOException{
		drawDot(g, shot.getXY(), offset, color);
	}
	
	public static double avg(Collection<Double> numbers){
		double total = 0;
		for(double num : numbers){
			total += num;
		}
		return total/numbers.size();
	}
	
	public static <T> List<T> flatten(List<List<T>> list){
		List<T> result = new LinkedList<T>();
		for(List<T> inner : list){
			result.addAll(inner);
		}
		return result;
	}
	
}