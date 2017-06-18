package draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import draw.generator.NumberGenerator;
import draw.generator.StringGenerator;

public class LineGraph{
	
	public static final double borderFactor = 0.05;
	public static final double axisLineFactor = 0.01;
	private static final Stroke GRAPH_STROKE = new BasicStroke(1);
	private static final Stroke GRAPH_DASHED = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
	
	private List<Double> points = new LinkedList<>();
	private int height;
	private int width;
	private StringGenerator xScale;
	private List<String> yScale;
	private int yBorder;
	private int xBorder;
	private double maxYValue;
	private double yScaleStep;
	
	public LineGraph(int width, int height, StringGenerator xScale, List<String> yScale, double maxYValue, double yScaleStep){
		this.height = height;
		this.width = width;
		this.xScale = xScale;
		this.yScale = yScale;
		this.yBorder = (int)(height*borderFactor);
		this.xBorder = (int)(width*borderFactor);
		this.maxYValue = maxYValue;
		this.yScaleStep = yScaleStep;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setPoints(List<Double> points){
		this.points = points;
	}
	
	public BufferedImage draw(){
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) result.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.setStroke(GRAPH_STROKE);
		drawScaleX(g);
		drawScaleY(g);
		Iterator<Double> iter = points.iterator();
		if(iter.hasNext()){
			Double prev = iter.next();
			int i = 1;
			while(iter.hasNext()){
				Double next = iter.next();
				if(next != null){
					drawLine(g, i-1, prev, i, next);
					prev = next;
				}
				i++;
			}
		}
		
		return result;
	}
	
	public static void write(BufferedImage img, File outputFile) throws IOException{
		ImageIO.write(img, "png", outputFile);
	}
	
	private Graphics2D drawScaleX(Graphics2D g){
		int x = xBorder;
		int xLineLength = (int)(height*axisLineFactor);
		int xStep = (width - xBorder*2)/(points.size()-1);
		g.drawLine(xBorder, height-yBorder, width-xBorder, height-yBorder);
		while(x <= width-xBorder){
			g.drawLine(x, height-yBorder-xLineLength, x, height-yBorder);
			g.drawString(xScale.next(), x-3, height-yBorder+3*xLineLength);
			x += xStep;
		}
		return g;
	}
	
	private Graphics2D drawScaleY(Graphics2D g){
		int y = height-yBorder;
		int yLineLength = (int)(width*axisLineFactor);
		int yStep = (int)((height-yBorder*2)/(maxYValue/yScaleStep));
		g.drawLine(xBorder, yBorder, xBorder, height-yBorder);
		Iterator<String> yValues = yScale.iterator();
		while(y >= yBorder){
			g.drawLine(xBorder, y, xBorder+yLineLength, y);
			g.setStroke(GRAPH_DASHED);
			g.drawLine(xBorder, y, width-xBorder, y);
			g.setStroke(GRAPH_STROKE);
			if(yValues.hasNext()){
				String yValue = yValues.next();
				FontRenderContext frc = new FontRenderContext(new AffineTransform(),true,true);
				int x = xBorder - yLineLength - (int)g.getFont().getStringBounds(yValue, frc).getWidth();
				g.drawString(yValue, x, y+4);
			}
			y -= yStep;
		}
		return g;
	}
	
	private Graphics2D drawLine(Graphics2D g, int x1, Double y1, int x2, Double y2){
		int iy1 = yScaleToCanvas(y1);
		int iy2 = yScaleToCanvas(y2);
		int ix1 = xScaleToCanvas(x1);
		int ix2 = xScaleToCanvas(x2);
		g.drawLine(ix1, iy1, ix2, iy2);
		return g;
	}
	
	private int xScaleToCanvas(int x){
		return (int)(((double)x/(double)(points.size()-1))*(width-xBorder*2))+xBorder;
	}
	
	private int yScaleToCanvas(Double y){
		return height-yBorder - (int)((y/maxYValue)*(height-yBorder*2));
	}
	
	@Deprecated
	public static void main(String[] args){
		StringGenerator iter = new NumberGenerator(1,1);
		List<String> yScale = new LinkedList<>();
		yScale.add("0");
		yScale.add("1");
		yScale.add("2");
		yScale.add("3");
		List<Double> points = new LinkedList<>();
		points.add(0.0);
		points.add(1.0);
		points.add(2.0);
		points.add(3.0);
		LineGraph g = new LineGraph(1000, 500, iter, yScale, 4.0, 1.0);
		g.setPoints(points);
		try{
			write(g.draw(), new File("output/tst/tstGraph.png"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	
	
	
}