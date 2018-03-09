
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * Container for all the graphics for one island
 * @author legge
 *
 */
public class ImageForIsland {
	
	/**
	 * Container for any lines that are drawn
	 * @author legge
	 *
	 */
	class LineDef {
		
		Line2D line;
		Stroke stroke;
		Color  color;
	
		LineDef(){}
		void setLine(Line2D l) { line = l; }
		void setColor(Color c) { color = c; }
		void setStroke(Stroke s) { stroke = s; }
		
	};
	
	Color color;
	ArrayList<LineDef> lineDefs;
	int x, y;
	int width, height;
	int centerx, centery;
	RectangularShape islandImage;
	ArrayList<String> textLabel;
	boolean textCenter;
	
	/**
	 * Position island at x,y
	 * @param xpos x point
	 * @param ypos y point
	 */
	public ImageForIsland(int xpos, int ypos) {
		x = xpos;
		y = ypos;
		lineDefs = new ArrayList<LineDef>();
		textLabel = new ArrayList<String> ();
		textCenter = true;
	}
	
	/**
	 * Add a solid island with given width and height
	 * @param w  width
	 * @param h  height
	 * @param l  label for island
	 */
	public void solid(int w, int h, String l) {
		width = w;
		height = h;
		islandImage = new Ellipse2D.Float( x, y, width, height);
		centerx = x + width/2;
		centery = y + height/2;
		setColor(Color.GREEN);
	}
	/**
	 * Add a constraint box island with given width and height
	 * @param w  width
	 * @param h  height
	 * @param l  label for island
	 */
	public void constraint(int w, int h, String l) {
		width = w;
		height = h;
		islandImage = new Rectangle2D.Float( x, y, width, height);
		centerx = x + width/2;
		centery = y + height/2;
		setColor(Color.YELLOW);
	}
	
	/**
	 * Set the color of the island
	 * @param c color
	 */
	public void setColor(Color c) {
		color = c;
	}
	/**
	 * Get the color of the island
	 * @return color
	 */
	public Color color() {
		return color;
	}
	
	/** 
	 * Add a label to the island
	 * @param label  The label for the island
	 * @param center  Whether to center or not
	 */
	public void addLabel(String label, boolean center) {
		String[] array = label.split("\n"); 
		for (String l : array) {
			textLabel.add(l);
		}
		textCenter = center;
	}
	
	/** 
	 * Get the label for the island
	 * @param g2d - the graphics object
	 * @return array of text layouts to display
	 */
	public ArrayList<TextLayout> getLabels(Graphics2D g2d) {
		 FontRenderContext frc = g2d.getFontRenderContext();	
		 Font font1 = new Font("Courier", Font.BOLD, 16);
		 ArrayList<TextLayout> tl = new ArrayList<TextLayout>();
		 textLabel.stream().forEach(t -> { tl.add(new TextLayout(t, font1, frc));});
		 return tl;
	}
	
	/**
	 * Add a line
	 * @param x1  start x
	 * @param y1  start y
	 * @param x2  end x
	 * @param y2  end y
	 * @param color  color of line
	 * @param stroke  thickness of line
	 */
	public void addLine(int x1, int y1, int x2, int y2, Color color, Stroke stroke) {
		LineDef ld = new LineDef();
		ld.setLine(new Line2D.Double(x1,y1,x2,y2));
		ld.setColor(color);
		ld.setStroke(stroke);
		lineDefs.add(ld);
	}
	
	/**
	 * 
	 * @return the line definitions associated to the island
	 */
	public ArrayList<LineDef> getLineDefs(){
		return lineDefs;
	}
	
	/**
	 * 
	 * @return the image info for an island
	 */
	public RectangularShape getIslandImage() {
		return islandImage;
	}
}
