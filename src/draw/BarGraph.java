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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class BarGraph{

	private static final Stroke GRAPH_DASHED = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
	private static final Stroke BACKGROUND_STROKE = new BasicStroke(1);
	public static final double borderFactor = 0.05;
	public static final double axisLineFactor = 0.01;
	
	int width;
	int height;
	int yFactor;
	int xBorder;
	int yBorder;
	double yMax;
	List<String> xValues;
	Map<String, Double> yValues;

	private Graphics2D canvas;
	private BufferedImage result;
	
	public BarGraph(int width, int height, int yFactor) {
		this.width = width;
		this.height = height;
		this.yFactor = yFactor;
		this.yBorder = (int)(height*borderFactor);
		this.xBorder = (int)(width*borderFactor);
	}
	
	public void setValues(List<String> xValues, Map<String, Double> yValues){
		this.xValues = xValues;
		this.yValues = yValues;
		this.yMax = calcMax();
	}
	
	private void init(){
		this.result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.canvas = (Graphics2D) result.getGraphics();
		canvas.setColor(Color.WHITE);
		canvas.fillRect(0, 0, width, height);
		canvas.setStroke(BACKGROUND_STROKE);
		canvas.setColor(Color.BLACK);
		drawScaleX(canvas);
		drawScaleY(canvas);
		canvas.setColor(Color.WHITE);
	}
	
	private Graphics2D drawScaleX(Graphics2D g){
//		double xDouble = xBorder;
//		int xLineLength = (int)(height*axisLineFactor);
//		double xStep = (width - (double)xBorder*2)/(double)(this.nrXValues-1);
		g.drawLine(xBorder, height-yBorder, width-xBorder, height-yBorder);
//		while(xDouble < width-xBorder){
//			int xInt = (int)xDouble;
//			g.drawLine(xInt, height-yBorder-xLineLength, xInt, height-yBorder);
//			String textLabel = xScale.next();
//			g.drawString(textLabel, xInt-textLabel.length()*4, height-yBorder+3*xLineLength);
//			xDouble += xStep;
//		}
		return g;
	}
	
	private double calcMax(){
		double max = 0;
		for(Double value : yValues.values()){
			max = Math.max(max, value);
		}
		int result = (int)(max / this.yFactor);
		if(max % this.yFactor > 0){
			result++;
		}
		return result * this.yFactor;
	}
	
	private Graphics2D drawScaleY(Graphics2D g){
		int yUpper = height-yBorder;
		int yLineLength = (int)(width*axisLineFactor);
//		int yStep = (int)((height-yBorder*2)/(maxYValue/yScaleStep));
		g.drawLine(xBorder, yBorder, xBorder, height-yBorder);
//		Iterator<String> yValues = yScale.iterator();
		double yStep = (height-2*yBorder)/yMax*yFactor;
		for(int i = 0; i*yFactor <= this.yMax; i++){
			int y = (int)(yUpper - i*yStep);
			g.drawLine(xBorder, y, xBorder+yLineLength, y);
			g.setStroke(GRAPH_DASHED);
			g.drawLine(xBorder, y, width-xBorder, y);
			g.setStroke(BACKGROUND_STROKE);
//			if(yValues.hasNext()){
				String yValue = Integer.toString(i * yFactor);
				FontRenderContext frc = new FontRenderContext(new AffineTransform(),true,true);
				int x = xBorder - yLineLength - (int)g.getFont().getStringBounds(yValue, frc).getWidth();
				g.drawString(yValue, x, y+4);
//			}
		}
		return g;
	}
	
	public Graphics2D draw(Color color){
		this.init();
		int xWidth = (int)((width-2*xBorder)/(xValues.size()));
		double yHeight = (height-2*yBorder)/this.yMax;
		int i = 0;
		FontRenderContext frc = new FontRenderContext(new AffineTransform(),true,true);
		for(String x : this.xValues){
			Double yValue = this.yValues.get(x);
			int rectHeight = (int)(yValue * yHeight);
			canvas.setColor(color);
			canvas.fillRect((int)(xBorder+(i+0.05)*xWidth), height-yBorder-rectHeight, (int)(xWidth*0.9), rectHeight);
			
			int moveLeft = (int)canvas.getFont().getStringBounds(x, frc).getWidth();
			int moveDown = (int)canvas.getFont().getStringBounds(x, frc).getHeight();
			canvas.setColor(Color.black);
			canvas.drawString(x, (int)(xBorder+i*xWidth+(xWidth/2)-moveLeft+xWidth*0.05), height - yBorder + moveDown+4);
			
			i++;
		}
		canvas.setColor(Color.white);
		return canvas;
	}
	
	public void write(File outputFile) throws IOException{
		ImageIO.write(this.result, "png", outputFile);
	}
	
	public static void main(String[] args){
		BarGraph graph = new BarGraph(800, 800, 5);
		List<String> xValues = new LinkedList<>();
		xValues.add("1");
		xValues.add("2");
		xValues.add("3");
		Map<String, Double> values = new TreeMap<String, Double>();
		values.put("1", 20.0);
		values.put("2", 10.0);
		values.put("3", 3.0);
		graph.setValues(xValues, values);
		graph.draw(Color.BLUE);
		try{
			graph.write(new File("output/tst/tstGraph.png"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
}