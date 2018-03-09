
/*
 * The set of allowable actions on bridges
 * islands or constraints
 */
/**
 * A helper class to track all actions on bridges, islands or constraints.
 * Operates on the respective trackers of the entities rather than the fixed definitions.
 * 
 * @author legge
 *
 */
public class Action {
	
	boolean updated;
	boolean printErr;
	
	Helper thisLog;

	int historyState;
	
	public Action() {
		updated = false;
		thisLog = new Helper("Actions");
		historyState = 0;
	}
	/**
	 * 
	 * @return the current history state id
	 */
	public int getHistoryState() {
		return historyState;
	}

	// For each bridge/island/constraint
	// fixAllocation
	// changeMinimum
	// changeMaximum
	
	// Each one returns 
	//   true if a change to any object is made
	//   false if no action is taken

	/**
	 * Fix the score of a tracker to a final value.
	 * @param t  Tracker to modify (bridge/island/constraint tracker)
	 * @param newTotal - value to set tracker score to
	 * @return true if tracker score is changed, false otherwise.
	 */
	public boolean fixAllocation(Tracker t, int newTotal) {
		
		Tracker validator = new Tracker();
		validator.copyBase(t);
		validator.freezeFinalScore(newTotal);
	
		if (!t.validateTracker(t, validator, "fixAllocation" + t.Entity())) {
			return false;
		}
		t.freezeFinalScore(newTotal);
		updateTracker(t);
		return updated;
	}
	/**
	 * Change the minimum score of a tracker to a final value.
	 * @param t  Tracker to modify (bridge/island/constraint tracker)
	 * @param newTotal - value to set minimum tracker score to
	 * @return true if minimum tracker score is changed, false otherwise.
	 */
	public boolean changeMinimum(Tracker t, int newMin) {
		int change = newMin - t.getMinPossibleScore();
		if (change == 0) {
			return false;
		}
		Tracker validator = new Tracker();
		validator.copyBase(t);
		validator.setMinPossibleScore(newMin);
		if (!t.validateTracker(t, validator,	"changeMinimum" + t.Entity())) {
			return false;
		}
		t.copyBase(validator);
		updateTracker(t);
		return updated;
	}
	/**
	 * Change the maximum score of a tracker to a final value.
	 * @param t  Tracker to modify (bridge/island/constraint tracker)
	 * @param newTotal - value to set maximum tracker score to
	 * @return true if maximum tracker score is changed, false otherwise.
	 */
	public boolean changeMaximum(Tracker t, int newMax) {
		int change = newMax - t.getMaxPossibleScore();
		if (change == 0) {
			return false;
		}
		Tracker validator = new Tracker();
		validator.copyBase(t);
		validator.setMaxPossibleScore(newMax);
		if (!t.validateTracker(t, validator, "changeMaximum" + t.Entity())) {
			return false;
		}
		t.copyBase(validator);
		updateTracker(t);
		return updated;
	}
	
	/*
	public boolean updateBridge(Bridge b) {
		updateTracker(b.tracker);
		updateTracker(b.fromNode.tracker);
		updateTracker(b.toNode.tracker);
		
		// Handle the case where null crossing paths have changed
		for (Bridge nullbr : b.tracker.getNullCrossingBridges()) {
			updateTracker(nullbr.fromNode.tracker);
			updateTracker(nullbr.toNode.tracker);
		}
		
		for (Constraint cs : b.constraints) {
			updateTracker(cs.tracker);
		}
		
		// TODO Want some consistency check
		updated = true;
		return true;
	}
	
	public boolean updateIsland(IslandBase ib) {
		updateTracker(ib.tracker);
		for (Bridge b : ib.bridges) {
			updateTracker(b.tracker);
		}
		for (Constraint cs : ib.constraints) {
			updateTracker(cs.tracker);
		}
		// TODO Want some consistency check
		return updated;
	}
	public boolean updateConstraint(Constraint c) {
		updateTracker(c.tracker);
		for (IslandBase ib : c.solidIslands) {
			updateTracker(ib.tracker);
		}
		for (Bridge b : c.bridges) {
			updateTracker(b.tracker);
		}
		// TODO Want some consistency check
		return updated;
	}
	*/
	
	private boolean updateTracker(Tracker t) {
		t.update();
		updated = true;
		return true;
	}
	
	/**
	 * Save the current state of all trackers - the save is performed within
	 * each tracker. This just iterates over all trackers so we save a consistent
	 * state under the same checkpoint.
	 * 
	 * @param gd - the grid definition of the puzzle
	 * @return id for the new checkpoint
	 */
	public int saveAllStates(GridDefinition gd) {
		historyState += 1;
		for (IslandBase ib : gd.getAllSolidIslands()) {
			ib.tracker.addHistory(historyState);	
		}
		for (Bridge b : gd.getAllBridges()) {
			b.tracker.addHistory(historyState);	
		}
		for (Constraint c : gd.getAllConstraints()) {
			c.tracker.addHistory(historyState);
		}
		return historyState;
	}
	
	/**
	 * Revert the grid definition to the given checkpoint id
	 * This will discard any checkpoints after the id.
	 * 
	 * @param gd - the grid definition of the puzzle
	 * @param status - the current status of the solution
	 * @param checkpoint - the checkpoint id to revert to
	 * @return
	 */
	public boolean revertState(GridDefinition gd, SolverStatus status, int checkpoint) {
		historyState = checkpoint;
		for (IslandBase ib : gd.getAllIslands()) {
			ib.tracker.revertHistory(historyState);	
		}
		for (Bridge b : gd.getAllBridges()) {
			b.tracker.revertHistory(historyState);	
		}
		for (Constraint c : gd.getAllConstraints()) {
			c.tracker.revertHistory(historyState);
		}
		status.resetCounts(gd);
		status.setValid(true);
		return true;
	}
	
	/**
	 * Reset the grid definition to a given checkpoint state of a 
	 * successful solution. 
	 * This keeps all checkpoints intact.
	 * 
	 * @param gd the grid definition of the puzzle
	 * @param checkpoint - the checkpoint id to replay to
	 * @return true if checkpoint id successfully replayed, false otherwise
	 */
	public boolean replayState(GridDefinition gd, int checkpoint) {
		historyState = checkpoint;
		boolean found = false;
		for (IslandBase ib : gd.getAllIslands()) {
			found = ib.tracker.revertHistoryButKeep(historyState) || found;	
		}
		for (Bridge b : gd.getAllBridges()) {
			found = b.tracker.revertHistoryButKeep(historyState) || found;	
		}
		for (Constraint c : gd.getAllConstraints()) {
			found = c.tracker.revertHistoryButKeep(historyState) || found;
		}
		return found;
	}

}
