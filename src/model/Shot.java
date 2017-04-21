package model;

public class Shot{
	
	double score;
	int angle;
	
	public Shot(double score, int angle) {
		this.score = score;
		this.angle = angle;
		
	}
	
	public int getPoints(){
		return (int)Math.ceil(score);
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
	
	public Coordinate getXY(){
		return Coordinate.fromPolar(angle, 10-score);
	}
	
	
	
}