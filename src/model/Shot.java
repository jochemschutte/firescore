package model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import model.DoubleAverager.DoubleValue;

public class Shot implements DoubleValue{
	
	double score;
	int angle;
	double size;
	
	public Shot(double score, int angle, double size) {
		this.score = score;
		this.angle = angle;
		this.size = size;
	}
	
	public int getPoints(){
		return Math.max((int)(score+size), 0);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}
	
	public double getSize(){
		return this.size;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public Coordinate getXY(){
		return Coordinate.fromPolar(angle, 10-score);
	}
	
	public static Coordinate avgXY(Collection<Shot> shots){
		List<Coordinate> cs = new LinkedList<>();
		for(Shot shot : shots){
			cs.add(shot.getXY());
		}
		return Coordinate.avg(cs);
	}
	
	public static double avgPoints(Collection<Shot> shots){
		return Math.round((double)Shot.sumPoints(shots)/shots.size()*10)/10.0;
	}
	
	public static int sumPoints(Collection<Shot> shots){
		int total = 0;
		for(Shot shot : shots){
			total += shot.getPoints();
		}
		return total;
	}
	
	@Override
	public double toDouble(){
		return this.getScore();
	}
}