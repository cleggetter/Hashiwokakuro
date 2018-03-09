import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Representation of a potential bridge connection between two solid islands.
 * This is just a potential bridge that can have zero or non zero number of
 * bridge spans.
 * 
 * The class members are mostly read-only once defined, with bridge scores and logic for 
 * a solution contained only in a tracker field.
 * 
 * @author legge
 *
 */
public class Bridge {
	
	IslandBase fromIsland;
	IslandBase toIsland;
	ArrayList<IslandBase> iNulls;
	ArrayList<Constraint> constraints;
	private Constraint insideConstraint;
	
	TrackerBridge tracker;
	
	/**
	 * Constructor creates basic structure that needs filling in
	 * @param a - island where bridge starts
	 * @param b - island where bridge ends
	 */
	public Bridge(IslandBase a, IslandBase b) {
		tracker = new TrackerBridge();
		
		// ensure ordering is consistent
		if (IslandBase.compare(a,b) > 0) {
			fromIsland = b;
			toIsland = a;
		}
		else {
			fromIsland = a;
			toIsland = b;
		}
	
		iNulls = new ArrayList<IslandBase>();
		constraints = new ArrayList<Constraint>();
	}
	
	/**
	 * Record which null islands the bridge passes through
	 * @param ib A null island on the bridge
	 */
	public void addNull(IslandBase ib) {
		iNulls.add(ib);
	}
	/**
	 * Record which constraints the bridge belongs to
	 * @param cs constraint to record
	 */
	public void addConstraint(Constraint cs) {
		constraints.add(cs);
	}
	/**
	 * 
	 * @param cs Constraint which this bridge is wholly inside of
	 */
	public void setInsideConstraint(Constraint cs) {
		insideConstraint = cs;
	}
	
	/**
	 * Convenience method to check if a bridge connects two points on the grid
	 * @param r1 - row of point 1
	 * @param c1 - column of point 1
	 * @param r2 - row of point 2
	 * @param c2 - column of point 2
	 * @return
	 */
	public boolean matchFromIslandToIsland(int r1, int c1, int r2, int c2) {
		return (fromIsland.matchPos(r1, c1) && toIsland.matchPos(r2,c2))
				|| (toIsland.matchPos(r1, c1) && fromIsland.matchPos(r2,c2));
	}
	/**
	 * Get the island at the opposite end of a bridge
	 * @param oneEnd - the reference island
	 * @return
	 */
	public IslandBase otherEndIsland(IslandBase oneEnd) {
		if ( toIsland.compareTo(oneEnd) == 0) {
			return fromIsland;
		}
		if ( fromIsland.compareTo(oneEnd) == 0) {
			return toIsland;
		}
		return null;		
	}

	/** 
	 * @return true if the bridge is horizontal in the grid false otherwise
	 */
	public boolean isHorizontal() {
		return  (fromIsland.row == toIsland.row);
	}
	/**
	 * return true if the bridge is vertical in the grid, false otherwise
	 * @return
	 */
	public boolean isVertical() {
		return  (fromIsland.col == toIsland.col);
	}
	// Is this bridge directionally up?
	/**
	 * Test if the bridge is in a given direction from the reference node
	 * @param checkNode reference node 
	 * @param d direction to check
	 * @return
	 */
	public boolean bridgeInDirection(IslandBase checkNode, Direction d) {
		switch(d){
		case up:
			return isVertical() && 
					(IslandBase.compare(checkNode, toIsland) == 0);
		case down:
			return isVertical() && 
					(IslandBase.compare(checkNode, fromIsland) == 0);
		case left:
			return isHorizontal() && 
					(IslandBase.compare(checkNode, toIsland) == 0);
		case right:
			return isHorizontal() && 
					(IslandBase.compare(checkNode, fromIsland) == 0);
		default: break;
		}
		return false;
	}
	
	/**
	 * Compares two bridges for equality - connecting the same islands.
	 * @param a Bridge
	 * @param b Bridge
	 * @return Compare result -1, 0, 1
	 */
	static public int compare(Bridge a, Bridge b) {
		
		int res = IslandBase.compare(a.fromIsland, b.fromIsland);
		if (res != 0) {
			return res;
		}
		return IslandBase.compare(a.toIsland, b.toIsland);
	}
	
	/**
	 * String representation of Bridge.
	 */
	public String toString() {
		return String.format("B:%s-%s",  fromIsland.toString(), toIsland.toString());
	}
};


