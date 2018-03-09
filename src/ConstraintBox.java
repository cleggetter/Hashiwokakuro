import java.util.TreeMap;

/**
 * Representation of a constraint box, for use within a islandConstraint
 * 
 * @author legge
 *
 */
public class ConstraintBox {
	
	private TreeMap<Direction, Constraint> targets; 
	
	/**
	 * ConstraintBox definition - one constraint for each direction.
	 * This is an intermediate structure used to construct the grid definition
	 * 
	 * @param left - target for left direction constraint
	 * @param up - target for up direction constraint
	 * @param right - target for right direction constraint
	 * @param down - target for down direction constraint
	 */
	public ConstraintBox(int left, int up, int right, int down) {
		targets = new TreeMap<Direction, Constraint>();
		targets.put(Direction.left, new Constraint(left));
		targets.put(Direction.up, new Constraint(up));
		targets.put(Direction.right, new Constraint(right));
		targets.put(Direction.down, new Constraint(down));
	}
	
	/**
	 * Get the constraint in a particular direction
	 * @param d Direction of constraint of interest
	 * @return Constraint of interest
	 */
	public Constraint getConstraintForDirection(Direction d) {
		return targets.get(d);
	}
	
	/**
	 * Helper function to generate consistent labels
	 * @param l
	 * @return
	 */
	private String convertLabel(int l) {
		if (l < 0) {
			return "-";
		}
		return String.format("%s", l);
	}
	/**
	 * Label for a UI for this constraint box
	 * @return label string
	 */
	public String getLabel() {
		String u = convertLabel(targets.get(Direction.up).Target());
		String l = convertLabel(targets.get(Direction.left).Target());
		String r = convertLabel(targets.get(Direction.right).Target());
		String d = convertLabel(targets.get(Direction.down).Target());
		return String.format("%10s\n%-5s%4s%5s\n%10s", u, l, "", r, d);
	}
	
}
