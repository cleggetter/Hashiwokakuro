
/**
 * Logging helper class to control debugging output
 * 
 * Allows conditions to be triggered for output to be active.
 * 
 * Should be replaced by a standard logger class.
 * 
 * @author legge
 *
 */
public class Helper {
	
	private boolean active;
	private boolean suppressErrors;
	private String unit;
	private StringBuilder sBuilder;
	
	public Helper(String u) {
		unit = u;
		active = false;
		suppressErrors = false;
		sBuilder = new StringBuilder();
	}
	
	private static void Show(String message) {
		System.out.println(message);
	}
	/**
	 * Print a message 
	 * @param unit Unit
	 * @param s Message
	 */
	public static void Print(String unit, String s) {
		Show(String.format("%s:%s", unit, s));
	}
	/**
	 * Print a message if condition is satisfied
	 * @param c Condition
	 * @param u Unit
	 * @param s Message
	 */
	public static void Print(boolean c, String u, String s) {
		if (c) {
			Print(u, s);
		}
	}
	public void debugIf(boolean d) {
		active = d;
	}
	public void debugIfOr(boolean d) {
		active = d || active;
	}
	public void debugIfAnd(boolean d) {
		active = d && active;
	}
	
	public void resetSBuilder() {
		sBuilder.delete(0,  sBuilder.length());
	}
	public void addSBuilder(String s) {
		//Log(true, "Adding sBuilder" + s);
		sBuilder.append(s);
	}
	public String getSBuilder() {
		return sBuilder.toString();
	}
	
	/**
	 * Log message including unit id
	 * @param message
	 */
	public void Log(String message) {
		Log(active, message);
	}
	/**
	 * Log message including unit id
	 * @param cond  condition under which to show message
	 * @param message
	 */
	public void Log(boolean cond, String message) {
		if (cond) {
			Show(String.format("%s:%s", unit, message));
		}
	}
	
	/**
	 * Print an error message unless suppressed
	 * @param message
	 */
	public void Error(String message) {
		if (!suppressErrors) {
			Show(String.format("%s:%s", unit, message));
		}
	}
	
	
	/**
	 * Debugging helper 
	 * @param ib Island to check
	 * @return
	 */
	public boolean interest(IslandBase ib){
		if (ib.matchPos(7, 9)) {
			//return true;
		}	
		return false;
	}
	/**
	 * Debugging helper 
	 * @param c Constraint to check
	 * @return
	 */
	public boolean interest(Constraint c){
		return false;
	}
	/**
	 * Debugging helper 
	 * @param b Bridge to check
	 * @return
	 */
	public boolean interest(Bridge b){
		if (b.matchFromIslandToIsland(5,0,5,3)) {
			//return true;
		}
		return false;
	}
	
}
