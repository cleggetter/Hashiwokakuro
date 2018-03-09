import java.util.Observable;
import java.util.Observer;


/**
 * Model is a wrapper data structure around the puzzle definition.
 * 
 * It is observable, so that when the data changes the UI can be signaled.
 * 
 * @author legge
 *
 */
public class Model extends Observable {

	GridDefinition grid;
	/**
	 * Constructor just creates a grid definition
	 * 
	 */
	public Model() {
		
		PuzzleDefinition pd = new PuzzleDefinition();
		
		//grid = pd.getPuzzleGrid("Puzzle1");
		grid = pd.getPuzzleGrid("Puzzle2");
		grid.printGrid();
	}

	
	/**
	 * @return The grid definition for the puzzle
	 */
	public GridDefinition getGrid() {
		return grid;
	}


	/**
	 * Observable implementation. Notify the UI of changes
	 */
	public void changeSomething() {
		// Notify observers of change
		setChanged();
		notifyObservers(grid);
	}

}
