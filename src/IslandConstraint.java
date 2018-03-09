
import java.util.*;
import java.util.stream.Collectors;

import org.omg.CORBA.portable.ApplicationException;

/**
 * Implementation of a constraint box island.
 * 
 * This specifies the islands that make up a constraint along with a target
 * 
 * @author legge
 *
 */
public class IslandConstraint extends IslandNull {
	
	private ConstraintBox constraintBox;

	/** 
	 * Constructor a a constraint box island.
	 * @param r  row number
	 * @param c  column number
	 * @param cbox  constraint Box to associate
	 */
	public IslandConstraint( int r, int c, ConstraintBox cbox) {
		super(r,c);
		constraintBox = cbox;
	}
	/**
	 *  @return true since this island is a constraint box island
	 */
	@Override
	public boolean isConstraint() {
		return true;
	}
	/**
	 * 
	 * @return the constraint box
	 */
	public ConstraintBox getConstraintBox() {
		return constraintBox;
	}
	
	/**
	 * A constraint box island cannot have bridges
	 */
	@Override
	public void addBridge(Bridge bridge)  {
	}

	/**
	 * Label to use in the view of this island
	 */
	@Override
	public String getLabel() {
		return constraintBox.getLabel();
	}
	
	/**
	 * String representation of constraint island
	 */
	public String toString() {
		return String.format(String.format("C:%s", id));
		//return String.format("%d/%d (%s)\n", xPos, yPos, String.format("C:%s", id));
	}
}
