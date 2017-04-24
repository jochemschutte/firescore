package model;

import java.util.Collection;

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
	
	public Coordinate add(Coordinate c){
		return instance(this.x + c.x(), this.y + c.y());
	}
	
	public Coordinate schale(double factor){
		return instance(this.x*factor, this.y*factor);
	}
	
	public static Coordinate avg(Collection<Coordinate> cs){
		double xTotal= 0;
		double yTotal = 0;
		for(Coordinate c : cs){
			xTotal += c.x();
			yTotal += c.y();
		}
		return Coordinate.instance(xTotal/cs.size(), yTotal/cs.size());
	}
	
	public static Coordinate instance(double x, double y){
		return new Coordinate(x,y);
	}
	
	public static Coordinate fromPolar(int angle, double radius){
		return new Coordinate(radius * Math.cos(Math.toRadians(angle)), radius * Math.sin(Math.toRadians(angle)));
	}
	
	
}