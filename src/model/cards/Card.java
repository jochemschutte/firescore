package model.cards;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import model.Coordinate;
import model.Shot;

public class Card{
	
	public static final String dotPath = "cards/reddot_70.png";
	public static final int factor = 18;
	public static final int bulletSize = 30;
	protected static Coordinate[] offsets = {
			Coordinate.instance(237.000000, 237.000000), 
			Coordinate.instance(765.000000, 237.000000), 
			Coordinate.instance(501.000000, 501.000000), 
			Coordinate.instance(237.000000, 765.000000), 
			Coordinate.instance(765.000000, 765.000000)
			};

	protected BufferedImage background;
	protected List<Shot> shots = new LinkedList<>();
	
	public Card(){
		try{
			this.background = ImageIO.read(new File("cards/air_rifle_5v_10m.png"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public List<Shot> getShots() {
		return shots;
	}

	public void setShots(List<Shot> shots) {
		this.shots = shots;
	}
	
	public void draw(File output) throws IOException{
		Graphics g = this.background.getGraphics();
		BufferedImage dot = ImageIO.read(new File(dotPath));
		int toCenter = bulletSize/2;
		int i = 0;
		g.setFont(g.getFont().deriveFont(30f));
		g.setColor(Color.RED);
		for(Shot shot : shots){
			Coordinate factored = Coordinate.instance(shot.getXY().x()*factor, shot.getXY().y()*factor);
			Coordinate offset = offsets[i%offsets.length];
			Coordinate draw = offset.combine(factored);
			int x = (int)(draw.x()-toCenter);
			int y = (int)(draw.y()-toCenter);
			g.drawImage(dot, x, y, bulletSize, bulletSize, null);
			g.drawString(Integer.toString(Math.max(0,shot.getPoints())), (int)(offset.x()+9.5*factor), (int)offset.y());
			i++;
		}
		ImageIO.write(this.background, "PNG", output);
	}
	
	public static void main(String[] args){
		try{
			Card c = new Card();
			c.getShots().add(new Shot(8.3, -80));
			c.getShots().add(new Shot(8.4, -110));
			c.getShots().add(new Shot(6.9, -175));
			c.getShots().add(new Shot(3.7, -120));
			c.getShots().add(new Shot(8.5, -125));
			c.draw(new File("test/test.png"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
}