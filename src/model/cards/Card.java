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
	public static final String cardPath = "cards";
	public static final String dotPath = "cards/dots";
	
	private int nrVisuals;
	private int factor;
	private Coordinate[] offsets;
	private Coordinate avgOffset;
	private Coordinate scoreOffset;
	private Font font;
	private int minScore;
	private double shotDelta;
	
	private AVGMode avgMode;
	private BufferedImage background;
	private List<List<Shot>> shots = new LinkedList<>();
	
	public Card(File backgroundLocation, Map<Offset, Object> offsetMap, int factor, double bulletFactor, double shotDelta, int textSize, int minScore, AVGMode mode){
		this.avgMode = mode;
		this.offsets = (Coordinate[])(offsetMap.get(Offset.SHOTS));
		this.nrVisuals = this.offsets.length;
		this.factor = factor;
		this.minScore = minScore;
		this.shotDelta = shotDelta;
		this.avgOffset = (Coordinate)offsetMap.get(Offset.AVGSHOT);
		this.scoreOffset = (Coordinate)offsetMap.get(Offset.SCORE);
		this.font = new Font(Font.SANS_SERIF, Font.PLAIN, textSize);
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
			Shot shot = iter.next();
			if(shot != null){
				this.shots.get(i%nrVisuals).add(shot);
			}
		}
	}
	
	public void setShotList(List<List<Shot>> shots){
		this.shots = filterNull(shots);
	}
	
	public static List<List<Shot>> filterNull(List<List<Shot>> shots){
		for(List<Shot> inner : shots){
			Iterator<Shot> iter = inner.iterator();
			while(iter.hasNext()){
				if(iter.next() == null){
					iter.remove();
				}
			}
		}
		return shots;
	}
	
	public void draw(File output) throws IOException{
		System.out.println(String.format("Generating '%s'.", output.getName()));
		int visualScoreMove = (int)(10+1-minScore-shotDelta);
		Graphics g = this.background.getGraphics();
		g.setFont(font);
		g.setColor(java.awt.Color.RED);
		for(int i = 0; i < this.shots.size(); i++){
			int visualPointer = i%nrVisuals;
			List<Shot> shotList = shots.get(i);
			for(Shot shot : shotList){
				drawDot(g, shot, offsets[visualPointer], Color.RED);
			}
			String points = null;
			if(this.shots.get(i).size() == 1){
				points = Integer.toString(shotList.get(0).getPoints());
				g.drawString(points, (int)(offsets[visualPointer].x()+visualScoreMove*factor), (int)offsets[visualPointer].y());
			}else if(this.shots.get(i).size() > 1){
				points = Double.toString(Shot.avgPoints(shotList));
				g.drawString(points, (int)(offsets[visualPointer].x()+visualScoreMove*factor), (int)offsets[visualPointer].y());
				g.drawString(String.format("%.1f", Shot.avgDev(shotList)), (int)(offsets[visualPointer].x()+visualScoreMove*factor), (int)offsets[visualPointer].y()+font.getSize());
			}
		}
		List<Shot> flattened = flatten(this.shots);
		
		int dotSize = this.calcBulletSize();
		switch(this.avgMode){
		case PERVISUAL:
			for(int i = 0; i < this.shots.size(); i++){
				drawDot(g, Shot.avgXY(this.shots.get(i)), offsets[i%nrVisuals], dotSize, Color.BLUE);
			}
			break;
		case TOTAL:
			drawDot(g, Shot.avgXY(flattened), avgOffset, dotSize, Color.BLUE);
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
	
	public int calcBulletSize(){
		Shot s = null;
		Iterator<List<Shot>> listIter = this.shots.iterator();
		while(s == null && listIter.hasNext()){
			List<Shot> inner = listIter.next();
			Iterator<Shot> shotIter = inner.iterator();
			while(s == null && shotIter.hasNext()){
				s = shotIter.next();
			}
		}
		return (int)(this.factor * s.getSize());
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
	
	public void drawDot(Graphics g, Coordinate shot, Coordinate offset, int dotSize, Card.Color color) throws IOException{
		BufferedImage dot = ImageIO.read(new File(String.format("%s/dot_%s.png", dotPath, color.toString())));
		Coordinate c = Coordinate.instance(shot.x(), -shot.y());
		c = c.schale(factor).add(offset); 
		int x = (int)c.x()-dotSize;
		int y = (int)c.y()-dotSize;
		g.drawImage(dot, x, y, dotSize*2, dotSize*2, null);
	}
	
	public void drawDot(Graphics g, Shot shot, Coordinate offset, Card.Color color) throws IOException{
		drawDot(g, shot.getXY(), offset, (int)(factor*shot.getSize()), color);
	}
	
	public void drawString(Graphics g, String printText, Coordinate c){
		g.drawString(printText, (int)c.x(), (int)c.y());
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
	
	public Coordinate extactOffset(Coordinate c, int offsetNr){
		return c.subtract(this.offsets[offsetNr]);
	}
	
}