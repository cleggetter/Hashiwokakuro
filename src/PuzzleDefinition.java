
import java.util.*;
import java.util.function.Function;

/** 
 * Allows a puzzle description via a simplified format.
 * This definition is parsed to construct a set of
 * islands and constraint sets, which is turned into a
 * full GridDefinition.
 */
public class PuzzleDefinition {
	
	static HashMap<String, Function<PuzzleDefinition, GridDefinition> > knownPuzzles =
			new HashMap<String, Function<PuzzleDefinition, GridDefinition> > () {
		{
		put("Puzzle1", PuzzleDefinition::Puzzle1);
		put("Puzzle2", PuzzleDefinition::Puzzle2);
		}
	};
	

	// Enum for encoding the items in the text format 
	// see checkKey
	private enum keyMeaning {
		Blank,
		Island,
		Constraint,
		Unknown
	};

	/**
	 *  Class definition is empty.
	 *  Call PuzzleX to configure a puzzle.
	 */
	public PuzzleDefinition() {
	}
	
	/**
	 * Return a filled in grid definition for a given puzzle
	 * @param puzzleName String
	 * @return GridDefinition - fully formed grid ready for solving
	 */
	public GridDefinition getPuzzleGrid(String puzzleName) {
		if (knownPuzzles.containsKey( puzzleName )){
			return knownPuzzles.get(puzzleName).apply(this);
		}
		return null;
	}
	/**
	 * 
	 * @return a set of all the available puzzle names
	 */
	public Set<String> getPuzzleNames() {
		return knownPuzzles.keySet();
	}
	
	/**
	 * Definition for puzzle 1
	 * @return grid definition
	 */
	private GridDefinition Puzzle1() {
		
		// sgrid is a string representation
		//  0 => an empty space
		//  1 => an island
		//  letter => a constraint box label
		String[] sgrid = new String[] {
			"010011A1010",
			"00111010B11",
			"C0110101001",
			"0100101011D",
			"0E100101101",
			"1001F011010",
			"0010110G101",
			"00111H11011",
		};

		// Constraint box labels => Constraint limits
		// Format of map is left, up, right, down
		//  -1 => no constraint
		//  >0 => bridge score limit in direction d
		Map<String, ConstraintBox> keyMap2 = new TreeMap<String, ConstraintBox>();
		keyMap2.put("A",  new ConstraintBox( 13, -1, 7, 11 ));
		keyMap2.put("B",  new ConstraintBox( 11, -1, 3, 11 ));
		keyMap2.put("C",  new ConstraintBox( -1, -1, 15, 1 ));
		keyMap2.put("D",  new ConstraintBox( 15, 4, -1, 9 ));
		keyMap2.put("E",  new ConstraintBox( -1, -1, -1, -1 ));
		keyMap2.put("F",  new ConstraintBox(  6, 15,  9, 5 ));
		keyMap2.put("G",  new ConstraintBox(  9, 18, 4, 3 ));
		keyMap2.put("H",  new ConstraintBox(  9, 10, 11, -1 ));
		
		return makeGrid(sgrid, keyMap2);
	}
	
	/**
	 * Definition for puzzle 2
	 * @return grid definition
	 */
	private GridDefinition Puzzle2() {

		// sgrid is a string representation
		//  0 => an empty space
		//  1 => an island
		//  letter => a constraint box label
		String [] sgrid = new String[] {
			"1010111A110",
			"100001B1010",
			"10101C11100",
			"110D1010010",
			"E1110101000",
			"1010101011F",
			"00G00001000",
			"1101H101010",
		};
	
		// Constraint box labels => Constraint limits
		// Format of map is left, up, right, down
		//  -1 => no constraint
		//  >0 => bridge score limit in direction d
		Map<String, ConstraintBox> keyMap = new TreeMap<String, ConstraintBox>();
		keyMap.put("A",  new ConstraintBox( 15, -1, 5, 17 ));
		keyMap.put("B",  new ConstraintBox( 4, 2, 5, 6 ));
		keyMap.put("C",  new ConstraintBox( 15, 5, 15, 7 ));
		keyMap.put("D",  new ConstraintBox( 6, -1, 7, 3 ));
		keyMap.put("E",  new ConstraintBox( -1, 13, 15, 7 ));
		keyMap.put("F",  new ConstraintBox(  21, -1,  -1, -1 ));
		keyMap.put("G",  new ConstraintBox(  -1, -1, -1, -1 ));
		keyMap.put("H",  new ConstraintBox(  6, 18, 9, -1 ));
		
		return makeGrid(sgrid, keyMap);
	}
	
	/**
	 * Convert the grid spec into a grid definition
	 * by constructing islands of different types at each grid point
	 * and then finalizing the grid to fill in all possible bridges and
	 * constraints
	 * @param sgrid string representation of grid
	 * @param keyMap mapping of entry labels to constraint box targets
	 * @return fully formed grid definition
	 */
	// Convert the grid spec into a grid definition
	// by constructing islands of different types at each grid point
	// and then finalizing the grid to fill in all possible bridges and
	// constraints
	private GridDefinition makeGrid(String[] sgrid, 
								   Map<String, ConstraintBox> keyMap ) {
		
		int numRows = sgrid.length;
		int numCols = sgrid[0].length();
	
		System.out.println(String.format("Dims: %d x %d", numRows, numCols));

		// Set up the islands once.
		GridDefinition fgrid = new GridDefinition(numRows, numCols);
		for (int i=0; i< numRows; i++) {
			for (int j=0; j< numCols; j++){
				String k = fetchKey(sgrid,i,j);
				IslandBase island = null;
				if (checkKey(keyMap, k) == keyMeaning.Island) {
					island = new IslandSolid(i,j);
				}
				else if (checkKey(keyMap, k) == keyMeaning.Constraint) {
					island = new IslandConstraint(i, j, keyMap.get(k));
				}
				else {
					island = new IslandNull(i, j);
				}
				fgrid.addElement(i, j,  island);
			}
		}
		fgrid.finalizeGrid();
		return fgrid;
	}
	/**
	 * Private methods to lookup keys for interpreting constraint labels.
	 * @param keyMap  - map for letter to constraint box
	 * @param k - key to use for looking up map.
	 * @return keyMeaning
	 */
	
	static private keyMeaning checkKey(Map<String, ConstraintBox> keyMap , String k) {
		if (k.equals("1")) {
			return keyMeaning.Island;
		}
		if (k.equals("0")) {
			return keyMeaning.Blank;
		}
		if (keyMap.containsKey(k)) {
			return keyMeaning.Constraint;
		}
		return keyMeaning.Unknown;
	}
	static private String fetchKey(String[] sgrid, int i, int j) {
		return sgrid[i].substring(j, j+1);
	}

}
