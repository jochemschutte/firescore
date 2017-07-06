package generic.gui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import config.Config;
import exec.Job;
import model.Coordinate;
import model.Shot;
import model.cards.Card;
import model.cards.Card.AVGMode;

public class InputFrame extends JFrame{
	
	public static double moveScoreDelta = 0.1;
	public static int moveAngleDelta = 2;
	
	public enum Move {UP, DOWN, LEFT, RIGHT};
	
	private static final long serialVersionUID = 1L;
	private int width;
	private int height;
	private File imageFile;
	private int pointer = 0;
	private Card card;
	private Config config;
	private Coordinate[] offsets;
	private int nrVisuals;
	private BufferedImage buffImg;
	private JLabel mainLabel;
	private double scaleFactor;
	private String discipline;
	private int factor;
	private Font font;
	private int textSize;
	private int nextScoreHeight;
	private KeyProcessQue que;
	private boolean debugMode = false;
	ShotRepo repo;
	
	public InputFrame(int width, int height, String discipline, Config cardConfig, ShotRepo repo) throws IOException {
		this.width = width;
		this.height = height;
		this.discipline = discipline;
		this.config = cardConfig;
		this.offsets = Job.getCoordinates(config.getList("offsets.SHOTS"));
		this.imageFile = new File(String.format("cards/%s.png", discipline));		
		this.card = Job.getCard(AVGMode.TOTAL, this.config);
		this.nrVisuals = config.getInt("nrVisuals");
		this.scaleFactor = 1.0/(double)config.getInt("deviation");
		this.factor = config.getInt("deviation");
		this.repo = repo;
		this.textSize = config.getInt("textSize");
		this.font = new Font(Font.SANS_SERIF, Font.PLAIN, textSize);
		
		this.init();
		this.que = new KeyProcessQue();
		this.addKeyListener(que);
		this.addKeyListener(new InputFrameKeyListener(que));
		this.addWindowListener(new InputFrameWindowListener());
	}
	
	public void setDebugMode(boolean d){
		this.debugMode = d;
	}
	
	public void setKeyProcessQue(KeyProcessQue que){
		for(KeyListener l : this.getKeyListeners()){
			this.removeKeyListener(l);
		}
		this.que = que;
		this.addKeyListener(que);
		this.addKeyListener(new InputFrameKeyListener(que));
	}
	
	public InputFrame duplicate() throws IOException{
		InputFrame result = new InputFrame(width, height, discipline, config, repo);
		result.setKeyProcessQue(this.que);
		return result;
	}
	
	private void init() throws IOException{
		this.buffImg = ImageIO.read(imageFile);
		nextScoreHeight = 0;
		ImageIcon imgIcon = new ImageIcon(getScaledImage(buffImg, width, height));
		mainLabel = new JLabel("", imgIcon, JLabel.CENTER);
		mainLabel.setHorizontalAlignment(JLabel.LEFT);
		mainLabel.setVerticalAlignment(JLabel.TOP);
		mainLabel.addMouseListener(new InputFrameMouseListener());
		this.getContentPane().removeAll();
		this.getContentPane().add(mainLabel);
		
		this.invalidate();
		this.validate();
		mainLabel.repaint();
		repaint();
		
		this.setVisible(true);
		this.setSize(width + this.getInsets().left, height + this.getInsets().top);
	}
	
	public void clear() throws IOException{
		this.repo = new ShotRepo(repo.discipline, repo.getOutputFile());
		this.init();
	}
	
	public void addShot(Shot s){
		repo.add(s);
		try{
			Graphics g = buffImg.getGraphics();
			g.setColor(java.awt.Color.RED);
			g.setFont(font);
			card.drawDot(g, s, offsets[pointer], Card.Color.RED);
			drawString(g, Integer.toString(s.getPoints()));
			buffImg.flush();
			ImageIcon imgIcon = new ImageIcon(getScaledImage(buffImg, width, height));
			mainLabel.setIcon(imgIcon);
			mainLabel.repaint();
			repaint();
		}catch(IOException exc){
			exc.printStackTrace();
		}
		pointer = (pointer+1) % nrVisuals;
	}
	
	private void drawString(Graphics g, String txt){
		double x = offsets[pointer].x()+9.5*factor;
		double y = offsets[pointer].y()+nextScoreHeight;
		card.drawString(g, txt, Coordinate.instance(x, y));
		this.nextScoreHeight += textSize;
	}
	
