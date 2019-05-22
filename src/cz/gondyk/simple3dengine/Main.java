package cz.gondyk.simple3dengine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {

	private static final Color COLOR_SKY = new Color(188, 243, 255);
	private static final Color COLOR_FLOOR = new Color(33, 178, 41);
	private static final Color COLOR_FPS = new Color(0, 57, 191);
	
	private static final double MOVE_SPEED = 1d;
	private static final double ROTATE_SPEED = 1.5d;
	
	int frames = 0;
	int frameCounter = 0;
	long frameLastTime = 0;
	long lastFrame = 0;
	int FPS = 0;
	
	double pX;
	double pY;
	double pRot;
	
	boolean keyW = false;
	boolean keyA = false;
	boolean keyS = false;
	boolean keyD = false;
	boolean keyQ = false;
	boolean keyE = false;
	boolean keySpace = false;
	
	int[][] map = {
			{1,1,1,1,1,1,1},
			{1,0,0,0,1,0,1},
			{1,0,1,0,0,0,1},
			{1,1,1,0,1,0,1},
			{1,0,0,0,1,0,1},
			{1,0,1,0,1,0,0},
			{1,0,0,0,0,0,1},
			{1,1,1,1,1,1,1},
	};
	
	int mapXSize = map[0].length;
	int mapYSize = map.length;
	
	BufferedImage wallTexture;
	
	public Main() {
		initMap();
		initWindow();
	}
	
	public void initMap() {
		pX = 1.9D;
		pY = 1.5D;
		pRot = 0;
		try {
			wallTexture = ImageIO.read(getClass().getClassLoader().getResourceAsStream("stone.png"));
		} catch (IOException e) {
		}
	}
	
	public void initWindow() {
		JFrame frame = new JFrame();
		frame.setSize(800, 640);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		JPanel panel = new JPanel() {

			@Override
			protected void paintComponent(Graphics g1) {
				Graphics2D g = (Graphics2D) g1;
				render(g);
				frames++;
				frameCounter++;
				long time = System.currentTimeMillis();
				if (time - frameLastTime >= 1000) {
					frameLastTime = time;
					FPS = frameCounter;
					frameCounter = 0;
				}
			}
		};
		panel.setSize(800, 640);
		frame.add(panel);
		frame.repaint();
		frame.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				keyEvent(e.getKeyCode(), false);
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				keyEvent(e.getKeyCode(), true);
			}
		});
		new Thread() {
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000/30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					frame.repaint();
				}
			}
		}.start();
	}
	
	public void keyEvent(int keyCode, boolean pressed) {
		switch (keyCode) {
		case KeyEvent.VK_W:
			keyW = pressed;
			break;
		case KeyEvent.VK_A:
			keyA = pressed;
			break;
		case KeyEvent.VK_S:
			keyS = pressed;
			break;
		case KeyEvent.VK_D:
			keyD = pressed;
			break;
		case KeyEvent.VK_Q:
			keyQ = pressed;
			break;
		case KeyEvent.VK_E:
			keyE = pressed;
			break;

		}
	}
	
	public int getBlock(double x, double y) {
		int x1 = (int) Math.floor(x);
		int y1 = (int) Math.floor(y);
		if (x1 < 0 || x1 > (mapXSize-1) || y1 < 0 || y1 > (mapYSize-1)) {
			return 0;
		}
		return map[y1][x1];
	}
	
	public double getDistance(double x, double y) {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	public void render(Graphics2D g) {
		g.setColor(COLOR_SKY);
		g.fillRect(0, 0, 800, 640/2);
		g.setColor(COLOR_FLOOR);
		g.fillRect(0, 640/2, 800, 640);
		long time = System.currentTimeMillis();
		double delta = (time - lastFrame)/1000d;
		lastFrame = time;
		if (keyW) {
			pX += MOVE_SPEED * Math.cos(pRot) * delta;
			pY += MOVE_SPEED * Math.sin(-pRot) * delta;
		}
		if (keyS) {
			pX += MOVE_SPEED * Math.cos(pRot+Math.PI) * delta;
			pY += MOVE_SPEED * Math.sin(-pRot+Math.PI) * delta;
		}
		if (keyA) {
			pX += MOVE_SPEED * Math.cos(pRot+Math.PI/2) * delta;
			pY += MOVE_SPEED * Math.sin(-(pRot+Math.PI/2)) * delta;
		}
		if (keyD) {
			pX += MOVE_SPEED * Math.cos(pRot-Math.PI/2) * delta;
			pY += MOVE_SPEED * Math.sin(-(pRot-Math.PI/2)) * delta;
		}
		if (keyQ) {
			pRot += ROTATE_SPEED * delta;
		}
		if (keyE) {
			pRot += -ROTATE_SPEED * delta;
		}
		BufferedImage image = new BufferedImage(800, 640, BufferedImage.TYPE_INT_ARGB);
		final int textureWidth = wallTexture.getWidth();
		final int textureHeight = wallTexture.getHeight();
		for (int p = 0; p < 800; p++) {
			double a = (800/2d - p)/400;
			double o = a;
			double z = pRot + o;
			int block1 = 0;
			double distance = 0;
			double bx1 = 0;
			double by1 = 0;
			for(int i = 0; i < 1000; i++) {
				double j = i / 50d;
				double dX = Math.cos(z) * j;
				double dY = Math.sin(-z) * j;
				double x1 = pX + dX;
				double y1 = pY + dY;
				int block = getBlock(x1, y1);
				if (block != 0) {
					bx1 = x1;
					by1 = y1;
					block1 = block;
					distance = getDistance(dX, dY);
					break;
				}
			}
			double bx2 = bx1 % 1;
			double by2 = by1 % 1;
			bx2 = bx2 > 0.98 ? 1 : (bx2 < 0.02 ? 1 : bx2);
			by2 = by2 > 0.98 ? 1 : (by2 < 0.02 ? 1 : by2);
			if (block1 == 0) {
				continue;
			}
			int height = (int) (200/distance);
			final double height2 = height*2;
			int textureX = (int) ((textureWidth-1) * (bx2 * by2));
			int y1 = 0;
			for (int y = (640/2-height); y < (640/2+height); y++) {
				int textureY = (int) ((textureHeight-1) * Math.min((y1/height2), 1d));
				y1++;
				Color color = new Color(wallTexture.getRGB(textureX, textureY));
				image.setRGB(p, Math.max(0, Math.min(y, 640-1)), color.getRGB());
			}
		}
		g.drawImage(image, 0, 0, null);
		g.setColor(COLOR_FPS);
		g.drawString("FPS: " + FPS, 10, 20);
	}
	
	public static void main(String[] args) {
		new Main();
	}

}
