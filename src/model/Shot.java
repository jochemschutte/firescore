package model;

import java.util.Collection; 
import java.util.LinkedList;
import java.util.List;

import model.DoubleAverager.DoubleValue;

public class Shot implements DoubleValue{
	
	double score;
	int angle;
	double size;
	double delta;
	int min;
	
	public Shot(double score, int angle, double size, double scoreDelta, int min){
		this.score = score;
		this.angle = angle;
		this.size = size;
		this.delta = scoreDelta;
		this.min = min;
	}
	
	public Shot(double score, int angle, double size, double scoreDelta) {
		this(score, angle, size, scoreDelta, 0);
	}
	
	public int getPoints(){
		int points = (int)(score+size-delta);
		points = points >= min ? points : 0;
		return Math.min(Math.max(points, 0), 10);
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
	
	public void setSize(double size){
		this.size = size;
	}
	
	public double getDelta(){
		return this.delta;
	}
	
	public void setDelta(double scoreDelta){
		this.delta = scoreDelta;
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
	
	public static double avgDev(Collection<Shot> shots) {
		Coordinate avg = avgXY(shots);
		double dev = shots.stream().map(s->s.getXY().subtract(avg)).mapToDouble(c->c.toPolar()[0]).sum();
		return dev/shots.size();
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
	
	public Shot move(double delta){
		return new Shot(this.getScore()+delta, this.getAngle(), this.getSize(), this.getDelta());
		
	}
	
	@Override
	public double toDouble(){
		return this.getScore();
	}
}