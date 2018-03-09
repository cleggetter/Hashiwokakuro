import java.util.ArrayList;
import java.util.Observable;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Contains the structures for fully defining a puzzle grid
 * A puzzle grid contains:
 * <ul>
 *   <li>  all islands  </li>
 *   <li>  all constraint sets (group of islands with a group target) </li>
 *   <li>  all bridges between islands (islands to island connections) possible </li>
 * </ul>
 *    This is intended to be a canonical representation of the parameters of the
 *    puzzle, rather than a working copy for the solution. It is effectively 
 *    immutable once it has been finalized.
 *    
 */
public class GridDefinition {

	// Dimensions of the grid
	private int numRows;
	private int numCols;
	
	// The grid is represented by an island in each x,y position.
	// Each island will have properties, including bridges and constraint group membership
	private IslandBase[][] grid;
	
	// For the purposes of processing we maintain lists of all constraints, islands and bridges.
	private ArrayList<Constraint> allConstraints;
	private ArrayList<Bridge> allBridges;
	private ArrayList<IslandBase> allIslands;
	private ArrayList<IslandBase> allSolidIslands;
			
	/**
	 * GridDefinition
	 * Constructor initializes the grid shape with empty elements
	 * @param r  Number of rows in the grid
	 * @param c  Number of columns in the grid
	 */
	public GridDefinition(int r, int c) {
		numRows = r;
		numCols = c;
		grid = new IslandBase[numRows][numCols];
		allConstraints = new ArrayList<Constraint>();
		allBridges = new ArrayList<Bridge>();
		allIslands = new ArrayList<IslandBase>();
		allSolidIslands = new ArrayList<IslandBase>();
	}
	
	// Get / Set
	/** @return Number of rows in grid */
	public int getNumRows() {
		return numRows;
	}
	/** @return Number of columns in grid */
	public int getNumCols() {
		return numCols;
	}

	/** @return A reference to a given island */
	public IslandBase get(int r, int c) {
		return grid[r][c];
	}
	/** @return A reference to the list of all constraints */
	@SuppressWarnings("unchecked")
	public ArrayList<Constraint> getAllConstraints() {
		return (ArrayList<Constraint>) allConstraints.clone();
	}

	/** @return A reference to the list of all bridges */
	@SuppressWarnings("unchecked")
	public ArrayList<Bridge> getAllBridges() {
		return (ArrayList<Bridge>) allBridges.clone();
	}

	/** @return A reference to the list of all islands (including virtual ones and constraint specs) */
	@SuppressWarnings("unchecked")
	public ArrayList<IslandBase> getAllIslands() {
		return (ArrayList<IslandBase>) allIslands.clone();
	}

	/** @return A reference to the list of all solid (bridgeable) islands */
	@SuppressWarnings("unchecked")
	public ArrayList<IslandBase> getAllSolidIslands() {
		return (ArrayList<IslandBase>) allSolidIslands.clone();
	}
	
	/**
	 * addElement
	 * Used to populate a grid entry with an new island of any type to the grid
	 * @param r  row number (0-based)
	 * @param c  column number (0-based)
	 * @param e  IslandBase to add in position (row,col)
	 */
	public void addElement(int r, int c, IslandBase e) {
		try {
			grid[r][c] = e;
		}
		catch (ArrayIndexOutOfBoundsException exc) {
			Helper.Print("addElement", "Out of bounds: " + r + " , " + c);
		}
	}

	// Helper for filter
	private boolean aLessThanB(int a, int b) {
		return a < b;
	}
	
	
	/** finalizeGrid()
	 * This creates a fully instantiated grid definition based on the
	 * current available information. Until this call the grid definition is not useable.
	 * 
	 * i.e. it takes the constraint specs, groups islands into constraint groups
	 * and defines the bridge options between them.
	 * 
	 * After this, each solid island will belong to 1 or more constraint group
	 * and have 1 or more bridges assigned which connects to another solid island.
	 * 
	 * Also, to prevent bridges crossing,  where potential bridges cross at a null 
	 * island we add both bridges to the null island so that we can invalidate solutions. 
	 * where both are active.
	 * 
	 */
	