	private void closeCard(){
		if(!repo.lastLineEmpty()){
			repo.newLine();
			try{
				InputFrame f = duplicate();
				f.setDebugMode(debugMode);				
			}catch(IOException exc){
				exc.printStackTrace();
				System.exit(0);
			}
		}else{
			if(!debugMode){
				if(!repo.isEmpty()){
					try{
						repo.persist();
					}catch(IOException exc){
						exc.printStackTrace();
					}
				}else{
					System.out.println("No data entered. did not persist");
				}
			}else{
				System.out.println("Did not persist due to debug mode");
			}
			System.out.println("_Finished");
			System.exit(0);
		}
		this.setVisible(false);
	}
	
	private static Image getScaledImage(BufferedImage buffImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.drawImage(buffImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	
	public class InputFrameMouseListener extends MouseAdapter{
		
		@Override
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON1){
				Coordinate c = Coordinate.instance(e.getX(), e.getY());
				c = c.subtract(offsets[pointer]);
				c = Coordinate.instance(c.x(), -c.y());
				c = c.schale(scaleFactor);
				double[] polar = c.toPolar();
				Shot s = new Shot(10-polar[0], (int)polar[1]);
				addShot(s);
			}
		}
	}
	
	public class InputFrameWindowListener extends WindowAdapter{
		
		@Override
		public void windowClosing(WindowEvent e){
			closeCard();
		}
	}
	
	public class KeyProcessQue extends KeyAdapter{
		
		Set<Integer> available = new TreeSet<>();
		
		public KeyProcessQue(){
			available.add(KeyEvent.VK_W);
		}
		
		@Override
		public void keyReleased(KeyEvent e){
			available.add(e.getKeyCode());
		}
		
		public boolean process(int keyCode){
			return available.remove(keyCode);
		}
	}
	
	public class InputFrameKeyListener extends KeyAdapter{
		
		KeyProcessQue toggle;
		
		public InputFrameKeyListener(KeyProcessQue toggle){
			this.toggle = toggle;
		}
		
		@Override
		public void keyPressed(KeyEvent e){
			new InputWorker(e, toggle).start();
		}
		
	}
	
	public class InputWorker extends Thread{
		
		KeyEvent e;
		KeyProcessQue que;
		
		public InputWorker(KeyEvent ev, KeyProcessQue que){
			this.e = ev;
			this.que = que;
		}
		
		@Override
		public void run() {
			if(e.isControlDown()){
				try{
					switch(e.getKeyCode()){
					case KeyEvent.VK_R:
						reload();
						break;
					case KeyEvent.VK_Z:
						stepBack();
						break;
					case KeyEvent.VK_UP:
						move(Move.UP);
						break;
					case KeyEvent.VK_DOWN:
						move(Move.DOWN);
						break;
					case KeyEvent.VK_LEFT:
						move(Move.LEFT);
						break;
					case KeyEvent.VK_RIGHT:
						move(Move.RIGHT);
						break;
					case KeyEvent.VK_W:
						if(que.process(KeyEvent.VK_W)){
							closeCard();
						}
						break;
					default:
						return;
					}
				}catch(IOException exc){
					exc.printStackTrace();
				}
			}
		}
		
		private void reload() throws IOException{
			clear();
		}
		
		private Shot stepBack() throws IOException{
			Shot result = null;
			if(!repo.isEmpty()){
				result = repo.removeLastShot();
				List<Shot> lastLine = repo.removeLastCard();
				init();
				for(Shot s : lastLine){
					addShot(s);
				}
			}
			return result;
		}
		
		private void move(Move move) throws IOException{
			Shot lastShot = stepBack();
			if(lastShot != null){
				double score = lastShot.getScore();
				int angle = lastShot.getAngle();
				switch(move){
				case UP:
					score =  Math.min(10.0, score+moveScoreDelta);
					break;
				case DOWN:
					score -= moveScoreDelta;
					break;
				case LEFT:
					angle = (angle-moveAngleDelta) % 360;
					break;
				case RIGHT:
					angle = (angle+moveAngleDelta) % 360;
				}
				addShot(new Shot(score, angle));				
			}
		}
	}
	
}