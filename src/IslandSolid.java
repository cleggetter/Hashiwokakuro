
import java.util.*;
import java.util.stream.Collectors;

// Defines a land island which can have bridges
/**
 * Implementation of a solid island. This is an (x,y) position on the
 * grid that can have bridge to another solid island.
 * 
 * @author legge
 *
 */
public class IslandSolid extends IslandBase {
	
	// Creates a fully bridgeable island
	/**
	 * 
	 * @param r  row number
	 * @param c  column number
 	 */
	public IslandSolid( int r, int c) {
		super(r,c);
	}
	
	/**
	 * @return True - by definition solid islands are solid
	 */
	public boolean isSolid() {
		return true;
	}
	
	/**
	 * String label for use in the view
	 */
	public String getLabel() {
		return String.format("%-2d/%d\n%d", 
				tracker.minPossibleScore,
				tracker.maxPossibleScore,
				tracker.groupId);
	}
	
	/**
	 * String representation of class
	 */
	public String toString() {
		return String.format("%d/%d ", row, col); //, String.join(",",cs));
	}
}
