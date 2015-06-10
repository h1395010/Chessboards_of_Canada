import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.sun.istack.internal.logging.Logger;



public final class BoardView extends javax.swing.JPanel implements MouseListener, MouseMotionListener {
	
	private static final Color TRANS_RED = new Color(255, 0, 0,  100);
	private static final Color TRANS_GREEN = new Color(0, 255, 0, 150);
	private static final Color TRANS_BLUE = new Color(0, 0, 255, 150);
	private static final Color TRANS_GRAY = new Color(175, 175, 175, 230);
	private static final Color TRANS_ORANGE = new Color(255, 140, 0, 100);
	private static final Color lightColor = Color.white;
	private static final Color darkColor = Color.black;
	private static BufferedImage lightPawnImage, lightRookImage, lightBishopImage, lightKnightImage, lightQueenImage, lightKingImage;
	private static BufferedImage darkPawnImage, darkRookImage, darkBishopImage, darkKnightImage, darkQueenImage, darkKingImage;
	private static BufferedImage yellowSquare, greenSquare;
	private Coordinate moveStart, moveFinish, prevStart, prevFinish;
	private boolean getAMove = false;
	private Set<Coordinate> legals;
	
	static {
		try {
			lightPawnImage = ImageIO.read(Board.class.getResourceAsStream("resources/light_pawn.png"));
			lightRookImage = ImageIO.read(Board.class.getResourceAsStream("resources/light_rook.png"));
			lightBishopImage = ImageIO.read(Board.class.getResourceAsStream("resources/light_bishop.png"));
			lightKnightImage = ImageIO.read(Board.class.getResourceAsStream("resources/light_knight.png"));
			lightQueenImage = ImageIO.read(Board.class.getResourceAsStream("resources/light_queen.png"));
			lightKingImage = ImageIO.read(Board.class.getResourceAsStream("resources/light_king.png"));
			
			darkPawnImage = ImageIO.read(Board.class.getResourceAsStream("resources/dark_pawn.png"));
			darkRookImage = ImageIO.read(Board.class.getResourceAsStream("resources/dark_rook.png"));
			darkBishopImage = ImageIO.read(Board.class.getResourceAsStream("resources/dark_bishop.png"));
			darkKnightImage = ImageIO.read(Board.class.getResourceAsStream("resources/dark_knight.png"));
			darkQueenImage = ImageIO.read(Board.class.getResourceAsStream("resources/dark_queen.png"));
			darkKingImage = ImageIO.read(Board.class.getResourceAsStream("resources/dark_king.png"));
			
			yellowSquare = ImageIO.read(Board.class.getResourceAsStream("resources/yellow_square.png"));
			greenSquare = ImageIO.read(Board.class.getResourceAsStream("resources/green_square.png"));
		} catch ( IOException ex ) {
			Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private final int FRAME_LENGTH;
	private final int GRID_LENGTH;
	private BufferedImage figurineLayer;
	private BufferedImage boardLayer;
	private BufferedImage selectionLayer;
	private BufferedImage highlightLayer;
	private BufferedImage coachLayer;
	private BufferedImage illustrationLayer;
	private BufferedImage previousMoveLayer;
	private GameController controller;
	
	public void setController(GameController gc) {
		this.controller = gc;
	}
	
	/** 
	 * Creates new form ChessView
	 */
	public Board() {
		initComponents();
		/** make it resizable --> how? **/
		FRAME_LENGTH = 600; 
		GRID_LENGTH = FRAME_LENGTH / 8;
		boardLayer = new BufferedImage(FRAME_LENGTH, FRAME_LENGTH, BufferedImage.TYPE_4BYTE_ABGR);
		figurineLayer = new BufferedImage(FRAME_LENGTH, FRAME_LENGTH, BufferedImage.TYPE_4BYTE_ABGR);
		selectionLayer = new BufferedImage(FRAME_LENGTH, FRAME_LENGTH, BufferedImage.TYPE_4BYTE_ABGR);
		previousMoveLayer = new BufferedImage(FRAME_LENGTH, FRAME_LENGTH, BufferedImage.TYPE_4BYTE_ABGR);
		coachLayer = new BufferedImage(FRAME_LENGTH, FRAME_LENGTH, BufferedImage.TYPE_4BYTE_ABGR);
		illustrationLayer = new BufferedImage(FRAME_LENGTH, FRAME_LENGTH, BufferedImage.TYPE_4BYTE_ABGR);
		highlightLayer = new BufferedImage(FRAME_LENGTH, FRAME_LENGTH, BufferedImage.TYPE_4BYTE_ABGR);
		
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(boardLayer, null, this);
		g2d.drawImage(selectionLayer, null, this);
		g2d.drawImage(illustrationLayer, null, this);
		g2d.drawImage(previousMoveLayer, null, this);
		g2d.drawImage(highlightLayer, null, this);
		g2d.drawImage(coachImage, null, this);
		g2d.drawImage(figurineLayer, null, this);
	}
	
	public void update(Board board) {
		clearBuffereddImage(figurineLayer);
		Rectangle daRect;
		BufferedImage imageToDraw = null;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Slot slot = board.getSlotObject(i + 1, j + 1);
				daRect = getRectAtCoordinate(i + 1, j + 1);
				imageToDraw = getAppropriateImage(slot.getPiece());
				if (imageToDraw != null) {
					daRect.height = daRect.height - 5;
					drawImageIntoLayer(figurineLayer, imageToDraw, daRect);
				}
			}
		}
	}
	
