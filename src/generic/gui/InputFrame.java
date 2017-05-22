package generic.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
	
	private static final long serialVersionUID = 1L;
	private int width;
	private int height;
	private File imageFile;
	private int pointer = 0;
	private Card card;
	private Config config;;
	private Coordinate[] offsets;
	private int nrVisuals;
	private BufferedImage buffImg;
	private JLabel mainLabel;
	private double scaleFactor;
	private String discipline;
	ShotRepo repo;
	
	public InputFrame(int width, int height, String discipline, Config cardConfig, ShotRepo repo) throws IOException {
		this.width = width;
		this.height = height;
		this.discipline = discipline;
		this.config = cardConfig;
		this.offsets = Job.getCoordinates(config.getList("offsets.SHOTS"));
		this.imageFile = new File(String.format("cards/%s.png", discipline));
		this.buffImg = ImageIO.read(imageFile);		
		this.card = Job.getCard(AVGMode.TOTAL, this.config);
		this.nrVisuals = config.getInt("nrVisuals");
		this.scaleFactor = 1.0/(double)config.getInt("deviation");
		this.repo = repo;
		this.init();
	}
	
	public InputFrame duplicate() throws IOException{
		return new InputFrame(width, height, discipline, config, repo);
	}
	
	private void init() throws IOException{
		ImageIcon imgIcon = new ImageIcon(getScaledImage(buffImg, width, height));
		mainLabel = new JLabel("", imgIcon, JLabel.CENTER);
		mainLabel.setHorizontalAlignment(JLabel.LEFT);
		mainLabel.setVerticalAlignment(JLabel.TOP);
		mainLabel.addMouseListener(new InputFrameMouseListener());
		this.addWindowListener(new InputFrameWindowListener());
		this.getContentPane().add(mainLabel);
		
		this.setVisible(true);
		this.setSize(width + this.getInsets().left, height + this.getInsets().top);
		
	}
	
	private static Image getScaledImage(BufferedImage buffImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.drawImage(buffImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	@Deprecated
	public static void main(String[] args){
		if(args.length < 2){
			throw new IllegalStateException("2 arguments should be given. card type and date");
		}
		String cardType = args[0];
		String date = args[1];
		Config cardConfig = Config.getConfig(String.format("cards/%s", cardType));
		try{
			new InputFrame(300, 300, cardType, cardConfig, new ShotRepo(cardType, new File(String.format("data/%s/input.csv", date))));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public class InputFrameMouseListener extends MouseAdapter{
		
		@Override
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON1){
				Coordinate c = Coordinate.instance(e.getX(), e.getY());
				c = c.subtract(offsets[pointer]);
				c = Coordinate.instance(c.x(), -c.y());
				c = c.schale(scaleFactor);
//				System.out.println(c);
				double[] polar = c.toPolar();
				System.out.println(String.format("%f, %f", polar[0], polar[1]));
				repo.add(new Shot(polar[0], (int)polar[1]));
				try{
					card.drawDot(buffImg.getGraphics(), c, offsets[pointer], Card.Color.RED);
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
		}
	}
	
	public class InputFrameWindowListener extends WindowAdapter{
		
		@Override
		public void windowClosing(WindowEvent e){
			if(!repo.lastLineEmpty()){
				repo.newLine();
				try{
					duplicate();
				}catch(IOException exc){
					exc.printStackTrace();
					System.exit(0);
				}
			}else{
				if(!repo.isEmpty()){
					try{
						repo.persist();
					}catch(IOException exc){
						exc.printStackTrace();
					}
				}
				System.exit(0);
			}
		}
	}
	
}