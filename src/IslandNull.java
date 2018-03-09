
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of a null island. This is an (x,y) position on the
 * grid that doesn't have an island, but may have bridges passing through.
 * 
 * Since bridges cannot cross, we can only have 1 active bridge crossing
 * a null island.
 * 
 * @author legge
 *
 */
public class IslandNull extends IslandBase {
	/**
	 * 	
	 * @param r  row number
	 * @param c  column number
	 */
	public IslandNull( int r, int c) {
		super(r,c);
	}
	
	/**
	 * @return false : Null islands are always not solid
	 */
	public boolean isSolid() {
		return false;
	}
	
	/** 
	 * String representation of null island
	 */
	public String toString() {
		return String.format("(%s)", "Null");
		// return String.format("%d/%d (%s)", xPos, yPos, blocker.toString());
	}
}
