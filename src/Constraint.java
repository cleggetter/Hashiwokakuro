import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Representation of a constraint, which is a set of islands with a target score
 * For some constraints the target score is undefined, otherwise the target score 
 * must be satisfied to successfully complete the puzzle.
 * 
 * The score of a constraint is the sum of the score of the islands within the constraint
 * 
 * The class members are mostly read-only once defined, with constraint scores and logic for 
 * a solution contained only in a tracker field.
 * 
 * @author legge
 *
 */
public class Constraint {
	public String id;
	private int target;
	
	public ArrayList<IslandBase> solidIslands;
	
	public ArrayList<Bridge> bridges;
	public ArrayList<Bridge> internalBridges;
	public ArrayList<Bridge> externalBridges;
	
	public TrackerConstraint tracker;
	
	public Constraint(int t) {
		id = String.format("%d", UniqueId.getUid());
		tracker = new TrackerConstraint();
		target = t;
		
		solidIslands = new ArrayList<IslandBase>();
		bridges = new ArrayList<Bridge>();
		internalBridges = new ArrayList<Bridge>();
		externalBridges = new ArrayList<Bridge>();
	}
	/**
	 * Add an island to the constraint
	 * @param ib Island to add
	 */
	public void addIsland(IslandBase ib) {
		if (ib.isSolid()) {
			solidIslands.add(ib);
		}
	}
	/**
	 * Add a bridge to the constraint
	 * @param b Bridge to add
	 * @param inside 
	 */
	public void addBridge(Bridge b) {
		bridges.add(b);
	}
	
	/**
	 * 
	 * @return The target score for the constraint
	 */
	public int Target() {
		return target;
	}
	/**
	 *
	 * @return True if the constraint has a defined target, false otherwise
	 */
	public boolean hasTarget() {
		return (target > 0);
	}
	
	/**
	 * Test whether a value matches the target
	 * @param v the value to test against the target
	 * @return true if value matches target, false otherwise
	 */
	public boolean matchTarget(int v) {
		return (target <0) ? true : (v == target);
	}
	

	/**
	 * Check the bridges so we know which are internal and which 
	 * are external to the constraint
	 */
	public void checkInternalExternalBridges() {
		
		// First build a lookup
		TreeSet<IslandBase> nodeLookup = new TreeSet<IslandBase> (solidIslands);
		
		for (IslandBase ib : solidIslands) {
			for (Bridge b : ib.bridges) {
				if (nodeLookup.contains(b.toIsland) && nodeLookup.contains(b.fromIsland)){
					if (ib == b.toIsland) { // Prevent internal nodes being added twice
						internalBridges.add(b);
					}
				}
				else {
					externalBridges.add(b);
				}
			}
		}
		for (Bridge b : internalBridges) {
			b.setInsideConstraint(this);
		}
	}
	
	/**
	 * String representation of constraint
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (IslandBase n : solidIslands) {
			sb.append(n.id());
			sb.append("-");
		}
		sb.append(String.format("Target(%d)", target));
		return sb.toString();
	}
}
