import java.util.ArrayList;
import java.util.*;
import javax.swing.JPanel;

/**
 * Base class for all islands
 * Derived classes will be solid islands, null islands or constraints.
 * 
 * This contains the common attributes and functionality of islands
 * 
 * @author legge
 *
 */
public class IslandBase  implements Comparable<Object> {
	
	int row;   // x , y coordinates in grid
	int col;
	public String id;
	
	public TrackerIsland tracker;
	
	// list of bridges connected to this island
	public ArrayList<Bridge> bridges;  
	// List of constraint groups this island belongs to
	public ArrayList<Constraint> constraints; 
	
	// Initialize only with the x / y position
	public IslandBase(int r, int c) {
		row = r;
		col = c;
		tracker = new TrackerIsland();
		bridges = new ArrayList<Bridge>();
		constraints= new ArrayList<Constraint>();
		id = String.format("%d/%d", r,c);
	}
	
	
	/**
	 * 
	 * @return row index of island
	 */
	public int getRow() {
		return row;
	}

	/**
	 * 
	 * @return column index of island
	 */
	public int getCol() {
		return col;
	}


	/**
	 * 
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return tracker object for this island
	 */
	public TrackerIsland getTracker() {
		return tracker;
	}

	/**
	 * 
	 * @return bridges leaving this island
	 */
	public ArrayList<Bridge> getBridges() {
		return bridges;
	}

	/**
	 * 
	 * @return constraint sets this island belongs to
	 */
	public ArrayList<Constraint> getConstraints() {
		return constraints;
	}

	/** 
	 * 
	 * @param r row index
	 * @param c column index
	 * @return true if this island is in the row, column specified, false otherwise
	 */
	public boolean matchPos(int r, int c) {
		if (row == r) {
			return (col == c) || (c<0);
		}
		if (col == c) {
			return (row == r) || (r<0);
		}
		return (col < 0) && (r < 0);
	}
	
	/**
	 * 
	 * @return true if island is solid, false otherwise
	 */
	public boolean isSolid() {
		return false;
	}
	/**
	 * 
	 * @return true if island is a constraint box
	 */
	public boolean isConstraint() {
		return false;
	}
	
	/**
	 * Assign this island to a constraint group
	 * @param c constraint to associate with island
	 */
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}

	/**
	 *  Assign a bridge to this island
	 * @param bridge Bridge to add to the island
	 */
	// Add a bridge to this island
	public void addBridge(Bridge bridge) {
		bridges.add(bridge);
	}
	
	/**
	 * Get the number of bridge spans from this node in a given direction
	 * @param d direction of interest
	 * @return the number of actual spans in the given direction
	 */
	public int numBridgesInDirection(Direction d) {
		Iterator<Bridge> btr = bridges.iterator();
		int num = 1;
		while (btr.hasNext()) {
			Bridge b = btr.next();
			if (b.bridgeInDirection(this, d)) {
				num += b.tracker.getCurrentScore();
			}
		}
		return num;
	}
	
	/**
	 * Get all the bridges for this island
	 * @return list of bridges out of this island
	 */
	public ArrayList<Bridge> getBridgesOut(){
		ArrayList<Bridge> bs = new ArrayList<Bridge>();
		Iterator<Bridge> btr = bridges.iterator();
		while (btr.hasNext()) {
			Bridge b = btr.next();
			if (compare(this,  b.fromIsland) == 0) {
				bs.add(b);
			}
		}
		return bs;
	}
	
	/**
	 * 
	 * @return Unique id for this island
	 */
	public String id() { return id;}
	
	// A label string for the node.
	/**
	 * String representation of this island - overridden by sub classing
	 */
	public String toString() { return "Undefined GridEntry"; }
	
	/**
	 * 
	 * @return Label to use in the view - overridden by sub classing
	 */
	public String getLabel() {
		return "Unk";
	}
	
	// Comparison for two nodes - only checks the positions
	static public int compare(int g1, int g2) {
		if (g1 == g2) { return 0;}
		if (g2 < g1) { return 1;}
		return -1;
	}
	/**
	 * Compare two island objects. Equal if same row, col
	 * @param g1
	 * @param g2
	 * @return comparison result (-1, 0, 1)
	 */
	static public int compare(IslandBase g1, IslandBase g2) {
		if (IslandBase.compare(g1.row, g2.row) == 0) {
			return IslandBase.compare(g1.col, g2.col);
		}
		return IslandBase.compare(g1.row, g2.row);
	}
	/**
	 * Comparable interface implementation.
	 */
	public int compareTo(Object g1) throws ClassCastException  {
		if (!(g1 instanceof IslandBase)) {
		      throw new ClassCastException("IslandBase object expected.");
		}
		return IslandBase.compare(this,  (IslandBase) g1);    
		
	}
	
}