	private Coordinate getGridCoordinateFromRawCoordinate(int x, int y) {
		int xx = 0, yy = 0;
		double frameDouble = (double) FRAME_LENGTH;
		double q = (x * 8) / frameDouble;
		double w = (y * 8) / frameDouble;
		for (int i = 0; i < 8; i++) {
			if (q > i && q <= i + 1) {
				xx = i;
			}
		}
		for (int i = 0; i < 8; i++) {
			if (w > i && w <= i +1) {
				yy = i;
			}
		}
		xx++;
		yy++;
		return new Coordinate(xx, yy);
	}
	
	private void fillRectWithColorInImage(Rectangle r, Color color, BufferedImage canvas) {
		Graphics2D g2 = canvas.createGraphics();
		g2.setColor(color);
		g2.fill(r);
		g2.dispose();
		repaint();
	}
	
	public void highlightCoordinate(Coordinate c) {
		Rectangle daRect = getRectAtCoordinate(c.x, c.y);
		drawImageIntoLayer(selectionLayer, greenSquare, daRect);
	}
	
	public BufferedImage getAppropriateImage(Piece p) {
		PieceType type = p.getType();
		TeamEnum team = p.getTeam();
		BufferedImage imageToDraw = null;
		if (type == PeiceType.EMPTY) {
			imageToDraw = null;
		}
		if (type == PieceType.PAWN) {
			// Draw a pawn in this rectangle
			if (team == TeamEnum.LIGHT) {
				imageToDraw = lightPawnImage;
			}
			if (team == TeamEnum.LIGHT) {
				imageToDraw = darkPawnImage;
			}
		}
		if (type = PieceType.ROOK) {
			if (team == TeamEnum.LIGHT) {
				imageToDraw = lightRookImage;
			}
			if (team == TeamEnum.DARK) {
				imageToDraw = darkRookImage;
			}
		}
		if (type = PieceType.BISHOP) {
			if (team == TeamEnum.LIGHT) {
				imageToDraw = lightBishopImage;
			}
			if (team == TeamEnum.DARK) {
				imageToDraw = darkBishopImage;
			}
		}
		if (type = PieceType.KNIGHT) {
			if (team == TeamEnum.LIGHT) {
				imageToDraw = lightKnightImage;
			}
			if (team == TeamEnum.DARK) {
				imageToDraw = darkKnightImage;
			}
		}
		if (type = PieceType.QUEEN) {
			if (team == TeamEnum.LIGHT) {
				imageToDraw = lightQueenImage;
			}
			if (team == TeamEnum.DARK) {
				imageToDraw = darkQueenImage;
			}
		}
		if (type = PieceType.KING) {
			if (team == TeamEnum.LIGHT) {
				imageToDraw = lightKingImage;
			}
			if (team == TeamEnum.DARK) {
				imageToDraw = darkKingImage;
			}
		}
		
		return imageToDraw
	}
	
	public void highlightCoordinate(Set<Coordinate> coordSet) {
		for (Coordinate c : coordSet) {
			highlightCoordinate(c);
		}
	}
	
	private void drawImageIntoLayer(BufferedImage canvas, BufferedImage item, Rectangle r) {
		Graphics2D g2 = canvas.createGraphics();
		g2.drawImage(item, r.x, r.y, r.width, r.height, this);
		g2.dispose();
		repaint();
	}
	
	private void drawLineIntoLayer(Coordinate a, Coordinate b, BufferedImage canvas) {
		Graphics2D g2 = canvas.createGraphics();
	}
	
	private void clearBufferedImage(BufferedImage img) {
		Graphics2D g = img.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, FRAME_LENGTH, FRAME_LENGTH);
		g.dispose();
	}
	
	public void mousePressed(MouseEvent me) {
		Coordinate scaledCoord = getGridCoordinateFromRawCoordinate(me.getPoint().x, me.getPoint().y);
		moveStart = scaledCoord;
		
		// Clear the selection drawings
		
		if (me.getButton() == MouseEvent.BUTTON1) {
			clearBufferedImage(illustrationLayer);
		}
		// Draw the yellow square on the clicked spot
		
		Rectangle r = getRectAtCoordinate(scaledCoord.x, scaledCoord.y);
	}
	
	public void mouseClicked(MouseEvent me) {
		if (me.getButton() == MouseEvent.BUTTON) {
			Coordinate scaledCoord = getGridCoordinateFromRawCoordinate(me.getPoint().x, me.getpoint().y);
			fillRectWithColorInImage(getRectAtCoordinate(scaledCoord), TRANS_ORANGE, illustrationLayer);
		}
	}
	
}