package model;

public class Coordinate{

	private double x;
	private double y;
	
	private Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}
	
	public Coordinate combine(Coordinate c){
		return instance(this.x + c.x(), this.y + c.y());
	}
	
	public static Coordinate instance(double x, double y){
		return new Coordinate(x,y);
	}
	
	public static Coordinate fromPolar(int angle, double radius){
		return new Coordinate(radius * Math.cos(Math.toRadians(angle)), radius * Math.sin(Math.toRadians(angle)));
	}
	
	
}