	public void finalizeGrid() {
		// Build list of all islands for easier iteration
		for (int i=0; i < numRows; i++) {	
			for (int j=0; j < numCols; j++) {	
				allIslands.add( grid[i][j]);
			}
		}
		// Find all constraint box islands and generate a constraint group (of islands).
		// A constraint box is an island that defines the expected total
		// for islands in each direction (left, up, right, down) from itself
		
		IslandConstraint[] constraintBoxIslands = allIslands.stream()
				.filter(ai -> ai.isConstraint()).toArray(IslandConstraint[]::new);
		for (IslandConstraint ic : constraintBoxIslands) {
			ConstraintBox cb = ic.getConstraintBox();
			Stream<IslandBase> iStream; 
			iStream = allIslands.stream()
					.filter(ai -> ai.matchPos(ic.row, -1) )
					.filter(s -> aLessThanB(s.col, ic.col));
			assignConstraints(
					iStream.toArray(IslandBase[]::new), 
					cb.getConstraintForDirection(Direction.left)
			);	
			iStream = allIslands.stream()
					.filter(ai -> ai.matchPos(ic.row, -1) )
					.filter(s -> aLessThanB(ic.col, s.col));
			assignConstraints( 
					iStream.toArray(IslandBase[]::new), 
					cb.getConstraintForDirection(Direction.right)
			);	
			iStream = allIslands.stream()
					.filter(ai -> ai.matchPos(-1, ic.col))
					.filter(s -> aLessThanB(s.row, ic.row));
			assignConstraints( 
					iStream.toArray(IslandBase[]::new), 
					cb.getConstraintForDirection(Direction.up)
			);	
			iStream = allIslands.stream()
					.filter(ai -> ai.matchPos(-1, ic.col) )
					.filter(s -> aLessThanB(ic.row, s.row));
			assignConstraints( 
					iStream.toArray(IslandBase[]::new), 
					cb.getConstraintForDirection(Direction.down)
			);	
		}
		// Handle rows/cols with no constraint boxes;
		// Use process of elimination to find rows/cols without constraint boxes
		TreeSet<Integer> prows = IntStream.range(0, numRows).boxed()
				.collect(Collectors.toCollection(TreeSet::new));
		TreeSet<Integer> pcols = IntStream.range(0, numCols).boxed()
				.collect(Collectors.toCollection(TreeSet::new));
		allIslands.stream().filter(i -> i.isConstraint())
			.forEach(i -> { prows.remove(i.row); pcols.remove(i.col);} );
		
		// Create null constraint boxes for each unassigned rows/cols
		prows.stream().forEach(
				row -> assignConstraints( 
						allIslands.stream()
							.filter(i -> i.matchPos(row, -1) )
							.toArray(IslandBase[]::new),
						new Constraint(-1)
						));
		pcols.stream().forEach(
				col -> assignConstraints( 
						allIslands.stream()
							.filter(i -> i.matchPos(-1, col) )
							.toArray(IslandBase[]::new),
						new Constraint(-1)
						));
		
		allSolidIslands = allIslands.stream().filter( i -> i.isSolid())
			.collect(Collectors.toCollection(ArrayList::new));
		allConstraints.stream().forEach(c -> c.checkInternalExternalBridges());
	}

	
	private void assignConstraints(IslandBase[] ilist, Constraint cs) {
		if (ilist.length == 0) {
			return;
		}
		ArrayList<IslandBase> islandList = new ArrayList<IslandBase>();
		allConstraints.add(cs);
		IslandSolid lastSolid = null;
		
		for (IslandBase ib : ilist) {
			islandList.add(ib);
			
			cs.addIsland(ib);
			ib.addConstraint(cs);
			if (ib.isSolid()) {
				if (lastSolid == null) {
					lastSolid = (IslandSolid) ib;
				}
				else {
					// Found a pair of solids. Create a bridge
					Bridge b = new Bridge(lastSolid, ib);
					allBridges.add(b);
					// Bridge associates to constraint set.
					b.addConstraint(cs);
					// Constraint set contains bridge
					cs.addBridge(b);
					// Add bridge between solids, and include all nulls between
					for (IslandBase il : islandList) {
						il.addBridge(b);
						if (! il.isSolid()) {
							b.addNull(il);
						}
					}					
					lastSolid = (IslandSolid) ib;
				}
				// List resets to contain only the current solid
				islandList.clear();
				islandList.add(ib);
			}
		}
	}
		
	
		
	
	
	private String ft(String s) {
		return String.format("%12s", s);
	}
	
	/*
	 * printGrid
	 * print a simple view of the grid to stdout
	 */
	public void printGrid() {
		for (int i=0; i< numRows; i++) {
			StringBuilder sb = new StringBuilder();
			StringBuilder vbUp = new StringBuilder();
			StringBuilder vbDn = new StringBuilder();
			for (int j=0; j< numCols; j++){
				if (grid[i][j].isConstraint()) {
					sb.append(ft("C"));
					continue;
				}
				StringBuilder ms = new StringBuilder("");
				// sb.append(ft(grid[i][j].toString()));
				if (grid[i][j].numBridgesInDirection(Direction.up)>0) {
					vbUp.append(ft("|"));
				}	
				if (grid[i][j].numBridgesInDirection(Direction.down)>0) {
					vbDn.append(ft("|"));
				}	
				if (grid[i][j].numBridgesInDirection(Direction.left)>0) {
					ms.append("->");
				}
				if (grid[i][j].isSolid()) {
					ms.append(grid[i][j].toString());
				}
				else {
					ms.append("x");
				}
				
				if (grid[i][j].numBridgesInDirection(Direction.right)>0) {
					ms.append("<-");
				}	
				sb.append(ft(ms.toString()));
			}
			System.out.println(vbUp);
			System.out.println(sb);
			System.out.println(vbDn);
		}
	}
		
}
